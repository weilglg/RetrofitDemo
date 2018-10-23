package com.cn.rx.interceptor;



import com.cn.rx.util.LogUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.reactivex.annotations.NonNull;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HeaderInterceptor implements Interceptor {

    private Map<String, String> map;

    public HeaderInterceptor(Map<String, String> map) {
        this.map = map;
    }

    public void addHeaderMap(Map<String, String> map) {
        if (this.map == null) {
            this.map = new HashMap<>();
        }
        this.map.putAll(map);
    }

    private void addHeader(String key, String value) {
        this.map.put(key, value);
    }

    public void clearAll() {
        if (this.map != null) {
            this.map.clear();
        }
    }

    public void remove(String key) {
        if (this.map != null && map.containsKey(key)) {
            this.map.remove(key);
        }
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        if (map != null && map.size() > 0) {
            Set<String> keys = map.keySet();
            for (String headerKey : keys) {
                builder.addHeader(headerKey, map.get(headerKey) == null ? "" : map.get(headerKey)).build();
            }
        }
        LogUtil.i("RxHttp", "-->>headers：" + builder.build().headers().toString());
        return chain.proceed(builder.build());
    }
}
