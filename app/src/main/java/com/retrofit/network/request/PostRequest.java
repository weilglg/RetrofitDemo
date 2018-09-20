package com.retrofit.network.request;

import android.nfc.Tag;

import com.retrofit.network.func.RetryExceptionFunc;
import com.retrofit.network.subscribe.CallBackSubsciber;
import com.retrofit.network.subscribe.ResponseCallback;
import com.retrofit.network.subscribe.RxSubscribe;
import com.retrofit.network.util.RxUtil;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class PostRequest extends HttpBodyRequest<PostRequest> {

    public PostRequest(String url) {
        super(url);
    }

    public <T> Disposable post(Object tag, ResponseCallback<T> callback) {
        Observable<T> observable = build().generateObservable(generateRequest(), callback);
        return observable.subscribeWith(new CallBackSubsciber<T>(callback, tag));
    }

    public <T> Observable<T> generateObservable(Observable observable, ResponseCallback<T> callback) {
        return observable.compose(isSyncRequest ? RxUtil._main() : RxUtil._io_main())
                .retryWhen(new RetryExceptionFunc(mRetryCount, mRetryDelay, mRetryIncreaseDelay));
    }
}
