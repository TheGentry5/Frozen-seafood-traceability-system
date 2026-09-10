package com.example.frozen_seafood_traceability_system.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.frozen_seafood_traceability_system.common.AuthContext;
import com.example.frozen_seafood_traceability_system.common.BizCode;
import com.example.frozen_seafood_traceability_system.common.BizException;
import com.example.frozen_seafood_traceability_system.common.StatusConst;
import com.example.frozen_seafood_traceability_system.entity.ColdChainRecord;
import com.example.frozen_seafood_traceability_system.entity.FarmBatch;
import com.example.frozen_seafood_traceability_system.entity.ProcBatch;
import com.example.frozen_seafood_traceability_system.entity.RetaBatch;
import com.example.frozen_seafood_traceability_system.entity.WholBatch;
import com.example.frozen_seafood_traceability_system.mapper.ColdChainRecordMapper;
import com.example.frozen_seafood_traceability_system.mapper.FarmBatchMapper;
import com.example.frozen_seafood_traceability_system.mapper.ProcBatchMapper;
import com.example.frozen_seafood_traceability_system.mapper.RetaBatchMapper;
import com.example.frozen_seafood_traceability_system.mapper.WholBatchMapper;
import com.example.frozen_seafood_traceability_system.service.ColdChainService;

/**
 * 冷链温度填报与曲线展示实现。
 * 读取方法按批号归属校验；消费者接口经溯源码走 养殖→加工→批发→零售 全链聚合。
 */
@Service
public class ColdChainServiceImpl implements ColdChainService {

    private static final BigDecimal HUMIDITY_MIN = BigDecimal.ZERO;
    private static final BigDecimal HUMIDITY_MAX = new BigDecimal("100");
    private static final int REMARK_MAX = 255;

    @Autowired
    private ColdChainRecordMapper coldChainRecordMapper;
    @Autowired
    private FarmBatchMapper farmBatchMapper;
    @Autowired
    private ProcBatchMapper procBatchMapper;
    @Autowired
    private WholBatchMapper wholBatchMapper;
    @Autowired
    private RetaBatchMapper retaBatchMapper;

    // 填报温度
    @Override
    @Transactional
    public void report(ColdChainRecord req) {
        requireNode();
        if (req == null || req.getBatchType() == null || req.getBatchId() == null) {
            throw new BizException(BizCode.BAD_REQUEST, "缺少批号类型或批号 id");
        }
        if (!Objects.equals(req.getBatchType(), AuthContext.nodeType())) {
            throw new BizException(BizCode.FORBIDDEN, "只能填报本企业环节的批号温度");
        }
        requireOwnedBatch(req.getBatchType(), req.getBatchId());

        if (req.getTemperature() == null) {
            throw new BizException(BizCode.BAD_REQUEST, "温度不能为空");
        }
        if (req.getTemperature().compareTo(StatusConst.COLD_MIN) < 0
                || req.getTemperature().compareTo(StatusConst.COLD_MAX) > 0) {
            throw new BizException(BizCode.BAD_REQUEST,
                    "温度须在 " + StatusConst.COLD_MIN + " ~ " + StatusConst.COLD_MAX + " ℃ 之间");
        }
        if (req.getHumidity() != null
                && (req.getHumidity().compareTo(HUMIDITY_MIN) < 0
                    || req.getHumidity().compareTo(HUMIDITY_MAX) > 0)) {
            throw new BizException(BizCode.BAD_REQUEST, "湿度须在 0 ~ 100 之间");
        }
        if (req.getRemark() != null && req.getRemark().length() > REMARK_MAX) {
            throw new BizException(BizCode.BAD_REQUEST, "备注长度不能超过 " + REMARK_MAX + " 位");
        }

        LocalDateTime recordTime = req.getRecordTime() == null ? LocalDateTime.now() : req.getRecordTime();
        if (recordTime.isAfter(LocalDateTime.now().plusMinutes(1))) {
            throw new BizException(BizCode.BAD_REQUEST, "采集时间不能晚于当前时间");
        }

        ColdChainRecord row = new ColdChainRecord();
        row.setBatchType(req.getBatchType());
        row.setBatchId(req.getBatchId());
        row.setNodeId(AuthContext.nodeId());
        row.setTemperature(req.getTemperature());
        row.setHumidity(req.getHumidity());
        row.setRecordTime(recordTime);
        row.setRemark(req.getRemark());
        coldChainRecordMapper.insert(row);
    }

    // 本企业某批号温度记录
    @Override
    public List<ColdChainRecord> listMy(Integer batchType, Long batchId) {
        requireNode();
        requireOwnedBatch(batchType, batchId);
        return coldChainRecordMapper.listByBatch(batchType, batchId);
    }

    // 某批号曲线
    @Override
    public Map<String, Object> curve(Integer batchType, Long batchId) {
        requireNode();
        requireOwnedBatch(batchType, batchId);
        return buildCurve(coldChainRecordMapper.listByBatch(batchType, batchId));
    }

