package com.retrofit.network;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface ApiManager {

    @POST()
    Observable<ResponseBody> postBody(@Url String url, @Body RequestBody mRequestBody);

    @POST()
    Observable<ResponseBody> postBody(@Url String url, @Body Object object);

    @POST()
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    Observable<ResponseBody> potJsonStr(@Url String url, @Body RequestBody json);

    @POST()
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    Observable<ResponseBody> postJson(@Url String url, @Body Object object);

    Observable<ResponseBody> post(@Url String url);

//    @POST()
//    @Headers({"Content-Type: application/json", "Accept: application/json"})
//    Observable<ResponseBody> postJsonArr(@Url String mUrl, @Body JSONArray jsonArray);
}
