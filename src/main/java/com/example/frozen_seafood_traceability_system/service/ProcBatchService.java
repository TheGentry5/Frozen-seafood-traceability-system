package com.example.frozen_seafood_traceability_system.service;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.frozen_seafood_traceability_system.common.PageResult;
import com.example.frozen_seafood_traceability_system.entity.ProcBatch;

/**
 * 加工企业批号接口（docs/开发实施文档.md §6.3 proc 行），含"养殖端确认本模块批号"两方法。
 */
public interface ProcBatchService extends IService<ProcBatch> {

    PageResult<ProcBatch> pageMy(long page, long size, Integer status, String keyword);

    boolean existsBatchCode(String batchCode, Long excludeId);

    /** 新建：status=新建；进场信息 in_area/in_product_name 由服务端回填 */
    void createMy(ProcBatch req);

    /** 更新：仅"新建"可更新；req.sendConfirm=true 时置"待确认" */
    void updateMy(ProcBatch req);

    void offMy(Long id);

    void deleteMy(Long id);

    ProcBatch detailMy(Long id);

    /** 养殖端"下游进场确认"待办列表（进场=养殖企业 inNodeId 且状态=待确认） */
    PageResult<ProcBatch> waitConfirmForFarm(Long farmNodeId, String keyword, long page, long size);

    /** 养殖端确认本加工批号进场 */
    void confirmByFarm(Long id, Long farmNodeId);
}
