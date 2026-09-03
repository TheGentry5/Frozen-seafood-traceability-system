package com.example.frozen_seafood_traceability_system.common;

/**
 * 统一返回结构 { code, msg, data }，HTTP 恒为 200，业务结果以 code 表达。
 */
public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> ok() {
        return new Result<>(BizCode.OK, "ok", null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(BizCode.OK, "ok", data);
    }

    public static <T> Result<T> fail(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> badRequest(String msg) {
        return fail(BizCode.BAD_REQUEST, msg);
    }

    public static <T> Result<T> forbidden(String msg) {
        return fail(BizCode.FORBIDDEN, msg);
    }

    public static <T> Result<T> conflict(String msg) {
        return fail(BizCode.CONFLICT, msg);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
