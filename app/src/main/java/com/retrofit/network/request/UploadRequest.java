package com.retrofit.network.request;

import android.text.TextUtils;

import com.retrofit.network.ApiManager;
import com.retrofit.network.RxHttp;
import com.retrofit.network.entity.UploadFileType;
import com.retrofit.network.callback.ResultCallback;
import com.retrofit.network.callback.ResultCallbackProxy;
import com.retrofit.network.callback.ResultClazzCallProxy;
import com.retrofit.network.callback.ResultProgressCallback;
import com.retrofit.network.entity.ApiResultEntity;
import com.retrofit.network.entity.FileEntity;
import com.retrofit.network.func.ApiResultFunc;
import com.retrofit.network.func.RetryExceptionFunc;
import com.retrofit.network.interceptor.ProgressRequestInterceptor;
import com.retrofit.network.subscriber.ResultCallbackSubscriber;
import com.retrofit.network.util.RxUtil;
import com.retrofit.network.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import retrofit2.Retrofit;

@SuppressWarnings(value = {"unchecked", "deprecation"})
public class UploadRequest extends BaseRequest<UploadRequest> {

    private RequestBody mRequestBody;
    private UploadFileType mUploadType;

    public UploadRequest(String url) {
        super(url);
    }

    public UploadRequest requestBody(RequestBody requestBody) {
        this.mRequestBody = requestBody;
        return this;
    }

    public UploadRequest params(String key, File file) {
        mHttpParams.put(key, file);
        return this;
    }

    public UploadRequest params(String key, String fileName, InputStream stream) {
        mHttpParams.put(key, fileName, stream);
        return this;
    }

    public UploadRequest params(String key, String fileName, byte[] bytes) {
        mHttpParams.put(key, fileName, bytes);
        return this;
    }

    public UploadRequest addFileParams(String key, List<File> files) {
        if (TextUtils.isEmpty(key) && files != null && !files.isEmpty()) {
            for (File file : files) {
                params(key, file);
            }
        }
        return this;
    }

    public UploadRequest addFileWrapperParams(String key, List<FileEntity> fileWrappers) {
        mHttpParams.put(key, fileWrappers);
        return this;
    }

    public UploadRequest params(String key, File file, String fileName) {
        mHttpParams.put(key, file, fileName);
        return this;
    }

    public <T> UploadRequest params(String key, String fileName, T file, MediaType contentType) {
        mHttpParams.put(key, fileName, file, contentType);
        return this;
    }

    public UploadRequest uploadType(UploadFileType type) {
        this.mUploadType = type;
        return this;
    }


    protected <T> UploadRequest build(Object tag, ResultProgressCallback callback) {
        OkHttpClient.Builder okHttpClientBuilder = generateOkHttpClientBuilder();
        okHttpClientBuilder.addInterceptor(new ProgressRequestInterceptor(tag, callback));
        Retrofit.Builder retrofitBuilder = generateRetrofitBuilder();
        if (mHttpClient != null) {
            retrofitBuilder.client(mHttpClient);
        } else if (RxHttp.getInstance().getHttpClient() != null) {
            retrofitBuilder.client(RxHttp.getInstance().getHttpClient());
        } else {
            retrofitBuilder.client(okHttpClientBuilder.build());
        }
        Retrofit mRetrofit = retrofitBuilder.build();
        mApiManager = mRetrofit.create(ApiManager.class);
        return build();
    }

    @Override
    protected Observable<ResponseBody> generateRequest() {
        Util.checkNotNull(mUploadType, "UploadType is null");
        if (mUploadType == UploadFileType.BODY_MAP) {
            return uploadFilesWithBodyMap();
        } else if (mUploadType == UploadFileType.PART_FROM) {
            return uploadFilesWithPartList();
        } else if (mUploadType == UploadFileType.PART_MAP) {
            return uploadFilesWithPartMap();
        } else {
            return mApiManager.postBody(mUrl, mRequestBody);
        }
    }


    public <T> Observable<T> execute(Class<T> clazz, ResultProgressCallback<T> callback) {
        return execute(new ResultClazzCallProxy<ApiResultEntity<T>, T>(clazz) {
        }, callback);
    }

    public <T> Observable<T> execute(Type type, ResultProgressCallback<T> callback) {
        return execute(new ResultClazzCallProxy<ApiResultEntity<T>, T>(type) {
        }, callback);
    }

    public <T> Observable<T> execute(ResultClazzCallProxy<? extends ApiResultEntity<T>, T> proxy, ResultProgressCallback<T> callback) {
        return build(null, callback).generateRequest()
                .map(new ApiResultFunc(proxy.getType()))
                .compose(isSyncRequest ? RxUtil._io_main_result() : RxUtil._main_result())
                .retryWhen(new RetryExceptionFunc(mRetryCount, mRetryDelay, mRetryIncreaseDelay));
    }

