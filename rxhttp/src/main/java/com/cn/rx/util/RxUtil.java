package com.cn.rx.util;

import com.cn.rx.entity.ApiResultEntity;
import com.cn.rx.func.HandleResultFunc;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RxUtil {

    public static <T> ObservableTransformer<T, T> io_main() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
                return upstream
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(@NonNull Disposable disposable) throws Exception {
                                LogUtil.i("+++doOnSubscribe+++" + disposable.isDisposed());
                            }
                        })
                        .doFinally(new Action() {
                            @Override
                            public void run() throws Exception {
                                LogUtil.i("+++doFinally+++");
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static <T> ObservableTransformer<T, T> _io_main() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
                return upstream
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(@NonNull Disposable disposable) throws Exception {
                                LogUtil.i("+++doOnSubscribe+++" + disposable.isDisposed());
                            }
                        })
                        .doFinally(new Action() {
                            @Override
                            public void run() throws Exception {
                                LogUtil.i("+++doFinally+++");
                            }
                        });
            }
        };
    }

    public static <T> ObservableTransformer<ApiResultEntity<T>, T> _io_main_result() {
        return new ObservableTransformer<ApiResultEntity<T>, T>() {
            @Override
            public ObservableSource<T> apply(@NonNull Observable<ApiResultEntity<T>> upstream) {
                return upstream
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(new HandleResultFunc<T>())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(@NonNull Disposable disposable) throws Exception {
                                LogUtil.i("+++doOnSubscribe+++" + disposable.isDisposed());
                            }
                        })
                        .doFinally(new Action() {
                            @Override
                            public void run() throws Exception {
                                LogUtil.i("+++doFinally+++");
                            }
                        });
            }
        };
    }


    public static <T> ObservableTransformer<T, T> _main() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(@NonNull Observable<T> upstream) {
                return upstream
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(@NonNull Disposable disposable) throws Exception {
                                LogUtil.i("+++doOnSubscribe+++" + disposable.isDisposed());
                            }
                        })
                        .doFinally(new Action() {
                            @Override
                            public void run() throws Exception {
                                LogUtil.i("+++doFinally+++");
                            }
                        });
            }
        };
    }

    public static <T> ObservableTransformer<ApiResultEntity<T>, T> _main_result() {
        return new ObservableTransformer<ApiResultEntity<T>, T>() {
            @Override
            public ObservableSource<T> apply(@NonNull Observable<ApiResultEntity<T>> upstream) {
                return upstream
                        .map(new HandleResultFunc<T>())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(@NonNull Disposable disposable) throws Exception {
                                LogUtil.i("+++doOnSubscribe+++" + disposable.isDisposed());
                            }
                        })
                        .doFinally(new Action() {
                            @Override
                            public void run() throws Exception {
                                LogUtil.i("+++doFinally+++");
                            }
                        });
            }
        };
    }


}
