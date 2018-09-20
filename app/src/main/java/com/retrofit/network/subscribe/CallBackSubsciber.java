/*
 * Copyright (C) 2017 zhouyou(478319399@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.retrofit.network.subscribe;

import android.content.Context;

import com.retrofit.network.exception.CommThrowable;
import com.retrofit.network.exception.ExceptionFactory;

import okhttp3.ResponseBody;


/**
 * <p>描述：带有callBack的回调</p>
 * 主要作用是不需要用户订阅，只要实现callback回调<br>
 * 作者： zhouyou<br>
 * 日期： 2016/12/28 17:10<br>
 * 版本： v2.0<br>
 */
public class CallBackSubsciber<T> extends BaseSubscriber<T> {
    private ResponseCallback<T> callback;
    private Object tag = null;

    public CallBackSubsciber(ResponseCallback<T> callback, Object tag) {
        this.callback = callback;
        this.tag = tag;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (callback != null) {
            callback.onStart(tag);
        }
    }

    @Override
    public void onComplete() {
        super.onComplete();
        if (callback != null) {
            callback.onCompleted(tag);
        }
    }

    @Override
    public void onNext(T result) {
//        try {
//            if (callback != null) {
//                callback.onSuccess(tag, callback.onTransformationResponse(tag, result));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            if (callback != null) {
//                callback.onError(tag, ExceptionFactory.handleException(e));
//            }
//        }
    }

    @Override
    public void onError(CommThrowable throwable) {
        if (callback != null) {
            callback.onError(tag, throwable);
        }
    }
}
