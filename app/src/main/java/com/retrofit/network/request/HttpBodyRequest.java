package com.retrofit.network.request;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.retrofit.network.UploadFileType;
import com.retrofit.network.util.Util;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

@SuppressWarnings(value = {"unchecked", "deprecation"})
public class HttpBodyRequest<R extends BaseRequest> extends BaseRequest<R> {
    private RequestBody mRequestBody;
    private String mJsonStr;
    private JSONObject mJsonObj;
    private JSONArray mJsonArr;
    private byte[] mBytes;
    private String mStr;
    private Object mObject;
    private MediaType mMediaType;
    private UploadFileType mUploadType;

    public HttpBodyRequest(String url) {
        super(url);
    }

    public R uploadFileType(UploadFileType type) {
        this.mUploadType = type;
        return (R) this;
    }

    public R requestBody(RequestBody requestBody) {
        this.mRequestBody = requestBody;
        return (R) this;
    }

    public R json(String jsonStr) {
        this.mJsonStr = jsonStr;
        return (R) this;
    }

    public R jsonObj(JSONObject obj) {
        this.mJsonObj = obj;
        return (R) this;
    }

    public R jsonArr(JSONArray jsonArray) {
        this.mJsonArr = jsonArray;
        return (R) this;
    }

    public R object(Object obj) {
        this.mObject = obj;
        return (R) this;
    }

    public R bytes(byte[] bytes) {
        this.mBytes = bytes;
        return (R) this;
    }

    public R txt(String txt) {
        this.mStr = txt;
        this.mMediaType = okhttp3.MediaType.parse("text/plain");
        return (R) this;
    }

    public R mediaType(String txt, MediaType mediaType) {
        this.mStr = txt;
        Util.checkNotNull(mediaType, "mediaType==null");
        this.mMediaType = mediaType;
        return (R) this;
    }

    @Override
    protected Observable<ResponseBody> generateRequest() {
        if (mRequestBody != null) {
            return mApiManager.postBody(mUrl, mRequestBody);
        } else if (!TextUtils.isEmpty(mJsonStr)) {
            return mApiManager.potJsonStr(mUrl, Util.createJson(mJsonStr));
        } else if (mJsonObj != null) {
            return mApiManager.postJson(mUrl, mJsonObj);
        } else if (mJsonArr != null) {
            return mApiManager.postJson(mUrl, mJsonArr);
        } else if (!TextUtils.isEmpty(mStr)) {
            RequestBody requestBody = RequestBody.create(mMediaType, mStr);
            return mApiManager.postBody(mUrl, requestBody);
        } else if (mBytes != null) {
            return mApiManager.postBody(mUrl, Util.createBytes(mBytes));
        } else if (mObject != null) {
            return mApiManager.postBody(mUrl, mObject);
        } else if (mUploadType == UploadFileType.PART) {
            return uploadFilesWithParts();
        } else if (mUploadType == UploadFileType.BODY) {
            return uploadFilesWithBodys();
        } else {
            return mApiManager.post(mUrl);
        }
    }

    private Observable<ResponseBody> uploadFilesWithBodys() {
        return null;
    }

    private Observable<ResponseBody> uploadFilesWithParts() {
        List<MultipartBody.Part> partList = new ArrayList<>();
        for (String key : mParameters.keySet()) {
            partList.add(MultipartBody.Part.createFormData(key, mParameters.get(key)));
        }

        return null;
    }


}
