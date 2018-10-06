/**
 * Copyright 2017 区长
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.retrofit.network.body;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.retrofit.network.callback.ResultProgressCallback;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 带进度的输出流
 */
class ProgressOutputStream extends OutputStream {
    private final OutputStream stream;
    private final ResultProgressCallback listener;
    private Object tag;

    private long total;
    private long totalWritten;
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

    ProgressOutputStream(OutputStream stream, ResultProgressCallback listener, long total, Object tag) {
        this.stream = stream;
        this.listener = listener;
        this.total = total;
        this.tag = tag;
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
                                listener.onUIProgressStart(tag, startData.getLong(TOTAL_BYTES));
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
                                listener.onUIProgressChanged(tag, numBytes, totalBytes, percent, speed);
                                break;
                            case WHAT_FINISH:
                                listener.onUIProgressFinish(tag);
                                break;
                            default:
                                break;

                        }
                    }
                };
            }
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.stream.write(b, off, len);
        if (this.total < 0) {
           onProgressChanged(-1, -1, -1);
            return;
        }
        if (len < b.length) {
            this.totalWritten += len;
        } else {
            this.totalWritten += b.length;
        }
       onProgressChanged(this.totalWritten, this.total, (this.totalWritten * 1.0F) / this.total);
    }

    @Override
    public void write(int b) throws IOException {
        this.stream.write(b);
        if (this.total < 0) {
           onProgressChanged(-1, -1, -1);
            return;
        }
        this.totalWritten++;
        onProgressChanged(this.totalWritten, this.total, (this.totalWritten * 1.0F) / this.total);
    }

    @Override
    public void close() throws IOException {
        if (this.stream != null) {
            this.stream.close();
        }
    }

    @Override
    public void flush() throws IOException {
        if (this.stream != null) {
            this.stream.flush();
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
            listener.onUIProgressChanged(tag, numBytes, totalBytes, percent, speed);
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
            listener.onUIProgressStart(tag, totalBytes);
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
            listener.onUIProgressFinish(tag);
            return;
        }
        ensureHandler();
        Message message = mHandler.obtainMessage();
        message.what = WHAT_FINISH;
        mHandler.sendMessage(message);
    }
}
