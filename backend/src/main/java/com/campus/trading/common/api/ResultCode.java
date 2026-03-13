package com.campus.trading.common.api;

public enum ResultCode {
    SUCCESS(0, "success"),
    FAIL(1, "fail");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
