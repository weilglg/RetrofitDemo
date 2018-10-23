package com.cn.rx.subscriber;

import com.cn.rx.callback.ResponseCallback;
import com.cn.rx.exception.ApiThrowable;
import com.cn.rx.exception.ExceptionFactory;

import okhttp3.ResponseBody;

public class RxSubscribe<T> extends BaseSubscriber<ResponseBody> {

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
                callback.onSuccess(tag, callback.onTransformationResponse(responseBody));
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onError(tag, ExceptionFactory.handleException(e));
            }
        }
    }

    @Override
    public void onError(ApiThrowable throwable) {
        if (callback != null) {
            callback.onError(tag, throwable);
        }
    }


}
