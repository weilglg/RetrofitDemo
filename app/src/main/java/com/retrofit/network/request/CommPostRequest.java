package com.retrofit.network.request;

import com.retrofit.network.callback.ResultCallback;
import com.retrofit.network.callback.ResultCallbackProxy;
import com.retrofit.network.entity.CommResultEntity;

import io.reactivex.disposables.Disposable;

public class CommPostRequest extends ApiResultPostRequest {

    public CommPostRequest(String url) {
        super(url);
    }

    @Override
    public <T> Disposable execute(Object tag, ResultCallback<T> callback) {
        return super.execute(tag, new ResultCallbackProxy<CommResultEntity<T>, T>(callback) {
        });
    }

}
