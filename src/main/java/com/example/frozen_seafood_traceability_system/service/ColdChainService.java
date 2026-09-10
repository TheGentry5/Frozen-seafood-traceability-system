package com.example.frozen_seafood_traceability_system.service;

import java.util.List;
import java.util.Map;

import com.example.frozen_seafood_traceability_system.entity.ColdChainRecord;

/**
 * 冷链温度填报与曲线展示（扩展功能）。
 * 节点企业填报本企业批号温度；消费者按溯源码查看全链温度曲线。
 */
public interface ColdChainService {

    /** 节点填报温度（校验归属与范围，服务端回填 nodeId） */
    void report(ColdChainRecord req);

    /** 本企业某批号温度记录列表 */
    List<ColdChainRecord> listMy(Integer batchType, Long batchId);

    /** 某批号曲线数据：points / max / min / avg / abnormal / 阈值 */
    Map<String, Object> curve(Integer batchType, Long batchId);

    /** 消费者：按溯源码取全链温度曲线 */
    Map<String, Object> traceCurve(String traceCode);
}
