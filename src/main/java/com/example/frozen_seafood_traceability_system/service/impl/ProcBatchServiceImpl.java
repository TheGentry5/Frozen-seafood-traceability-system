package com.example.frozen_seafood_traceability_system.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.frozen_seafood_traceability_system.common.BizCode;
import com.example.frozen_seafood_traceability_system.common.BizException;
import com.example.frozen_seafood_traceability_system.common.PageResult;
import com.example.frozen_seafood_traceability_system.entity.ProcBatch;
import com.example.frozen_seafood_traceability_system.mapper.ProcBatchMapper;
import com.example.frozen_seafood_traceability_system.service.ProcBatchService;

/**
 * 骨架占位：加工批号业务待实现（docs/开发实施文档.md §8 任务 7）。
 */
@Service
public class ProcBatchServiceImpl extends ServiceImpl<ProcBatchMapper, ProcBatch> implements ProcBatchService {

    @Override
    public PageResult<ProcBatch> pageMy(long page, long size, Integer status, String keyword) {
        throw todo();
    }

    @Override
    public boolean existsBatchCode(String batchCode, Long excludeId) {
        throw todo();
    }

    @Override
    public void createMy(ProcBatch req) {
        throw todo();
    }

    @Override
    public void updateMy(ProcBatch req) {
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
    public ProcBatch detailMy(Long id) {
        throw todo();
    }

    @Override
    public PageResult<ProcBatch> waitConfirmForFarm(Long farmNodeId, String keyword, long page, long size) {
        throw todo();
    }

    @Override
    public void confirmByFarm(Long id, Long farmNodeId) {
        throw todo();
    }

    private BizException todo() {
        return new BizException(BizCode.NOT_IMPLEMENTED, "接口待业务实现（编码阶段）");
    }
}
