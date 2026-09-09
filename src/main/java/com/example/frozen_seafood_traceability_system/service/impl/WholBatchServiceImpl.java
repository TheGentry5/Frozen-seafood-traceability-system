package com.example.frozen_seafood_traceability_system.service.impl;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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
import com.example.frozen_seafood_traceability_system.entity.City;
import com.example.frozen_seafood_traceability_system.entity.NodeInfo;
import com.example.frozen_seafood_traceability_system.entity.ProcBatch;
import com.example.frozen_seafood_traceability_system.entity.RetaBatch;
import com.example.frozen_seafood_traceability_system.entity.WholBatch;
import com.example.frozen_seafood_traceability_system.mapper.CityMapper;
import com.example.frozen_seafood_traceability_system.mapper.NodeInfoMapper;
import com.example.frozen_seafood_traceability_system.mapper.ProcBatchMapper;
import com.example.frozen_seafood_traceability_system.mapper.RetaBatchMapper;
import com.example.frozen_seafood_traceability_system.mapper.WholBatchMapper;
import com.example.frozen_seafood_traceability_system.service.WholBatchService;

/**
 * 批发商产品批号业务实现（docs/开发实施文档.md §6.3 whol 行）。
 * 除批发侧 CRUD/状态流转外，还包含加工端对"待确认进场"批发批号的列表与确认（任务 5）。
 */
@Service
public class WholBatchServiceImpl extends ServiceImpl<WholBatchMapper, WholBatch> implements WholBatchService {

    private static final int BATCH_CODE_MAX = 32;
    private static final int PRODUCT_NAME_MAX = 64;
    private static final int PRODUCT_TYPE_MAX = 64;
    private static final int INSPECTION_CERT_MAX = 255;
    private static final int INSPECTOR_MAX = 32;

    @Autowired
    private ProcBatchMapper procBatchMapper;
    @Autowired
    private RetaBatchMapper retaBatchMapper;
    @Autowired
    private NodeInfoMapper nodeInfoMapper;
    @Autowired
    private CityMapper cityMapper;

    // 分页查询
    @Override
    public PageResult<WholBatch> pageMy(long page, long size, Integer status, String keyword) {
        requireWhol();
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

        Page<WholBatch> pageParam = new Page<>(page, size);
        baseMapper.selectPageForNode(pageParam, AuthContext.nodeId(), status, keyword);
        // MyBatis-Plus 会把 `total`/`records` 回填进 `pageParam`
        return new PageResult<>(pageParam.getTotal(), pageParam.getRecords());
    }

    // 校验批号是否存在
    @Override
    public boolean existsBatchCode(String batchCode, Long excludeId) {
        requireWhol();
        if (batchCode == null || batchCode.trim().isEmpty()) {
            return false;
        }

        LambdaQueryWrapper<WholBatch> qw = new LambdaQueryWrapper<WholBatch>()
                .eq(WholBatch::getBatchCode, batchCode.trim());
        if (excludeId != null) {
            qw.ne(WholBatch::getId, excludeId);
        }
        Long cnt = baseMapper.selectCount(qw);
        return cnt != null && cnt > 0;
    }

