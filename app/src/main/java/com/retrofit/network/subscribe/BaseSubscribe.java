package com.retrofit.network.subscribe;

import android.content.Context;

import com.retrofit.network.util.LogUtil;
import com.retrofit.network.exception.CommThrowable;
import com.retrofit.network.exception.ExceptionFactory;

import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;

public abstract class BaseSubscribe<T> extends DisposableObserver<T> {

    private Context context;

    public BaseSubscribe() {
    }

    public BaseSubscribe(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
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
        if (e instanceof CommThrowable) {
            LogUtil.e("RxHttp", "--> e instanceof CommThrowable");
            LogUtil.e("RxHttp", "--> " + e.getCause().toString());
            onError((CommThrowable) e);
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

    abstract void onError(CommThrowable throwable);



}
