package com.retrofit.network.func;

import android.support.annotation.NonNull;

import com.retrofit.network.exception.CommThrowable;
import com.retrofit.network.exception.ExceptionFactory;
import com.retrofit.network.util.LogUtil;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class RetryExceptionFunc implements Function<Observable<? extends Throwable>, Observable<?>> {
    /* retry次数*/
    private int count = 0;
    /*延迟*/
    private long delay = 500;
    /*叠加延迟*/
    private long increaseDelay = 3000;

    public RetryExceptionFunc() {

    }

    public RetryExceptionFunc(int count, long delay) {
        this.count = count;
        this.delay = delay;
    }

    public RetryExceptionFunc(int count, long delay, long increaseDelay) {
        this.count = count;
        this.delay = delay;
        this.increaseDelay = increaseDelay;
    }

    @Override
    public Observable<?> apply(@NonNull Observable<? extends Throwable> observable) throws Exception {
        return observable.zipWith(Observable.range(1, count + 1), new BiFunction<Throwable, Integer, Wrapper>() {
            @Override
            public Wrapper apply(@NonNull Throwable throwable, @NonNull Integer integer) throws Exception {
                return new Wrapper(throwable, integer);
            }
        }).flatMap(new Function<Wrapper, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(@NonNull Wrapper wrapper) throws Exception {
                if (wrapper.index > 1)
                    LogUtil.i("重试次数：" + (wrapper.index));
                int errCode = 0;
                if (wrapper.throwable instanceof CommThrowable) {
                    CommThrowable exception = (CommThrowable) wrapper.throwable;
                    errCode = exception.getCode();
                }
                if ((wrapper.throwable instanceof ConnectException
                        || wrapper.throwable instanceof SocketTimeoutException
                        || errCode == ExceptionFactory.ERROR.NETWORD_ERROR
                        || errCode == ExceptionFactory.ERROR.TIMEOUT_ERROR
                        || wrapper.throwable instanceof SocketTimeoutException
                        || wrapper.throwable instanceof TimeoutException)
                        && wrapper.index < count + 1) { //如果超出重试次数也抛出错误，否则默认是会进入onCompleted
                    return Observable.timer(delay + (wrapper.index - 1) * increaseDelay, TimeUnit.MILLISECONDS);

                }
                return Observable.error(wrapper.throwable);
            }
        });
    }

    private class Wrapper {
        private int index;
        private Throwable throwable;

        public Wrapper(Throwable throwable, int index) {
            this.index = index;
            this.throwable = throwable;
        }
    }

}
