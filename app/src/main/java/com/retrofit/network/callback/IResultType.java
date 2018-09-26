package com.retrofit.network.callback;

import java.lang.reflect.Type;

public interface IResultType<T> {

    /**
     * 获取需要解析的泛型T类型
     */
    Type getType();

}
