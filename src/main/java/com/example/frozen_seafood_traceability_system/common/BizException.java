package com.example.frozen_seafood_traceability_system.common;

/**
 * 业务异常：service 中 throw，由 GlobalExceptionHandler 转成统一 JSON。
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public BizException(String msg) {
        this(BizCode.BAD_REQUEST, msg);
    }

    public int getCode() {
        return code;
    }
}