    // 创建批号产品
    @Override
    public void createMy(WholBatch req) {
        requireWhol();
        if (req.getBatchCode() == null || req.getBatchCode().trim().isEmpty()) {
            throw new BizException(BizCode.BAD_REQUEST, "产品批号不能为空");
        }

        req.setBatchCode(req.getBatchCode().trim());
        checkTextLen(req.getBatchCode(), "产品批号", BATCH_CODE_MAX);
        checkTextLen(req.getProductName(), "产品名称", PRODUCT_NAME_MAX);
        checkTextLen(req.getProductType(), "产品类型", PRODUCT_TYPE_MAX);
        checkTextLen(req.getInspectionCert(), "检验检疫合格证明", INSPECTION_CERT_MAX);
        checkTextLen(req.getInspector(), "官方检验员名称", INSPECTOR_MAX);
        if (existsBatchCode(req.getBatchCode(), null)) {
            throw new BizException(BizCode.CONFLICT, "该产品批号已存在");
        }

        // 上游必须是"启用中的加工企业"
        if (req.getInNodeId() == null) {
            throw new BizException(BizCode.BAD_REQUEST, "请选择上游加工企业");
        }
        NodeInfo proc = nodeInfoMapper.selectByIdWithArea(req.getInNodeId());
        if (proc == null) {
            throw new BizException(BizCode.BAD_REQUEST, "所选上游企业不存在");
        }
        if (!Objects.equals(proc.getNodeType(), StatusConst.NODE_PROC)) {
            throw new BizException(BizCode.BAD_REQUEST, "进场企业必须是加工企业");
        }
        if (!Objects.equals(proc.getStatus(), StatusConst.NODE_ENABLED)) {
            throw new BizException(BizCode.BAD_REQUEST, "所选企业已停用");
        }

        // 进场批号必须属于该加工企业且"已确认"
        if (req.getInBatchId() == null) {
            throw new BizException(BizCode.BAD_REQUEST, "请选择进场加工批号");
        }
        ProcBatch procBatch = procBatchMapper.selectById(req.getInBatchId());
        if (procBatch == null || !Objects.equals(procBatch.getNodeId(), proc.getId())) {
            throw new BizException(BizCode.BAD_REQUEST, "所选加工批号不属于该企业");
        }
        if (!Objects.equals(procBatch.getStatus(), StatusConst.BATCH_CONFIRMED)) {
            throw new BizException(BizCode.BAD_REQUEST, "所选加工批号未处于已确认状态");
        }

        // 服务端回填：in_area=省名+市名、in_product_name=上游批号品种（忽略前端传入）
        if (proc.getCityCode() != null) {
            City areaCity = cityMapper.selectOne(new LambdaQueryWrapper<City>()
                    .eq(City::getCityCode, proc.getCityCode()));
            if (areaCity == null || !Objects.equals(areaCity.getProvinceCode(), proc.getProvinceCode())) {
                throw new BizException(BizCode.BAD_REQUEST, "所选加工企业区域数据异常，请重新选择");
            }
        }
        String area = (proc.getProvinceName() != null ? proc.getProvinceName() : "")
                + (proc.getCityName() != null ? proc.getCityName() : "");

        // 补 nodeId，status=新建
        WholBatch row = new WholBatch();
        row.setNodeId(AuthContext.nodeId());
        row.setBatchCode(req.getBatchCode());
        row.setProductName(req.getProductName());
        row.setProductType(req.getProductType());
        row.setInspectionCert(req.getInspectionCert());
        row.setInspector(req.getInspector());
        row.setInNodeId(proc.getId());
        row.setInArea(area);
        row.setInBatchId(procBatch.getId());
        row.setInProductName(procBatch.getProductName());
        row.setStatus(StatusConst.BATCH_NEW);
        try {
            baseMapper.insert(row);
        } catch (DuplicateKeyException e) {
            throw new BizException(BizCode.CONFLICT, "该产品批号已存在");
        }
    }

