package com.cn.rx.callback;

import okhttp3.ResponseBody;

public interface IResponseCallback<T> {

    T onTransformationResponse(ResponseBody body) throws Exception;

}
