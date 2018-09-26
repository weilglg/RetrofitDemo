package com.retrofit.network.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.retrofit.network.callback.ResponseCallback;
import com.retrofit.network.func.RetryExceptionFunc;
import com.retrofit.network.subscriber.RxCallbackSubscriber;
import com.retrofit.network.transformer.HandleErrorTransformer;
import com.retrofit.network.transformer.HandleResponseBodyTransformer;
import com.retrofit.network.util.RxUtil;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;

@SuppressWarnings(value = {"unchecked", "deprecation"})
public class PostRequest extends HttpBodyRequest<PostRequest> {

    public PostRequest(String url) {
        super(url);
    }

    public <T> Observable<T> execute(final Class<T> clazz) {
        return build().generateRequest()
                .compose(isSyncRequest ? RxUtil._io_main() : RxUtil._main())
                .compose(new HandleErrorTransformer())
                .retryWhen(new RetryExceptionFunc(mRetryCount, mRetryDelay, mRetryIncreaseDelay))
                .map(new Function<ResponseBody, T>() {
                    @Override
                    public T apply(ResponseBody body) throws Exception {
                        String jsonStr = body.string();
                        return JSON.parseObject(jsonStr, clazz, Feature.UseBigDecimal);
                    }
                });
    }

    public <T> Disposable execute(final Object tag, final ResponseCallback<T> callback) {
        Observable<ResponseBody> observable = build().generateObservable(generateRequest());
        return observable.compose(new HandleResponseBodyTransformer<T>(callback, tag))
                .compose(new HandleErrorTransformer<T>())
                .subscribeWith(new RxCallbackSubscriber<T>(mContext, tag, callback));
    }

    private Observable<ResponseBody> generateObservable(Observable observable) {
        return observable.compose(isSyncRequest ? RxUtil._io_main() : RxUtil._main())
                .retryWhen(new RetryExceptionFunc(mRetryCount, mRetryDelay, mRetryIncreaseDelay));
    }
}
