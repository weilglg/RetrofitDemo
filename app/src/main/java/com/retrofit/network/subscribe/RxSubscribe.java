package com.retrofit.network.subscribe;

import com.retrofit.network.exception.CommThrowable;
import com.retrofit.network.exception.ExceptionFactory;

import okhttp3.ResponseBody;

public class RxSubscribe<T> extends BaseSubscribe<ResponseBody> {

    private ResponseCallback<T> callback;
    private Object tag = null;

    public RxSubscribe(ResponseCallback<T> callback, Object tag) {
        this.callback = callback;
        this.tag = tag;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (callback != null) {
            callback.onStart(tag);
        }
    }

    @Override
    public void onComplete() {
        super.onComplete();
        if (callback != null) {
            callback.onCompleted(tag);
        }
    }

    @Override
    public void onNext(ResponseBody responseBody) {
        try {
            if (callback != null) {
                callback.onSuccess(tag, callback.onTransformationResponse(tag, responseBody));
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onError(tag, ExceptionFactory.handleException(e));
            }
        }
    }

    @Override
    void onError(CommThrowable throwable) {
        if (callback != null) {
            callback.onError(tag, throwable);
        }
    }



}
