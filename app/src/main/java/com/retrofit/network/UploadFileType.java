package com.retrofit.network;

import okhttp3.MultipartBody;

public enum UploadFileType {
    /**
     * List<MultipartBody.Part>方式上传
     */
    PART_FROM,
    /**
     * Map<String, MultipartBody.Part>方式上传
     */
    PART_MAP,
    /**
     * Map<RequestBody>方式上传
     */
    BODY_MAP
}
