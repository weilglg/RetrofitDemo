package com.retrofit.network.entity;

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
    BODY_MAP,
    /**
     * RequestBody方式上传，该方式不支持普通参数
     */
    BODY
}
