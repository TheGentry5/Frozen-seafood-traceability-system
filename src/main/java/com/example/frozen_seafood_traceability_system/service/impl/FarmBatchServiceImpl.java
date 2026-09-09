package com.example.frozen_seafood_traceability_system.service.impl;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.frozen_seafood_traceability_system.common.AuthContext;
import com.example.frozen_seafood_traceability_system.common.BizCode;
import com.example.frozen_seafood_traceability_system.common.BizException;
import com.example.frozen_seafood_traceability_system.common.PageResult;
import com.example.frozen_seafood_traceability_system.common.StatusConst;
import com.example.frozen_seafood_traceability_system.entity.FarmBatch;
import com.example.frozen_seafood_traceability_system.entity.ProcBatch;
import com.example.frozen_seafood_traceability_system.mapper.FarmBatchMapper;
import com.example.frozen_seafood_traceability_system.mapper.ProcBatchMapper;
import com.example.frozen_seafood_traceability_system.service.FarmBatchService;

/**
 * 养殖企业批号业务实现（docs/开发实施文档.md §6.3 farm 行 / §8 任务 3）。
 */
@Service
public class FarmBatchServiceImpl extends ServiceImpl<FarmBatchMapper, FarmBatch> implements FarmBatchService {

    private static final int BATCH_CODE_MAX = 32;
    private static final int PRODUCT_NAME_MAX = 64;
    private static final int INSPECTION_CERT_MAX = 255;
    private static final int INSPECTOR_MAX = 32;

    @Autowired
    private ProcBatchMapper procBatchMapper;

