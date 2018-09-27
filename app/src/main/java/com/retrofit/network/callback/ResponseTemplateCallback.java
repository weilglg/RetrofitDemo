package com.retrofit.network.callback;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.CollectionCodec;
import com.retrofit.network.RxHttp;
import com.retrofit.network.config.ResultConfigLoader;
import com.retrofit.network.exception.ServerException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.ResponseBody;

/**
 * 返回的数据格式是统一的并且只需要处理需要的数据
 * 例如：{"code":"0",
 * "msg":"成功",
 * "data":{
 * "name"："xxxx"
 * }
 * }
 */

@SuppressWarnings(value = {"unchecked", "deprecation"})
public abstract class ResponseTemplateCallback<T> extends ResponseCallback<T> {

    protected ResponseTemplateCallback() {
        ResultConfigLoader.init(RxHttp.getInstance().getContext());
    }

    @Override
    public T onTransformationResponse(ResponseBody body) throws Exception {
        String jsonStr = body.string();
        if (TextUtils.isEmpty(jsonStr)) throw new NullPointerException("body is null");
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
                        return (T) dataStr;
                    }
                    ObjectDeserializer deserializer = ParserConfig.getGlobalInstance().getDeserializer(paramType);
                    if (deserializer instanceof CollectionCodec) {
                        if (dataStr.startsWith("{") && dataStr.endsWith("}")) {
                            JSONObject pageJson = JSON.parseObject(dataStr);
                            String pageDataStr = getDataStr(pageJson);
                            if ("[]".equals(pageDataStr) || pageDataStr == null || "".equals(pageDataStr)) {
                                throw new NullPointerException("result data is null");
                            }
                            dataStr = pageDataStr;
                        }
                    }
                    return JSON.parseObject(dataStr, paramType, Feature.UseBigDecimal);
                }
            }
        }
        throw new ServerException(Integer.valueOf(code), msg);
    }

    public boolean checkSuccessCode(int code, String msg) {
        return true;
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
