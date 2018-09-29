package com.retrofit.network.callback;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * 进度回调
 *
 * @author lizhangqu
 * @version V1.0
 * @since 2017-07-12 16:19
 */
public abstract class ResultProgressCallback<T> extends ResultCallback<T> {
    private Object mTag;
    boolean started;
    long lastRefreshTime = 0L;
    long lastBytesWritten = 0L;
    int minTime = 100;//最小回调时间100ms，避免频繁回调

    private Handler mHandler;
    private static final int WHAT_START = 0x01;
    private static final int WHAT_UPDATE = 0x02;
    private static final int WHAT_FINISH = 0x03;
    private static final String CURRENT_BYTES = "numBytes";
    private static final String TOTAL_BYTES = "totalBytes";
    private static final String PERCENT = "percent";
    private static final String SPEED = "speed";

    public void setTag(Object mTag) {
        this.mTag = mTag;
    }

    public Object getTag() {
        return mTag;
    }

    private void ensureHandler() {
        if (mHandler != null) {
            return;
        }
        synchronized (ResultProgressCallback.class) {
            if (mHandler == null) {
                mHandler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg == null) {
                            return;
                        }
                        switch (msg.what) {
                            case WHAT_START:
                                Bundle startData = msg.getData();
                                if (startData == null) {
                                    return;
                                }
                                onUIProgressStart(mTag, startData.getLong(TOTAL_BYTES));
                                break;
                            case WHAT_UPDATE:
                                Bundle updateData = msg.getData();
                                if (updateData == null) {
                                    return;
                                }
                                long numBytes = updateData.getLong(CURRENT_BYTES);
                                long totalBytes = updateData.getLong(TOTAL_BYTES);
                                float percent = updateData.getFloat(PERCENT);
                                float speed = updateData.getFloat(SPEED);
                                onUIProgressChanged(mTag, numBytes, totalBytes, percent, speed);
                                break;
                            case WHAT_FINISH:
                                onUIProgressFinish(mTag);
                                break;
                            default:
                                break;

                        }
                    }
                };
            }
        }
    }

    /**
     * 进度发生了改变，如果numBytes，totalBytes，percent都为-1，则表示总大小获取不到
     *
     * @param numBytes   已读/写大小
     * @param totalBytes 总大小
     * @param percent    百分比
     */
    public final void onProgressChanged(long numBytes, long totalBytes, float percent) {
        if (!started) {
            onProgressStart(totalBytes);
            started = true;
        }
        if (numBytes == -1 && totalBytes == -1 && percent == -1) {
            onProgressChanged(-1, -1, -1, -1);
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRefreshTime >= minTime || numBytes == totalBytes || percent >= 1F) {
            long intervalTime = (currentTime - lastRefreshTime);
            if (intervalTime == 0) {
                intervalTime += 1;
            }
            long updateBytes = numBytes - lastBytesWritten;
            final long networkSpeed = updateBytes / intervalTime;
            onProgressChanged(numBytes, totalBytes, percent, networkSpeed);
            lastRefreshTime = System.currentTimeMillis();
            lastBytesWritten = numBytes;
        }
        if (numBytes == totalBytes || percent >= 1F) {
            onProgressFinish();
        }
    }

    /**
     * 进度发生了改变，如果numBytes，totalBytes，percent，speed都为-1，则表示总大小获取不到
     *
     * @param numBytes   已读/写大小
     * @param totalBytes 总大小
     * @param percent    百分比
     * @param speed      速度 bytes/ms
     */
    private void onProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onUIProgressChanged(mTag, numBytes, totalBytes, percent, speed);
            return;
        }
        ensureHandler();
        Message message = mHandler.obtainMessage();
        message.what = WHAT_UPDATE;
        Bundle data = new Bundle();
        data.putLong(CURRENT_BYTES, numBytes);
        data.putLong(TOTAL_BYTES, totalBytes);
        data.putFloat(PERCENT, percent);
        data.putFloat(SPEED, speed);
        message.setData(data);
        mHandler.sendMessage(message);
    }

    /**
     * 进度开始
     *
     * @param totalBytes 总大小
     */
    private void onProgressStart(long totalBytes) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onUIProgressStart(mTag, totalBytes);
            return;
        }
        ensureHandler();
        Message message = mHandler.obtainMessage();
        message.what = WHAT_START;
        Bundle data = new Bundle();
        data.putLong(TOTAL_BYTES, totalBytes);
        message.setData(data);
        mHandler.sendMessage(message);
    }

    /**
     * 进度结束
     */
    private void onProgressFinish() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onUIProgressFinish(mTag);
            return;
        }
        ensureHandler();
        Message message = mHandler.obtainMessage();
        message.what = WHAT_FINISH;
        mHandler.sendMessage(message);
    }

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
     *
     * @param totalBytes 总大小
     */
    public void onUIProgressStart(Object mTag, long totalBytes) {

    }

    /**
     * 进度结束
     */
    public void onUIProgressFinish(Object mTag) {

    }
}
