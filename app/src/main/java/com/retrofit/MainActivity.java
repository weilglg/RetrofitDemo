package com.retrofit;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory;
import com.retrofit.network.Demo;
import com.retrofit.network.RxHttp;
import com.retrofit.network.callback.ResultDownloadCallback;
import com.retrofit.network.entity.UploadFileType;
import com.retrofit.network.callback.ResponseTemplateCallback;
import com.retrofit.network.callback.ResultCallbackProxy;
import com.retrofit.network.callback.ResultProgressCallback;
import com.retrofit.network.config.ResultConfigLoader;
import com.retrofit.network.exception.ApiThrowable;
import com.retrofit.network.request.CommPostRequest;
import com.retrofit.network.util.LogUtil;
import com.retrofit.network.util.TestApi;

import java.io.File;
import java.util.List;

import io.reactivex.observers.DisposableObserver;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import static android.os.Environment.getExternalStorageState;

public class MainActivity extends AppCompatActivity {

    private RxHttp util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo);
        ResultConfigLoader.init(getBaseContext());
        RxHttp.getInstance().init(getBaseContext())
                .baseUrl("https://ygzk.ygego.cn/api/")
                .isLog(true)
                .callAdapterFactory(RxJava2CallAdapterFactory.create())
                .converterFactory(new Retrofit2ConverterFactory());
        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                post();
            }
        });
        findViewById(R.id.tv2).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CheckResult")
            @Override
            public void onClick(View view) {
                JSONObject param = new JSONObject();
                param.put("pageSize", 1);
                param.put("pageNum", 10);
                RxHttp.post("home/hotnews")
                        .jsonObj(param)
                        .execute(String.class).subscribeWith(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        LogUtil.e("MainActivity", "result=" + s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtil.e("MainActivity", "onError=" + e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }
        });
    }

    private void post() {
        JSONObject param = new JSONObject();
        param.put("pageSize", 1);
        param.put("pageNum", 10);

        RxHttp.post("home/hotnews")
                .addHeader("11", "2222")
                .addHeader("22", "2222")
                .addHeader("33", "2222")
                .addHeader("44", "2222")
                .addHeader("55", "2222")
                .addHeader("66", "2222")
                .addHeader("77", "2222")
                .jsonObj(param)
                .execute("CCCC", new ResponseTemplateCallback<Demo<List<String>>>() {

                    @Override
                    public boolean checkSuccessCode(int code, String msg) {
                        return code == 0;
                    }

                    @Override
                    public void onStart(Object tag) {
                        LogUtil.e("MainActivity", "onStart");
                    }

                    @Override
                    public void onSuccess(Object tag, Demo<List<String>> result) {
                        LogUtil.e("MainActivity", "result=" + result);
                    }

                    @Override
                    public void onCompleted(Object tag) {

                    }

                    @Override
                    public void onError(Object tag, ApiThrowable throwable) {
                        Log.e("tag", "onError=" + throwable.getMessage());
                    }
                });


//                .execute("cccc", new ResponseGenericsCallback<String>() {
//                    @Override
//                    public void onError(Object tag, ApiThrowable throwable) {
//                        LogUtil.e("MainActivity", "throwable=" + throwable.toString());
//                    }
//
//                    @Override
//                    public void onSuccess(Object tag, String result) {
//                        LogUtil.e("MainActivity", "result=" + result);
//                    }
//                });
    }


    public void post_2(View v) {
        JSONObject param = new JSONObject();
        param.put("pageSize", 1);
        param.put("pageNum", 10);

        RxHttp.resultPost("home/hotnews")
                .jsonObj(param)
                .execute("resultPost", new ResultCallbackProxy<TestApi<String>, String>(new ResultProgressCallback<String>() {
                    @Override
                    public void onUIProgressChanged(Object mTag, long numBytes, long totalBytes, float percent, float speed) {
                        Toast.makeText(MainActivity.this, "numBytes=" + numBytes + "  " + "totalBytes=" + totalBytes + "  " + "percent=" + percent + "  " + "speed=" + speed, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onStart(Object tag) {
                        Log.e("tag", "onStart");
                    }

                    @Override
                    public void onCompleted(Object tag) {
                        Log.e("tag", "onCompleted");
                    }

                    @Override
                    public void onError(Object tag, ApiThrowable throwable) {
                        Log.e("tag", "onError=" + throwable.getMessage());
                    }

                    @Override
                    public void onSuccess(Object tag, String s) {
                        Log.e("tag", "onSuccess=" + s);
                    }
                }) {
                });
//                .execute("resultPost", new ResultCallback<String>() {
//                    @Override
//                    public void onStart(Object tag) {
//                        Log.e("tag", "onCompleted");
//                    }
//
//                    @Override
//                    public void onCompleted(Object tag) {
//                        Log.e("tag", "onCompleted");
//                    }
//
//                    @Override
//                    public void onError(Object tag, ApiThrowable throwable) {
//                        Log.e("tag", "onError=" + throwable.getMessage());
//                    }
//
//                    @Override
//                    public void onSuccess(Object tag, String s) {
//                        Log.e("tag", "onSuccess=" + s);
//                    }
//                });
    }

    public void uploadPartFile(View v) {
        File file = new File(Environment.getExternalStorageDirectory() +
                File.separator + "1.jpg");
        File file2 = new File(Environment.getExternalStorageDirectory() +
                File.separator + "2.jpg");
        new CommPostRequest("upload5273")
                .param("appId", "27")
                .baseUrl("http://business-workbench.qingtian.ygego.alpha3/rest/")
                .uploadType(UploadFileType.PART_FROM)
                .params("file", file)
                .params("file", file2)
                .execute("upload", new ResultProgressCallback<String>() {
                    @Override
                    public void onStart(Object tag) {
                        Toast.makeText(MainActivity.this, "开始上传", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCompleted(Object tag) {

                    }

                    @Override
                    public void onError(Object tag, ApiThrowable e) {

                    }

                    @Override
                    public void onSuccess(Object tag, String s) {
                        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onUIProgressChanged(Object tag, long numBytes, long totalBytes, float percent, float speed) {
                        Toast.makeText(MainActivity.this, "numBytes=" + numBytes + "  " + "totalBytes=" + totalBytes + "  " + "percent=" + percent + "  " + "speed=" + speed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void uploadBodyFile(View v) {
        File file = new File(Environment.getExternalStorageDirectory() +
                File.separator + "1.jpg");
        RequestBody requestBody1 = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("appId", "27")
                .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("image/*"), file))
                .build();
        new CommPostRequest("upload5273")
                .baseUrl("http://business-workbench.qingtian.ygego.alpha3/rest/")
                .uploadType(UploadFileType.BODY)
                .requestBody(requestBody1)
                .execute("upload", new ResultProgressCallback<String>() {
                    @Override
                    public void onStart(Object tag) {
                        Toast.makeText(MainActivity.this, "开始上传", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCompleted(Object tag) {

                    }

                    @Override
                    public void onError(Object tag, ApiThrowable e) {

                    }

                    @Override
                    public void onSuccess(Object tag, String s) {
                        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onUIProgressChanged(Object tag, long numBytes, long totalBytes, float percent, float speed) {
                        Toast.makeText(MainActivity.this, "numBytes=" + numBytes + "  " + "totalBytes=" + totalBytes + "  " + "percent=" + percent + "  " + "speed=" + speed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void downFile(View v) {
        RxHttp.download("http://res.qingtian.ygego.alpha3/userbusipub/M00/00/01/CiWTOluu1s-AYF60AAHpjiW__vM457.jpg")
                .saveName("111")
                .savePath(Environment.getExternalStorageDirectory().getAbsolutePath())
                .execute("file", new ResultDownloadCallback() {
                    @Override
                    public void onUIProgressChanged(Object mTag, long numBytes, long totalBytes, float percent, float speed) {
                        Log.e("TAG", "numBytes=" + numBytes + " totalBytes=" + totalBytes + " percent=" + percent + " speed=" + speed);
                    }

                });
    }

}
