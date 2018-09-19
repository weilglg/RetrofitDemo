package com.retrofit.network.request;

import android.content.Context;

import com.retrofit.network.RxHttp;
import com.retrofit.network.util.SSLUtil;

import java.io.File;
import java.io.InputStream;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;

public class BaseRequest {
    private String url;                                              //请求Url
    private Cache cache;                                              //OkHttp缓存对象
    private File cacheFile;                                           //缓存目录
    private Proxy proxy;                                              //OkHttp代理
    private HostnameVerifier hostnameVerifier;                        //https的全局访问规则
    private Converter.Factory converterFactory;                       //Converter.Factory
    private CallAdapter.Factory callAdapterFactory;                   //CallAdapter.Factory
    private SSLSocketFactory sslSocketFactory;                        //签名验证规则
    private X509TrustManager trustManager;
    private SSLUtil.SSLParams sslParams;                            //https签名证书
    private CookieJar cookieJar;                                      //Cookie管理
    private ConnectionPool connectionPool;                            //链接池管理
    private Map<String, String> headers = new HashMap<>();            //公共请求头
    private Map<String, String> parameters = new HashMap<>();         //公共请求参数
    private OkHttpClient httpClient;                                  //自定义OkHttpClient
    private int readTimeOut;                                          //读超时
    private int writeTimeOut;                                         //写超时
    private int connectTimeOut;                                       //链接超时
    private int mRetryCount;                    //重试次数默认3次
    private int mRetryDelay;                    //延迟xxms重试
    private int mRetryIncreaseDelay;    //叠加延迟
    private List<Interceptor> interceptorList = new ArrayList<>();
    private List<Interceptor> networkInterceptorList = new ArrayList<>();
    private Context context;
    private String baseUrl;
    private boolean isLog = true;
    private boolean isCookie = false;
    private boolean isCache = true;
    private boolean isSign = false;

    public BaseRequest(String url) {
        this.url = url;
        RxHttp rxHttp = RxHttp.getInstance();
        if (baseUrl == null && url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
            HttpUrl httpUrl = HttpUrl.parse(url);
            if (httpUrl != null)
                baseUrl = httpUrl.url().getProtocol() + "://" + httpUrl.url().getHost() + "/";
        }
        mRetryCount = rxHttp.getRetryCount();
        mRetryDelay = rxHttp.getRetryDelay();
        mRetryIncreaseDelay = rxHttp.getRetryIncreaseDelay();
    }

    public BaseRequest baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public boolean isLog() {
        return isLog;
    }

    public BaseRequest isLog(boolean log) {
        isLog = log;
        return this;
    }

    public boolean isCookie() {
        return isCookie;
    }

    public BaseRequest isCookie(boolean cookie) {
        isCookie = cookie;
        return this;
    }

    public boolean isCache() {
        return isCache;
    }

    public BaseRequest isCache(boolean cache) {
        isCache = cache;
        return this;
    }

    public BaseRequest cache(Cache cache) {
        this.cache = cache;
        return this;
    }

    public BaseRequest cacheFile(File cacheFile) {
        this.cacheFile = cacheFile;
        return this;
    }

    public BaseRequest okProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public BaseRequest hostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    public BaseRequest converterFactory(Converter.Factory converterFactory) {
        this.converterFactory = converterFactory;
        return this;
    }

    public BaseRequest callAdapterFactory(CallAdapter.Factory callAdapterFactory) {
        this.callAdapterFactory = callAdapterFactory;
        return this;
    }

    public BaseRequest sslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    public BaseRequest trustManager(X509TrustManager trustManager) {
        this.trustManager = trustManager;
        return this;
    }

    public BaseRequest cookieJar(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
        return this;
    }

    public BaseRequest connectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        return this;
    }

    public BaseRequest headers(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    public BaseRequest parameters(Map<String, String> parameters) {
        this.parameters.putAll(parameters);
        return this;
    }

    public BaseRequest httpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public BaseRequest readTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
        return this;
    }

    public BaseRequest writeTimeOut(int writeTimeOut) {
        this.writeTimeOut = writeTimeOut;
        return this;
    }

    public BaseRequest connectTimeOut(int connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
        return this;
    }

    public BaseRequest interceptorList(List<Interceptor> interceptorList) {
        this.interceptorList = interceptorList;
        return this;
    }

    public BaseRequest networkInterceptorList(List<Interceptor> networkInterceptorList) {
        this.networkInterceptorList = networkInterceptorList;
        return this;
    }

    public BaseRequest isSign(boolean isSign) {
        this.isSign = isSign;
        return this;
    }

    public BaseRequest retryCount(int mRetryCount) {
        this.mRetryCount = mRetryCount;
        return this;
    }

    public BaseRequest retryDelay(int mRetryDelay) {
        this.mRetryDelay = mRetryDelay;
        return this;
    }

    public BaseRequest retryIncreaseDelay(int mRetryIncreaseDelay) {
        this.mRetryIncreaseDelay = mRetryIncreaseDelay;
        return this;
    }

    public BaseRequest certificates(InputStream... certificates) {
        this.sslParams = SSLUtil.getSslSocketFactory(null, null, certificates);
        return this;
    }

    public BaseRequest certificates(InputStream bksFile, String password, InputStream... certificates) {
        this.sslParams = SSLUtil.getSslSocketFactory(bksFile, password, certificates);
        return this;
    }
}
