package com.jzl.xinfafristversion.net;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import com.jzl.xinfafristversion.XMLDataManager;
import com.jzl.xinfafristversion.base.MyLogger;
import com.jzl.xinfafristversion.utils.BaseUtils;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class HeartBeatService extends Service {
    private static final String TAG = "HeartBeatService";
    private HeartBeatBinder mHeartBeatBinder = new HeartBeatBinder();
    private Timer mTimer;
    private TimerTask mTimerTask;

    public HeartBeatService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mHeartBeatBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        MyLogger.i(TAG,"onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        MyLogger.i(TAG,"onDestroy");
        super.onDestroy();
        release();
    }


    public class HeartBeatBinder extends Binder {
        public HeartBeatService getService(){
            return HeartBeatService.this;
        }
    }

    public void sendHeartBeatData(){
        try {
            if (mTimer == null) {
                mTimer = new Timer();
            }
            if (mTimerTask == null) {
                mTimerTask = new TimerTask() {
                    int i = 1;
                    @Override
                    public void run() {
//                        MyLogger.i(TAG,"num i ... "+(i++));
                        connectNet();
                    }
                };
            }
            if (!TextUtils.isEmpty(XMLDataManager.interval)) {
                int interval = Integer.valueOf(XMLDataManager.interval)*1000;
                mTimer.schedule(mTimerTask,0,interval);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connectNet(){
        if (!TextUtils.isEmpty(XMLDataManager.ip) && !TextUtils.isEmpty(XMLDataManager.port)) {
            String ip = XMLDataManager.ip;
            String port = XMLDataManager.port;
            String url = BaseUtils.getURL(ip,port)+"rt=heart&ip="+BaseUtils.getHostIP();
//            MyLogger.i(TAG,"sendHeartBeatData url ... "+url);
            Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
            OkHttp3Manager.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    MyLogger.i(TAG,"******* sendHeartBeatData fail ******* ");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    MyLogger.i(TAG,"******* sendHeartBeatData success ******* ");
                }
            });
        }
    }
    private void release(){
        if (mTimerTask!=null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (mTimer!=null) {
            mTimer.purge();
            mTimer.cancel();
            mTimer = null;
        }
    }
}
