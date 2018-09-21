package com.retrofit.network.request;

import com.retrofit.network.func.HandleErrorFunc;
import com.retrofit.network.func.RetryExceptionFunc;
import com.retrofit.network.subscriber.ResponseCallback;
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

    public <T> Disposable execute(final Object tag, final ResponseCallback<T> callback) {
        Observable<ResponseBody> observable = build().generateObservable(generateRequest());
        return observable.compose(new HandleResponseBodyTransformer<T>(callback, tag))
                .compose(new HandleErrorTransformer<T>())
                .subscribeWith(new RxCallbackSubscriber<T>(mContext, tag, callback));
    }

    private  Observable<ResponseBody> generateObservable(Observable observable) {
        return observable.compose(isSyncRequest ? RxUtil._main() : RxUtil._io_main())
                .retryWhen(new RetryExceptionFunc(mRetryCount, mRetryDelay, mRetryIncreaseDelay));
    }
}
