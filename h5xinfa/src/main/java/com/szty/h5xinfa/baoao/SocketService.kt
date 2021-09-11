package com.szty.h5xinfa.baoao

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import kotlin.concurrent.thread

class SocketService : Service() {
    companion object {
        const val TAG = "SocketService"
    }

    var winNum by Preference(KEY_WINDOW_NUM, DEFAULT_WINDOW_NUM)
    var serverIp by Preference(KEY_SERVER_IP, DEFAULT_SERVER_IP)
    var serverPort by Preference(KEY_SERVER_PORT, DEFAULT_SERVER_PORT)
    private val binder = MyBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class MyBinder : Binder() {
        fun getService(): SocketService {
            return this@SocketService
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            //设置窗口号
            NettyClient.getInstance().setWinNumber(winNum)
            thread {
                NettyClient.getInstance().connect(serverIp, serverPort).subscribe(object :
                        Observer<Boolean?> {
                    override fun onSubscribe(d: @NonNull Disposable?) {}
                    override fun onNext(success: @NonNull Boolean?) {
                        if (success!!) {
                            Log.i(TAG, "onNext: 连接成功")
                        } else {
                            Log.i(TAG, "onNext: 连接失败")
                        }
                    }

                    override fun onError(e: @NonNull Throwable?) {}
                    override fun onComplete() {}
                }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand: ")
        return START_STICKY
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy: ")
        NettyClient.getInstance().disConnect()
        super.onDestroy()
    }
}
