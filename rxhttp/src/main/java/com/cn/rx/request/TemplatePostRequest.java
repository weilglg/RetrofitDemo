package com.cn.rx.request;

import com.cn.rx.callback.ResponseCallback;
import com.cn.rx.callback.ResponseClazzCallback;
import com.cn.rx.func.RetryExceptionFunc;
import com.cn.rx.subscriber.RxCallbackSubscriber;
import com.cn.rx.transformer.HandleClazzBodyTransformer;
import com.cn.rx.transformer.HandleErrorTransformer;
import com.cn.rx.transformer.HandleResponseBodyTransformer;
import com.cn.rx.util.RxUtil;

import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;

@SuppressWarnings(value = {"unchecked", "deprecation"})
public class TemplatePostRequest extends HttpBodyRequest<TemplatePostRequest> {

    public TemplatePostRequest(String url) {
        super(url);
    }

    public <T> Observable<T> execute(Type type) {
        return build().generateRequest()
                .compose(isSyncRequest ? RxUtil._io_main() : RxUtil._main())
                .compose(new HandleErrorTransformer())
                .retryWhen(new RetryExceptionFunc(mRetryCount, mRetryDelay, mRetryIncreaseDelay))
                .compose(new HandleClazzBodyTransformer(type, null));
    }

    public <T> Observable<T> execute(Class<T> clazz) {
        return build(null).generateRequest()
                .compose(isSyncRequest ? RxUtil._io_main() : RxUtil._main())
                .compose(new HandleErrorTransformer())
                .retryWhen(new RetryExceptionFunc(mRetryCount, mRetryDelay, mRetryIncreaseDelay))
                .compose(new HandleClazzBodyTransformer(clazz, null));
    }

    public <T> Observable<T> execute(Type type, ResponseClazzCallback callback) {
        return build().generateRequest()
                .compose(isSyncRequest ? RxUtil._io_main() : RxUtil._main())
                .compose(new HandleErrorTransformer())
                .retryWhen(new RetryExceptionFunc(mRetryCount, mRetryDelay, mRetryIncreaseDelay))
                .compose(new HandleClazzBodyTransformer(type, callback));
    }

    public <T> Observable<T> execute(Class<T> clazz, ResponseClazzCallback callback) {
        return build().generateRequest()
                .compose(isSyncRequest ? RxUtil._io_main() : RxUtil._main())
                .compose(new HandleErrorTransformer())
                .retryWhen(new RetryExceptionFunc(mRetryCount, mRetryDelay, mRetryIncreaseDelay))
                .compose(new HandleClazzBodyTransformer(clazz, callback));
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
