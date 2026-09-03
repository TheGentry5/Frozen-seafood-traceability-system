package com.example.frozen_seafood_traceability_system.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.frozen_seafood_traceability_system.common.AuthContext;
import com.example.frozen_seafood_traceability_system.common.BizCode;
import com.example.frozen_seafood_traceability_system.common.BizException;
import com.example.frozen_seafood_traceability_system.entity.NodeInfo;
import com.example.frozen_seafood_traceability_system.service.AuthService;

/**
 * 骨架占位：登录 / 用户共通方法待业务实现（docs/开发实施文档.md §8 任务 3）。
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public Map<String, Object> login(String nodeCode, String password) {
        throw todo();
    }

    @Override
    public Map<String, Object> adminLogin(String username, String password) {
        throw todo();
    }

    @Override
    public NodeInfo currentUser() {
        throw todo();
    }

    @Override
    public void updatePassword(String oldPwd, String newPwd) {
        throw todo();
    }

    @Override
    public void logout() {
        AuthContext.clear();
        throw todo();
    }

    private BizException todo() {
        return new BizException(BizCode.NOT_IMPLEMENTED, "接口待业务实现（编码阶段）");
    }
}
