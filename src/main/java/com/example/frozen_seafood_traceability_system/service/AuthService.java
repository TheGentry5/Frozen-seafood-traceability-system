package com.example.frozen_seafood_traceability_system.service;

import java.util.Map;

import com.example.frozen_seafood_traceability_system.entity.NodeInfo;

/**
 * 登录 / 用户共通接口（docs/开发实施文档.md §6.1）。
 */
public interface AuthService {

    /** 节点企业登录，返回 { token, user } */
    Map<String, Object> login(String nodeCode, String password);

    /** 管理员登录，返回 { token, role: ADMIN, username } */
    Map<String, Object> adminLogin(String username, String password);

    /** 当前登录节点企业信息（接口 GET /api/user/info） */
    NodeInfo currentUser();

    /** 更新密码（先校旧密码），成功后当前 token 失效需重新登录 */
    void updatePassword(String oldPwd, String newPwd);

    /** 退出登录，作废 token */
    void logout();
}
