package com.retrofit.network.transformer;

import com.retrofit.network.func.HandleErrorFunc;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;

public class HandleErrorTransformer<T> implements ObservableTransformer<T, T> {
    @Override
    public ObservableSource<T> apply(Observable<T> upstream) {
        return upstream.onErrorResumeNext(new HandleErrorFunc<T>());
    }
}
