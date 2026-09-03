package com.example.frozen_seafood_traceability_system.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.frozen_seafood_traceability_system.common.BizCode;
import com.example.frozen_seafood_traceability_system.common.BizException;
import com.example.frozen_seafood_traceability_system.entity.NodeInfo;
import com.example.frozen_seafood_traceability_system.service.UpstreamService;

/**
 * 骨架占位：上游级联待业务实现（docs/开发实施文档.md §8 任务 5）。
 */
@Service
public class UpstreamServiceImpl implements UpstreamService {

    @Override
    public List<NodeInfo> enterprises(Integer nodeType, String provinceCode, String cityCode) {
        throw todo();
    }

    @Override
    public List<Map<String, Object>> batches(Long nodeId, Integer nodeType) {
        throw todo();
    }

    private BizException todo() {
        return new BizException(BizCode.NOT_IMPLEMENTED, "接口待业务实现（编码阶段）");
    }
}
