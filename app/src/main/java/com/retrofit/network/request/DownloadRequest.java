package com.retrofit.network.request;

import com.retrofit.network.callback.ResultDownloadCallback;
import com.retrofit.network.func.RetryExceptionFunc;
import com.retrofit.network.interceptor.ProgressResponseInterceptor;
import com.retrofit.network.subscriber.RxDownloadSubscriber;
import com.retrofit.network.transformer.HandleErrorTransformer;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;

@SuppressWarnings(value = {"unchecked", "deprecation"})
public class DownloadRequest extends BaseRequest<DownloadRequest> {

    private String savePath;
    private String saveName;

    public DownloadRequest savePath(String savePath) {
        this.savePath = savePath;
        return this;
    }

    public DownloadRequest saveName(String saveName) {
        this.saveName = saveName;
        return this;
    }

    public DownloadRequest(String url) {
        super(url);
    }

    @Override
    protected Observable<ResponseBody> generateRequest() {
        return mApiManager.downloadFile(mUrl);
    }

    protected <T> DownloadRequest build(Object tag, ResultDownloadCallback callback) {
        OkHttpClient.Builder okHttpClientBuilder = generateOkHttpClientBuilder();
        okHttpClientBuilder.addInterceptor(new ProgressResponseInterceptor(callback, tag));
        mHttpClient = okHttpClientBuilder.build();
        return build();
    }


    public <T> Disposable execute(Object tag, ResultDownloadCallback callback) {
        return build(tag, callback).generateRequest().compose(new ObservableTransformer<ResponseBody, ResponseBody>() {
            @Override
            public ObservableSource<ResponseBody> apply(Observable<ResponseBody> upstream) {
                if (isSyncRequest) {
                    return upstream.subscribeOn(Schedulers.io())
                            .unsubscribeOn(Schedulers.io())
                            .observeOn(Schedulers.computation());
                } else {
                    return upstream;//.observeOn(AndroidSchedulers.mainThread());
                }
            }
        }).compose(new HandleErrorTransformer<>()).retryWhen(new RetryExceptionFunc(mRetryCount, mRetryDelay, mRetryIncreaseDelay))
                .subscribeWith(new RxDownloadSubscriber(mContext, savePath, saveName, callback, tag));
    }

}
