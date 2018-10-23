package com.cn.rx.exception;

public class ApiThrowable extends Exception {
    private int code;
    private String message;

    public ApiThrowable(java.lang.Throwable throwable, int code) {
        super(throwable);
        this.code = code;
    }

    public ApiThrowable(java.lang.Throwable throwable, int code, String message) {
        super(throwable);
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
