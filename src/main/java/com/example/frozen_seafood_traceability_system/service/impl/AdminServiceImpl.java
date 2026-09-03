package com.example.frozen_seafood_traceability_system.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.frozen_seafood_traceability_system.common.BizCode;
import com.example.frozen_seafood_traceability_system.common.BizException;
import com.example.frozen_seafood_traceability_system.common.PageResult;
import com.example.frozen_seafood_traceability_system.entity.NodeInfo;
import com.example.frozen_seafood_traceability_system.service.AdminService;

/**
 * 骨架占位：管理端业务待实现（docs/开发实施文档.md §8 任务 11）。
 */
@Service
public class AdminServiceImpl implements AdminService {

    @Override
    public PageResult<NodeInfo> pageNode(long page, long size, String nodeName, Integer nodeType,
                                         String provinceCode, String cityCode) {
        throw todo();
    }

    @Override
    public NodeInfo detailNode(Long id) {
        throw todo();
    }

    @Override
    public void createNode(NodeInfo req) {
        throw todo();
    }

    @Override
    public void updateNode(NodeInfo req) {
        throw todo();
    }

    @Override
    public void deleteNode(Long id) {
        throw todo();
    }

    @Override
    public Map<String, Object> stats() {
        throw todo();
    }

    private BizException todo() {
        return new BizException(BizCode.NOT_IMPLEMENTED, "接口待业务实现（编码阶段）");
    }
}
