package com.retrofit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.retrofit.Retrofit2ConverterFactory;
import com.retrofit.network.RxHttp;
import com.retrofit.network.config.ResultConfigLoader;
import com.retrofit.network.exception.ApiThrowable;
import com.retrofit.network.subscriber.ResponseGenericsCallback;
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
                Observable.just(1, 2, 3).onErrorResumeNext(new Function<Throwable, ObservableSource<? extends Integer>>() {
                    @Override
                    public ObservableSource<? extends Integer> apply(Throwable throwable) throws Exception {
                        return null;
                    }
                }).flatMap(new Function<Integer, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Integer i) throws Exception {
                        if (i == 2) {
                            return Observable.error(new NullPointerException());
                        }
                        return Observable.just(i);
                    }
                }).subscribeWith(new DisposableObserver() {
                    @Override
                    public void onNext(Object o) {
                        Log.e("TAG", "----------onNext-------");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("TAG", "--------onError-------");
                    }

                    @Override
                    public void onComplete() {
                        Log.e("TAG", "--------onComplete-------");
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
                .execute("cccc", new ResponseGenericsCallback<String>() {
                    @Override
                    protected void onError(Object tag, ApiThrowable throwable) {
                        LogUtil.e("MainActivity", "throwable=" + throwable.toString());
                    }

                    @Override
                    protected void onSuccess(Object tag, String result) {
                        LogUtil.e("MainActivity", "result=" + result);
                    }

                    @Override
                    protected boolean checkSuccessCode(int code, String msg) {
                        return code == 0;
                    }
                });
    }
}
