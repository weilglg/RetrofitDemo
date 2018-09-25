package com.retrofit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory;
import com.retrofit.network.RxHttp;
import com.retrofit.network.callback.ResponseGenericsCallback;
import com.retrofit.network.callback.ResponseStringCallback;
import com.retrofit.network.callback.ResponseTemplateCallback;
import com.retrofit.network.config.ResultConfigLoader;
import com.retrofit.network.exception.ApiThrowable;
import com.retrofit.network.util.LogUtil;

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
        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                post();
            }
        });
        findViewById(R.id.tv2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject param = new JSONObject();
                param.put("pageSize", 1);
                param.put("pageNum", 10);
                RxHttp.getInstance().init(getBaseContext())
                        .baseUrl("https://ygzk.ygego.cn/api/")
                        .isLog(true)
                        .callAdapterFactory(RxJava2CallAdapterFactory.create())
                        .converterFactory(new Retrofit2ConverterFactory())
                        .post("home/hotnews")
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
        RxHttp.getInstance().init(getBaseContext())
                .baseUrl("https://ygzk.ygego.cn/api/")
                .isLog(true)
                .callAdapterFactory(RxJava2CallAdapterFactory.create())
                .converterFactory(new Retrofit2ConverterFactory())
                .post("home/hotnews")
                .jsonObj(param)
                .execute("CCCC", new ResponseTemplateCallback<String>() {

                    @Override
                    public boolean checkSuccessCode(int code, String msg) {
                        return false;
                    }

                    @Override
                    public void onStart(Object tag) {
                        LogUtil.e("MainActivity", "onStart");
                    }

                    @Override
                    public void onSuccess(Object tag, String result) {
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
}
