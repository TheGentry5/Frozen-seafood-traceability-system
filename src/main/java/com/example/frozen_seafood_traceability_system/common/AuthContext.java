package com.example.frozen_seafood_traceability_system.common;

/**
 * 当前请求登录上下文（ThreadLocal），由 LoginInterceptor 写入/清理。
 */
public final class AuthContext {

    private static final ThreadLocal<Principal> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(Principal principal) {
        HOLDER.set(principal);
    }

    public static Principal get() {
        return HOLDER.get();
    }

    /** 是否管理员登录 */
    public static boolean isAdmin() {
        Principal p = get();
        return p != null && "ADMIN".equals(p.getType());
    }

    /** 是否节点企业登录 */
    public static boolean isNode() {
        Principal p = get();
        return p != null && "NODE".equals(p.getType());
    }

    public static Long nodeId() {
        Principal p = get();
        return p != null && "NODE".equals(p.getType()) ? p.getId() : null;
    }

    public static Integer nodeType() {
        Principal p = get();
        return p != null && "NODE".equals(p.getType()) ? p.getNodeType() : null;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
