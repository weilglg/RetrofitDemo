package com.retrofit.network.subscribe;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public interface IResponseCallback<T> {

    T onTransformationResponse(Object tag, ResponseBody body) throws Exception;

}
