package com.cn.rx.func;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.cn.rx.entity.ApiResultEntity;
import com.cn.rx.exception.ServerException;
import com.cn.rx.util.Util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import io.reactivex.functions.Function;
import okhttp3.ResponseBody;

@SuppressWarnings("unchecked")
public class ApiResultFunc<T> implements Function<ResponseBody, ApiResultEntity<T>> {

    private Type type;

    public ApiResultFunc(Type type) {
        this.type = type;
    }

    @Override
    public ApiResultEntity<T> apply(ResponseBody body) throws Exception {
        ApiResultEntity<T> apiResult = new ApiResultEntity<>();
        String jsonStr = body.string();
        apiResult.setCode(-1);
        if (TextUtils.isEmpty(jsonStr))
            throw new NullPointerException("json is null");
        try {
            final Class<T> subClazz = (Class) ((ParameterizedType) type).getRawType();
            if (ApiResultEntity.class.isAssignableFrom(subClazz)) {
                apiResult = JSON.parseObject(jsonStr, type, Feature.UseBigDecimal);
            } else {
                apiResult.setCode(-1);
                apiResult.setMsg("ApiResultEntity.class.isAssignableFrom(subClazz) err!!");
            }
//            if (type instanceof ParameterizedType) {//自定义的ApiResult继承自com.retrofit.network.entity.ApiResultEntity<T>
//                final Class<T> cls = (Class) ((ParameterizedType) type).getRawType();
//                if (ApiResultEntity.class.isAssignableFrom(cls)) { //判断传入的是否是ApiResultEntity或者其子类
//                    final Type[] params = ((ParameterizedType) type).getActualTypeArguments();
//                    final Class clazz = Util.getClass(params[0], 0);
//                    final Class rawType = Util.getClass(type, 0);
//                    if (!List.class.isAssignableFrom(rawType) && clazz.equals(String.class)) {
//                        ApiResultEntity<String> resultEntity = JSON.parseObject(jsonStr, type, Feature.UseBigDecimal);
//                        apiResult.setData((T) resultEntity.getData());
//                        apiResult.setCode(resultEntity.getCode());
//                        apiResult.setMsg(resultEntity.getMsg());
//                    } else {
//                        apiResult = JSON.parseObject(jsonStr, type, Feature.UseBigDecimal);
//                    }
//                } else {
//                    throw new ServerException(-1, "传入的泛型不正确，应该继承自AipResultEntity");
//                }
//
//            } else {
//                final Class<T> clazz = Util.getClass(type, 0);
//                if (clazz.equals(String.class)) {
//                    ApiResultEntity<String> resultEntity = JSON.parseObject(jsonStr, type, Feature.UseBigDecimal);
//                    apiResult.setData((T) resultEntity.getData());
//                    apiResult.setCode(resultEntity.getCode());
//                    apiResult.setMsg(resultEntity.getMsg());
//                } else {
//                    apiResult = JSON.parseObject(jsonStr, type, Feature.UseBigDecimal);
//                }
//            }
        } finally {
            body.close();
        }
        return apiResult;
    }
}
