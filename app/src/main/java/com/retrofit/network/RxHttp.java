package com.retrofit.network;


import android.content.Context;

import com.retrofit.network.entity.HttpConfigEntity;
import com.retrofit.network.util.LogUtil;
import com.retrofit.network.util.SSLUtil;

import java.io.File;
import java.io.InputStream;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class RxHttp {

    private static final String TAG = RxHttp.class.getSimpleName();
    public static final int DEFAULT_MILLISECONDS = 60000;             //默认的超时时间
    private static final int DEFAULT_RETRY_COUNT = 3;                 //默认重试次数
    private static final int DEFAULT_RETRY_INCREASEDELAY = 0;         //默认重试叠加时间
    private static final int DEFAULT_RETRY_DELAY = 500;               //默认重试延时
    private static final long CACHE_MAX_SIZE = 10 * 1024 * 1024;      //默认缓存大小
    private static final int DEFAULT_MAXIDLE_CONNECTIONS = 5;         //默认最大空闲连接数
    private static final long DEFAULT_KEEP_ALIVEDURATION = 8;


    private HttpConfigEntity defaultConfig;
    private HttpConfigEntity useConfig;

    private static RxHttp mInstance;
    private OkHttpClient.Builder okhttpBuilder;                       //okhttp请求的客户端
    private Retrofit.Builder retrofitBuilder;                         //Retrofit请求Builder
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
    private int mRetryCount = DEFAULT_RETRY_COUNT;                    //重试次数默认3次
    private int mRetryDelay = DEFAULT_RETRY_DELAY;                    //延迟xxms重试
    private int mRetryIncreaseDelay = DEFAULT_RETRY_INCREASEDELAY;    //叠加延迟
    private List<Interceptor> interceptorList = new ArrayList<>();
    private List<Interceptor> networkInterceptorList = new ArrayList<>();
    private Context context;
    private String baseUrl;
    private boolean isLog = true;
    private boolean isCookie = false;
    private boolean isCache = true;
    private boolean isSign = false;

    public void init(Context context) {
        this.context = context;
    }


    private RxHttp() {
        okhttpBuilder = new OkHttpClient.Builder();
        okhttpBuilder.connectTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        okhttpBuilder.readTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        okhttpBuilder.writeTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        retrofitBuilder = new Retrofit.Builder();
        retrofitBuilder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());//增加RxJava2CallAdapterFactory
    }

    public static RxHttp getInstance() {
        if (mInstance == null) {
            synchronized (RxHttp.class) {
                if (mInstance == null) {
                    mInstance = new RxHttp();
                }
            }
        }
        return mInstance;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public RxHttp baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        retrofitBuilder.baseUrl(Util.checkNotNull(baseUrl, "OkHttpClient is null"));
        return this;
    }

    public RxHttp OkHttpClient(OkHttpClient okHttpClient) {
        retrofitBuilder.client(Util.checkNotNull(okHttpClient, "OkHttpClient is null"));
        return this;
    }

    public RxHttp isLog(boolean log) {
        LogUtil.setDebug(log);
        if (log) {
            addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS));
            addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        return this;
    }

    private RxHttp addNetworkInterceptor(Interceptor interceptor) {
        okhttpBuilder.addInterceptor(Util.checkNotNull(interceptor, "interceptor is null"));
        return this;
    }

    private RxHttp addInterceptor(Interceptor interceptor) {
        okhttpBuilder.addInterceptor(Util.checkNotNull(interceptor, "interceptor is null"));
        return this;
    }

    public RxHttp cache(Cache cache) {
        okhttpBuilder.cache(Util.checkNotNull(cache, "cache is null"));
        return this;
    }


    public RxHttp cacheFile(File cacheFile) {
        this.cacheFile = cacheFile;
        return this;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public RxHttp okProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    public RxHttp hostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    public Converter.Factory getConverterFactory() {
        return converterFactory;
    }

    public RxHttp converterFactory(Converter.Factory converterFactory) {
        this.converterFactory = converterFactory;
        return this;
    }

    public CallAdapter.Factory getCallAdapterFactory() {
        return callAdapterFactory;
    }

    public RxHttp callAdapterFactory(CallAdapter.Factory callAdapterFactory) {
        this.callAdapterFactory = callAdapterFactory;
        return this;
    }

    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public RxHttp sslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    public X509TrustManager getTrustManager() {
        return trustManager;
    }

    public RxHttp trustManager(X509TrustManager trustManager) {
        this.trustManager = trustManager;
        return this;
    }

    public CookieJar getCookieJar() {
        return cookieJar;
    }

    public RxHttp cookieJar(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
        return this;
    }

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    public RxHttp connectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public RxHttp headers(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public RxHttp parameters(Map<String, String> parameters) {
        this.parameters.putAll(parameters);
        return this;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public RxHttp httpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public int getReadTimeOut() {
        return readTimeOut;
    }

    public RxHttp readTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
        return this;
    }

    public int getWriteTimeOut() {
        return writeTimeOut;
    }

    public RxHttp writeTimeOut(int writeTimeOut) {
        this.writeTimeOut = writeTimeOut;
        return this;
    }

    public int getConnectTimeOut() {
        return connectTimeOut;
    }

    public RxHttp connectTimeOut(int connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
        return this;
    }

    public List<Interceptor> getInterceptorList() {
        return interceptorList;
    }

    public RxHttp interceptorList(List<Interceptor> interceptorList) {
        this.interceptorList = interceptorList;
        return this;
    }

    public List<Interceptor> getNetworkInterceptorList() {
        return networkInterceptorList;
    }

    public RxHttp networkInterceptorList(List<Interceptor> networkInterceptorList) {
        this.networkInterceptorList = networkInterceptorList;
        return this;
    }

    public Context getContext() {
        testInitialize();
        return context;
    }

    public OkHttpClient.Builder getOkHttpBuilder() {
        return okhttpBuilder;
    }

    public Retrofit.Builder getRetrofitBuilder() {
        return retrofitBuilder;
    }

    public SSLUtil.SSLParams getSslParams() {
        return sslParams;
    }

    public int getRetryCount() {
        return mRetryCount;
    }

    public int getRetryDelay() {
        return mRetryDelay;
    }

    public int getRetryIncreaseDelay() {
        return mRetryIncreaseDelay;
    }

    public boolean isSign() {
        return isSign;
    }

    public RxHttp isSign(boolean isSign) {
        this.isSign = isSign;
        return this;
    }

    public RxHttp retryCount(int mRetryCount) {
        this.mRetryCount = mRetryCount;
        return this;
    }

    public RxHttp retryDelay(int mRetryDelay) {
        this.mRetryDelay = mRetryDelay;
        return this;
    }

    public RxHttp retryIncreaseDelay(int mRetryIncreaseDelay) {
        this.mRetryIncreaseDelay = mRetryIncreaseDelay;
        return this;
    }

    public RxHttp certificates(InputStream... certificates) {
        this.sslParams = SSLUtil.getSslSocketFactory(null, null, certificates);
        return this;
    }

    public RxHttp certificates(InputStream bksFile, String password, InputStream... certificates) {
        this.sslParams = SSLUtil.getSslSocketFactory(bksFile, password, certificates);
        return this;
    }

    private void testInitialize() {
        if (this.context == null)
            throw new ExceptionInInitializerError("请先在全局Application中调用 EasyHttp.init() 初始化！");
    }
}
