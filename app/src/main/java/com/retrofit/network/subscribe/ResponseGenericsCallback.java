package com.retrofit.network.subscribe;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.CollectionCodec;
import com.retrofit.network.config.ResultConfigLoader;
import com.retrofit.network.exception.ServerException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.ResponseBody;

public abstract class ResponseGenericsCallback<T> extends ResponseCallback<T> {

    private Context context;

    public void setContext(Context context) {
        this.context = context;
        ResultConfigLoader.init(context);
    }

    @Override
    public T onTransformationResponse(Object tag, ResponseBody body) throws Exception {
        String jsonStr = new String(body.bytes());
        JSONObject object = JSON.parseObject(jsonStr);
        String code = getCode(object);
        String msg = getMessage(object);
        String dataStr = getDataStr(object);
        boolean isSuccess = checkSuccessCode(Integer.valueOf(code), msg);
        if (isSuccess) {
            Type genType = getClass().getGenericSuperclass();
            if (genType instanceof ParameterizedType) {
                Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
                if (params.length > 0) {
                    Type paramType = params[0];
                    if (paramType instanceof Class && String.class.isAssignableFrom((Class) paramType)) {
                        return (T) new String(body.bytes());
                    }
                    ObjectDeserializer deserializer = ParserConfig.getGlobalInstance().getDeserializer(paramType);
                    if (deserializer instanceof CollectionCodec) {
                        if (dataStr.startsWith("{") && dataStr.endsWith("}")) {
                            JSONObject pageJson = JSON.parseObject(dataStr);
                            String pageDataStr = getDataStr(pageJson);
                            if ("[]".equals(pageDataStr) || pageDataStr == null || "".equals(pageDataStr)) {
                                throw new NullPointerException("数据为空");
                            }
                            dataStr = pageDataStr;
                        }
                    }
                    return JSON.parseObject(dataStr, paramType, Feature.UseBigDecimal);
                }
            }
        }
        throw new ServerException(Integer.valueOf(code));
    }

    protected abstract boolean checkSuccessCode(int code, String msg);

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
