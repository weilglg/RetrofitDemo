package com.retrofit.network.util;

import android.support.annotation.NonNull;

import com.retrofit.network.entity.HttpConfigEntity;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class Util {

    public static final String MULTIPART_FORM_DATA = "multipart/form-data;";
    public static final String MULTIPART_IMAGE_DATA = "image/*; charset=utf-8";
    public static final String MULTIPART_JSON_DATA = "application/json; charset=utf-8";
    public static final String MULTIPART_VIDEO_DATA = "video/*";
    public static final String MULTIPART_AUDIO_DATA = "audio/*";
    public static final String MULTIPART_TEXT_DATA = "text/plain";
    public static final String MULTIPART_APK_DATA = "application/vnd.android.package-archive";
    public static final String MULTIPART_JAVA_DATA = "java/*";
    public static final String MULTIPART_MESSAGE_DATA = "message/rfc822";
    public static final String MULTIPART_BYTE_DATE = "application/octet-stream";

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str) || "null".equals(str);
    }

    public static HttpConfigEntity copyConfig(HttpConfigEntity configEntity) {
        if (configEntity == null) {
            return null;
        }
        HttpConfigEntity entity = new HttpConfigEntity();
        entity.connectTimeOut = configEntity.connectTimeOut;
        entity.readTimeOut = configEntity.readTimeOut;
        entity.context = configEntity.context;
        entity.baseUrl = configEntity.baseUrl;
        entity.isLog = configEntity.isLog;
        entity.isCookie = configEntity.isCookie;
        entity.isCache = configEntity.isCache;
        entity.isUseDefault = configEntity.isUseDefault;
        entity.tag = configEntity.tag;
        entity.cache = configEntity.cache;
        entity.cacheFile = configEntity.cacheFile;
        entity.proxy = configEntity.proxy;
        entity.hostnameVerifier = configEntity.hostnameVerifier;
        entity.converterFactory = configEntity.converterFactory;
        entity.callAdapterFactory = configEntity.callAdapterFactory;
        entity.sslSocketFactory = configEntity.sslSocketFactory;
        entity.trustManager = configEntity.trustManager;
        entity.cookieJar = configEntity.cookieJar;
        entity.connectionPool = configEntity.connectionPool;
        entity.headers = configEntity.headers;
        entity.parameters = configEntity.parameters;
        entity.httpClient = configEntity.httpClient;
        entity.interceptorList = configEntity.interceptorList;
        entity.networkInterceptorList = configEntity.networkInterceptorList;
        return entity;
    }

    public static <T> T checkNotNull(T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }

    public static RequestBody createBytes(byte[] bytes) {
        checkNotNull(bytes, "json not null!");
        return RequestBody.create(okhttp3.MediaType.parse(MULTIPART_BYTE_DATE), bytes);
    }

    public static RequestBody createJson(String jsonString) {
        checkNotNull(jsonString, "json not null!");
        return RequestBody.create(okhttp3.MediaType.parse(MULTIPART_JSON_DATA), jsonString);
    }


    public static RequestBody createText(String text) {
        checkNotNull(text, "text not null!");
        return RequestBody.create(MediaType.parse(MULTIPART_TEXT_DATA), text);
    }

    public static RequestBody createString(String name) {
        checkNotNull(name, "name not null!");
        return RequestBody.create(okhttp3.MediaType.parse(MULTIPART_FORM_DATA + "; charset=utf-8"), name);
    }


    public static RequestBody createFile(File file) {
        checkNotNull(file, "file not null!");
        return RequestBody.create(okhttp3.MediaType.parse(MULTIPART_FORM_DATA + "; charset=utf-8"), file);
    }

    @NonNull
    public static RequestBody createImage(File file) {
        checkNotNull(file, "file not null!");
        return RequestBody.create(okhttp3.MediaType.parse(MULTIPART_IMAGE_DATA), file);
    }

    @NonNull
    public static RequestBody createPartFromString(String descriptionString) {
        checkNotNull(descriptionString, "description string not null!");
        return RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA + "; charset=utf-8"), descriptionString);
    }

}
