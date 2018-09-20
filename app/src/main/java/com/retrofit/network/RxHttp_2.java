package com.retrofit.network;


import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory;
import com.retrofit.network.cookie.CookieCacheImpl;
import com.retrofit.network.cookie.CookieManager;
import com.retrofit.network.cookie.SharedPrefsCookiePersistor;
import com.retrofit.network.entity.HttpConfigEntity;
import com.retrofit.network.exception.ExceptionFactory;
import com.retrofit.network.interceptor.HeaderInterceptor;
import com.retrofit.network.subscribe.ResponseCallback;
import com.retrofit.network.subscribe.RxSubscribe;

import java.io.File;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
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

@SuppressWarnings("unchecked")
public class RxHttp_2 {

    private static final String TAG = RxHttp_2.class.getSimpleName();
    public static final int DEFAULT_MILLISECONDS = 60000;             //默认的超时时间
    private static final int DEFAULT_RETRY_COUNT = 3;                 //默认重试次数
    private static final int DEFAULT_RETRY_INCREASEDELAY = 0;         //默认重试叠加时间
    private static final int DEFAULT_RETRY_DELAY = 500;               //默认重试延时
    public static final int DEFAULT_CACHE_NEVER_EXPIRE = -1;          //缓存过期时间，默认永久缓存

    private static RxHttp_2 mInstance;
    private static OkHttpClient.Builder okhttpBuilder;
    private static Retrofit.Builder retrofitBuilder;
    private static Retrofit retrofit;
    private HttpConfigEntity defaultConfig;
    private HttpConfigEntity useConfig;

    private void init(HttpConfigEntity configEntity) {
        HttpConfigEntity entity = Util.copyConfig(configEntity);
        if (defaultConfig == null) {
            defaultConfig = entity;
        }
        useConfig = entity;
    }


    private RxHttp_2() {

    }

    public static RxHttp_2 getInstance() {
        if (mInstance == null) {
            synchronized (RxHttp_2.class) {
                if (mInstance == null) {
                    mInstance = new RxHttp_2();
                }
            }
        }
        return mInstance;
    }

    /**
     * 创建{@link Builder}
     */
    public static Builder createBuilder(Context context) {
        return new Builder(context);
    }

    /**
     * 创建新的配置{@link Builder}
     */
    public static Builder createNewBuilder() {
        return mInstance.newBuilder();
    }

    /**
     * 重置为默认的配置{@link Builder}
     */
    public static Builder restoreDefaultBuilder() {
        return mInstance.defaultBuilder();
    }


    public <T> T create(Class<T> clazz) {
        return retrofit.create(clazz);
    }


    public <T> DisposableObserver rxHttp(Observable<T> observable, Observer<T> subscriber) {
        return (DisposableObserver) observable.compose(schedulersTransformer)
                .compose(exceptTransformer)
                .subscribeWith(subscriber);
    }

    public <T> DisposableObserver rxHttp(Object tag, Observable<T> observable, ResponseCallback<T> callback) {
        return (DisposableObserver) observable.compose(schedulersTransformer)
                .compose(exceptTransformer)
                .subscribeWith(new RxSubscribe(callback, tag));
    }

    private final ObservableTransformer exceptTransformer = new ObservableTransformer() {
        @Override
        public ObservableSource apply(Observable observable) {
            return observable.onErrorResumeNext(new HttpResponseFunc());
        }
    };

