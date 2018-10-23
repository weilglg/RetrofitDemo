package com.cn.rx.func;

import com.cn.rx.entity.ApiResultEntity;
import com.cn.rx.exception.ServerException;

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
