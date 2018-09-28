package com.retrofit.network;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Url;

public interface ApiManager {

    @POST()
    Observable<ResponseBody> postBody(@Url String url, @Body RequestBody mRequestBody);

    @POST()
    Observable<ResponseBody> postBody(@Url String url, @Body Object object);

    @POST()
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    Observable<ResponseBody> potJsonStr(@Url String url, @Body RequestBody jsonBody);

    @POST()
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    Observable<ResponseBody> postJson(@Url String url, @Body Object object);

    @POST()
    Observable<ResponseBody> post(@Url String url);

    @POST()
    @FormUrlEncoded
    Observable<ResponseBody> postMap(@Url String mUrl, @FieldMap Map<String, String> maps);

    @Multipart
    @POST()
    Observable<ResponseBody> uploadFile(@Url String mUrl, @Part() List<MultipartBody.Part> partList);

    @Multipart
    @POST()
    Observable<ResponseBody> uploadFile(@Url String mUrl, @PartMap() Map<String, RequestBody> maps);

    @POST()
    Observable<ResponseBody> uploadFile(@Url() String url, @Body RequestBody Body);

//    @POST()
//    @Headers({"Content-Type: application/json", "Accept: application/json"})
//    Observable<ResponseBody> postJsonArr(@Url String mUrl, @Body JSONArray jsonArray);
}