    // 更新批号产品
    @Override
    @Transactional
    public void updateMy(WholBatch req) {
        requireWhol();
        WholBatch exist = requireOwned(req.getId());
        if (!Objects.equals(exist.getStatus(), StatusConst.BATCH_NEW)) {
            throw new BizException(BizCode.BAD_REQUEST, "仅新建状态可更新");
        }

        checkTextLen(req.getProductName(), "产品名称", PRODUCT_NAME_MAX);
        checkTextLen(req.getProductType(), "产品类型", PRODUCT_TYPE_MAX);
        checkTextLen(req.getInspectionCert(), "检验检疫合格证明", INSPECTION_CERT_MAX);
        checkTextLen(req.getInspector(), "官方检验员名称", INSPECTOR_MAX);

        // 只更新产品信息；batch_code、in_* 进场来源沿用原值。null 表示未提交该字段，保留原值不覆盖。
        WholBatch update = new WholBatch();
        if (req.getProductName() != null) {
            update.setProductName(req.getProductName());
        }
        if (req.getProductType() != null) {
            update.setProductType(req.getProductType());
        }
        if (req.getInspectionCert() != null) {
            update.setInspectionCert(req.getInspectionCert());
        }
        if (req.getInspector() != null) {
            update.setInspector(req.getInspector());
        }
        update.setStatus(Boolean.TRUE.equals(req.getSendConfirm())
                ? StatusConst.BATCH_WAIT_CONFIRM
                : StatusConst.BATCH_NEW);

        LambdaUpdateWrapper<WholBatch> updateWrapper = new LambdaUpdateWrapper<WholBatch>()
                .eq(WholBatch::getId, exist.getId())
                .eq(WholBatch::getStatus, StatusConst.BATCH_NEW);
        if (baseMapper.update(update, updateWrapper) == 0) {
            throw new BizException(BizCode.BAD_REQUEST, "批号状态已变更，请刷新后重试");
        }
    }

    // 下架批号产品
    @Override
    @Transactional
    public void offMy(Long id) {
        requireWhol();
        WholBatch exist = requireOwned(id);
        if (!Objects.equals(exist.getStatus(), StatusConst.BATCH_CONFIRMED)) {
            throw new BizException(BizCode.BAD_REQUEST, "仅已确认状态可下架");
        }

        // 已被下游零售批号（新建/待确认/已确认）引用时禁止下架，避免切断溯源链
        Long refs = retaBatchMapper.selectCount(new LambdaQueryWrapper<RetaBatch>()
                .eq(RetaBatch::getInNodeId, AuthContext.nodeId())
                .eq(RetaBatch::getInBatchId, exist.getId())
                .in(RetaBatch::getStatus, StatusConst.BATCH_NEW,
                        StatusConst.BATCH_WAIT_CONFIRM, StatusConst.BATCH_CONFIRMED));
        if (refs != null && refs > 0) {
            throw new BizException(BizCode.BAD_REQUEST, "该批号已被下游引用，暂不能下架");
        }

        WholBatch update = new WholBatch();
        update.setStatus(StatusConst.BATCH_OFF);
        LambdaUpdateWrapper<WholBatch> updateWrapper = new LambdaUpdateWrapper<WholBatch>()
                .eq(WholBatch::getId, exist.getId())
                .eq(WholBatch::getStatus, StatusConst.BATCH_CONFIRMED);
        if (baseMapper.update(update, updateWrapper) == 0) {
            throw new BizException(BizCode.BAD_REQUEST, "批号状态已变更，请刷新后重试");
        }
    }

    // 删除批号产品
    @Override
    @Transactional
    public void deleteMy(Long id) {
        requireWhol();
        WholBatch exist = requireOwned(id);
        if (!Objects.equals(exist.getStatus(), StatusConst.BATCH_NEW)) {
            throw new BizException(BizCode.BAD_REQUEST, "仅新建状态可删除");
        }

        LambdaQueryWrapper<WholBatch> deleteWrapper = new LambdaQueryWrapper<WholBatch>()
                .eq(WholBatch::getId, exist.getId())
                .eq(WholBatch::getStatus, StatusConst.BATCH_NEW);
        if (baseMapper.delete(deleteWrapper) == 0) {
            throw new BizException(BizCode.BAD_REQUEST, "批号状态已变更，请刷新后重试");
        }
    }

    // 批号产品详情（联出上游加工批号与企业名）
    @Override
    public WholBatch detailMy(Long id) {
        requireWhol();
        if (id == null) {
            throw new BizException(BizCode.BAD_REQUEST, "缺少批号 id");
        }

        WholBatch batch = baseMapper.selectByIdForNode(id);
        if (batch == null) {
            throw new BizException(BizCode.BAD_REQUEST, "批号不存在");
        }
        if (!Objects.equals(batch.getNodeId(), AuthContext.nodeId())) {
            throw new BizException(BizCode.FORBIDDEN, "无权查看他人批号");
        }
        return batch;
    }

