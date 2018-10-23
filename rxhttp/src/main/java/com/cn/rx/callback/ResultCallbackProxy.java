package com.cn.rx.callback;

import com.cn.rx.entity.ApiResultEntity;
import com.cn.rx.util.$Gson$Types;
import com.cn.rx.util.Util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;

/**
 * 回调代理类，实现泛型的解析以及组装（定义为抽象类是因为方便获取泛型）
 *
 * @param <T> {@link ApiResultEntity}的子类及返回结果的统一基类
 * @param <R> 真正的返回结果类型
 */
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

    /**
     * 返回结果为ApiResultEntity<T>格式的回调代理类
     */
    public static <T> ResultCallbackProxy<ApiResultEntity<T>, T> NEW_DEFAULT_INSTANCE(ResultCallback<T> callback) {
        return new ResultCallbackProxy<ApiResultEntity<T>, T>(callback) {
        };
    }

}
