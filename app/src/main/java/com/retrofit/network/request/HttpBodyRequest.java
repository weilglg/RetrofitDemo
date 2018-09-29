package com.retrofit.network.request;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.retrofit.network.UploadFileType;
import com.retrofit.network.entity.FileEntity;
import com.retrofit.network.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

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

    public R params(String key, String value) {
        mHttpParams.param(key, value);
        return (R) this;
    }

    public R params(Map<String, String> params) {
        mHttpParams.param(params);
        return (R) this;
    }

    public R params(String key, File file) {
        mHttpParams.put(key, file);
        return (R) this;
    }

    public R params(String key, String fileName, InputStream stream) {
        mHttpParams.put(key, fileName, stream);
        return (R) this;
    }

    public R params(String key, String fileName, byte[] bytes) {
        mHttpParams.put(key, fileName, bytes);
        return (R) this;
    }

    public R addFileParams(String key, List<File> files) {
        if (TextUtils.isEmpty(key) && files != null && !files.isEmpty()) {
            for (File file : files) {
                params(key, file);
            }
        }
        return (R) this;
    }

    public R addFileWrapperParams(String key, List<FileEntity> fileWrappers) {
        mHttpParams.put(key, fileWrappers);
        return (R) this;
    }

    public R params(String key, File file, String fileName) {
        mHttpParams.put(key, file, fileName);
        return (R) this;
    }

    public <T> R params(String key, String fileName, T file, MediaType contentType) {
        mHttpParams.put(key, fileName, file, contentType);
        return (R) this;
    }

    public R uploadType(UploadFileType type) {
        this.mUploadType = type;
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
        } else if (!mHttpParams.isParamsEmpty() && mHttpParams.isFilesEmpty()) {
            return mApiManager.postMap(mUrl, mHttpParams.getParamMap());
        } else if (!mHttpParams.isFilesEmpty()) {
            Util.checkNotNull(mUploadType, "UploadType is null");
            if (mUploadType == UploadFileType.BODY_MAP) {
                return uploadFilesWithBodyMap();
            } else if (mUploadType == UploadFileType.PART_FROM) {
                return uploadFilesWithPartList();
            } else {
                return uploadFilesWithPartMap();
            }
        } else {
            return mApiManager.post(mUrl);
        }
    }

    /**
     * 以RequestBody的Map形式提交
     *
     * @return
     */
    private Observable<ResponseBody> uploadFilesWithBodyMap() {
        Map<String, RequestBody> mBodyMap = new HashMap<>();
        //拼接参数键值对
        for (Map.Entry<String, String> mapEntry : mHttpParams.getParamMap().entrySet()) {
            RequestBody body = RequestBody.create(MediaType.parse("text/plain"), mapEntry.getValue());
            mBodyMap.put(mapEntry.getKey(), body);
        }
        //拼接文件
        for (Map.Entry<String, List<FileEntity>> entry : mHttpParams.getFileMap().entrySet()) {
            List<FileEntity> fileValues = entry.getValue();
            for (FileEntity fileWrapper : fileValues) {
                RequestBody requestBody = getRequestBody(fileWrapper);
                mBodyMap.put(entry.getKey(), requestBody);
            }
        }
        return mApiManager.uploadFileWithBodyMap(mUrl, mBodyMap);
    }

    /**
     * 以MultipartBody.Part的List形式提交
     *
     * @return
     */
    private Observable<ResponseBody> uploadFilesWithPartList() {
        List<MultipartBody.Part> partList = new ArrayList<>();
        HashMap<String, String> paramMap = mHttpParams.getParamMap();
        for (String key : paramMap.keySet()) {
            partList.add(MultipartBody.Part.createFormData(key, paramMap.get(key)));
        }
        for (Map.Entry<String, List<FileEntity>> fileEntity : mHttpParams.getFileMap().entrySet()) {
            for (FileEntity entity : fileEntity.getValue()) {
                MultipartBody.Part part = createPartBody(fileEntity.getKey(), entity);
                partList.add(part);
            }
        }
        return mApiManager.uploadFileWithPartList(mUrl, partList);
    }

    /**
     * 以MultipartBody.Part的Map形式提交
     *
     * @return
     */
    private Observable<ResponseBody> uploadFilesWithPartMap() {
        Map<String, MultipartBody.Part> partMap = new HashMap<>();
        //拼接普通参数
        HashMap<String, String> paramMap = mHttpParams.getParamMap();
        for (String key : paramMap.keySet()) {
            partMap.put(key, MultipartBody.Part.createFormData(key, paramMap.get(key)));
        }
        //拼接文件参数
        HashMap<String, List<FileEntity>> fileMap = mHttpParams.getFileMap();
        for (Map.Entry<String, List<FileEntity>> entitySet : fileMap.entrySet()) {
            String key = entitySet.getKey();
            for (FileEntity entity : entitySet.getValue()) {
                MultipartBody.Part part = createPartBody(key, entity);
                partMap.put(key, part);
            }
        }
        return mApiManager.uploadFileWithPartMap(mUrl, partMap);
    }

    private MultipartBody.Part createPartBody(String key, FileEntity value) {
        RequestBody requestBody = getRequestBody(value);
        Util.checkNotNull(requestBody, "requestBody==null fileEntity.data must is File/InputStream/byte[]");
        return MultipartBody.Part.createFormData(key, value.getFileName(), requestBody);
    }

    private RequestBody getRequestBody(FileEntity value) {
        RequestBody requestBody = null;
        if (value.getData() instanceof File) {
            requestBody = RequestBody.create(value.getMediaType(), (File) value.getData());
        } else if (value.getData() instanceof InputStream) {
            requestBody = create(value.getMediaType(), (InputStream) value.getData());
        } else if (value.getData() instanceof byte[]) {
            requestBody = RequestBody.create(value.getMediaType(), (byte[]) value.getData());
        }
        return requestBody;
    }

    private RequestBody create(final MediaType mediaType, final InputStream inputStream) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public long contentLength() {
                try {
                    return inputStream.available();
                } catch (IOException e) {
                    return 0;
                }
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source = null;
                try {
                    source = Okio.source(inputStream);
                    sink.writeAll(source);
                } finally {
                    okhttp3.internal.Util.closeQuietly(source);
                }
            }
        };
    }

}
