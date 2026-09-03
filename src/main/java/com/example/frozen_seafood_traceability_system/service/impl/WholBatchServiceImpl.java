package com.example.frozen_seafood_traceability_system.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.frozen_seafood_traceability_system.common.BizCode;
import com.example.frozen_seafood_traceability_system.common.BizException;
import com.example.frozen_seafood_traceability_system.common.PageResult;
import com.example.frozen_seafood_traceability_system.entity.WholBatch;
import com.example.frozen_seafood_traceability_system.mapper.WholBatchMapper;
import com.example.frozen_seafood_traceability_system.service.WholBatchService;

/**
 * 骨架占位：批发批号业务待实现（docs/开发实施文档.md §8 任务 8）。
 */
@Service
public class WholBatchServiceImpl extends ServiceImpl<WholBatchMapper, WholBatch> implements WholBatchService {

    @Override
    public PageResult<WholBatch> pageMy(long page, long size, Integer status, String keyword) {
        throw todo();
    }

    @Override
    public boolean existsBatchCode(String batchCode, Long excludeId) {
        throw todo();
    }

    @Override
    public void createMy(WholBatch req) {
        throw todo();
    }

    @Override
    public void updateMy(WholBatch req) {
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
    public WholBatch detailMy(Long id) {
        throw todo();
    }

    @Override
    public PageResult<WholBatch> waitConfirmForProc(Long procNodeId, String keyword, long page, long size) {
        throw todo();
    }

    @Override
    public void confirmByProc(Long id, Long procNodeId) {
        throw todo();
    }

    private BizException todo() {
        return new BizException(BizCode.NOT_IMPLEMENTED, "接口待业务实现（编码阶段）");
    }
}