    public <T> Disposable execute(Object tag, ResultCallback<T> callback) {
        return execute(tag, ResultCallbackProxy.NEW_DEFAULT_INSTANCE(callback));
    }

    public <T> Disposable execute(Object tag, ResultCallbackProxy<? extends ApiResultEntity<T>, T> proxy) {
        Observable<T> observable = build(tag, (ResultProgressCallback) proxy.getCallback()).generateObservable(generateRequest(), proxy);
        return observable.subscribeWith(new ResultCallbackSubscriber<T>(tag, proxy.getCallback()));
    }

    private <T> Observable<T> generateObservable(Observable observable, ResultCallbackProxy<? extends ApiResultEntity<T>, T> proxy) {
        return observable.map(new ApiResultFunc(proxy.getType()))
                .compose(isSyncRequest ? RxUtil._io_main_result() : RxUtil._main_result())
                .retryWhen(new RetryExceptionFunc(mRetryCount, mRetryDelay, mRetryIncreaseDelay));
    }


    /**
     * 以RequestBody的Map形式提交
     *
     * @return
     */
    private Observable<ResponseBody> uploadFilesWithBodyMap() {
        Map<String, RequestBody> mBodyMap = new HashMap<>();
        //拼接参数键值对
        for (Map.Entry<String, String> mapEntry : mHttpParams.getParamMap().entrySet()) {
            RequestBody body = RequestBody.create(MediaType.parse("text/plain"), mapEntry.getValue());
            mBodyMap.put(mapEntry.getKey(), body);
        }
        //拼接文件
        for (Map.Entry<String, List<FileEntity>> entry : mHttpParams.getFileMap().entrySet()) {
            List<FileEntity> fileValues = entry.getValue();
            for (FileEntity fileWrapper : fileValues) {
                RequestBody requestBody = getRequestBody(fileWrapper);
                mBodyMap.put(entry.getKey(), requestBody);
            }
        }
        return mApiManager.uploadFileWithBodyMap(mUrl, mBodyMap);
    }

    /**
     * 以MultipartBody.Part的List形式提交
     *
     * @return
     */
    private Observable<ResponseBody> uploadFilesWithPartList() {
        List<MultipartBody.Part> partList = new ArrayList<>();
        HashMap<String, String> paramMap = mHttpParams.getParamMap();
        for (String key : paramMap.keySet()) {
            partList.add(MultipartBody.Part.createFormData(key, paramMap.get(key)));
        }
        for (Map.Entry<String, List<FileEntity>> fileEntity : mHttpParams.getFileMap().entrySet()) {
            for (FileEntity entity : fileEntity.getValue()) {
                MultipartBody.Part part = createPartBody(fileEntity.getKey(), entity);
                partList.add(part);
            }
        }
        return mApiManager.uploadFileWithPartList(mUrl, partList);
    }

    /**
     * 以MultipartBody.Part的Map形式提交
     *
     * @return
     */
    private Observable<ResponseBody> uploadFilesWithPartMap() {
        Map<String, MultipartBody.Part> partMap = new HashMap<>();
        //拼接普通参数
        HashMap<String, String> paramMap = mHttpParams.getParamMap();
        for (String key : paramMap.keySet()) {
            partMap.put(key, MultipartBody.Part.createFormData(key, paramMap.get(key)));
        }
        //拼接文件参数
        HashMap<String, List<FileEntity>> fileMap = mHttpParams.getFileMap();
        for (Map.Entry<String, List<FileEntity>> entitySet : fileMap.entrySet()) {
            String key = entitySet.getKey();
            for (FileEntity entity : entitySet.getValue()) {
                MultipartBody.Part part = createPartBody(key, entity);
                partMap.put(key, part);
            }
        }
        return mApiManager.uploadFileWithPartMap(mUrl, partMap);
    }

    private MultipartBody.Part createPartBody(String key, FileEntity value) {
        RequestBody requestBody = getRequestBody(value);
        Util.checkNotNull(requestBody, "requestBody==null fileEntity.data must is File/InputStream/byte[]");
        return MultipartBody.Part.createFormData(key, value.getFileName(), requestBody);
    }

    private RequestBody getRequestBody(FileEntity value) {
        RequestBody requestBody = null;
        if (value.getData() instanceof File) {
            requestBody = RequestBody.create(value.getMediaType(), (File) value.getData());
        } else if (value.getData() instanceof InputStream) {
            requestBody = create(value.getMediaType(), (InputStream) value.getData());
        } else if (value.getData() instanceof byte[]) {
            requestBody = RequestBody.create(value.getMediaType(), (byte[]) value.getData());
        }
        return requestBody;
    }

    private RequestBody create(final MediaType mediaType, final InputStream inputStream) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public long contentLength() {
                try {
                    return inputStream.available();
                } catch (IOException e) {
                    return 0;
                }
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source = null;
                try {
                    source = Okio.source(inputStream);
                    sink.writeAll(source);
                } finally {
                    okhttp3.internal.Util.closeQuietly(source);
                }
            }
        };
    }

}
