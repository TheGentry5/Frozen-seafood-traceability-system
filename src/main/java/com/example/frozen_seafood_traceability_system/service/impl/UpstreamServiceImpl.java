package com.example.frozen_seafood_traceability_system.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.frozen_seafood_traceability_system.entity.FarmBatch;
import com.example.frozen_seafood_traceability_system.entity.NodeInfo;
import com.example.frozen_seafood_traceability_system.entity.ProcBatch;
import com.example.frozen_seafood_traceability_system.entity.WholBatch;
import com.example.frozen_seafood_traceability_system.mapper.FarmBatchMapper;
import com.example.frozen_seafood_traceability_system.mapper.NodeInfoMapper;
import com.example.frozen_seafood_traceability_system.mapper.ProcBatchMapper;
import com.example.frozen_seafood_traceability_system.mapper.WholBatchMapper;
import com.example.frozen_seafood_traceability_system.service.UpstreamService;

/**
 * 上游级联（docs/开发实施文档.md §6.2）：省→市→企业→批号。
 */
@Service
public class UpstreamServiceImpl implements UpstreamService {

    @Autowired
    private NodeInfoMapper nodeInfoMapper;

    @Autowired
    private FarmBatchMapper farmBatchMapper;

    @Autowired
    private ProcBatchMapper procBatchMapper;

    @Autowired
    private WholBatchMapper wholBatchMapper;

    // 选企业
    @Override
    public List<NodeInfo> enterprises(Integer nodeType, String provinceCode, String cityCode) {
        return nodeInfoMapper.upstreamList(nodeType, provinceCode, cityCode);
    }

    // 选该企业可被采购的批号
    @Override
    public List<Map<String, Object>> batches(Long nodeId, Integer nodeType) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (nodeType == null) {
            return result;
        }
        switch (nodeType) {
            case 1:
                for (FarmBatch b : farmBatchMapper.selectReleasedByNode(nodeId)) {
                    result.add(batchRow(b.getId(), b.getBatchCode(), b.getProductName()));
                }
                break;
            case 2:
                for (ProcBatch b : procBatchMapper.selectSelectableByNode(nodeId)) {
                    result.add(batchRow(b.getId(), b.getBatchCode(), b.getProductName()));
                }
                break;
            case 3:
                for (WholBatch b : wholBatchMapper.selectSelectableByNode(nodeId)) {
                    result.add(batchRow(b.getId(), b.getBatchCode(), b.getProductName()));
                }
                break;
            case 4:
            default:
                break;
        }
        return result;
    }

    private Map<String, Object> batchRow(Long id, String batchCode, String productName) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", id);
        row.put("batchCode", batchCode);
        row.put("productName", productName);
        return row;
    }
}
