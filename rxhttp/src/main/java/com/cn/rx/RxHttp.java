package com.cn.rx;


import android.content.Context;


import com.cn.rx.interceptor.HeaderInterceptor;
import com.cn.rx.request.TemplatePostRequest;
import com.cn.rx.request.ApiResultPostRequest;
import com.cn.rx.util.LogUtil;
import com.cn.rx.util.SSLUtil;
import com.cn.rx.util.Util;

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

public final class RxHttp {

    private static final String TAG = RxHttp.class.getSimpleName();
    public static final int DEFAULT_MILLISECONDS = 30;             //默认的超时时间
    private static final int DEFAULT_RETRY_COUNT = 3;                 //默认重试次数
    private static final int DEFAULT_RETRY_INCREASEDELAY = 0;         //默认重试叠加时间
    private static final int DEFAULT_RETRY_DELAY = 500;               //默认重试延时
    private static final long CACHE_MAX_SIZE = 10 * 1024 * 1024;      //默认缓存大小


    private static RxHttp mInstance;
    private OkHttpClient.Builder okHttpClientBuilder;                       //okhttp请求的客户端
    private Retrofit.Builder retrofitBuilder;                         //Retrlofit请求Builder
    private Cache cache;                                              //OkHttp缓存对象
    private File cacheFile;                                           //缓存目录
    private long mCacheMaxSize;                                       //最大缓存
    private Proxy proxy;                                              //OkHttp代理
    private HostnameVerifier hostnameVerifier;                        //https的全局访问规则
    private Converter.Factory converterFactory;                       //Converter.Factory
    private CallAdapter.Factory callAdapterFactory;                   //CallAdapter.Factory
    private SSLSocketFactory sslSocketFactory;                        //签名验证规则
    private X509TrustManager trustManager;
    private SSLUtil.SSLParams sslParams;                              //https签名证书
    private CookieJar cookieJar;                                      //Cookie管理
    private ConnectionPool connectionPool;                            //链接池管理
    private Map<String, String> headers = new HashMap<>();            //公共请求头
    private Map<String, String> parameters = new HashMap<>();         //公共请求参数
    private OkHttpClient httpClient;                                  //自定义OkHttpClient
    private int readTimeout;                                          //读超时
    private int writeTimeout;                                         //写超时
    private int connectTimeout;                                       //链接超时
    private int mRetryCount = DEFAULT_RETRY_COUNT;                    //重试次数默认3次
    private int mRetryDelay = DEFAULT_RETRY_DELAY;                    //延迟xxms重试
    private int mRetryIncreaseDelay = DEFAULT_RETRY_INCREASEDELAY;    //叠加延迟
    private List<Interceptor> interceptorList = new ArrayList<>();
    private List<Interceptor> networkInterceptorList = new ArrayList<>();
    private Context context;
    private String baseUrl;
    private boolean isSign = false;
    private boolean accessToken = false;
    private boolean isSyncRequest = true;
    private HeaderInterceptor mHeaderInterceptor;

    public RxHttp init(Context context) {
        this.context = context;
        return this;
    }


    private RxHttp() {
        okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.connectTimeout(DEFAULT_MILLISECONDS, TimeUnit.SECONDS);
        okHttpClientBuilder.readTimeout(DEFAULT_MILLISECONDS, TimeUnit.SECONDS);
        okHttpClientBuilder.writeTimeout(DEFAULT_MILLISECONDS, TimeUnit.SECONDS);
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

    /**
     * 全局设置访问域
     */
    public RxHttp baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        retrofitBuilder.baseUrl(Util.checkNotNull(baseUrl, "OkHttpClient is null"));
        return this;
    }

    /**
     * 全局设置自定义OkHttpClient
     */
    public RxHttp OkHttpClient(OkHttpClient okHttpClient) {
        retrofitBuilder.client(Util.checkNotNull(okHttpClient, "OkHttpClient is null"));
        return this;
    }

    /**
     * 全局设置日志输出
     */
    public RxHttp isLog(boolean log) {
        LogUtil.setDebug(log);
        if (log && BuildConfig.DEBUG) {
            // Log信息拦截器
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            //设置 Debug Log 模式
            this.okHttpClientBuilder.addInterceptor(loggingInterceptor);
        }
        return this;
    }

    /**
     * 全局网络拦截器
     */
    public RxHttp addNetworkInterceptor(Interceptor interceptor) {
        if (!networkInterceptorList.contains(Util.checkNotNull(interceptor, "interceptor is null"))) {
            networkInterceptorList.add(interceptor);
        }
        return this;
    }

