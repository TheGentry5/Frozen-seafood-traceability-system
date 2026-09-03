package com.example.frozen_seafood_traceability_system.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.frozen_seafood_traceability_system.common.BizCode;
import com.example.frozen_seafood_traceability_system.common.BizException;
import com.example.frozen_seafood_traceability_system.common.PageResult;
import com.example.frozen_seafood_traceability_system.entity.RetaBatch;
import com.example.frozen_seafood_traceability_system.mapper.RetaBatchMapper;
import com.example.frozen_seafood_traceability_system.service.RetaBatchService;

/**
 * 骨架占位：零售批号业务待实现（docs/开发实施文档.md §8 任务 9）。
 */
@Service
public class RetaBatchServiceImpl extends ServiceImpl<RetaBatchMapper, RetaBatch> implements RetaBatchService {

    @Override
    public PageResult<RetaBatch> pageMy(long page, long size, Integer status, String keyword) {
        throw todo();
    }

    @Override
    public boolean existsBatchCode(String batchCode, Long excludeId) {
        throw todo();
    }

    @Override
    public void createMy(RetaBatch req) {
        throw todo();
    }

    @Override
    public void updateMy(RetaBatch req) {
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
    public RetaBatch detailMy(Long id) {
        throw todo();
    }

    @Override
    public PageResult<RetaBatch> waitConfirmForWhol(Long wholNodeId, String keyword, long page, long size) {
        throw todo();
    }

    @Override
    public void confirmByWhol(Long id, Long wholNodeId) {
        throw todo();
    }

    @Override
    public RetaBatch findByTraceCode(String traceCode) {
        throw todo();
    }

    private BizException todo() {
        return new BizException(BizCode.NOT_IMPLEMENTED, "接口待业务实现（编码阶段）");
    }
}
