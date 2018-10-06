package com.retrofit.network.callback;

import com.retrofit.network.exception.ApiThrowable;

public abstract class ResultDownloadCallback extends ResultProgressCallback<String> {

    /**
     * 进度发生了改变，如果numBytes，totalBytes，percent，speed都为-1，则表示总大小获取不到
     *
     * @param numBytes   已读/写大小
     * @param totalBytes 总大小
     * @param percent    百分比
     * @param speed      速度 bytes/ms
     */
    public abstract void onUIProgressChanged(Object mTag, long numBytes, long totalBytes, float percent, float speed);

    /**
     * 进度开始
     */
    public void onUIProgressStart(Object mTag) {

    }

    /**
     * 进度结束
     */
    public void onUIProgressFinish(Object mTag) {

    }

    public void onError(Object tag, ApiThrowable e) {

    }

    @Override
    public void onCompleted(Object tag) {

    }
}
