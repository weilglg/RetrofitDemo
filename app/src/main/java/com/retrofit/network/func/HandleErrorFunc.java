package com.retrofit.network.func;


import com.retrofit.network.exception.ExceptionFactory;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class HandleErrorFunc<T> implements Function<Throwable, Observable<T>> {

    @Override
    public Observable<T> apply(Throwable throwable) throws Exception {
        return Observable.error(ExceptionFactory.handleException(throwable));
    }

}
