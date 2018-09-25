package com.retrofit.network.interceptor;

import android.support.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestInterceptor implements Interceptor {
    private Object tag;

    public RequestInterceptor(Object tag) {
        this.tag = tag;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder builder = chain.
                request()
                .newBuilder()
                .tag(tag);
        return chain.proceed(builder.build());
    }
}
