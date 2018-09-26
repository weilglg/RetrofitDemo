package com.retrofit.network.callback;

import com.retrofit.network.entity.ApiResultEntity;
import com.retrofit.network.util.$Gson$Types;
import com.retrofit.network.util.Util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;

public abstract class ResultCallbackProxy<T extends ApiResultEntity<R>, R> implements IResultType<T> {

    ResultCallback<R> mCallback;

    public ResultCallbackProxy(ResultCallback<R> mCallback) {
        this.mCallback = mCallback;
    }

    public ResultCallback<R> getCallback() {
        return mCallback;
    }

    @Override
    public Type getType() {
        Type typeArguments = null;
        if (mCallback != null) {
            Type rawType = mCallback.getRawType();//如果用户的信息是返回List需单独处理
            if (List.class.isAssignableFrom(Util.getClass(rawType, 0)) || Map.class.isAssignableFrom(Util.getClass(rawType, 0))) {
                typeArguments = mCallback.getType();
            } else {
                Type type = mCallback.getType();
                typeArguments = Util.getClass(type, 0);
            }
        }
        if (typeArguments == null) {
            typeArguments = ResponseBody.class;
        }
        Type rawType = Util.findNeedType(getClass());
        if (rawType instanceof ParameterizedType) {
            rawType = ((ParameterizedType) rawType).getRawType();
        }

        return $Gson$Types.newParameterizedTypeWithOwner(null, rawType, typeArguments);
    }

}
