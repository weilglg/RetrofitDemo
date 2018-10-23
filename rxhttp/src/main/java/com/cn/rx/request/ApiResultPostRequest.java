package com.cn.rx.request;

import com.cn.rx.callback.ResultCallback;
import com.cn.rx.callback.ResultCallbackProxy;
import com.cn.rx.callback.ResultClazzCallProxy;
import com.cn.rx.callback.ResultProgressCallback;
import com.cn.rx.entity.ApiResultEntity;
import com.cn.rx.func.ApiResultFunc;
import com.cn.rx.func.RetryExceptionFunc;
import com.cn.rx.subscriber.ResultCallbackSubscriber;
import com.cn.rx.util.RxUtil;

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
        return build().generateRequest()
                .map(new ApiResultFunc(proxy.getType()))
                .compose(isSyncRequest ? RxUtil._io_main_result() : RxUtil._main_result())
                .retryWhen(new RetryExceptionFunc(mRetryCount, mRetryDelay, mRetryIncreaseDelay));
    }

    public <T> Disposable execute(Object tag, ResultCallback<T> callback) {
        return execute(tag, ResultCallbackProxy.NEW_DEFAULT_INSTANCE(callback));
    }

    public <T> Disposable execute(Object tag, ResultCallbackProxy<? extends ApiResultEntity<T>, T> proxy) {
        ResultCallback<T> callback = proxy.getCallback();
        if (callback instanceof ResultProgressCallback) {
            ((ResultProgressCallback) callback).setTag(tag);
        }
        Observable<T> observable = build(callback).generateObservable(generateRequest(), proxy);
        return observable.subscribeWith(new ResultCallbackSubscriber<T>(tag, proxy.getCallback()));
    }

    private <T> Observable<T> generateObservable(Observable observable, ResultCallbackProxy<? extends ApiResultEntity<T>, T> proxy) {
        return observable.map(new ApiResultFunc(proxy.getType()))
                .compose(isSyncRequest ? RxUtil._io_main_result() : RxUtil._main_result())
                .retryWhen(new RetryExceptionFunc(mRetryCount, mRetryDelay, mRetryIncreaseDelay));
    }

}
