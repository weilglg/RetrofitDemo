package com.retrofit.network.interceptor;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class BaseInterceptor implements Interceptor {

    private Map<String, String> map;

    public BaseInterceptor(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder builder = chain.request()
                .newBuilder();
        if (map != null && map.size() > 0) {
            Set<String> keys = map.keySet();
            for (String headerKey : keys) {
                builder.addHeader(headerKey, map.get(headerKey) == null? "": (String)map.get(headerKey)).build();
            }
        }
        return chain.proceed(builder.build());
    }
}
