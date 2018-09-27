package com.retrofit.network.transformer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.retrofit.network.callback.ResponseClazzCallback;
import com.retrofit.network.config.ResultConfigLoader;

import java.lang.reflect.Type;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;

public class HandleClazzBodyTransformer<T> implements ObservableTransformer<ResponseBody, T> {

    private Type type;
    private ResponseClazzCallback callback;

    public HandleClazzBodyTransformer(Type type, ResponseClazzCallback callback) {
        this.type = type;
        this.callback = callback;

    }

    @Override
    public ObservableSource<T> apply(Observable<ResponseBody> upstream) {
        return upstream.map(new Function<ResponseBody, T>() {
            @Override
            public T apply(ResponseBody body) throws Exception {
                if (callback != null) {
                    String jsonStr = callback.onTransformationResponse(body);
                    return JSON.parseObject(jsonStr, type, Feature.UseBigDecimal);
                } else {
                    return JSON.parseObject(body.string(), type, Feature.UseBigDecimal);
                }
            }
        });
    }

    private String getCode(JSONObject object) {
        String codeKey = ResultConfigLoader.getCodeKey();
        String code = "-1";
        if (object.containsKey(codeKey)) {
            code = object.getString(codeKey);
        }
        return code;
    }

    private String getMessage(JSONObject object) {
        String msgKey = ResultConfigLoader.getMsgKey();
        if (object.containsKey(msgKey)) {
            return object.getString(msgKey);
        }
        return "";
    }

    private String getDataStr(JSONObject object) {
        List<String> dataKey = ResultConfigLoader.getDataKey();
        for (String key : dataKey) {
            if (object.containsKey(key)) {
                return object.getString(key);
            }
        }
        return "";
    }
}
