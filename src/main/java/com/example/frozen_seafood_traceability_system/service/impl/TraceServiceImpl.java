package com.example.frozen_seafood_traceability_system.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.frozen_seafood_traceability_system.common.BizCode;
import com.example.frozen_seafood_traceability_system.common.BizException;
import com.example.frozen_seafood_traceability_system.entity.FarmBatch;
import com.example.frozen_seafood_traceability_system.entity.ProcBatch;
import com.example.frozen_seafood_traceability_system.entity.RetaBatch;
import com.example.frozen_seafood_traceability_system.entity.WholBatch;
import com.example.frozen_seafood_traceability_system.mapper.FarmBatchMapper;
import com.example.frozen_seafood_traceability_system.mapper.ProcBatchMapper;
import com.example.frozen_seafood_traceability_system.mapper.RetaBatchMapper;
import com.example.frozen_seafood_traceability_system.mapper.WholBatchMapper;
import com.example.frozen_seafood_traceability_system.service.TraceService;

/**
 * 消费者溯源：按溯源码组装 养殖→加工→批发→零售 全链条（docs/开发实施文档.md §6.4）。
 */
@Service
public class TraceServiceImpl implements TraceService {

    @Autowired
    private RetaBatchMapper retaBatchMapper;
    @Autowired
    private WholBatchMapper wholBatchMapper;
    @Autowired
    private ProcBatchMapper procBatchMapper;
    @Autowired
    private FarmBatchMapper farmBatchMapper;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> trace(String traceCode) {
        if (traceCode == null || traceCode.trim().isEmpty()) {
            throw notFound();
        }
        String code = traceCode.trim();

        // ① 零售（终端）：按溯源码定位，联出零售商企业名
        RetaBatch reta = retaBatchMapper.selectByTraceCode(code);
        if (reta == null) {
            throw notFound();
        }

        // ② 批发：id = 零售批号 inBatchId，联出批发企业名
        WholBatch whol = reta.getInBatchId() == null
                ? null : wholBatchMapper.selectByIdForNode(reta.getInBatchId());
        if (whol == null) {
            throw new BizException(BizCode.BAD_REQUEST, "溯源码链条不完整：批发环节数据缺失");
        }

        // ③ 加工：id = 批发批号 inBatchId，联出加工企业名
        ProcBatch proc = whol.getInBatchId() == null
                ? null : procBatchMapper.selectByIdForNode(whol.getInBatchId());
        if (proc == null) {
            throw new BizException(BizCode.BAD_REQUEST, "溯源码链条不完整：加工环节数据缺失");
        }

        // ④ 养殖：id = 加工批号 inBatchId，联出养殖企业名
        FarmBatch farm = proc.getInBatchId() == null
                ? null : farmBatchMapper.selectByIdForNode(proc.getInBatchId());
        if (farm == null) {
            throw new BizException(BizCode.BAD_REQUEST, "溯源码链条不完整：养殖环节数据缺失");
        }

        // 组装 养殖 → 加工 → 批发 → 零售（各环节企业名取本批号所属企业）
        List<Map<String, Object>> chain = new ArrayList<>(4);
        chain.add(stage("养殖", farm.getNodeName(),
                farm.getBatchCode(), farm.getProductName(),
                farm.getInspectionCert(), farm.getInspector(), farm.getCreateTime()));
        chain.add(stage("加工", proc.getNodeName(),
                proc.getBatchCode(), proc.getProductName(),
                proc.getInspectionCert(), proc.getInspector(), proc.getCreateTime()));
        chain.add(stage("批发", whol.getNodeName(),
                whol.getBatchCode(), whol.getProductName(),
                whol.getInspectionCert(), whol.getInspector(), whol.getCreateTime()));
        chain.add(stage("零售", reta.getNodeName(),
                reta.getBatchCode(), reta.getProductName(),
                reta.getInspectionCert(), reta.getInspector(), reta.getCreateTime()));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("traceCode", reta.getTraceCode());
        data.put("chain", chain);
        return data;

    }

    //数据封装
    private Map<String, Object> stage(String stage, String nodeName, String batchCode,
                                      String productName, String inspectionCert,
                                      String inspector, LocalDateTime createTime) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("stage", stage);
        item.put("nodeName", nodeName);
        item.put("batchCode", batchCode);
        item.put("productName", productName);
        item.put("inspectionCert", inspectionCert);
        item.put("inspector", inspector);
        item.put("createTime", createTime);
        return item;
    }

    private BizException notFound() {
        return new BizException(BizCode.BAD_REQUEST, "溯源码不存在或商品尚未完成溯源确认");
    }
}
