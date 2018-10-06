package com.retrofit.network.interceptor;


import com.retrofit.network.body.ProgressRequestBody;
import com.retrofit.network.callback.ResultProgressCallback;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by admin on 2017/9/5.
 */

public class ProgressRequestInterceptor implements Interceptor {
    private ResultProgressCallback progressListener;
    private Object tag;

    public ProgressRequestInterceptor(Object tag, ResultProgressCallback progressListener) {
        this.progressListener = progressListener;
        this.tag = tag;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request request = original.newBuilder()
                .method(original.method(), new ProgressRequestBody(original.body(), progressListener, tag))
                .build();
        return chain.proceed(request);
    }
}
