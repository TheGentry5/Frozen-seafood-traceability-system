package com.example.frozen_seafood_traceability_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.frozen_seafood_traceability_system.common.PageResult;
import com.example.frozen_seafood_traceability_system.entity.WholBatch;

/**
 * 批发商批号接口（docs/开发实施文档.md §6.3 whol 行），含"加工端确认本模块批号"两方法。
 */
public interface WholBatchService extends IService<WholBatch> {

    PageResult<WholBatch> pageMy(long page, long size, Integer status, String keyword);

    boolean existsBatchCode(String batchCode, Long excludeId);

    void createMy(WholBatch req);

    void updateMy(WholBatch req);

    void offMy(Long id);

    void deleteMy(Long id);

    WholBatch detailMy(Long id);

    /** 加工端"下游进场确认"待办列表 */
    PageResult<WholBatch> waitConfirmForProc(Long procNodeId, String keyword, long page, long size);

    /** 加工端确认本批发批号进场 */
    void confirmByProc(Long id, Long procNodeId);
}
