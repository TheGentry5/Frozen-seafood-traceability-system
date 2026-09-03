package com.example.frozen_seafood_traceability_system.common;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * 内存 Token 存储（单实例教学演示）。
 * 生产替换方案：Redis / 数据库（见 docs/开发实施文档.md §11）。
 */
@Component
public class TokenStore {

    private final Map<String, Principal> store = new ConcurrentHashMap<>();

    /** 生成 token 并保存登录主体 */
    public String create(Principal principal) {
        String token = UUID.randomUUID().toString().replace("-", "");
        store.put(token, principal);
        return token;
    }

    public Principal get(String token) {
        return token == null ? null : store.get(token);
    }

    public void remove(String token) {
        if (token != null) {
            store.remove(token);
        }
    }
}
