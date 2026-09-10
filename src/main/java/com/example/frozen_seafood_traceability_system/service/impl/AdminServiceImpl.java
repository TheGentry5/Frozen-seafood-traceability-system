package com.example.frozen_seafood_traceability_system.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.frozen_seafood_traceability_system.common.AuthContext;
import com.example.frozen_seafood_traceability_system.common.BizCode;
import com.example.frozen_seafood_traceability_system.common.BizException;
import com.example.frozen_seafood_traceability_system.common.PageResult;
import com.example.frozen_seafood_traceability_system.common.StatusConst;
import com.example.frozen_seafood_traceability_system.common.TokenStore;
import com.example.frozen_seafood_traceability_system.entity.City;
import com.example.frozen_seafood_traceability_system.entity.ColdChainRecord;
import com.example.frozen_seafood_traceability_system.entity.FarmBatch;
import com.example.frozen_seafood_traceability_system.entity.NodeInfo;
import com.example.frozen_seafood_traceability_system.entity.ProcBatch;
import com.example.frozen_seafood_traceability_system.entity.Province;
import com.example.frozen_seafood_traceability_system.entity.RetaBatch;
import com.example.frozen_seafood_traceability_system.entity.WholBatch;
import com.example.frozen_seafood_traceability_system.mapper.CityMapper;
import com.example.frozen_seafood_traceability_system.mapper.ColdChainRecordMapper;
import com.example.frozen_seafood_traceability_system.mapper.FarmBatchMapper;
import com.example.frozen_seafood_traceability_system.mapper.NodeInfoMapper;
import com.example.frozen_seafood_traceability_system.mapper.ProcBatchMapper;
import com.example.frozen_seafood_traceability_system.mapper.ProvinceMapper;
import com.example.frozen_seafood_traceability_system.mapper.RetaBatchMapper;
import com.example.frozen_seafood_traceability_system.mapper.WholBatchMapper;
import com.example.frozen_seafood_traceability_system.service.AdminService;

/**
 * 系统管理端：节点企业 CRUD 与注册统计（docs/开发实施文档.md §6.5 / §8 任务 11）。
 */
@Service
public class AdminServiceImpl implements AdminService {

    // 类内常量
    private static final int NODE_CODE_MAX = 32;
    private static final int NODE_NAME_MAX = 64;
    private static final int AREA_CODE_MAX = 16;
    private static final int CONTACT_MAX = 32;
    private static final int PHONE_MAX = 32;
    private static final String DEFAULT_PASSWORD = "123456";