    /**
     * 对Observable进行转换添加线程切换
     */
    private final ObservableTransformer schedulersTransformer = new ObservableTransformer() {
        @Override
        public ObservableSource apply(Observable upstream) {
            return upstream.subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    };

    /**
     * HttpResponseFunc
     */
    private static class HttpResponseFunc implements Function<Throwable, Observable> {

        @Override
        public Observable apply(Throwable throwable) throws Exception {
            return Observable.error(ExceptionFactory.handleException(throwable));
        }
    }


    private Builder newBuilder() {
        if (useConfig == null) {
            throw new IllegalStateException("No builder config");
        }
        return new Builder(useConfig);
    }

    private Builder defaultBuilder() {
        if (defaultConfig == null) {
            throw new IllegalStateException("No default config");
        }
        return new Builder(defaultConfig);
    }


    public static class Builder {
        private static final int DEFAULT_TIMEOUT = 15;
        private static final int DEFAULT_MAXIDLE_CONNECTIONS = 5;
        private static final long DEFAULT_KEEP_ALIVEDURATION = 8;
        private static final long CACHE_MAX_SIZE = 10 * 1024 * 1024;
        private HttpConfigEntity configEntity;
        private Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR;
        private Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR_OFFLINE;

        private Builder(Context context) {
            if (context == null)
                throw new RuntimeException("Context is not null");
            configEntity = new HttpConfigEntity();
            configEntity.readTimeOut = DEFAULT_TIMEOUT;
            configEntity.connectTimeOut = DEFAULT_TIMEOUT;
            if (context instanceof Activity) {
                configEntity.context = ((Activity) context).getApplicationContext();
            } else {
                configEntity.context = context;
            }
            okhttpBuilder = new OkHttpClient.Builder();
            retrofitBuilder = new Retrofit.Builder();
        }


        private Builder(HttpConfigEntity useConfig) {
            this.configEntity = Util.copyConfig(useConfig);
        }

        /**
         * {@link Retrofit.Builder#baseUrl(String)}
         */
        public Builder baseUrl(String baseUrl) {
            configEntity.baseUrl = baseUrl;
            return this;
        }

        /**
         * 上下文{@link Context}
         */
        public Builder context(Context context) {
            configEntity.context = context;
            if (context == null) {
                throw new RuntimeException("Context is null");
            }
            return this;
        }

        /**
         * 是否开启log日志
         */
        public Builder isLog(boolean log) {
            configEntity.isLog = log;
            return this;
        }

        /**
         * 标识
         */
        public Builder tag(Object tag) {
            configEntity.tag = tag;
            return this;
        }

        public Builder cookie(boolean cookie) {
            configEntity.isCookie = cookie;
            return this;
        }

        /**
         * 是否开启缓存
         */
        public Builder isCache(boolean cache) {
            configEntity.isCache = cache;
            return this;
        }

        /**
         * 缓存
         * {@link OkHttpClient.Builder#cache(Cache)}
         */
        public Builder cache(Cache cache) {
            configEntity.cache = cache;
            return this;
        }

        /**
         * 代理Proxy
         * {@link OkHttpClient.Builder#proxy(Proxy)}
         */
        public Builder proxy(Proxy proxy) {
            configEntity.proxy = proxy;
            return this;
        }

        /**
         * 解析器Converter.Factory
         * {@link Retrofit.Builder#addConverterFactory(Converter.Factory)}
         */
        public Builder converterFactory(Converter.Factory converterFactory) {
            configEntity.converterFactory = converterFactory;
            return this;
        }

        /**
         * 适配器CallAdapter.Factory
         * {@link Retrofit.Builder#addCallAdapterFactory(CallAdapter.Factory)}
         */
        public Builder callAdapterFactory(CallAdapter.Factory callAdapterFactory) {
            configEntity.callAdapterFactory = callAdapterFactory;
            return this;
        }

        /**
         * 证书认证SSLSocketFactory
         * {@link OkHttpClient.Builder#sslSocketFactory(SSLSocketFactory)}, {@link OkHttpClient.Builder#sslSocketFactory(SSLSocketFactory, X509TrustManager)}
         */
        public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
            configEntity.sslSocketFactory = sslSocketFactory;
            return this;
        }

        /**
         * 证书认证
         * {@link OkHttpClient.Builder#sslSocketFactory(SSLSocketFactory, X509TrustManager)}
         */
        public Builder trustManager(X509TrustManager x509TrustManager) {
            configEntity.trustManager = x509TrustManager;
            return this;
        }

        /**
         * Cookie管理
         * {@link OkHttpClient.Builder#cookieJar(CookieJar)}
         */
        public Builder cookieJar(CookieJar cookieJar) {
            configEntity.cookieJar = cookieJar;
            return this;
        }

        /**
         * 连接池
         * {@link OkHttpClient.Builder#connectionPool(ConnectionPool)}
         */
        public Builder connectionPool(ConnectionPool connectionPool) {
            configEntity.connectionPool = connectionPool;
            return this;
        }

        /**
         * 缓存File
         */
        public Builder cacheFile(File file) {
            configEntity.cacheFile = file;
            return this;
        }

        /**
         * 缓存文件路径
         */
        public Builder cachePath(String path) {
            try {
                configEntity.cacheFile = new File(path);
            } catch (Exception e) {
                Log.e(TAG, "Cache file path is empty", e);
            }
            return this;
        }

        /**
         * {@link OkHttpClient.Builder#hostnameVerifier(HostnameVerifier)}
         */
        public Builder hostnameVerifier(HostnameVerifier hostnameVerifier) {
            configEntity.hostnameVerifier = hostnameVerifier;
            return this;
        }

        /**
         * 添加请求Header，通过{@link HeaderInterceptor}完成添加
         * {@link OkHttpClient.Builder#addInterceptor(Interceptor)}
         */
        public Builder addHeader(Map<String, String> headers) {
            configEntity.headers = headers;
            return this;
        }

        /**
         * 添加参数，通过{@link HeaderInterceptor}完成添加
         * {@link OkHttpClient.Builder#addInterceptor(Interceptor)}
         */
        public Builder addParameters(Map<String, String> parameters) {
            configEntity.parameters = parameters;
            return this;
        }

        /**
         * 自定义OKHttpClient
         * {@link Retrofit.Builder#client(OkHttpClient)}
         */
        public Builder httpClient(OkHttpClient httpClient) {
            configEntity.httpClient = httpClient;
            return this;
        }

        /**
         * 读写超时时间
         * {@link OkHttpClient.Builder#readTimeout(long, TimeUnit)}
         * {@link OkHttpClient.Builder#writeTimeout(long, TimeUnit)}
         */
        public Builder readTimeOut(int readTimeOut) {
            configEntity.readTimeOut = readTimeOut;
            return this;
        }

        /**
         * 连接超时时间
         * {@link OkHttpClient.Builder#connectTimeout(long, TimeUnit)}
         */
        public Builder connectTimeOut(int connectTimeOut) {
            configEntity.connectTimeOut = connectTimeOut;
            return this;
        }

        /**
         * 添加Interceptor
         * {@link OkHttpClient.Builder#addInterceptor(Interceptor)}
         */
        public Builder addInterceptor(Interceptor interceptor) {
            if (configEntity.interceptorList == null) {
                configEntity.interceptorList = new ArrayList<>();
            }
            configEntity.interceptorList.add(interceptor);
            return this;
        }

        /**
         * 添加Interceptor
         * {@link OkHttpClient.Builder#addNetworkInterceptor(Interceptor)}
         */
        public Builder addNetworkInterceptor(Interceptor interceptor) {
            if (configEntity.networkInterceptorList == null) {
                configEntity.networkInterceptorList = new ArrayList<>();
            }
            configEntity.networkInterceptorList.add(interceptor);
            return this;
        }

        /**
         * 删除Interceptor
         */
        public Builder removeInterceptor(Interceptor interceptor) {
            if (configEntity.interceptorList != null) {
                Iterator<Interceptor> iterators = configEntity.interceptorList.iterator();
                while (iterators.hasNext()) {
                    if (iterators.next() == interceptor) {
                        iterators.remove();
                    }
                }
            }
            return this;
        }

        /**
         * 删除某一个Interceptor
         */
        public Builder removeNetworkInterceptor(Interceptor interceptor) {
            if (configEntity.networkInterceptorList != null) {
                Iterator<Interceptor> iterators = configEntity.networkInterceptorList.iterator();
                while (iterators.hasNext()) {
                    if (iterators.next() == interceptor) {
                        iterators.remove();
                    }
                }
            }
            return this;
        }

        /**
         * 设置是否使用初始化时的配置项
         */
        public Builder useDefault(boolean isUseDefault) {
            configEntity.isUseDefault = isUseDefault;
            return this;
        }

        /**
         * 设置OKHttpClient的缓存
         *
         * @param cache
         * @return
         */
        public Builder addCache(Cache cache) {
            int maxStale = 60 * 60 * 24 * 3;
            return addCache(cache, maxStale);
        }

        public Builder addCache(Cache cache, final int cacheTime) {
            addCache(cache, String.format("max-age=%d", cacheTime));
            return this;
        }

        public Builder addCache(Cache cache, final String cacheControlValue) {
            REWRITE_CACHE_CONTROL_INTERCEPTOR = new CacheInterceptor(configEntity.context, cacheControlValue);
            REWRITE_CACHE_CONTROL_INTERCEPTOR_OFFLINE = new CacheInterceptorOffline(configEntity.context, cacheControlValue);
            addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR);
            addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR_OFFLINE);
            addInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR_OFFLINE);
            configEntity.cache = cache;
            return this;
        }

        public synchronized RxHttp_2 build() {
            if (Util.isEmpty(configEntity.baseUrl)) {
                throw new IllegalStateException("Base URL required.");
            }
            if (okhttpBuilder == null) {
                throw new IllegalStateException("okhttpBuilder required.");
            }

            if (retrofitBuilder == null) {
                throw new IllegalStateException("retrofitBuilder required.");
            }

            if (configEntity.context == null) {
                throw new IllegalStateException("context required.");
            }


            // 设置baseUrl
            retrofitBuilder.baseUrl(configEntity.baseUrl);

            // 设置解析器
            if (configEntity.converterFactory == null) {
                configEntity.converterFactory = new Retrofit2ConverterFactory();
            }
            retrofitBuilder.addConverterFactory(configEntity.converterFactory);

            // 设置适配器
            if (configEntity.callAdapterFactory == null) {
                configEntity.callAdapterFactory = RxJava2CallAdapterFactory.create();
            }
            retrofitBuilder.addCallAdapterFactory(configEntity.callAdapterFactory);

            // 设置标识
            if (configEntity.tag != null) {
                okhttpBuilder.addInterceptor(new RequestInterceptor(configEntity.tag));
            }

            if (configEntity.isLog) {
                addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS));
                addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
            }

            // SSL认证
            if (configEntity.sslSocketFactory != null) {
                if (configEntity.trustManager == null) {
                    okhttpBuilder.sslSocketFactory(configEntity.sslSocketFactory);
                } else {
                    okhttpBuilder.sslSocketFactory(configEntity.sslSocketFactory, configEntity.trustManager);
                }
            }

            if (configEntity.hostnameVerifier != null) {
                okhttpBuilder.hostnameVerifier(configEntity.hostnameVerifier);
            }

            // 请求数据缓存
            if (configEntity.cacheFile == null) {
                configEntity.cacheFile = new File(configEntity.context.getCacheDir(), "retrofit_http_cache");
            }

            if (configEntity.isCache) {
                try {
                    if (configEntity.cache == null) {
                        configEntity.cache = new Cache(configEntity.cacheFile, CACHE_MAX_SIZE);
                    }
                    addCache(configEntity.cache);
                } catch (Exception e) {
                    Log.e("OKHttp", "Could not create http cache", e);
                }
            }
            if (configEntity.cache != null) {
                okhttpBuilder.cache(configEntity.cache);
            }

            if (configEntity.connectionPool == null) {
                configEntity.connectionPool = new ConnectionPool(DEFAULT_MAXIDLE_CONNECTIONS, DEFAULT_KEEP_ALIVEDURATION, TimeUnit.SECONDS);
            }


            if (configEntity.proxy != null) {
                okhttpBuilder.proxy(configEntity.proxy);
            }

            // 设置Cookie管理
            if (configEntity.isCookie && configEntity.cookieJar == null) {
                configEntity.cookieJar = new CookieManager(new CookieCacheImpl(), new SharedPrefsCookiePersistor(configEntity.context));
            }
            if (configEntity.cookieJar != null) {
                okhttpBuilder.cookieJar(configEntity.cookieJar);
            }

            if (configEntity.headers != null && configEntity.headers.size() > 0) {
                okhttpBuilder.addInterceptor(new HeaderInterceptor(configEntity.headers));
            }

            if (configEntity.parameters != null && configEntity.parameters.size() > 0) {
                okhttpBuilder.addInterceptor(new HeaderInterceptor(configEntity.headers));
            }

            if (configEntity.interceptorList != null && configEntity.interceptorList.size() > 0) {
                for (Interceptor interceptor : configEntity.interceptorList) {
                    okhttpBuilder.addInterceptor(interceptor);
                }
            }

            if (configEntity.networkInterceptorList != null && configEntity.networkInterceptorList.size() > 0) {
                for (Interceptor interceptor : configEntity.networkInterceptorList) {
                    okhttpBuilder.addNetworkInterceptor(interceptor);
                }
            }

            OkHttpClient okhttpClient = okhttpBuilder.build();

            retrofitBuilder.client(okhttpClient);

            retrofit = retrofitBuilder.build();

            if (mInstance == null) {
                mInstance = new RxHttp_2();
            }
            mInstance.init(configEntity);

            Log.e("TAG", "------>>>ConfigEntity === " + configEntity.toString());

            return mInstance;
        }
    }

}
