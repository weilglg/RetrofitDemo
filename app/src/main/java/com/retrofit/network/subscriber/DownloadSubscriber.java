package com.retrofit.network.subscriber;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.retrofit.network.callback.DownloadProgressCallBack;
import com.retrofit.network.callback.ResultCallback;
import com.retrofit.network.exception.ApiThrowable;
import com.retrofit.network.exception.ExceptionFactory;
import com.retrofit.network.util.RxUtil;
import com.retrofit.network.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import okhttp3.MediaType;

@SuppressWarnings(value = {"unchecked", "deprecation"})
public class DownloadSubscriber<ResponseBody extends okhttp3.ResponseBody> extends BaseSubscriber<ResponseBody> {

    private WeakReference<Context> contextWeakReference;
    private Object mTag;
    private ResultCallback mCallback;
    private String mSavePath;
    private String mSaveName;


    public DownloadSubscriber(Object tag, Context mContext, String savePath, String saveName, ResultCallback callback) {
        contextWeakReference = new WeakReference<>(mContext);
        this.mSaveName = saveName;
        this.mSavePath = savePath;
        this.mTag = tag;
        this.mCallback = callback;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCallback != null) {
            mCallback.onStart(mTag);
        }
    }

    @Override
    public void onNext(ResponseBody result) {
        super.onNext(result);
        writeResponseBodyToDisk(result, mTag, mSaveName, mSavePath, mCallback);
    }

    @SuppressLint("CheckResult")
    private void writeResponseBodyToDisk(ResponseBody body, final Object mTag, String fileName, String filePath, final ResultCallback mCallback) {
        Util.checkNotNull(body, "ResponseBody is null");
        String fileSuffix = ".tmpl";
        MediaType mediaType = body.contentType();
        if (mediaType != null) {
            String subtype = mediaType.subtype();
            if (!TextUtils.isEmpty(subtype)) {
                fileSuffix = "." + subtype;
            }
        }
        if (!TextUtils.isEmpty(fileName)) {
            if (!fileName.contains(".")) {
                fileName = fileName + fileSuffix;
            }
        } else {
            fileName = System.currentTimeMillis() + fileSuffix;
        }
        if (TextUtils.isEmpty(filePath)) {
            filePath = contextWeakReference.get().getExternalFilesDir(null) + File.separator + fileName;
        } else {
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            filePath = filePath + File.separator + fileName;
            filePath = filePath.replaceAll("//", "/");
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }

            long fileSize = body.contentLength();
            long fileSizeDownloaded = 0;
            int updateCount = 0;

            byte[] fileReader = new byte[1024 * 4];
            inputStream = body.byteStream();
            outputStream = new FileOutputStream(file);
            while (true) {
                int read = inputStream.read(fileReader);
                if (read == -1) {
                    break;
                }
                outputStream.write(fileReader, 0, read);
                fileSizeDownloaded += read;
                float progress = 0;
                if (fileSize == -1 || fileSize == 0) {
                    progress = 100;
                } else {
                    progress = fileSizeDownloaded * 100 / fileSize;
                }
                if (updateCount == 0 || progress >= updateCount) {
                    updateCount += 1;
                    if (mCallback != null) {
                        final long finalFileSizeDownloaded = fileSizeDownloaded;
                        final long finaProgress = fileSizeDownloaded;
                        final long finalFileSize = fileSize;
                        Observable.just(fileSizeDownloaded)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<Long>() {
                                    @Override
                                    public void accept(Long aLong) throws Exception {
                                        if (mCallback instanceof DownloadProgressCallBack) {
                                            ((DownloadProgressCallBack) mCallback).onProgress(mTag, finalFileSizeDownloaded, finalFileSize, finaProgress);
                                        }
                                    }
                                });
                    }
                }
            }

            outputStream.flush();

            if (mCallback != null)
                Observable.just(filePath).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String path) throws Exception {
                                if (mCallback instanceof DownloadProgressCallBack) {
                                    ((DownloadProgressCallBack) mCallback).onSucess(mTag, path);
                                }
                            }
                        });

        } catch (IOException e) {
            finalOnError(e);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }

                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                finalOnError(e);
            }
        }
    }

    @Override
    public void onError(ApiThrowable throwable) {
        if (mCallback != null) {
            mCallback.onError(mTag, throwable);
        }
    }

    @Override
    public void onComplete() {
        if (mCallback != null) {
            mCallback.onCompleted(mTag);
        }
    }

    private void finalOnError(Exception e) {
        if (mCallback != null)
            Observable.just(e)
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(new Function<Exception, ApiThrowable>() {
                        @Override
                        public ApiThrowable apply(Exception e) throws Exception {
                            return ExceptionFactory.handleException(e);
                        }
                    }).subscribe(new Consumer<ApiThrowable>() {
                @Override
                public void accept(ApiThrowable apiThrowable) throws Exception {
                    mCallback.onError(mTag, apiThrowable);
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) {
                    mCallback.onError(mTag, ExceptionFactory.handleException(throwable));
                }
            });
    }
}
