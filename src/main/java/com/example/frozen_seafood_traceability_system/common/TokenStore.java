package com.example.frozen_seafood_traceability_system.common;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

/**
 * 内存 Token 存储（单实例教学演示）。
 * 生产替换方案：Redis / 数据库（见 docs/开发实施文档.md §11）。
 * 说明：本地实现带滑动过期（默认 2 小时无访问即失效）与按主体批量吊销。
 */
@Component
public class TokenStore {

    /** 滑动过期时间：距最后一次访问超过该时长即失效 */
    private static final long TTL_MILLIS = TimeUnit.HOURS.toMillis(2);

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    /** 生成 token 并保存登录主体 */
    public String create(Principal principal) {
        String token = UUID.randomUUID().toString().replace("-", "");
        store.put(token, new Entry(principal));
        return token;
    }

    public Principal get(String token) {
        if (token == null) {
            return null;
        }
        Entry entry = store.get(token);
        if (entry == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        if (now - entry.lastAccess > TTL_MILLIS) {
            store.remove(token);
            return null;
        }
        entry.lastAccess = now;
        return entry.principal;
    }

    public void remove(String token) {
        if (token != null) {
            store.remove(token);
        }
    }

    /** 吊销某登录主体的全部 token（如停用企业 / 修改密码 / 变更类型） */
    public void removeBySubject(String type, Long id) {
        store.entrySet().removeIf(e -> {
            Principal p = e.getValue().principal;
            return Objects.equals(p.getType(), type) && Objects.equals(p.getId(), id);
        });
    }

    // token 记录：主体快照 + 最后访问时间（滑动过期）
    private static final class Entry {
        private final Principal principal;
        private volatile long lastAccess;

        private Entry(Principal principal) {
            this.principal = principal;
            this.lastAccess = System.currentTimeMillis();
        }
    }
}
