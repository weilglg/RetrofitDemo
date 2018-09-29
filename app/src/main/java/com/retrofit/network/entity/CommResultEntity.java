package com.retrofit.network.entity;

public class CommResultEntity<T> extends ApiResultEntity<T> {

    private String retDesc;
    private int retCode;
    private T rspBody;

    public String getRetDesc() {
        return retDesc;
    }

    public void setRetDesc(String retDesc) {
        this.retDesc = retDesc;
    }

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public T getRspBody() {
        return rspBody;
    }

    public void setRspBody(T rspBody) {
        this.rspBody = rspBody;
    }

    @Override
    public int getCode() {
        return retCode;
    }

    @Override
    public T getData() {
        return rspBody;
    }

    @Override
    public String getMsg() {
        return retDesc;
    }

    @Override
    public boolean isOk() {
        return retCode == 0;
    }
}
