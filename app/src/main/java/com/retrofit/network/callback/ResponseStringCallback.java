package com.retrofit.network.callback;


import java.io.IOException;

import okhttp3.ResponseBody;

/**
 * 直接以字符串形式返回
 */
public abstract class ResponseStringCallback extends ResponseCallback<String> {

    @Override
    public String onTransformationResponse(ResponseBody body) throws Exception {
        String jsonStr = "";
        try {
            body.string();
        } finally {
            body.close();
        }
        return jsonStr;
    }

}
