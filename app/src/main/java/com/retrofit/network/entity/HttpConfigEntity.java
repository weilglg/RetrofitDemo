package com.retrofit.network.entity;

import android.content.Context;

import java.io.File;
import java.io.Serializable;
import java.net.Proxy;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;

public class HttpConfigEntity implements Serializable {
    public Context context;
    public String baseUrl;
    public boolean isLog = true;
    public boolean isCookie = false;
    public boolean isCache = true;
    public boolean isUseDefault = true;
    public Object tag;
    public Cache cache;
    public File cacheFile;
    public Proxy proxy;
    public HostnameVerifier hostnameVerifier;
    public Converter.Factory converterFactory;
    public CallAdapter.Factory callAdapterFactory;
    public SSLSocketFactory sslSocketFactory;
    public X509TrustManager trustManager;
    public CookieJar cookieJar;
    public ConnectionPool connectionPool;
    public Map<String, String> headers;
    public Map<String, String> parameters;
    public OkHttpClient httpClient;
    public int readTimeOut;
    public int writeTimeOut;
    public int connectTimeOut;
    public List<Interceptor> interceptorList;
    public List<Interceptor> networkInterceptorList;

    @Override
    public String toString() {
        return "ConfigEntity{" +
                "context=" + context +
                ", baseUrl='" + baseUrl + '\'' +
                ", isLog=" + isLog +
                ", isCookie=" + isCookie +
                ", getmCache=" + isCache +
                ", isUseDefault=" + isUseDefault +
                ", tag=" + tag +
                ", cache=" + cache +
                ", cacheFile=" + cacheFile +
                ", proxy=" + proxy +
                ", hostnameVerifier=" + hostnameVerifier +
                ", converterFactory=" + converterFactory +
                ", callAdapterFactory=" + callAdapterFactory +
                ", sslSocketFactory=" + sslSocketFactory +
                ", trustManager=" + trustManager +
                ", cookieJar=" + cookieJar +
                ", connectionPool=" + connectionPool +
                ", headers=" + headers +
                ", parameters=" + parameters +
                ", httpClient=" + httpClient +
                ", readTimeOut=" + readTimeOut +
                ", connectTimeOut=" + connectTimeOut +
                ", interceptorList=" + interceptorList +
                ", networkInterceptorList=" + networkInterceptorList +
                '}';
    }
}