    /**
     * 全局拦截器
     */
    public RxHttp addInterceptor(Interceptor interceptor) {
        if (!interceptorList.contains(Util.checkNotNull(interceptor, "interceptor is null"))) {
            interceptorList.add(interceptor);
        }
        return this;
    }

    /**
     * 全局缓存
     */
    public RxHttp cache(Cache cache) {
        okHttpClientBuilder.cache(Util.checkNotNull(cache, "cache is null"));
        return this;
    }

    /**
     * 缓存文件大小
     */
    public RxHttp cacheMaxSize(long cacheMaxSize) {
        this.mCacheMaxSize = mCacheMaxSize;
        return this;
    }

    /**
     * 全局缓存文件
     */
    public RxHttp cacheFile(File cacheFile) {
        Cache cache = new Cache(Util.checkNotNull(cacheFile, "cacheFile is null"), Math.max(5 * 1024 * 1024, mCacheMaxSize));
        return cache(cache);
    }

    /**
     * 全局OkHttp的代理
     */
    public RxHttp okProxy(Proxy proxy) {
        okHttpClientBuilder.proxy(proxy);
        return this;
    }

    /**
     * https的全局访问规则
     */
    public RxHttp hostnameVerifier(HostnameVerifier hostnameVerifier) {
        okHttpClientBuilder.hostnameVerifier(Util.checkNotNull(hostnameVerifier, "HostnameVerifier is null"));
        return this;
    }

    /**
     * 全局设置Converter.Factory
     */
    public RxHttp converterFactory(Converter.Factory converterFactory) {
        retrofitBuilder.addConverterFactory(Util.checkNotNull(converterFactory, "Converter.Factory is null"));
        return this;
    }

    /**
     * 全局设置CallAdapter.Factory,默认RxJavaCallAdapterFactory.create()
     */
    public RxHttp callAdapterFactory(CallAdapter.Factory callAdapterFactory) {
        retrofitBuilder.addCallAdapterFactory(Util.checkNotNull(callAdapterFactory, "CallAdapter.Factory is null"));
        return this;
    }

    /**
     * 全局设置Retrofit对象Factory
     */
    public RxHttp setCallFactory(okhttp3.Call.Factory factory) {
        retrofitBuilder.callFactory(Util.checkNotNull(factory, "factory == null"));
        return this;
    }

    /**
     * https的全局自签名证书
     */
    public RxHttp certificates(InputStream... certificates) {
        SSLUtil.SSLParams sslParams = SSLUtil.getSslSocketFactory(null, null, certificates);
        okHttpClientBuilder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        return this;
    }

    /**
     * https双向认证证书
     */
    public RxHttp certificates(InputStream bksFile, String password, InputStream... certificates) {
        SSLUtil.SSLParams sslParams = SSLUtil.getSslSocketFactory(bksFile, password, certificates);
        okHttpClientBuilder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        return this;
    }

    public RxHttp sslSocketFactory(SSLSocketFactory sslSocketFactory) {
        okHttpClientBuilder.sslSocketFactory(sslSocketFactory);
        return this;
    }


    public RxHttp trustManager(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
        okHttpClientBuilder.sslSocketFactory(sslSocketFactory, trustManager);
        return this;
    }

    /**
     * 全局cookie存取规则
     */
    public RxHttp cookieJar(CookieJar cookieJar) {
        okHttpClientBuilder.cookieJar(cookieJar);
        return this;
    }

