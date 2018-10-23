package com.cn.rx.subscriber;

import android.content.Context;

import com.cn.rx.callback.ResultCallback;
import com.cn.rx.exception.ApiThrowable;

public class ResultCallbackSubscriber<T> extends BaseSubscriber<T> {

    private ResultCallback<T> mCallback;
    private Object mTag;

    public ResultCallbackSubscriber(Object tag, ResultCallback<T> callback) {
        this.mCallback = callback;
        this.mTag = tag;
    }

    public ResultCallbackSubscriber(Context context, Object tag, ResultCallback<T> callback) {
        super(context);
        this.mCallback = callback;
        this.mTag = tag;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCallback != null) {
            mCallback.onStart(mTag);
        }
    }

    @Override
    public void onNext(T result) {
        super.onNext(result);
        if (mCallback != null) {
            mCallback.onSuccess(mTag, result);
        }
    }

    @Override
    public void onComplete() {
        if (mCallback != null) {
            mCallback.onCompleted(mTag);
        }
    }

    @Override
    public void onError(ApiThrowable throwable) {
        if (mCallback != null) {
            mCallback.onError(mTag, throwable);
        }
    }
}
