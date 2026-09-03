package com.example.frozen_seafood_traceability_system.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.frozen_seafood_traceability_system.common.BizCode;
import com.example.frozen_seafood_traceability_system.common.BizException;
import com.example.frozen_seafood_traceability_system.service.TraceService;

/**
 * 骨架占位：消费者溯源待业务实现（docs/开发实施文档.md §8 任务 10）。
 */
@Service
public class TraceServiceImpl implements TraceService {

    @Override
    public Map<String, Object> trace(String traceCode) {
        throw todo();
    }

    private BizException todo() {
        return new BizException(BizCode.NOT_IMPLEMENTED, "接口待业务实现（编码阶段）");
    }
}
