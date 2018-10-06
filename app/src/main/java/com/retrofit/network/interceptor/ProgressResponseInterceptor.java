package com.retrofit.network.interceptor;


import com.retrofit.network.body.ProgressResponseBody;
import com.retrofit.network.callback.ResultProgressCallback;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by admin on 2017/9/5.
 */

public class ProgressResponseInterceptor implements Interceptor {
    private ResultProgressCallback progressListener;
    private Object tag;

    public ProgressResponseInterceptor(ResultProgressCallback progressListener, Object tag) {
        this.progressListener = progressListener;
        this.tag = tag;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        //拦截
        Response originalResponse = chain.proceed(chain.request());

        //包装响应体并返回
        return originalResponse.newBuilder()
                .body(new ProgressResponseBody(originalResponse.body(), progressListener, tag))
                .build();
    }
}
