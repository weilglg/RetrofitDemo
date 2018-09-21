package com.retrofit.network.subscriber;

import android.os.Handler;
import android.os.Looper;

import com.retrofit.network.exception.ApiThrowable;

public abstract class ResponseCallback<T> implements IResponseCallback<T> {

    private Handler handler;
    private String TAG = this.getClass().getSimpleName();

    public ResponseCallback() {
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * 请求开始
     */
    protected void onStart(Object tag) {

    }

    /**
     * 请求结束
     */
    protected void onCompleted(Object tag) {

    }

    /**
     * 下载进度
     */
    protected void onProgress(Object tag, float progress, long transfer, long total) {

    }

    protected void onProgress(Object tag, float progress, long speed, long transfer, long total) {

    }

    protected abstract void onError(Object tag, ApiThrowable throwable);

    protected abstract void onSuccess(Object tag, T result);


}
