package com.example.frozen_seafood_traceability_system.common;

/**
 * 错误码统一约定（docs/开发实施文档.md §4.2）。
 */
public interface BizCode {

    /** 成功 */
    int OK = 200;
    /** 参数或业务校验失败 */
    int BAD_REQUEST = 400;
    /** 未登录 / token 失效 */
    int UNAUTHORIZED = 401;
    /** 无权限 / 角色不符 */
    int FORBIDDEN = 403;
    /** 冲突（批号/编码重复等） */
    int CONFLICT = 409;
    /** 服务器异常 */
    int INTERNAL_ERROR = 500;
    /** 骨架占位：接口尚未实现业务 */
    int NOT_IMPLEMENTED = 501;
}
