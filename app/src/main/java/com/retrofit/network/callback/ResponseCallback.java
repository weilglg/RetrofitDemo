package com.retrofit.network.callback;

import com.retrofit.network.exception.ApiThrowable;

@SuppressWarnings(value = {"unchecked", "deprecation"})
public abstract class ResponseCallback<T> implements IResponseCallback<T> {

    /**
     * 请求开始
     */
    public void onStart(Object tag) {

    }

    /**
     * 请求结束
     */
    public void onCompleted(Object tag) {

    }

    /**
     * 下载进度
     */
    protected void onProgress(Object tag, float progress, long transfer, long total) {

    }

    protected void onProgress(Object tag, float progress, long speed, long transfer, long total) {

    }

    public abstract void onError(Object tag, ApiThrowable throwable);

    public abstract void onSuccess(Object tag, T result);


}
