package com.example.frozen_seafood_traceability_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.frozen_seafood_traceability_system.common.PageResult;
import com.example.frozen_seafood_traceability_system.entity.RetaBatch;

/**
 * 零售商批号接口（docs/开发实施文档.md §6.3 reta 行），含"批发端确认（触发溯源码）"两方法。
 */
public interface RetaBatchService extends IService<RetaBatch> {

    PageResult<RetaBatch> pageMy(long page, long size, Integer status, String keyword);

    boolean existsBatchCode(String batchCode, Long excludeId);

    void createMy(RetaBatch req);

    void updateMy(RetaBatch req);

    void offMy(Long id);

    void deleteMy(Long id);

    /** 详情（含 traceCode） */
    RetaBatch detailMy(Long id);

    /** 批发端"下游进场确认"待办列表（零售商批号） */
    PageResult<RetaBatch> waitConfirmForWhol(Long wholNodeId, String keyword, long page, long size);

    /** 批发端确认零售批号进场：置"已确认"并同事务生成溯源码，返回生成的溯源码（docs/开发实施文档.md §5.4） */
    String confirmByWhol(Long id, Long wholNodeId);

    /** 消费者溯源：按溯源码查询 */
    RetaBatch findByTraceCode(String traceCode);
}
