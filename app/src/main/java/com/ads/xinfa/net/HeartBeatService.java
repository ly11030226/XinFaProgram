package com.ads.xinfa.net;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import com.ads.xinfa.XMLDataOperator;
import com.ads.xinfa.base.MyLogger;
import com.ads.xinfa.utils.BaseUtils;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 发送心跳包的Service
 * @author Ly
 */
public class HeartBeatService extends Service {
    private static final String TAG = "HeartBeatService";
    private HeartBeatBinder mHeartBeatBinder = new HeartBeatBinder();

    private XMLDataOperator mXMLDataOperator;
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
    }


    public class HeartBeatBinder extends Binder {
        public HeartBeatService getService(){
            return HeartBeatService.this;
        }
    }

    public void sendHeartBeatData(XMLDataOperator mXMLDataOperator){
        this.mXMLDataOperator = mXMLDataOperator;
        try {
            int interval = Integer.valueOf(mXMLDataOperator.getInterval());
            //多线程并行处理定时任务时，Timer运行多个TimeTask时，
            //只要其中之一没有捕获抛出的异常，其它任务便会自动终止运行，
            //使用ScheduledExecutorService则没有这个问题。
            ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
            executorService.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    connectNet();
                }
            },0,interval, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connectNet(){
        if (mXMLDataOperator!=null && !TextUtils.isEmpty(mXMLDataOperator.getIp()) && !TextUtils.isEmpty(mXMLDataOperator.getPort())) {
            String ip = mXMLDataOperator.getIp();
            String port = mXMLDataOperator.getPort();
            String url = BaseUtils.getURL(ip,port)+"rt=heart&ip="+BaseUtils.getHostIP();
            MyLogger.i(TAG,"sendHeartBeatData url ... "+url);
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
}
