package com.retrofit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.retrofit.network.Demo;
import com.retrofit.network.RxHttp;
import com.retrofit.network.config.ResultConfigLoader;
import com.retrofit.network.exception.CommThrowable;
import com.retrofit.network.subscribe.ResponseCallback;
import com.retrofit.network.subscribe.ResponseGenericsCallback;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.observers.DisposableObserver;
import okhttp3.ResponseBody;

import static com.retrofit.network.Util.MULTIPART_JSON_DATA;

public class MainActivity extends AppCompatActivity {

    private RxHttp util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo);
        ResultConfigLoader.init(getBaseContext());

        HashMap<String, String> map = new HashMap<>();
        map.put("111", "2222");
        util = RxHttp.createBuilder(getApplicationContext())
                .baseUrl("http://www.baidu.com")
                .connectTimeOut(60)
                .readTimeOut(100)
                .context(this)
                .addHeader(map)
                .build();
//        Log.e("TAG", util.toString());
        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResponseGenericsCallback<Demo> demo = new ResponseGenericsCallback<Demo>() {
                    @Override
                    protected boolean checkSuccessCode(int code, String msg) {
                        if (code == 0) {
                            return true;
                        }
                        return false;
                    }

                    @Override
                    protected void onError(Object tag, CommThrowable throwable) {

                    }

                    @Override
                    protected void onSuccess(Object tag, Demo result) {
                        Log.e("tag", "====Demo=====" + result.toString());
                    }

                };
                try {
                    demo.onTransformationResponse(null, ResponseBody.create(okhttp3.MediaType.parse(MULTIPART_JSON_DATA), "{'code':'000000','resultData':{'name':'ceShi','pwd':'1234'}}"));

                } catch (Exception e) {
                    Log.e("tag", "====Exception=====" + e.toString());
                }

            }
        });
        findViewById(R.id.tv2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                RxHttp.restoreDefaultBuilder().build();
                Observable<Integer> just = Observable.just(1, 2, 3);
                DisposableObserver disposableObserver = util.rxHttp(null, just, new ResponseCallback<Integer>() {
                    @Override
                    protected void onError(Object tag, CommThrowable throwable) {
                        Log.e("TAG", "onError");
                    }

                    @Override
                    protected void onSuccess(Object tag, Integer result) {
                        Log.e("TAG", "onSuccess");
                    }

                    @Override
                    public Integer onTransformationResponse(Object tag, ResponseBody body) throws Exception {
                        return null;
                    }
                });
            }
        });
    }
}
