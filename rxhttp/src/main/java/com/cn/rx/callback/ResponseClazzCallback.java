package com.cn.rx.callback;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cn.rx.config.ResultConfigLoader;
import com.cn.rx.exception.ServerException;

import java.util.List;

import okhttp3.ResponseBody;

public abstract class ResponseClazzCallback implements IResponseCallback<String> {

    @Override
    public String onTransformationResponse(ResponseBody body) throws Exception {
        String jsonStr = body.string();
        if (TextUtils.isEmpty(jsonStr)) throw new NullPointerException("body is null");
        JSONObject object = JSON.parseObject(jsonStr);
        int code = getCode(object);
        String msg = getMessage(object);
        String dataStr = getDataStr(object);
        if (checkSuccess(code)) {
            return dataStr;
        }
        throw new ServerException(code, msg);
    }

    abstract boolean checkSuccess(int code);

    private int getCode(JSONObject object) {
        String codeKey = ResultConfigLoader.getCodeKey();
        int code = -1;
        if (object.containsKey(codeKey)) {
            code = object.getIntValue(codeKey);
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
