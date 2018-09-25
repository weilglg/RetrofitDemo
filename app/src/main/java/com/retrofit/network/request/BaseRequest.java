package com.retrofit.network.request;

import android.content.Context;

import com.retrofit.network.ApiManager;
import com.retrofit.network.RxHttp;
import com.retrofit.network.util.Util;
import com.retrofit.network.interceptor.BaseDynamicInterceptor;
import com.retrofit.network.interceptor.HeaderInterceptor;
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

import io.reactivex.Observable;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

@SuppressWarnings(value = {"unchecked", "deprecation"})
public abstract class BaseRequest<R extends BaseRequest> {
    protected String mUrl;                                               //请求Url
    private Cache mCache;                                              //OkHttp缓存对象
    private File mCacheFile;                                           //缓存目录
    private long mCacheMaxSize;                                       //最大缓存
    private Proxy mProxy;                                              //OkHttp代理
    private HostnameVerifier mHostnameVerifier;                        //https的全局访问规则
    private Converter.Factory mConverterFactory;                       //Converter.Factory
    private CallAdapter.Factory mCallAdapterFactory;                   //CallAdapter.Factory
    private SSLSocketFactory mSslSocketFactory;                        //签名验证规则
    private X509TrustManager mTrustManager;
    private SSLUtil.SSLParams mSslParams;                              //https签名证书
    private CookieJar mCookieJar;                                      //Cookie管理
    private ConnectionPool mConnectionPool;                            //链接池管理
    private Map<String, String> mHeaders = new HashMap<>();            //公共请求头
    private Map<String, String> mParameters = new HashMap<>();         //公共请求参数
    private OkHttpClient mHttpClient;                                  //自定义OkHttpClient
    private int mReadTimeout;                                          //读超时
    private int mWriteTimeout;                                         //写超时
    private int mConnectTimeout;                                       //链接超时
    protected int mRetryCount;                    //重试次数默认3次
    protected int mRetryDelay;                    //延迟xxms重试
    protected int mRetryIncreaseDelay;    //叠加延迟
    private List<Interceptor> mInterceptorList = new ArrayList<>();
    private List<Interceptor> mNetworkInterceptorList = new ArrayList<>();
    protected Context mContext;
    private String mBaseUrl;
    private boolean isSign = false;
    private boolean accessToken = false;
    protected boolean isSyncRequest = false;
    private Retrofit mRetrofit;
    protected ApiManager mApiManager;
    private boolean isOperationHeader = false;


    public BaseRequest(String url) {
        this.mUrl = url;
        this.mContext = RxHttp.getInstance().getContext();
        if (mBaseUrl == null && url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
            HttpUrl httpUrl = HttpUrl.parse(url);
            if (httpUrl != null)
                mBaseUrl = httpUrl.url().getProtocol() + "://" + httpUrl.url().getHost() + "/";
        }
    }

    public R baseUrl(String baseUrl) {
        this.mBaseUrl = Util.checkNotNull(baseUrl, "baseUrl is null");
        return (R) this;
    }

    public R isAccessToken(boolean accessToken) {
        this.accessToken = accessToken;
        return (R) this;
    }

    public R isSyncRequest(boolean isSyncRequest) {
        this.isSyncRequest = isSyncRequest;
        return (R) this;
    }


    public R cache(Cache cache) {
        this.mCache = Util.checkNotNull(cache, "cache is null");
        return (R) this;
    }

    public R cacheFile(File cacheFile) {
        this.mCacheFile = Util.checkNotNull(cacheFile, "cacheFile is null");
        return (R) this;
    }

    public R okProxy(Proxy proxy) {
        this.mProxy = Util.checkNotNull(proxy, "proxy is null");
        ;
        return (R) this;
    }

