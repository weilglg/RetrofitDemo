package com.retrofit.network.subscribe;


import java.io.IOException;

import okhttp3.ResponseBody;

public abstract class ResponseStringCallback extends ResponseCallback<String> {

    @Override
    public String onTransformationResponse(Object tag, ResponseBody body) throws IOException {
        return new String(body.bytes());
    }

}
