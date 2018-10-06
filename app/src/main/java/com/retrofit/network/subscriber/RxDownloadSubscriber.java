package com.retrofit.network.subscriber;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.retrofit.network.callback.ResultDownloadCallback;
import com.retrofit.network.exception.ApiThrowable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;


public class RxDownloadSubscriber<ResponseBody extends okhttp3.ResponseBody> extends BaseSubscriber<ResponseBody> {
    private static final String APK_CONTENTTYPE = "application/vnd.android.package-archive";
    private static final String PNG_CONTENTTYPE = "image/png";
    private static final String JPG_CONTENTTYPE = "image/jpg";

    private ResultDownloadCallback mCallback;
    private String path;
    private String name;
    private Object tag;
    private String fileSuffix;

    public RxDownloadSubscriber(Context context, String path, String name, ResultDownloadCallback mCallback, Object tag) {
        super(context);
        this.mCallback = mCallback;
        this.path = path;
        this.name = name;
        this.tag = tag;
    }

    @Override
    protected void onStart() {
        if (mCallback != null) {
            mCallback.onUIProgressStart(tag);
        }
    }

    @Override
    public void onComplete() {
        if (mCallback != null) {
            mCallback.onUIProgressFinish(tag);
        }
    }

    @Override
    public void onError(ApiThrowable throwable) {
        if (mCallback != null) {
            mCallback.onError(tag, throwable);
        }
    }

    @Override
    public void onNext(ResponseBody responseBody) {
        writeResponseBodyToDisk(path, name, contextWeakReference.get(), responseBody);
    }

    private void writeResponseBodyToDisk(String path, String name, Context context, ResponseBody body) {
        if (!TextUtils.isEmpty(name)) {//text/html; charset=utf-8
            String type;
            if (!name.contains(".")) {
                type = body.contentType().toString();
                if (type.equals(APK_CONTENTTYPE)) {
                    fileSuffix = ".apk";
                } else if (type.equals(PNG_CONTENTTYPE)) {
                    fileSuffix = ".png";
                } else if (type.equals(JPG_CONTENTTYPE)) {
                    fileSuffix = ".jpg";
                } else {
                    fileSuffix = "." + body.contentType().subtype();
                }
                name = name + fileSuffix;
            }
        } else {
            name = System.currentTimeMillis() + fileSuffix;
        }
        if (path == null) {
            path = context.getExternalFilesDir(null) + File.separator + name;
        } else {
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            path = path + File.separator + name;
            path = path.replaceAll("//", "/");
        }
        File futureStudioIconFile = new File(path);
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(futureStudioIconFile);
            inputStream = body.byteStream();
            outputStream.write(inputStream.read());
            outputStream.flush();
        } catch (IOException e) {
            finalonError(e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                finalonError(e);
            }
        }
    }

    @SuppressLint("CheckResult")
    private void finalonError(final Exception e) {
        if (mCallback == null) {
            return;
        }
        //if (Utils.checkMain()) {
        Observable.just(e).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Exception>() {
            @Override
            public void accept(@NonNull Exception e) throws Exception {
                onError(e);
            }
        });
    }
}
