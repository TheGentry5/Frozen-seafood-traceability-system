package com.example.frozen_seafood_traceability_system.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.frozen_seafood_traceability_system.common.BizCode;
import com.example.frozen_seafood_traceability_system.common.BizException;
import com.example.frozen_seafood_traceability_system.common.PageResult;
import com.example.frozen_seafood_traceability_system.entity.FarmBatch;
import com.example.frozen_seafood_traceability_system.mapper.FarmBatchMapper;
import com.example.frozen_seafood_traceability_system.service.FarmBatchService;

/**
 * 骨架占位：养殖批号业务待实现（docs/开发实施文档.md §8 任务 6）。
 */
@Service
public class FarmBatchServiceImpl extends ServiceImpl<FarmBatchMapper, FarmBatch> implements FarmBatchService {

    @Override
    public PageResult<FarmBatch> pageMy(long page, long size, Integer status, String keyword) {
        throw todo();
    }

    @Override
    public boolean existsBatchCode(String batchCode, Long excludeId) {
        throw todo();
    }

    @Override
    public void createMy(FarmBatch req) {
        throw todo();
    }

    @Override
    public void updateMy(FarmBatch req) {
        throw todo();
    }

    @Override
    public void offMy(Long id) {
        throw todo();
    }

    @Override
    public void deleteMy(Long id) {
        throw todo();
    }

    @Override
    public FarmBatch detailMy(Long id) {
        throw todo();
    }

    private BizException todo() {
        return new BizException(BizCode.NOT_IMPLEMENTED, "接口待业务实现（编码阶段）");
    }
}
