package com.retrofit.network.callback;

public abstract class DownloadProgressCallBack extends ResultCallback<String> {

    /**
     * 下载进度回调
     *
     * @param tag       标识
     * @param bytesRead 下载的大小
     * @param fileSize  文件大小
     * @param progress  下载进度
     */
    public abstract void onProgress(Object tag, long bytesRead, long fileSize, float progress);


    /**
     * 下载完成的回调
     *
     * @param tag      标识
     * @param filePath 存储的文件路径
     */
    @Override
    public abstract void onSuccess(Object tag, String filePath);

    @Override
    public void onCompleted(Object tag) {

    }

}
