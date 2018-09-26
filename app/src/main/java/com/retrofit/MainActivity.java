package com.retrofit;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory;
import com.retrofit.network.Demo;
import com.retrofit.network.RxHttp;
import com.retrofit.network.callback.ResponseGenericsCallback;
import com.retrofit.network.callback.ResponseStringCallback;
import com.retrofit.network.callback.ResponseTemplateCallback;
import com.retrofit.network.callback.ResultCallback;
import com.retrofit.network.callback.ResultCallbackProxy;
import com.retrofit.network.config.ResultConfigLoader;
import com.retrofit.network.entity.ApiResultEntity;
import com.retrofit.network.exception.ApiThrowable;
import com.retrofit.network.util.LogUtil;
import com.retrofit.network.util.TestApi;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

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

        RxHttp.post_2("home/hotnews")
                .jsonObj(param)
                .execute("post_2", new ResultCallbackProxy<TestApi<String>, String>(new ResultCallback<String>() {
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
//                .execute("post_2", new ResultCallback<String>() {
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


}
