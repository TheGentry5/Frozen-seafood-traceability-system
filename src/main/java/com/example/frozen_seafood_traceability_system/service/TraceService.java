package com.example.frozen_seafood_traceability_system.service;

import java.util.Map;

/**
 * 消费者溯源接口（docs/开发实施文档.md §6.4）。
 */
public interface TraceService {

    /**
     * 按溯源码组装全链条（零售→批发→加工→养殖）。
     * 返回 { traceCode, chain: [ {stage,nodeName,batchCode,productName,inspectionCert,inspector,createTime} x4 ] }
     */
    Map<String, Object> trace(String traceCode);
}
