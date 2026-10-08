package com.example.demo.common;

/** 统一响应体 */
public record Result<T>(int code, String msg, T data) {
    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "success", data);
    }
    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }
    public boolean isOk() {
        return code == 0;
    }
}
