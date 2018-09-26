package com.retrofit.network.util;

import com.retrofit.network.entity.ApiResultEntity;

public class TestApi<T> extends ApiResultEntity<T> {

    private T results;
    private int status;

    public T getResults() {
        return results;
    }

    public void setResults(T results) {
        this.results = results;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public T getData() {
        return results;
    }

    @Override
    public int getCode() {
        return status;
    }

    @Override
    public boolean isOk() {
        return status == 0;
    }
}
