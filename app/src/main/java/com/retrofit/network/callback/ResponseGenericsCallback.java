package com.retrofit.network.callback;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.Feature;
import com.retrofit.network.exception.ApiThrowable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;

/**
 * 无模板解析，对返回的数据不进行其他处理
 */
public abstract class ResponseGenericsCallback<T> extends ResponseCallback<T> {


    @Override
    public T onTransformationResponse(Object tag, ResponseBody body) throws Exception {
        try {
            String jsonStr = body.string();
            if (TextUtils.isEmpty(jsonStr)) throw new NullPointerException("body is null");
            Type genType = getClass().getGenericSuperclass();
            if (genType instanceof ParameterizedType) {
                Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
                if (params.length > 0) {
                    Type paramType = params[0];
                    return JSON.parseObject(jsonStr, paramType, Feature.UseBigDecimal);
                }
            }
        } finally {
            body.close();
        }
        throw new JSONException("泛型解析异常");
    }
}
