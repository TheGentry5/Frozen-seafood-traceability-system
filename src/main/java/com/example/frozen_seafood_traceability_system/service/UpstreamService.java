package com.example.frozen_seafood_traceability_system.service;

import java.util.List;
import java.util.Map;

import com.example.frozen_seafood_traceability_system.entity.NodeInfo;

/**
 * 上游级联接口（docs/开发实施文档.md §6.2）：省→市→企业→批号。
 */
public interface UpstreamService {

    /** 按上游 node_type（+可选省市）查企业下拉 */
    List<NodeInfo> enterprises(Integer nodeType, String provinceCode, String cityCode);

    /**
     * 查询指定企业的"可选批号"下拉。
     * nodeType=1 → 养殖"已发布"批号；2/3 → 加工/批发"已确认"批号。
     * 返回项含 id / batchCode / productName。
     */
    List<Map<String, Object>> batches(Long nodeId, Integer nodeType);
}
