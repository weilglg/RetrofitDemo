package com.retrofit.network.transformer;

import com.retrofit.network.subscriber.ResponseCallback;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;

public class HandleResponseBodyTransformer<T> implements ObservableTransformer<ResponseBody, T> {
    private ResponseCallback<T> callback;
    private Object mTag;

    public HandleResponseBodyTransformer(ResponseCallback<T> callback, Object mTag) {
        this.callback = callback;
        this.mTag = mTag;
    }

    @Override
    public ObservableSource<T> apply(Observable<ResponseBody> upstream) {
        return upstream.map(new Function<ResponseBody, T>() {
            @Override
            public T apply(ResponseBody body) throws Exception {
                return callback.onTransformationResponse(mTag, body);
            }
        });
    }
}