    // 消费者全链曲线
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> traceCurve(String traceCode) {
        if (traceCode == null || traceCode.trim().isEmpty()) {
            throw new BizException(BizCode.BAD_REQUEST, "溯源码不能为空");
        }
        String code = traceCode.trim();

        RetaBatch reta = retaBatchMapper.selectByTraceCode(code);
        if (reta == null) {
            throw new BizException(BizCode.BAD_REQUEST, "溯源码不存在或商品尚未完成溯源确认");
        }

        List<ColdChainRecord> all = new ArrayList<>();
        appendStage(all, StatusConst.BATCH_TYPE_RETA, reta.getId(), "零售", reta.getNodeName());

        WholBatch whol = reta.getInBatchId() == null
                ? null : wholBatchMapper.selectByIdForNode(reta.getInBatchId());
        if (whol != null) {
            appendStage(all, StatusConst.BATCH_TYPE_WHOL, whol.getId(), "批发", whol.getNodeName());

            ProcBatch proc = whol.getInBatchId() == null
                    ? null : procBatchMapper.selectByIdForNode(whol.getInBatchId());
            if (proc != null) {
                appendStage(all, StatusConst.BATCH_TYPE_PROC, proc.getId(), "加工", proc.getNodeName());

                FarmBatch farm = proc.getInBatchId() == null
                        ? null : farmBatchMapper.selectByIdForNode(proc.getInBatchId());
                if (farm != null) {
                    appendStage(all, StatusConst.BATCH_TYPE_FARM, farm.getId(), "养殖", farm.getNodeName());
                }
            }
        }

        all.sort(Comparator.comparing(ColdChainRecord::getRecordTime,
                Comparator.nullsLast(Comparator.naturalOrder())));
        Map<String, Object> result = buildCurve(all);
        result.put("traceCode", code);
        return result;
    }

    // 查询某环节温度记录并打上环节/企业名标记
    private void appendStage(List<ColdChainRecord> all, Integer batchType, Long batchId,
                             String stage, String nodeName) {
        List<ColdChainRecord> records = coldChainRecordMapper.listByBatch(batchType, batchId);
        for (ColdChainRecord r : records) {
            r.setStage(stage);
            r.setNodeName(nodeName);
            all.add(r);
        }
    }

    // 组装曲线：点位 + 极值/均值 + 异常计数 + 阈值
    private Map<String, Object> buildCurve(List<ColdChainRecord> records) {
        List<Map<String, Object>> points = new ArrayList<>();
        BigDecimal min = null;
        BigDecimal max = null;
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        int abnormal = 0;

        for (ColdChainRecord r : records) {
            BigDecimal t = r.getTemperature();
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("recordTime", r.getRecordTime());
            point.put("temperature", t);
            point.put("humidity", r.getHumidity());
            if (r.getStage() != null) {
                point.put("stage", r.getStage());
            }
            if (r.getNodeName() != null) {
                point.put("nodeName", r.getNodeName());
            }
            points.add(point);

            if (t != null) {
                min = min == null ? t : min.min(t);
                max = max == null ? t : max.max(t);
                sum = sum.add(t);
                count++;
                if (t.compareTo(StatusConst.COLD_MIN) < 0 || t.compareTo(StatusConst.COLD_MAX) > 0) {
                    abnormal++;
                }
            }
        }

        BigDecimal avg = count == 0
                ? null : sum.divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("points", points);
        result.put("max", max);
        result.put("min", min);
        result.put("avg", avg);
        result.put("abnormal", abnormal);
        result.put("thresholdMin", StatusConst.COLD_MIN);
        result.put("thresholdMax", StatusConst.COLD_MAX);
        return result;
    }

    // 按环节类型取批号并校验归属与状态（缺 id / 不存在 / 非本企业 / 已下架 → 抛错）
    private void requireOwnedBatch(Integer batchType, Long batchId) {
        if (batchType == null || batchId == null) {
            throw new BizException(BizCode.BAD_REQUEST, "缺少批号类型或批号 id");
        }

        Long ownerId;
        Integer status;
        Integer offStatus;
        switch (batchType) {
            case StatusConst.BATCH_TYPE_FARM: {
                FarmBatch b = farmBatchMapper.selectById(batchId);
                if (b == null) {
                    throw new BizException(BizCode.BAD_REQUEST, "养殖批号不存在");
                }
                ownerId = b.getNodeId();
                status = b.getStatus();
                offStatus = StatusConst.FARM_OFF;
                break;
            }
            case StatusConst.BATCH_TYPE_PROC: {
                ProcBatch b = procBatchMapper.selectById(batchId);
                if (b == null) {
                    throw new BizException(BizCode.BAD_REQUEST, "加工批号不存在");
                }
                ownerId = b.getNodeId();
                status = b.getStatus();
                offStatus = StatusConst.BATCH_OFF;
                break;
            }
            case StatusConst.BATCH_TYPE_WHOL: {
                WholBatch b = wholBatchMapper.selectById(batchId);
                if (b == null) {
                    throw new BizException(BizCode.BAD_REQUEST, "批发批号不存在");
                }
                ownerId = b.getNodeId();
                status = b.getStatus();
                offStatus = StatusConst.BATCH_OFF;
                break;
            }
            case StatusConst.BATCH_TYPE_RETA: {
                RetaBatch b = retaBatchMapper.selectById(batchId);
                if (b == null) {
                    throw new BizException(BizCode.BAD_REQUEST, "零售批号不存在");
                }
                ownerId = b.getNodeId();
                status = b.getStatus();
                offStatus = StatusConst.BATCH_OFF;
                break;
            }
            default:
                throw new BizException(BizCode.BAD_REQUEST, "批号类型不合法");
        }

        if (!Objects.equals(ownerId, AuthContext.nodeId())) {
            throw new BizException(BizCode.FORBIDDEN, "无权操作他人批号");
        }
        if (Objects.equals(status, offStatus)) {
            throw new BizException(BizCode.BAD_REQUEST, "已下架批号不可填报温度");
        }
    }

    private void requireNode() {
        if (!AuthContext.isNode()) {
            throw new BizException(BizCode.FORBIDDEN, "无权操作本类批号（仅节点企业）");
        }
    }
}
