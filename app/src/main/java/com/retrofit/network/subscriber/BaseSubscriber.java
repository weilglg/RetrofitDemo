package com.retrofit.network.subscriber;

import android.content.Context;

import com.retrofit.network.exception.ApiThrowable;
import com.retrofit.network.util.LogUtil;
import com.retrofit.network.exception.ExceptionFactory;

import java.lang.ref.WeakReference;

import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;

public abstract class BaseSubscriber<T> extends DisposableObserver<T> {

    public WeakReference<Context> contextWeakReference;

    public BaseSubscriber() {
    }

    public BaseSubscriber(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    public Context getContext() {
        return contextWeakReference.get();
    }

    public void setContext(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    @Override
    protected void onStart() {
        LogUtil.v("RxHttp", "-->http is start");
    }

    @Override
    public void onNext(T result) {
        LogUtil.v("RxHttp", "-->http is onNext");
    }

    @Override
    public void onError(@NonNull Throwable e) {
        if (e != null && e.getMessage() != null) {
            LogUtil.v("RxHttp", e.getMessage());
        } else {
            LogUtil.v("RxHttp", "Throwable  || Message == Null");
        }
        if (e instanceof ApiThrowable) {
            LogUtil.e("RxHttp", "--> e instanceof ApiThrowable");
            LogUtil.e("RxHttp", "--> " + e.getCause().toString());
            onError((ApiThrowable) e);
        } else {
            LogUtil.e("RxHttp", "e !instanceof Throwable");
            String detail = "";
            if (e != null && e.getCause() != null) {
                detail = e.getCause().getMessage();
            }
            LogUtil.e("RxHttp", "--> " + detail);
            onError(ExceptionFactory.handleException(e));
        }
        onComplete();
    }

    @Override
    public void onComplete() {
        LogUtil.v("RxHttp", "-->http is Complete");
    }

    public abstract void onError(ApiThrowable throwable);


}
