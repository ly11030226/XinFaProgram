package com.szty.h5xinfa;

import com.szty.h5xinfa.baoao.L;
import com.szty.h5xinfa.baoao.Utils;

import androidx.multidex.MultiDexApplication;

public class MyApplication extends MultiDexApplication {
    private static final String TAG = "MyApplication";
    public static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Utils.init(this);
        L.getConfig().setGlobalTag("baoao");
        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
        //        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
        //
        //            @Override
        //            public void onViewInitFinished(boolean arg0) {
        //                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
        //                Log.i(TAG, " onViewInitFinished is " + arg0);
        //                String s;
        //                if (arg0) {
        //                    s = "x5内核加载成功";
        //                }else{
        //                    s = "x5内核加载失败";
        //                }
        //                Toast.makeText(MyApplication.this,s,Toast.LENGTH_LONG).show();
        //            }
        //
        //            @Override
        //            public void onCoreInitFinished() {
        //            }
        //        };
        //        //x5内核初始化接口
        //        QbSdk.initX5Environment(getApplicationContext(),  cb);
        //        QbSdk.setTbsListener(new TbsListener() {
        //            @Override
        //            public void onDownloadFinish(int i) {
        //                Log.i(TAG,"tbs内核下载完成回调");
        //                Toast.makeText(MyApplication.this,"tbs内核下载完成回调",Toast.LENGTH_LONG).show();
        //            }
        //
        //            @Override
        //            public void onInstallFinish(int i) {
        //                Log.i(TAG,"内核安装完成回调");
        //                Toast.makeText(MyApplication.this,"内核安装完成回调",Toast.LENGTH_LONG).show();
        //            }
        //
        //            @Override
        //            public void onDownloadProgress(int i) {
        //                Log.i(TAG,"下载进度监听");
        //            }
        //        });
    }
}
