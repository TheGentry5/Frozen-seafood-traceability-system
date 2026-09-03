package com.example.frozen_seafood_traceability_system.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.frozen_seafood_traceability_system.common.Result;
import com.example.frozen_seafood_traceability_system.entity.NodeInfo;
import com.example.frozen_seafood_traceability_system.service.AuthService;

/**
 * 当前用户 / 更新密码 / 退出（docs/开发实施文档.md §6.1）。
 */
@RestController
public class UserController {

    @Autowired
    private AuthService authService;

    @GetMapping("/api/user/info")
    public Result<NodeInfo> info() {
        return Result.ok(authService.currentUser());
    }

    @PostMapping("/api/user/updatePwd")
    public Result<Void> updatePwd(@RequestBody Map<String, String> body) {
        authService.updatePassword(body.get("oldPwd"), body.get("newPwd"));
        return Result.ok();
    }

    @PostMapping("/api/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }
}
