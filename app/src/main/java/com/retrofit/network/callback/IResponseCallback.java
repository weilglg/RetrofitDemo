package com.retrofit.network.callback;

import okhttp3.ResponseBody;

public interface IResponseCallback<T> {

    T onTransformationResponse(ResponseBody body) throws Exception;

}
