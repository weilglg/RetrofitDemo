package com.retrofit.network.entity;

import java.io.File;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;

public class HttpParamEntity {
    private HashMap<String, String> paramMap = new HashMap<>();
    private HashMap<String, List<FileEntity>> fileMap = new HashMap<>();

    public void put(HttpParamEntity paramEntity) {
        if (paramEntity != null) {
            if (!paramEntity.getParamMap().isEmpty()) {
                paramMap.putAll(paramEntity.getParamMap());
            }
            if (paramEntity.getFileMap().isEmpty()) {
                fileMap.putAll(paramEntity.getFileMap());
            }
        }
    }

    public HashMap<String, String> getParamMap() {
        return paramMap;
    }

    public HashMap<String, List<FileEntity>> getFileMap() {
        return fileMap;
    }

    public void param(Map<String, String> params) {
        if (params != null && params.isEmpty()) {
            paramMap.putAll(params);
        }
    }

    public void param(String key, String value) {
        paramMap.put(key, value);
    }

    public void put(Map<String, FileEntity> fileMap) {
        if (fileMap != null && !fileMap.isEmpty()) {
            fileMap.putAll(fileMap);
        }
    }

    public <T extends File> void put(String key, T file) {
        put(key, file, file.getName());
    }

    public <T extends File> void put(String key, T file, String fileName) {
        put(key, fileName, file, getMediaTypeByName(fileName));
    }

    public <T extends InputStream> void put(String key, String streamName, T inputStream) {
        put(key, streamName, inputStream, getMediaTypeByName(streamName));
    }

    public void put(String key, String name, byte[] bytes) {
        put(key, name, bytes, getMediaTypeByName(name));
    }

    public <T> void put(String key, String fileName, T data, MediaType mediaType) {
        FileEntity fileEntity = new FileEntity(data, fileName, mediaType);
        put(key, fileEntity);
    }

    public void put(String key, FileEntity fileEntity) {
        if (!fileMap.containsKey(key)) {
            fileMap.put(key, new ArrayList<FileEntity>());
        }
        fileMap.get(key).add(fileEntity);
    }

    public void put(String key, List<FileEntity> fileWrappers) {
        if (!fileMap.containsKey(key)) {
            fileMap.put(key, new ArrayList<FileEntity>());
        }
        fileMap.get(key).addAll(fileWrappers);
    }

    private MediaType getMediaTypeByName(String fileName) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        fileName = fileName.replace("#", "");   //解决文件名中含有#号异常的问题
        String contentType = fileNameMap.getContentTypeFor(fileName);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return MediaType.parse(contentType);
    }

    public boolean isParamsEmpty() {
        return paramMap.isEmpty();
    }

    public boolean isFilesEmpty() {
        return fileMap.isEmpty();
    }

}