    // 供加工企业查看"有哪些批发批号正等待本企业确认进场"
    @Override
    public PageResult<WholBatch> waitConfirmForProc(Long procNodeId, String keyword, long page, long size) {
        requireProc();
        if (!Objects.equals(procNodeId, AuthContext.nodeId())) {
            throw new BizException(BizCode.FORBIDDEN, "无权查看他人待确认批号");
        }
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

        Page<WholBatch> pageParam = new Page<>(page, size);
        baseMapper.waitConfirmList(pageParam, procNodeId, keyword);
        return new PageResult<>(pageParam.getTotal(), pageParam.getRecords());
    }

    // 进场确认"等待本企业确认进场"的批发批号
    @Override
    @Transactional
    public void confirmByProc(Long id, Long procNodeId) {
        requireProc();
        if (!Objects.equals(procNodeId, AuthContext.nodeId())) {
            throw new BizException(BizCode.FORBIDDEN, "无权确认他人进场批号");
        }
        if (id == null) {
            throw new BizException(BizCode.BAD_REQUEST, "缺少批号 id");
        }

        WholBatch batch = baseMapper.selectById(id);
        if (batch == null) {
            throw new BizException(BizCode.BAD_REQUEST, "批号不存在");
        }
        if (!Objects.equals(batch.getInNodeId(), procNodeId)) {
            throw new BizException(BizCode.FORBIDDEN, "该批号非本企业进场批号，无权确认");
        }
        if (!Objects.equals(batch.getStatus(), StatusConst.BATCH_WAIT_CONFIRM)) {
            throw new BizException(BizCode.BAD_REQUEST, "该批号不在待确认状态");
        }

        // 被引用的本企业加工批号仍须处于"已确认"，避免确认已下架/已删除的上游
        ProcBatch procBatch = batch.getInBatchId() != null
                ? procBatchMapper.selectById(batch.getInBatchId())
                : null;
        if (procBatch == null || !Objects.equals(procBatch.getStatus(), StatusConst.BATCH_CONFIRMED)) {
            throw new BizException(BizCode.BAD_REQUEST, "上游加工批号已不在确认状态，无法确认");
        }

        WholBatch update = new WholBatch();
        update.setStatus(StatusConst.BATCH_CONFIRMED);
        LambdaUpdateWrapper<WholBatch> updateWrapper = new LambdaUpdateWrapper<WholBatch>()
                .eq(WholBatch::getId, batch.getId())
                .eq(WholBatch::getStatus, StatusConst.BATCH_WAIT_CONFIRM);
        if (baseMapper.update(update, updateWrapper) == 0) {
            throw new BizException(BizCode.BAD_REQUEST, "批号状态已变更，请刷新后重试");
        }
    }

    // 校验当前登录者：未以节点身份登录 → 401；非加工企业 → 403
    private void requireProc() {
        if (!AuthContext.isNode()) {
            throw new BizException(BizCode.UNAUTHORIZED, "请先以节点企业身份登录");
        }
        if (!Objects.equals(AuthContext.nodeType(), StatusConst.NODE_PROC)) {
            throw new BizException(BizCode.FORBIDDEN, "仅加工企业可确认本类批号");
        }
    }

    // 校验当前登录者：未以节点身份登录 → 401；非批发商 → 403
    private void requireWhol() {
        if (!AuthContext.isNode()) {
            throw new BizException(BizCode.UNAUTHORIZED, "请先以节点企业身份登录");
        }
        if (!Objects.equals(AuthContext.nodeType(), StatusConst.NODE_WHOL)) {
            throw new BizException(BizCode.FORBIDDEN, "仅批发商可操作本类批号");
        }
    }

    // 按 id 取批号并校验归属（缺 id / 不存在 / 非本企业 → 抛错）
    private WholBatch requireOwned(Long id) {
        if (id == null) {
            throw new BizException(BizCode.BAD_REQUEST, "缺少批号 id");
        }

        WholBatch exist = baseMapper.selectById(id);
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
