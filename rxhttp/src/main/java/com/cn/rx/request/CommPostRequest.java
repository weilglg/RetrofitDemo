package com.cn.rx.request;

import com.cn.rx.callback.ResultCallback;
import com.cn.rx.callback.ResultCallbackProxy;
import com.cn.rx.entity.CommResultEntity;

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
