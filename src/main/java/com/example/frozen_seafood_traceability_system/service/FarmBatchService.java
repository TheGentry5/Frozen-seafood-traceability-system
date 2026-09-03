package com.example.frozen_seafood_traceability_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.frozen_seafood_traceability_system.common.PageResult;
import com.example.frozen_seafood_traceability_system.entity.FarmBatch;

/**
 * 养殖企业批号接口（docs/开发实施文档.md §6.3 farm 行）。
 */
public interface FarmBatchService extends IService<FarmBatch> {

    /** 本企业批号分页（status 空 → 排除已下架） */
    PageResult<FarmBatch> pageMy(long page, long size, Integer status, String keyword);

    /** 批号唯一性校验（编辑时 excludeId 排除自身） */
    boolean existsBatchCode(String batchCode, Long excludeId);

    /** 新建：状态置"待发布"，校验本企业身份与批号唯一 */
    void createMy(FarmBatch req);

    /** 更新：仅"待发布"可更新；req.publish=true 时置"已发布" */
    void updateMy(FarmBatch req);

    /** 下架：仅"已发布"可下架 */
    void offMy(Long id);

    /** 删除：仅"待发布"可删除 */
    void deleteMy(Long id);

    /** 详情（归属校验） */
    FarmBatch detailMy(Long id);
}
