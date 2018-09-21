package com.retrofit.network.subscriber;


import java.io.IOException;

import okhttp3.ResponseBody;

public abstract class ResponseStringCallback extends ResponseCallback<String> {

    @Override
    public String onTransformationResponse(Object tag, ResponseBody body) {
        try {
            return body.string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
