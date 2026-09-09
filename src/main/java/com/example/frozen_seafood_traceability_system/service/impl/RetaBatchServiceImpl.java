package com.example.frozen_seafood_traceability_system.service.impl;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import com.example.frozen_seafood_traceability_system.entity.RetaBatch;
import com.example.frozen_seafood_traceability_system.entity.WholBatch;
import com.example.frozen_seafood_traceability_system.mapper.CityMapper;
import com.example.frozen_seafood_traceability_system.mapper.NodeInfoMapper;
import com.example.frozen_seafood_traceability_system.mapper.RetaBatchMapper;
import com.example.frozen_seafood_traceability_system.mapper.WholBatchMapper;
import com.example.frozen_seafood_traceability_system.service.RetaBatchService;

/**
 * 骨架占位：零售批号业务待实现（docs/开发实施文档.md §8 任务 9）。
 */
@Service
public class RetaBatchServiceImpl extends ServiceImpl<RetaBatchMapper, RetaBatch> implements RetaBatchService {

    private static final int BATCH_CODE_MAX = 32;
    private static final int PRODUCT_NAME_MAX = 64;
    private static final int PRODUCT_TYPE_MAX = 64;
    private static final int INSPECTION_CERT_MAX = 255;
    private static final int INSPECTOR_MAX = 32;

    @Autowired
    private WholBatchMapper wholBatchMapper;
    @Autowired
    private NodeInfoMapper nodeInfoMapper;
    @Autowired
    private CityMapper cityMapper;

    @Override
    public PageResult<RetaBatch> pageMy(long page, long size, Integer status, String keyword) {
        requireReta();
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

        Page<RetaBatch> pageParam = new Page<>(page, size);
        baseMapper.selectPageForNode(pageParam, AuthContext.nodeId(), status, keyword);
        return new PageResult<>(pageParam.getTotal(), pageParam.getRecords());
    }

    // 校验批号是否存在
    @Override
    public boolean existsBatchCode(String batchCode, Long excludeId) {
        requireReta();
        if (batchCode == null || batchCode.trim().isEmpty()) {
            return false;
        }

        LambdaQueryWrapper<RetaBatch> queryWrapper = new LambdaQueryWrapper<RetaBatch>()
                .eq(RetaBatch::getBatchCode, batchCode.trim());
        if (excludeId != null) {
            queryWrapper.ne(RetaBatch::getId, excludeId);
        }
        Long cnt = baseMapper.selectCount(queryWrapper);
        return cnt != null && cnt > 0;
    }

