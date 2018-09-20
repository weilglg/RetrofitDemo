package com.retrofit.network.request;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class HttpBodyRequest extends BaseRequest {
    private RequestBody mRequestBody;

    public HttpBodyRequest(String url) {
        super(url);
    }

    @Override
    protected Observable<ResponseBody> generateRequest() {
        return null;
    }


}
