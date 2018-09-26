package com.retrofit.network.callback;

import com.retrofit.network.exception.ApiThrowable;
import com.retrofit.network.util.Util;

import java.lang.reflect.Type;

public abstract class ResultCallback<T> implements IResultType<T> {

    public abstract void onStart(Object tag);

    public abstract void onCompleted(Object tag);

    public abstract void onError(Object tag, ApiThrowable e);

    public abstract void onSuccess(Object tag, T t);

    @Override
    public Type getType() {
        return Util.findNeedClass(getClass());
    }

    /**
     * 获取需要解析的泛型T raw类型
     */
    public Type getRawType() {
        return Util.findRawType(getClass());
    }
}
