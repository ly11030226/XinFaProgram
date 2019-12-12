package com.ads.xinfa.net;


import com.ads.xinfa.BuildConfig;
import com.ads.xinfa.base.Constant;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * 观察者装饰器
 */
public class ObservableDecorator {

    public static <T> Observable<T> decorate(Observable<T> observable) {
        Observable<T> newObservable;
        if(Constant.isUnitTest) {
            newObservable = observable.subscribeOn(Schedulers.trampoline())
                    .observeOn(Schedulers.trampoline());
        } else {
            if (BuildConfig.API_ENV) {
                newObservable = observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }else{
                newObservable = observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .delay(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread()); // FIXME 模拟延迟,用于观察加载框等效果
            }
        }
        return newObservable;
    }
}
