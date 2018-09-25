package com.retrofit.network.callback;

import okhttp3.ResponseBody;

public interface IResponseCallback<T> {

    T onTransformationResponse(Object tag, ResponseBody body) throws Exception;

}