    // 联系电话：数字 / + / - / 空格 / 括号
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+()\\- ]+$");

    @Autowired
    private NodeInfoMapper nodeInfoMapper;
    @Autowired
    private CityMapper cityMapper;
    @Autowired
    private ProvinceMapper provinceMapper;
    @Autowired
    private FarmBatchMapper farmBatchMapper;
    @Autowired
    private ProcBatchMapper procBatchMapper;
    @Autowired
    private WholBatchMapper wholBatchMapper;
    @Autowired
    private RetaBatchMapper retaBatchMapper;
    @Autowired
    private ColdChainRecordMapper coldChainRecordMapper;
    @Autowired
    private TokenStore tokenStore;

    @Override
    public PageResult<NodeInfo> pageNode(long page, long size, String nodeName, Integer nodeType,
            String provinceCode, String cityCode) {
        requireAdmin();

        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        } else if (size > 100) {
            size = 100;
        }

        if (nodeType != null
                && (nodeType < StatusConst.NODE_FARM || nodeType > StatusConst.NODE_RETA)) {
            throw new BizException(BizCode.BAD_REQUEST, "企业类型不合法");
        }

        nodeName = trimToNull(nodeName);
        provinceCode = trimToNull(provinceCode);
        cityCode = trimToNull(cityCode);

        Page<NodeInfo> pageParam = new Page<>(page, size);
        nodeInfoMapper.selectPageWithArea(pageParam, nodeName, nodeType, provinceCode, cityCode);

        for (NodeInfo n : pageParam.getRecords()) {
            n.setNodeTypeName(nodeTypeName(n.getNodeType()));
        }
        return new PageResult<>(pageParam.getTotal(), pageParam.getRecords());
    }

    @Override
    public NodeInfo detailNode(Long id) {
        requireAdmin();
        if (id == null) {
            throw new BizException(BizCode.BAD_REQUEST, "缺少企业 id");
        }

        NodeInfo node = nodeInfoMapper.selectByIdWithArea(id);
        if (node == null) {
            throw new BizException(BizCode.BAD_REQUEST, "企业不存在");
        }
        node.setPassword(null);
        node.setNodeTypeName(nodeTypeName(node.getNodeType()));
        return node;
    }

    @Override
    public void createNode(NodeInfo req) {
        requireAdmin();
        if (req == null) {
            throw new BizException(BizCode.BAD_REQUEST, "请求体不能为空");
        }
        // node_code：必填 + 去空白 + 长度 + 唯一
        if (req.getNodeCode() == null || req.getNodeCode().trim().isEmpty()) {
            throw new BizException(BizCode.BAD_REQUEST, "企业编码不能为空");
        }
        req.setNodeCode(req.getNodeCode().trim());
        checkTextLen(req.getNodeCode(), "企业编码", NODE_CODE_MAX);

        // node_name：必填 + 长度
        if (req.getNodeName() == null || req.getNodeName().trim().isEmpty()) {
            throw new BizException(BizCode.BAD_REQUEST, "企业名称不能为空");
        }
        req.setNodeName(req.getNodeName().trim());
        checkTextLen(req.getNodeName(), "企业名称", NODE_NAME_MAX);

        // node_type； 必填且为合法类型1~4
        Integer type = req.getNodeType();
        if (type == null || type < StatusConst.NODE_FARM || type > StatusConst.NODE_RETA) {
            throw new BizException(BizCode.BAD_REQUEST, "企业类型不合法");
        }

        // 省/市/联系人/电话：可选，长度 + 格式 + 省市从属校验
        checkTextLen(req.getProvinceCode(), "省份编码", AREA_CODE_MAX);
        checkTextLen(req.getCityCode(), "城市编码", AREA_CODE_MAX);
        checkTextLen(req.getContact(), "联系人", CONTACT_MAX);
        checkTextLen(req.getPhone(), "联系电话", PHONE_MAX);
        String provinceCode = trimToNull(req.getProvinceCode());
        String cityCode = trimToNull(req.getCityCode());
        validateArea(provinceCode, cityCode);
        checkPhone(req.getPhone());

        // status：缺省启用；显式传入只允许 1/2
        Integer status = req.getStatus() == null ? StatusConst.NODE_ENABLED : req.getStatus();
        if (!Objects.equals(status, StatusConst.NODE_ENABLED)
                && !Objects.equals(status, StatusConst.NODE_DISABLED)) {
            throw new BizException(BizCode.BAD_REQUEST, "企业状态不合法");
        }

        // password：缺省 123456，去空白后长度 6~20，MD5 落库
        String rawPwd = req.getPassword();
        if (rawPwd == null || rawPwd.trim().isEmpty()) {
            rawPwd = DEFAULT_PASSWORD;
        } else {
            rawPwd = rawPwd.trim();
            if (rawPwd.length() < 6 || rawPwd.length() > 20) {
                throw new BizException(BizCode.BAD_REQUEST, "密码长度须为 6~20 位");
            }
        }

        // 编码唯一预检（并发兜底靠唯一索引 + DuplicateKeyException）
        Long cnt = nodeInfoMapper.selectCount(new LambdaQueryWrapper<NodeInfo>()
                .eq(NodeInfo::getNodeCode, req.getNodeCode()));
        if (cnt != null && cnt > 0) {
            throw new BizException(BizCode.CONFLICT, "该企业编码已存在");
        }

        // 白名单重建，避免前端回传 id/createTime 等字段被写入
        NodeInfo row = new NodeInfo();
        row.setNodeCode(req.getNodeCode());
        row.setNodeName(req.getNodeName());
        row.setNodeType(type);
        row.setProvinceCode(provinceCode);
        row.setCityCode(cityCode);
        row.setContact(trimToNull(req.getContact()));
        row.setPhone(trimToNull(req.getPhone()));
        row.setStatus(status);
        row.setPassword(md5(rawPwd));

        try {
            nodeInfoMapper.insert(row);
        } catch (DuplicateKeyException e) {
            throw new BizException(BizCode.CONFLICT, "该企业编码已存在");
        }

    }

    @Override
    @Transactional
    public void updateNode(NodeInfo req) {
        requireAdmin();
        if (req == null || req.getId() == null) {
            throw new BizException(BizCode.BAD_REQUEST, "缺少企业 id");
        }
        NodeInfo exist = nodeInfoMapper.selectById(req.getId());
        if (exist == null) {
            throw new BizException(BizCode.BAD_REQUEST, "企业不存在");
        }

        // node_code：提交了才校验；去空白 + 长度 + 唯一（排除自身）
        if (req.getNodeCode() != null) {
            if (req.getNodeCode().trim().isEmpty()) {
                throw new BizException(BizCode.BAD_REQUEST, "企业编码不能为空");
            }
            req.setNodeCode(req.getNodeCode().trim());
            checkTextLen(req.getNodeCode(), "企业编码", NODE_CODE_MAX);
            Long cnt = nodeInfoMapper.selectCount(new LambdaQueryWrapper<NodeInfo>()
                    .eq(NodeInfo::getNodeCode, req.getNodeCode())
                    .ne(NodeInfo::getId, req.getId()));
            if (cnt != null && cnt > 0) {
                throw new BizException(BizCode.CONFLICT, "该企业编码已存在");
            }
        }

        // node_name：提交了才校验非空 + 长度
        if (req.getNodeName() != null) {
            if (req.getNodeName().trim().isEmpty()) {
                throw new BizException(BizCode.BAD_REQUEST, "企业名称不能为空");
            }
            req.setNodeName(req.getNodeName().trim());
            checkTextLen(req.getNodeName(), "企业名称", NODE_NAME_MAX);
        }

        // node_type / status：提交了才校验合法取值
        if (req.getNodeType() != null
                && (req.getNodeType() < StatusConst.NODE_FARM
                        || req.getNodeType() > StatusConst.NODE_RETA)) {
            throw new BizException(BizCode.BAD_REQUEST, "企业类型不合法");
        }
        if (req.getStatus() != null
                && !Objects.equals(req.getStatus(), StatusConst.NODE_ENABLED)
                && !Objects.equals(req.getStatus(), StatusConst.NODE_DISABLED)) {
            throw new BizException(BizCode.BAD_REQUEST, "企业状态不合法");
        }

        checkTextLen(req.getProvinceCode(), "省份编码", AREA_CODE_MAX);
        checkTextLen(req.getCityCode(), "城市编码", AREA_CODE_MAX);
        checkTextLen(req.getContact(), "联系人", CONTACT_MAX);
        checkTextLen(req.getPhone(), "联系电话", PHONE_MAX);
        checkPhone(req.getPhone());

        // 省/市以更新后的有效值做从属校验（未提交则沿用原值）
        String provinceCode = req.getProvinceCode() != null
                ? trimToNull(req.getProvinceCode())
                : exist.getProvinceCode();
        String cityCode = req.getCityCode() != null
                ? trimToNull(req.getCityCode())
                : exist.getCityCode();
        validateArea(provinceCode, cityCode);

        // 白名单：只有非 null 才覆盖；可空字段用 set 显式赋值以支持传空串清空；
        // password 永不写入；create_time 不变
        LambdaUpdateWrapper<NodeInfo> uw = new LambdaUpdateWrapper<NodeInfo>()
                .eq(NodeInfo::getId, req.getId());
        boolean touched = false;
        if (req.getNodeCode() != null) {
            uw.set(NodeInfo::getNodeCode, req.getNodeCode());
            touched = true;
        }
        if (req.getNodeName() != null) {
            uw.set(NodeInfo::getNodeName, req.getNodeName());
            touched = true;
        }
        if (req.getNodeType() != null) {
            uw.set(NodeInfo::getNodeType, req.getNodeType());
            touched = true;
        }
        if (req.getStatus() != null) {
            uw.set(NodeInfo::getStatus, req.getStatus());
            touched = true;
        }
        if (req.getProvinceCode() != null) {
            uw.set(NodeInfo::getProvinceCode, provinceCode);
            touched = true;
        }
        if (req.getCityCode() != null) {
            uw.set(NodeInfo::getCityCode, cityCode);
            touched = true;
        }
        if (req.getContact() != null) {
            uw.set(NodeInfo::getContact, trimToNull(req.getContact()));
            touched = true;
        }
        if (req.getPhone() != null) {
            uw.set(NodeInfo::getPhone, trimToNull(req.getPhone()));
            touched = true;
        }

        if (!touched) {
            throw new BizException(BizCode.BAD_REQUEST, "没有需要更新的字段");
        }

        // 变更企业类型前，确保名下无未下架批号（否则原类型数据将无法再被管理）
        boolean typeChanged = req.getNodeType() != null
                && !Objects.equals(req.getNodeType(), exist.getNodeType());
        if (typeChanged && hasUnfinishedBatch(exist.getId())) {
            throw new BizException(BizCode.BAD_REQUEST, "该企业名下存在批号，暂不能变更企业类型，请先处理");
        }
        boolean disabled = req.getStatus() != null
                && Objects.equals(req.getStatus(), StatusConst.NODE_DISABLED);

        try {
            nodeInfoMapper.update(null, uw);
        } catch (DuplicateKeyException e) {
            throw new BizException(BizCode.CONFLICT, "该企业编码已存在");
        }

        // 停用或改类型后吊销该企业已发 token，强制重新登录
        if (typeChanged || disabled) {
            tokenStore.removeBySubject("NODE", exist.getId());
        }
    }

    @Override
    @Transactional
    public void deleteNode(Long id) {
        requireAdmin();
        if (id == null) {
            throw new BizException(BizCode.BAD_REQUEST, "缺少企业 id");
        }
        if (nodeInfoMapper.selectById(id) == null) {
            throw new BizException(BizCode.BAD_REQUEST, "企业不存在");
        }

        // 名下 4 张批号表中任一存在"未下架"批号 → 403，禁止删除
        if (hasUnfinishedBatch(id)) {
            throw new BizException(BizCode.FORBIDDEN, "请先处理该企业名下批号");
        }

        // 已被下游批号作为上游引用（in_node_id）→ 403，避免删除后溯源链断链
        Long procDown = procBatchMapper.selectCount(new LambdaQueryWrapper<ProcBatch>()
                .eq(ProcBatch::getInNodeId, id));
        Long wholDown = wholBatchMapper.selectCount(new LambdaQueryWrapper<WholBatch>()
                .eq(WholBatch::getInNodeId, id));
        Long retaDown = retaBatchMapper.selectCount(new LambdaQueryWrapper<RetaBatch>()
                .eq(RetaBatch::getInNodeId, id));
        if (isPositive(procDown) || isPositive(wholDown) || isPositive(retaDown)) {
            throw new BizException(BizCode.FORBIDDEN, "该企业已被下游批号引用，暂不能删除");
        }

        // 名下批号均为已下架且无下游引用，级联清理批号及其冷链记录，避免孤儿数据
        farmBatchMapper.delete(new LambdaQueryWrapper<FarmBatch>().eq(FarmBatch::getNodeId, id));
        procBatchMapper.delete(new LambdaQueryWrapper<ProcBatch>().eq(ProcBatch::getNodeId, id));
        wholBatchMapper.delete(new LambdaQueryWrapper<WholBatch>().eq(WholBatch::getNodeId, id));
        retaBatchMapper.delete(new LambdaQueryWrapper<RetaBatch>().eq(RetaBatch::getNodeId, id));
        coldChainRecordMapper.delete(new LambdaQueryWrapper<ColdChainRecord>().eq(ColdChainRecord::getNodeId, id));

        nodeInfoMapper.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> stats() {
        requireAdmin();

        // 1) monthTrend：查当前年，Java 侧补齐 1-12 月，缺月补 0
        int year = LocalDate.now().getYear();
        Map<Integer, Long> byMonth = new HashMap<>();
        for (Map<String, Object> row : nodeInfoMapper.selectMonthTrend(year)) {
            Integer month = toInt(row.get("month"));
            if (month != null) {
                byMonth.put(month, toLong(row.get("count")));
            }
        }
        List<Map<String, Object>> monthTrend = new ArrayList<>(12);
        for (int month = 1; month <= 12; month++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", month);
            item.put("count", byMonth.getOrDefault(month, 0L));
            monthTrend.add(item);
        }

        // 2) provinceDist / typeDist 直接透传（SQL 已按 provinceName / nodeType 起别名）
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("monthTrend", monthTrend);
        data.put("provinceDist", nodeInfoMapper.selectProvinceDist());
        data.put("typeDist", nodeInfoMapper.selectTypeDist());
        return data;
    }

    // 校验当前登录者：非管理员 → 403
    private void requireAdmin() {
        if (!AuthContext.isAdmin()) {
            throw new BizException(BizCode.FORBIDDEN, "请先以管理员身份登录");
        }
    }

    // 空白串归一为 null，避免 XML 里 LIKE '%%'
    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    // node_type → 中文名（§6.5 响应含 nodeTypeName）
    private static String nodeTypeName(Integer type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case StatusConst.NODE_FARM:
                return "养殖企业";
            case StatusConst.NODE_PROC:
                return "加工企业";
            case StatusConst.NODE_WHOL:
                return "批发商";
            case StatusConst.NODE_RETA:
                return "零售商";
            default:
                return null;
        }
    }

    // 复用 checkTextLen 风格
    private void checkTextLen(String value, String label, int max) {
        if (value != null && value.length() > max) {
            throw new BizException(BizCode.BAD_REQUEST, label + "长度不能超过 " + max + " 位");
        }
    }

    // 校验省存在、市存在且属于所选省份（未填则跳过）
    private void validateArea(String provinceCode, String cityCode) {
        if (provinceCode != null) {
            Long pc = provinceMapper.selectCount(new LambdaQueryWrapper<Province>()
                    .eq(Province::getProvinceCode, provinceCode));
            if (pc == null || pc == 0) {
                throw new BizException(BizCode.BAD_REQUEST, "所选省份不存在");
            }
        }
        if (cityCode == null) {
            return;
        }
        if (provinceCode == null) {
            throw new BizException(BizCode.BAD_REQUEST, "选择城市前请先选择省份");
        }
        City city = cityMapper.selectOne(new LambdaQueryWrapper<City>()
                .eq(City::getCityCode, cityCode));
        if (city == null) {
            throw new BizException(BizCode.BAD_REQUEST, "所选城市不存在");
        }
        if (!Objects.equals(city.getProvinceCode(), provinceCode)) {
            throw new BizException(BizCode.BAD_REQUEST, "所选城市不属于该省份");
        }
    }

    // 联系电话格式：允许数字、+、-、空格、括号（空串视为未填）
    private void checkPhone(String phone) {
        if (phone == null) {
            return;
        }
        phone = phone.trim();
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new BizException(BizCode.BAD_REQUEST, "联系电话格式不正确");
        }
    }

    // 与 AuthServiceImpl 一致：MD5 存库
    private static String md5(String s) {
        return DigestUtils.md5DigestAsHex(s.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean isPositive(Long v) {
        return v != null && v > 0;
    }

    // 企业名下任一类型批号表中存在"未下架"批号
    private boolean hasUnfinishedBatch(Long nodeId) {
        Long farmRefs = farmBatchMapper.selectCount(new LambdaQueryWrapper<FarmBatch>()
                .eq(FarmBatch::getNodeId, nodeId)
                .ne(FarmBatch::getStatus, StatusConst.FARM_OFF));
        Long procRefs = procBatchMapper.selectCount(new LambdaQueryWrapper<ProcBatch>()
                .eq(ProcBatch::getNodeId, nodeId)
                .ne(ProcBatch::getStatus, StatusConst.BATCH_OFF));
        Long wholRefs = wholBatchMapper.selectCount(new LambdaQueryWrapper<WholBatch>()
                .eq(WholBatch::getNodeId, nodeId)
                .ne(WholBatch::getStatus, StatusConst.BATCH_OFF));
        Long retaRefs = retaBatchMapper.selectCount(new LambdaQueryWrapper<RetaBatch>()
                .eq(RetaBatch::getNodeId, nodeId)
                .ne(RetaBatch::getStatus, StatusConst.BATCH_OFF));
        return isPositive(farmRefs) || isPositive(procRefs)
                || isPositive(wholRefs) || isPositive(retaRefs);
    }

    // 统计结果类型宽松转换，避免驱动返回类型差异导致 ClassCastException
    private static Integer toInt(Object v) {
        return v instanceof Number ? ((Number) v).intValue() : null;
    }

    private static long toLong(Object v) {
        return v instanceof Number ? ((Number) v).longValue() : 0L;
    }
}