    /**
     * 全局设置请求的连接池
     */
    public RxHttp connectionPool(ConnectionPool connectionPool) {
        okHttpClientBuilder.connectionPool(connectionPool);
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * 全局设置请求头
     */
    public RxHttp headers(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    public RxHttp remove(String key) {
        if (headers.containsKey(key)) {
            headers.remove(key);
        }
        if (this.mHeaderInterceptor != null) {
            this.mHeaderInterceptor.remove(key);
        }
        return this;
    }

    public RxHttp clearAllHeaders() {
        headers.clear();
        if (this.mHeaderInterceptor != null) {
            this.mHeaderInterceptor.clearAll();
        }
        return this;
    }

    /**
     * 添加全局公共请求参数
     */
    public RxHttp addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public HeaderInterceptor getBaseHeaderInterceptor() {
        return this.mHeaderInterceptor;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public RxHttp parameters(Map<String, String> parameters) {
        this.parameters.putAll(parameters);
        return this;
    }

    public RxHttp addParam(String key, String value) {
        this.parameters.put(key, value);
        return this;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * 全局为Retrofit设置自定义的OkHttpClient
     */
    public RxHttp httpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * 全局设置读超时时间
     */
    public RxHttp readTimeout(long readTimeout) {
        okHttpClientBuilder.readTimeout(readTimeout, TimeUnit.SECONDS);
        return this;
    }

    /**
     * 全局设置写超时时间
     */
    public RxHttp writeTimeout(int writeTimeout) {
        okHttpClientBuilder.writeTimeout(writeTimeout, TimeUnit.SECONDS);
        return this;
    }

    /**
     * 全局设置连接超时时间
     */
    public RxHttp connectTimeout(int connectTimeout) {
        okHttpClientBuilder.connectTimeout(connectTimeout, TimeUnit.SECONDS);
        return this;
    }

    public List<Interceptor> getInterceptorList() {
        return interceptorList;
    }

    public RxHttp interceptorList(List<Interceptor> interceptorList) {
        this.interceptorList.addAll(interceptorList);
        return this;
    }

    public List<Interceptor> getNetworkInterceptorList() {
        return networkInterceptorList;
    }

    public RxHttp networkInterceptorList(List<Interceptor> networkInterceptorList) {
        this.networkInterceptorList.addAll(networkInterceptorList);
        return this;
    }

    public Context getContext() {
        testInitialize();
        return context;
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

    public boolean isSyncRequest() {
        return isSyncRequest;
    }

    /**
     * 全局设置是否是异步请求
     */
    public RxHttp isSyncRequest(boolean isSyncRequest) {
        this.isSyncRequest = isSyncRequest;
        return this;
    }

    public boolean isAccessToken() {
        return accessToken;
    }

    public void accessToken(boolean accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * 超时重试次数
     */
    public RxHttp retryCount(int retryCount) {
        if (retryCount < 0) throw new IllegalArgumentException("retryCount must > 0");
        this.mRetryCount = retryCount;
        return this;
    }

    /**
     * 超时重试延迟时间
     */
    public RxHttp retryDelay(int retryDelay) {
        if (retryDelay < 0) throw new IllegalArgumentException("retryDelay must > 0");
        this.mRetryDelay = retryDelay;
        return this;
    }

    /**
     * 超时重试延迟叠加时间
     */
    public RxHttp retryIncreaseDelay(int retryIncreaseDelay) {
        if (retryIncreaseDelay < 0)
            throw new IllegalArgumentException("retryIncreaseDelay must > 0");
        this.mRetryIncreaseDelay = retryIncreaseDelay;
        return this;
    }

    private void testInitialize() {
        if (this.context == null)
            throw new ExceptionInInitializerError("请先在全局Application中调用 RxHttp.getInstance().init() 初始化！");
    }

    public OkHttpClient.Builder getOkHttpClientBuilder() {
        //加入请求参数以及头信息
        if (headers.size() > 0) {
            if (mHeaderInterceptor != null) {
                mHeaderInterceptor.addHeaderMap(headers);
            } else {
                mHeaderInterceptor = new HeaderInterceptor(headers);
                //将添加统一头内容的拦截器放在第一位方便后面的拦截器使用
                addInterceptor(mHeaderInterceptor);
            }
        }
        if (interceptorList != null && interceptorList.size() > 0) {
            for (Interceptor interceptor : interceptorList) {
                if (!okHttpClientBuilder.interceptors().contains(interceptor))
                    okHttpClientBuilder.addInterceptor(interceptor);
            }
        }
        if (networkInterceptorList != null && networkInterceptorList.size() > 0) {
            for (Interceptor interceptor : networkInterceptorList) {
                if (!okHttpClientBuilder.interceptors().contains(interceptor))
                    okHttpClientBuilder.addNetworkInterceptor(interceptor);
            }
        }
        return okHttpClientBuilder;
    }


    public OkHttpClient getOkHttpClient() {
        return getOkHttpClientBuilder().build();
    }

    public Retrofit.Builder getRetrofitBuilder() {
        return retrofitBuilder;
    }

    public Retrofit getRetrofit() {
        return retrofitBuilder.build();
    }

    public static TemplatePostRequest post(String url) {
        return new TemplatePostRequest(url);
    }

    public static ApiResultPostRequest resultPost(String url) {
        return new ApiResultPostRequest(url);
    }
}
