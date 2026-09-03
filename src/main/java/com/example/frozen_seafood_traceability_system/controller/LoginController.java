package com.example.frozen_seafood_traceability_system.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.frozen_seafood_traceability_system.common.Result;
import com.example.frozen_seafood_traceability_system.service.AuthService;

/**
 * 登录（docs/开发实施文档.md §6.1）。POST /api/login 为拦截器白名单。
 */
@RestController
public class LoginController {

    @Autowired
    private AuthService authService;

    @PostMapping("/api/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        return Result.ok(authService.login(body.get("nodeCode"), body.get("password")));
    }

    @PostMapping("/api/admin/login")
    public Result<Map<String, Object>> adminLogin(@RequestBody Map<String, String> body) {
        return Result.ok(authService.adminLogin(body.get("username"), body.get("password")));
    }
}
