package com.retrofit.network.request;

import com.retrofit.network.callback.ResultCallback;
import com.retrofit.network.callback.ResultCallbackProxy;
import com.retrofit.network.callback.ResultClazzCallProxy;
import com.retrofit.network.callback.ResultProgressCallback;
import com.retrofit.network.entity.ApiResultEntity;
import com.retrofit.network.func.ApiResultFunc;
import com.retrofit.network.func.RetryExceptionFunc;
import com.retrofit.network.subscriber.ResultCallbackSubscriber;
import com.retrofit.network.util.RxUtil;

import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

@SuppressWarnings(value = {"unchecked", "deprecation"})
public class ApiResultPostRequest extends HttpBodyRequest<ApiResultPostRequest> {

    public ApiResultPostRequest(String url) {
        super(url);
    }

    public <T> Observable<T> execute(Class<T> clazz) {
        return execute(new ResultClazzCallProxy<ApiResultEntity<T>, T>(clazz) {
        });
    }

    public <T> Observable<T> execute(Type type) {
        return execute(new ResultClazzCallProxy<ApiResultEntity<T>, T>(type) {
        });
    }

    public <T> Observable<T> execute(ResultClazzCallProxy<? extends ApiResultEntity<T>, T> proxy) {
        return build(null).generateRequest()
                .map(new ApiResultFunc(proxy.getType()))
                .compose(isSyncRequest ? RxUtil._io_main_result() : RxUtil._main_result())
                .retryWhen(new RetryExceptionFunc(mRetryCount, mRetryDelay, mRetryIncreaseDelay));
    }

    public <T> Disposable execute(Object tag, ResultCallback<T> callback) {
        return execute(tag, ResultCallbackProxy.NEW_DEFAULT_INSTANCE(callback));
    }

    public <T> Disposable execute(final Object tag, ResultCallbackProxy<? extends ApiResultEntity<T>, T> proxy) {
        Observable<T> observable = build(proxy.getCallback()).generateObservable(generateRequest(), proxy);
        return observable.subscribeWith(new ResultCallbackSubscriber<T>(tag, proxy.getCallback()));
    }

    private <T> Observable<T> generateObservable(Observable observable, ResultCallbackProxy<? extends ApiResultEntity<T>, T> proxy) {
        return observable.map(new ApiResultFunc(proxy.getType()))
                .compose(isSyncRequest ? RxUtil._io_main_result() : RxUtil._main_result())
                .retryWhen(new RetryExceptionFunc(mRetryCount, mRetryDelay, mRetryIncreaseDelay));
    }

}
