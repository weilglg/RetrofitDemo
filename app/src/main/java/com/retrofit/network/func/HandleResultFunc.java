package com.retrofit.network.func;

import com.retrofit.network.entity.ApiResultEntity;
import com.retrofit.network.exception.ServerException;

import io.reactivex.functions.Function;

public class HandleResultFunc<T> implements Function<ApiResultEntity<T>, T> {

    @Override
    public T apply(ApiResultEntity<T> resultEntity) throws Exception {
        if (resultEntity.isOk()) {
            return resultEntity.getData();
        }
        throw new ServerException(resultEntity.getCode(), resultEntity.getMsg());
    }
}