    // 分页查询
    @Override
    public PageResult<FarmBatch> pageMy(long page, long size, Integer status, String keyword) {
        requireFarm();
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        } else if (size > 100) {
            size = 100;
        }
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                keyword = null;
            }
        }

        Page<FarmBatch> pageParam = new Page<>(page, size);
        baseMapper.selectPageForNode(pageParam, AuthContext.nodeId(), status, keyword);
        return new PageResult<>(pageParam.getTotal(), pageParam.getRecords());
    }

    // 校验批号是否存在
    @Override
    public boolean existsBatchCode(String batchCode, Long excludeId) {
        requireFarm();
        if (batchCode == null || batchCode.trim().isEmpty()) {
            return false;
        }

        LambdaQueryWrapper<FarmBatch> qw = new LambdaQueryWrapper<FarmBatch>()
                .eq(FarmBatch::getBatchCode, batchCode.trim());
        if (excludeId != null) {
            qw.ne(FarmBatch::getId, excludeId);
        }
        // 空参/空串直接返回 `false`（不查库），兜底非空校验
        Long cnt = baseMapper.selectCount(qw);
        return cnt != null && cnt > 0;
    }

    // 创建批号产品
    @Override
    public void createMy(FarmBatch req) {
        requireFarm();
        if (req.getBatchCode() == null || req.getBatchCode().trim().isEmpty()) {
            throw new BizException(BizCode.BAD_REQUEST, "产品批号不能为空");
        }

        req.setBatchCode(req.getBatchCode().trim());
        checkTextLen(req.getBatchCode(), "产品批号", BATCH_CODE_MAX);
        checkTextLen(req.getProductName(), "产品品种", PRODUCT_NAME_MAX);
        checkTextLen(req.getInspectionCert(), "检验检疫合格证明", INSPECTION_CERT_MAX);
        checkTextLen(req.getInspector(), "官方检疫员名称", INSPECTOR_MAX);
        if (existsBatchCode(req.getBatchCode(), null)) {
            throw new BizException(BizCode.CONFLICT, "该产品批号已存在");
        }

        FarmBatch row = new FarmBatch();
        row.setNodeId(AuthContext.nodeId());
        row.setBatchCode(req.getBatchCode());
        row.setProductName(req.getProductName());
        row.setInspectionCert(req.getInspectionCert());
        row.setInspector(req.getInspector());
        row.setStatus(StatusConst.FARM_WAIT_RELEASE);
        baseMapper.insert(row);
    }

    // 更新批号产品
    @Override
    @Transactional
    public void updateMy(FarmBatch req) {
        requireFarm();
        FarmBatch exist = requireOwned(req.getId());
        if (exist.getStatus() != StatusConst.FARM_WAIT_RELEASE) {
            throw new BizException(BizCode.BAD_REQUEST, "仅待发布状态可更新");
        }
        checkTextLen(req.getProductName(), "产品品种", PRODUCT_NAME_MAX);
        checkTextLen(req.getInspectionCert(), "检验检疫合格证明", INSPECTION_CERT_MAX);
        checkTextLen(req.getInspector(), "官方检疫员名称", INSPECTOR_MAX);

        FarmBatch update = new FarmBatch();
        update.setProductName(req.getProductName());
        update.setInspectionCert(req.getInspectionCert());
        update.setInspector(req.getInspector());
        update.setStatus(Boolean.TRUE.equals(req.getPublish())
                ? StatusConst.FARM_RELEASED
                : StatusConst.FARM_WAIT_RELEASE);
        LambdaUpdateWrapper<FarmBatch> uw = new LambdaUpdateWrapper<FarmBatch>()
                .eq(FarmBatch::getId, exist.getId())
                .eq(FarmBatch::getStatus, StatusConst.FARM_WAIT_RELEASE);
        if (baseMapper.update(update, uw) == 0) {
            throw new BizException(BizCode.BAD_REQUEST, "批号状态已变更，请刷新后重试");
        }
    }

    // 下架批号产品
    @Override
    @Transactional
    public void offMy(Long id) {
        requireFarm();
        FarmBatch exist = requireOwned(id);
        if (exist.getStatus() != StatusConst.FARM_RELEASED) {
            throw new BizException(BizCode.BAD_REQUEST, "仅已发布状态可下架");
        }

        Long refs = procBatchMapper.selectCount(new LambdaQueryWrapper<ProcBatch>()
                .eq(ProcBatch::getInNodeId, AuthContext.nodeId())
                .eq(ProcBatch::getInBatchId, exist.getId())
                .in(ProcBatch::getStatus, StatusConst.BATCH_NEW,
                        StatusConst.BATCH_WAIT_CONFIRM, StatusConst.BATCH_CONFIRMED));
        if (refs != null && refs > 0) {
            throw new BizException(BizCode.BAD_REQUEST, "该批号已被下游引用，暂不能下架");
        }

        FarmBatch update = new FarmBatch();
        update.setStatus(StatusConst.FARM_OFF);
        LambdaUpdateWrapper<FarmBatch> uw = new LambdaUpdateWrapper<FarmBatch>()
                .eq(FarmBatch::getId, exist.getId())
                .eq(FarmBatch::getStatus, StatusConst.FARM_RELEASED);
        if (baseMapper.update(update, uw) == 0) {
            throw new BizException(BizCode.BAD_REQUEST, "批号状态已变更，请刷新后重试");
        }
    }

    // 删除批号产品
    @Override
    @Transactional
    public void deleteMy(Long id) {
        requireFarm();
        FarmBatch exist = requireOwned(id);
        if (exist.getStatus() != StatusConst.FARM_WAIT_RELEASE) {
            throw new BizException(BizCode.BAD_REQUEST, "仅待发布状态可删除");
        }

        LambdaQueryWrapper<FarmBatch> dw = new LambdaQueryWrapper<FarmBatch>()
                .eq(FarmBatch::getId, exist.getId())
                .eq(FarmBatch::getStatus, StatusConst.FARM_WAIT_RELEASE);
        if (baseMapper.delete(dw) == 0) {
            throw new BizException(BizCode.BAD_REQUEST, "批号状态已变更，请刷新后重试");
        }
    }

    // 批号产品详情
    @Override
    public FarmBatch detailMy(Long id) {
        requireFarm();
        if (id == null) {
            throw new BizException(BizCode.BAD_REQUEST, "缺少批号 id");
        }

        FarmBatch batch = baseMapper.selectByIdForNode(id);
        if (batch == null) {
            throw new BizException(BizCode.BAD_REQUEST, "批号不存在");
        }
        if (!Objects.equals(batch.getNodeId(), AuthContext.nodeId())) {
            throw new BizException(BizCode.FORBIDDEN, "无权查看他人批号");
        }
        return batch;
    }

    // 校验当前登录者：未以节点身份登录 → 401；非养殖企业 → 403
    private void requireFarm() {
        if (!AuthContext.isNode()) {
            throw new BizException(BizCode.UNAUTHORIZED, "请先以节点企业身份登录");
        }
        if (!Objects.equals(AuthContext.nodeType(), StatusConst.NODE_FARM)) {
            throw new BizException(BizCode.FORBIDDEN, "仅养殖企业可操作本类批号");
        }
    }

    // 按 id 取批号并校验归属（缺 id / 不存在 / 非本企业 → 抛错）
    private FarmBatch requireOwned(Long id) {
        if (id == null) {
            throw new BizException(BizCode.BAD_REQUEST, "缺少批号 id");
        }
        FarmBatch exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new BizException(BizCode.BAD_REQUEST, "批号不存在");
        }
        if (!Objects.equals(exist.getNodeId(), AuthContext.nodeId())) {
            throw new BizException(BizCode.FORBIDDEN, "无权操作他人批号");
        }
        return exist;
    }

    // 文本字段长度预校验，防止超长落库报 500（null 表示可空字段未填，跳过）
    private void checkTextLen(String value, String label, int max) {
        if (value != null && value.length() > max) {
            throw new BizException(BizCode.BAD_REQUEST, label + "长度不能超过 " + max + " 位");
        }
    }
}