    // 创建批号产品
    @Override
    public void createMy(RetaBatch req) {
        requireReta();
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

        // 上游必须是"启用中的批发商"
        if (req.getInNodeId() == null) {
            throw new BizException(BizCode.BAD_REQUEST, "请选择上游批发商");
        }
        NodeInfo whol = nodeInfoMapper.selectByIdWithArea(req.getInNodeId());
        if (whol == null) {
            throw new BizException(BizCode.BAD_REQUEST, "所选上游批发商不存在");
        }
        if (!Objects.equals(whol.getNodeType(), StatusConst.NODE_WHOL)) {
            throw new BizException(BizCode.BAD_REQUEST, "进场企业必须是批发商");
        }
        if (!Objects.equals(whol.getStatus(), StatusConst.NODE_ENABLED)) {
            throw new BizException(BizCode.BAD_REQUEST, "所选批发商已停用");
        }

        // 进场批号必须属于该批发商且"已确认"
        if (req.getInBatchId() == null) {
            throw new BizException(BizCode.BAD_REQUEST, "请选择进场批发批号");
        }
        WholBatch wholBatch = wholBatchMapper.selectById(req.getInBatchId());
        if (wholBatch == null || !Objects.equals(wholBatch.getNodeId(), whol.getId())) {
            throw new BizException(BizCode.BAD_REQUEST, "所选批发批号不属于该批发商");
        }
        if (!Objects.equals(wholBatch.getStatus(), StatusConst.BATCH_CONFIRMED)) {
            throw new BizException(BizCode.BAD_REQUEST, "所选批发批号未处于已确认状态");
        }

        // 服务端回填：in_area=省名+市名、in_product_name=上游批号品种（忽略前端传入）
        if (whol.getCityCode() != null) {
            City areaCity = cityMapper.selectOne(new LambdaQueryWrapper<City>()
                    .eq(City::getCityCode, whol.getCityCode()));
            if (areaCity == null || !Objects.equals(areaCity.getProvinceCode(), whol.getProvinceCode())) {
                throw new BizException(BizCode.BAD_REQUEST, "所选批发商区域数据异常，请重新选择");
            }
        }
        String area = (whol.getProvinceName() != null ? whol.getProvinceName() : "")
                + (whol.getCityName() != null ? whol.getCityName() : "");

        // 补 nodeId，status=新建
        RetaBatch row = new RetaBatch();
        row.setNodeId(AuthContext.nodeId());
        row.setBatchCode(req.getBatchCode());
        row.setProductName(req.getProductName());
        row.setProductType(req.getProductType());
        row.setInspectionCert(req.getInspectionCert());
        row.setInspector(req.getInspector());
        row.setInNodeId(whol.getId());
        row.setInArea(area);
        row.setInBatchId(wholBatch.getId());
        row.setInProductName(wholBatch.getProductName());
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
    public void updateMy(RetaBatch req) {
        requireReta();
        RetaBatch exist = requireOwned(req.getId());
        if (!Objects.equals(exist.getStatus(), StatusConst.BATCH_NEW)) {
            throw new BizException(BizCode.BAD_REQUEST, "仅新建状态可更新");
        }

        checkTextLen(req.getProductName(), "产品名称", PRODUCT_NAME_MAX);
        checkTextLen(req.getProductType(), "产品类型", PRODUCT_TYPE_MAX);
        checkTextLen(req.getInspectionCert(), "检验检疫合格证明", INSPECTION_CERT_MAX);
        checkTextLen(req.getInspector(), "官方检验员名称", INSPECTOR_MAX);

        // 只更新产品信息；batch_code、in_* 进场来源沿用原值。null 表示未提交该字段，保留原值不覆盖。
        RetaBatch update = new RetaBatch();
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

        LambdaUpdateWrapper<RetaBatch> updateWrapper = new LambdaUpdateWrapper<RetaBatch>()
                .eq(RetaBatch::getId, exist.getId())
                .eq(RetaBatch::getStatus, StatusConst.BATCH_NEW);
        if (baseMapper.update(update, updateWrapper) == 0) {
            throw new BizException(BizCode.BAD_REQUEST, "批号状态已变更，请刷新后重试");
        }
    }

    // 下架批号产品
    @Override
    @Transactional
    public void offMy(Long id) {
        requireReta();
        RetaBatch exist = requireOwned(id);
        if (!Objects.equals(exist.getStatus(), StatusConst.BATCH_CONFIRMED)) {
            throw new BizException(BizCode.BAD_REQUEST, "仅已确认状态可下架");
        }

        RetaBatch update = new RetaBatch();
        update.setStatus(StatusConst.BATCH_OFF);
        LambdaUpdateWrapper<RetaBatch> updateWrapper = new LambdaUpdateWrapper<RetaBatch>()
                .eq(RetaBatch::getId, exist.getId())
                .eq(RetaBatch::getStatus, StatusConst.BATCH_CONFIRMED);
        if (baseMapper.update(update, updateWrapper) == 0) {
            throw new BizException(BizCode.BAD_REQUEST, "批号状态已变更，请刷新后重试");
        }
    }

    // 删除批号产品
    @Override
    @Transactional
    public void deleteMy(Long id) {
        requireReta();
        RetaBatch exist = requireOwned(id);
        if (!Objects.equals(exist.getStatus(), StatusConst.BATCH_NEW)) {
            throw new BizException(BizCode.BAD_REQUEST, "仅新建状态可删除");
        }

        LambdaQueryWrapper<RetaBatch> deleteWrapper = new LambdaQueryWrapper<RetaBatch>()
                .eq(RetaBatch::getId, exist.getId())
                .eq(RetaBatch::getStatus, StatusConst.BATCH_NEW);
        if (baseMapper.delete(deleteWrapper) == 0) {
            throw new BizException(BizCode.BAD_REQUEST, "批号状态已变更，请刷新后重试");
        }
    }

    // 批号产品详情（含 traceCode；未确认时 traceCode=null，联出上游批发批号与企业名）
    @Override
    public RetaBatch detailMy(Long id) {
        requireReta();
        if (id == null) {
            throw new BizException(BizCode.BAD_REQUEST, "缺少批号 id");
        }

        RetaBatch batch = baseMapper.selectByIdForNode(id);
        if (batch == null) {
            throw new BizException(BizCode.BAD_REQUEST, "批号不存在");
        }
        if (!Objects.equals(batch.getNodeId(), AuthContext.nodeId())) {
            throw new BizException(BizCode.FORBIDDEN, "无权查看他人批号");
        }
        return batch;
    }

    // 供批发商查看"有哪些零售批号正等待本企业确认进场"
    @Override
    public PageResult<RetaBatch> waitConfirmForWhol(Long wholNodeId, String keyword, long page, long size) {
        requireWhol();
        if (!Objects.equals(wholNodeId, AuthContext.nodeId())) {
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

        Page<RetaBatch> pageParam = new Page<>(page, size);
        baseMapper.waitConfirmList(pageParam, wholNodeId, keyword);
        return new PageResult<>(pageParam.getTotal(), pageParam.getRecords());
    }

    @Override
    @Transactional
    public String confirmByWhol(Long id, Long wholNodeId) {
        requireWhol();
        if (!Objects.equals(wholNodeId, AuthContext.nodeId())) {
            throw new BizException(BizCode.FORBIDDEN, "无权确认他人进场批号");
        }
        if (id == null) {
            throw new BizException(BizCode.BAD_REQUEST, "缺少批号 id");
        }

        RetaBatch batch = baseMapper.selectById(id);
        if (batch == null) {
            throw new BizException(BizCode.BAD_REQUEST, "批号不存在");
        }
        if (!Objects.equals(batch.getInNodeId(), wholNodeId)) {
            throw new BizException(BizCode.FORBIDDEN, "该批号非本企业进场批号，无权确认");
        }
        if (!Objects.equals(batch.getStatus(), StatusConst.BATCH_WAIT_CONFIRM)) {
            throw new BizException(BizCode.BAD_REQUEST, "该批号不在待确认状态");
        }

        // 被引用的本企业批发批号仍须处于"已确认"，避免确认已下架/删除的上游
        WholBatch wholBatch = batch.getInBatchId() != null
                ? wholBatchMapper.selectById(batch.getInBatchId())
                : null;
        if (wholBatch == null || !Objects.equals(wholBatch.getStatus(), StatusConst.BATCH_CONFIRMED)) {
            throw new BizException(BizCode.BAD_REQUEST, "上游批发批号已不在确认状态，无法确认");
        }

        // 置"已确认"并同事务写溯源码；撞 trace_code 唯一键则换号重试（最多 5 次）
        for (int i = 0; i < 5; i++) {
            String traceCode = genTraceCode();
            RetaBatch update = new RetaBatch();
            update.setStatus(StatusConst.BATCH_CONFIRMED);
            update.setTraceCode(traceCode);
            LambdaUpdateWrapper<RetaBatch> updateWrapper = new LambdaUpdateWrapper<RetaBatch>()
                    .eq(RetaBatch::getId, batch.getId())
                    .eq(RetaBatch::getStatus, StatusConst.BATCH_WAIT_CONFIRM);
            try {
                if (baseMapper.update(update, updateWrapper) == 0) {
                    throw new BizException(BizCode.BAD_REQUEST, "批号状态已变更，请刷新后重试");
                }
                return traceCode;
            } catch (DuplicateKeyException e) {
                // trace_code 撞唯一键：换一个新号重试
            }
        }
        throw new BizException(BizCode.INTERNAL_ERROR, "溯源码生成失败，请稍后重试");
    }

    // 生成溯源码：TSF-yyyyMMdd-6位(A-Z0-9 随机)
    private static final String TRACE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private String genTraceCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder("TSF-").append(date).append('-');
        for (int i = 0; i < 6; i++) {
            sb.append(TRACE_CODE_CHARS.charAt(random.nextInt(TRACE_CODE_CHARS.length())));
        }
        return sb.toString();
    }

    // 消费者溯源第一步：按溯源码定位零售批号（联出批发企业名）；查不到返回 null，由上层判定"溯源码不存在"
    @Override
    public RetaBatch findByTraceCode(String traceCode) {
        if (traceCode == null || traceCode.trim().isEmpty()) {
            return null;
        }
        return baseMapper.selectByTraceCode(traceCode.trim());
    }

    // 校验当前登录者：未以节点身份登录 → 401；非批发商 → 403
    private void requireWhol() {
        if (!AuthContext.isNode()) {
            throw new BizException(BizCode.UNAUTHORIZED, "请先以节点企业身份登录");
        }
        if (!Objects.equals(AuthContext.nodeType(), StatusConst.NODE_WHOL)) {
            throw new BizException(BizCode.FORBIDDEN, "仅批发商可确认本类批号");
        }
    }

    // 校验当前登录者：未以节点身份登录 → 401；非批发商 → 403
    private void requireReta() {
        if (!AuthContext.isNode()) {
            throw new BizException(BizCode.UNAUTHORIZED, "请先以节点企业身份登录");
        }
        if (!Objects.equals(AuthContext.nodeType(), StatusConst.NODE_RETA)) {
            throw new BizException(BizCode.FORBIDDEN, "仅零售商可操作本类批号");
        }
    }

    // 按 id 取批号并校验归属（缺 id / 不存在 / 非本企业 → 抛错）
    private RetaBatch requireOwned(Long id) {
        if (id == null) {
            throw new BizException(BizCode.BAD_REQUEST, "缺少批号 id");
        }

        RetaBatch exist = baseMapper.selectById(id);
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
