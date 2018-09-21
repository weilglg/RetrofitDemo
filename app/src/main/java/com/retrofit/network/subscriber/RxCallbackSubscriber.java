package com.retrofit.network.subscriber;

import android.content.Context;

import com.retrofit.network.exception.ApiThrowable;

public class RxCallbackSubscriber<T> extends BaseSubscriber<T> {

    private ResponseCallback<T> mCallback;
    private Object mTag;

    public RxCallbackSubscriber(Object tag, ResponseCallback<T> callback) {
        this.mCallback = callback;
        this.mTag = tag;
    }

    public RxCallbackSubscriber(Context context, Object tag, ResponseCallback<T> callback) {
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
