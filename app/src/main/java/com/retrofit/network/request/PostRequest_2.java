package com.retrofit.network.request;

import com.retrofit.network.callback.ResultCallback;
import com.retrofit.network.callback.ResultCallbackProxy;
import com.retrofit.network.entity.ApiResultEntity;
import com.retrofit.network.func.ApiResultFunc;
import com.retrofit.network.func.RetryExceptionFunc;
import com.retrofit.network.subscriber.ResultCallbackSubscriber;
import com.retrofit.network.util.RxUtil;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

@SuppressWarnings(value = {"unchecked", "deprecation"})
public class PostRequest_2 extends HttpBodyRequest<PostRequest_2> {

    public PostRequest_2(String url) {
        super(url);
    }

    public <T> Disposable execute(Object tag, ResultCallback<T> callback) {
        return execute(tag, new ResultCallbackProxy<ApiResultEntity<T>, T>(callback){});
    }

    public <T> Disposable execute(final Object tag, ResultCallbackProxy<? extends ApiResultEntity<T>, T> proxy) {
        Observable<T> observable = build().generateObservable(generateRequest(), proxy);
        return observable.subscribeWith(new ResultCallbackSubscriber<T>(tag, proxy.getCallback()));
    }

    private <T> Observable<T> generateObservable(Observable observable, ResultCallbackProxy<? extends ApiResultEntity<T>, T> proxy) {
        return observable.map(new ApiResultFunc(proxy.getType()))
                .compose(isSyncRequest ? RxUtil._io_main_result() : RxUtil._main())
                .retryWhen(new RetryExceptionFunc(mRetryCount, mRetryDelay, mRetryIncreaseDelay));
    }
}