    public R hostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.mHostnameVerifier = Util.checkNotNull(hostnameVerifier, "hostnameVerifier is null");
        return (R) this;
    }

    public R converterFactory(Converter.Factory converterFactory) {
        this.mConverterFactory = Util.checkNotNull(converterFactory, "converterFactory is null");
        return (R) this;
    }

    public R callAdapterFactory(CallAdapter.Factory callAdapterFactory) {
        this.mCallAdapterFactory = Util.checkNotNull(callAdapterFactory, "callAdapterFactory is null");
        return (R) this;
    }

    public R sslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.mSslSocketFactory = Util.checkNotNull(sslSocketFactory, "sslSocketFactory is null");
        return (R) this;
    }

    public R trustManager(X509TrustManager trustManager) {
        this.mTrustManager = Util.checkNotNull(trustManager, "trustManager is null");
        return (R) this;
    }

    public R cookieJar(CookieJar cookieJar) {
        this.mCookieJar = Util.checkNotNull(cookieJar, "cookieJar is null");
        return (R) this;
    }

    public R connectionPool(ConnectionPool connectionPool) {
        this.mConnectionPool = Util.checkNotNull(connectionPool, "connectionPool is null");
        return (R) this;
    }

    public R headers(Map<String, String> headers) {
        isOperationHeader = true;
        this.mHeaders.putAll(Util.checkNotNull(headers, "headers is null"));
        return (R) this;
    }

    public R addHeader(String key, String value){
        this.mHeaders.put(key, value);
        isOperationHeader = true;
        return (R) this;
    }

    public R removeHeader(String key){
        isOperationHeader = true;
        return (R) this;
    }

    public R parameters(Map<String, String> parameters) {
        this.mParameters.putAll(Util.checkNotNull(parameters, "param is null"));
        return (R) this;
    }

    public R httpClient(OkHttpClient httpClient) {
        this.mHttpClient = Util.checkNotNull(httpClient, "OkHttpClient is null");
        return (R) this;
    }

    public R readTimeOut(int readTimeout) {
        if (mReadTimeout < 0)
            throw new IllegalArgumentException("readTimeout must > 0");
        this.mReadTimeout = readTimeout;
        return (R) this;
    }

    public R writeTimeOut(int writeTimeout) {
        if (mWriteTimeout < 0)
            throw new IllegalArgumentException("writeTimeout must > 0");
        this.mWriteTimeout = writeTimeout;
        return (R) this;
    }

    public R connectTimeOut(int connectTimeout) {
        if (mConnectTimeout < 0)
            throw new IllegalArgumentException("connectTimeout must > 0");
        this.mConnectTimeout = connectTimeout;
        return (R) this;
    }

    public R interceptorList(List<Interceptor> interceptorList) {
        this.mInterceptorList.addAll(Util.checkNotNull(interceptorList, "Interceptor is null"));
        return (R) this;
    }

    public R addInterceptor(Interceptor interceptor) {
        this.mInterceptorList.add(Util.checkNotNull(interceptor, "Interceptor is null"));
        return (R) this;
    }

    public R networkInterceptorList(List<Interceptor> networkInterceptorList) {
        this.mNetworkInterceptorList.addAll(Util.checkNotNull(networkInterceptorList, "NetworkInterceptor is null"));
        return (R) this;
    }

    public R addNetworkInterceptor(Interceptor interceptor) {
        this.mNetworkInterceptorList.add(Util.checkNotNull(interceptor, "NetworkInterceptor is null"));
        return (R) this;
    }

    public R isSign(boolean isSign) {
        this.isSign = isSign;
        return (R) this;
    }

    public R retryCount(int mRetryCount) {
        if (mRetryCount < 0)
            throw new IllegalArgumentException("retryIncreaseDelay must > 0");
        this.mRetryCount = mRetryCount;
        return (R) this;
    }

    public R retryDelay(int mRetryDelay) {
        if (mRetryDelay < 0)
            throw new IllegalArgumentException("retryIncreaseDelay must > 0");
        this.mRetryDelay = mRetryDelay;
        return (R) this;
    }

    public R retryIncreaseDelay(int mRetryIncreaseDelay) {
        if (mRetryIncreaseDelay < 0)
            throw new IllegalArgumentException("retryIncreaseDelay must > 0");
        this.mRetryIncreaseDelay = mRetryIncreaseDelay;
        return (R) this;
    }

    public R certificates(InputStream... certificates) {
        this.mSslParams = SSLUtil.getSslSocketFactory(null, null, certificates);
        return (R) this;
    }

    public R certificates(InputStream bksFile, String password, InputStream... certificates) {
        this.mSslParams = SSLUtil.getSslSocketFactory(bksFile, password, certificates);
        return (R) this;
    }

    public R cacheMaxSize(long cacheMaxSize){
        this.mCacheMaxSize = cacheMaxSize;
        return (R) this;
    }

    private OkHttpClient.Builder generateOkHttpClientBuilder() {
        if (mReadTimeout <= 0 && mWriteTimeout <= 0 && mConnectTimeout <= 0 && mSslParams == null
                && mCookieJar == null && mCache == null && mCacheFile == null && mCacheMaxSize <= 0
                && mInterceptorList.size() > 0 && mNetworkInterceptorList.size() > 0 && mProxy == null
                && mSslSocketFactory == null && mTrustManager == null && mHostnameVerifier == null
                && mCallAdapterFactory == null && mConverterFactory == null) {
            OkHttpClient.Builder builder = RxHttp.getInstance().getOkHttpClientBuilder();
            for (Interceptor interceptor : builder.interceptors()) {
                if (interceptor instanceof BaseDynamicInterceptor) {
                    ((BaseDynamicInterceptor) interceptor).sign(isSign).accessToken(accessToken);
                }
            }
            return builder;
        } else {
            OkHttpClient.Builder builder = RxHttp.getInstance().getOkHttpClient().newBuilder();
            if (mReadTimeout > 0) {
                builder.readTimeout(mReadTimeout, TimeUnit.SECONDS);
            }
            if (mWriteTimeout > 0) {
                builder.writeTimeout(mWriteTimeout, TimeUnit.SECONDS);
            }
            if (mConnectTimeout > 0) {
                builder.connectTimeout(mConnectTimeout, TimeUnit.SECONDS);
            }

            if (mSslSocketFactory != null) {
                if (mTrustManager == null) {
                    builder.sslSocketFactory(mSslSocketFactory);
                } else {
                    builder.sslSocketFactory(mSslSocketFactory, mTrustManager);
                }
            } else if (mSslParams != null) {
                builder.sslSocketFactory(mSslParams.sSLSocketFactory, mSslParams.trustManager);
            }
            if (mHostnameVerifier != null) {
                builder.hostnameVerifier(mHostnameVerifier);
            }
            if (mCacheFile == null) {
                mCacheFile = new File(mContext.getCacheDir(), "retrofit_http_cache");
            }
            if (mCache == null && mCacheFile != null) {
                mCache = new Cache(mCacheFile, Math.max(5 * 1024 * 1024, mCacheMaxSize));
            }
            if (mCache != null) {
                builder.cache(mCache);
            }
            if (mConnectionPool != null) {
                builder.connectionPool(mConnectionPool);
            }
            if (mProxy != null) {
                builder.proxy(mProxy);
            }
            if (mCookieJar != null) {
                builder.cookieJar(mCookieJar);
            }
            if (mInterceptorList.size() > 0) {
                for (Interceptor interceptor : mInterceptorList) {
                    builder.addInterceptor(interceptor);
                }
            }
            for (Interceptor interceptor : builder.interceptors()) {
                if (interceptor instanceof BaseDynamicInterceptor) {
                    ((BaseDynamicInterceptor) interceptor).sign(isSign).accessToken(accessToken);
                } else if (interceptor instanceof HeaderInterceptor && mHeaders.size() > 0) {
                    ((HeaderInterceptor) interceptor).setMap(mHeaders);
                }
            }
            if (mNetworkInterceptorList.size() > 0) {
                for (Interceptor interceptor : mNetworkInterceptorList) {
                    builder.addNetworkInterceptor(interceptor);
                }
            }
            return builder;
        }

    }

    private Retrofit.Builder generateRetrofitBuilder() {
        RxHttp rxHttp = RxHttp.getInstance();
        if (mBaseUrl == null || mBaseUrl.equals(rxHttp.getBaseUrl()) && mConverterFactory == null
                && mCallAdapterFactory == null && mHttpClient == null && rxHttp.getHttpClient() == null) {
            return rxHttp.getRetrofitBuilder();
        } else {
            Retrofit.Builder builder = rxHttp.getRetrofit().newBuilder();
            if (mBaseUrl != null) {
                builder.baseUrl(mBaseUrl);
            }
            if (mCallAdapterFactory != null) {
                builder.addCallAdapterFactory(mCallAdapterFactory);
            }
            if (mConverterFactory != null) {
                builder.addConverterFactory(mConverterFactory);
            }

            return builder;
        }
    }

    protected R build() {
        OkHttpClient.Builder okHttpClientBuilder = generateOkHttpClientBuilder();
        Retrofit.Builder retrofitBuilder = generateRetrofitBuilder();
        if (mHttpClient != null) {
            retrofitBuilder.client(mHttpClient);
        } else if (RxHttp.getInstance().getHttpClient() != null) {
            retrofitBuilder.client(RxHttp.getInstance().getHttpClient());
        } else {
            retrofitBuilder.client(okHttpClientBuilder.build());
        }
        mRetrofit = retrofitBuilder.build();
        mApiManager = mRetrofit.create(ApiManager.class);
        return (R) this;
    }

    protected abstract Observable<ResponseBody> generateRequest();

}
