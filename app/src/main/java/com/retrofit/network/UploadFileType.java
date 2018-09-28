package com.retrofit.network;

public enum UploadFileType {
    /**
     * MultipartBody.Part方式上传
     */
    PART,
    /**
     * RequestBody方式上传
     */
    BODY,
    /**
     * Map RequestBody方式上传
     */
    BODY_MAP
}
