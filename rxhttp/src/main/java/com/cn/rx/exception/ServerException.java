package com.cn.rx.exception;

public class ServerException extends RuntimeException {
    private int code;
    private String msg;

    public ServerException() {
    }

    public ServerException(int code) {
        this.code = code;
    }

    public ServerException(int code, String message) {
        super(message);
        this.code = code;
        this.msg = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
