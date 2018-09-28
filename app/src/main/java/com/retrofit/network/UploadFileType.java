package com.retrofit.network;

import okhttp3.MultipartBody;

public enum UploadFileType {
    /**
     * List<MultipartBody.Part>方式上传
     */
    PART_LIST,
    /**
     * Map<String, MultipartBody.Part>方式上传
     */
    PART_MAP,
    /**
     * MultipartBody.Part表单方式上传
     */
    FROM,
    /**
     * MultipartBody.Part方式上传（无法添加普通参数）
     */
    PART,
    /**
     * RequestBody方式上传（无法添加普通参数）
     */
    BODY,
    /**
     * Map<RequestBody>方式上传
     */
    BODY_MAP
}
