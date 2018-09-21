package com.retrofit.network.subscriber;

import okhttp3.ResponseBody;

public interface IResponseCallback<T> {

    T onTransformationResponse(Object tag, ResponseBody body) throws Exception;

}
