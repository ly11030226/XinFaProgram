package com.ads.clientconnection.application;

import android.app.Application;
import android.content.Context;

import com.ads.clientconnection.BuildConfig;
import com.ads.clientconnection.base.CrashHandler;
import com.tencent.bugly.crashreport.CrashReport;

public class ClientApplication extends Application {

    private static Context instance;
    public static Context getInstance(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = getApplicationContext();
        try {
            CrashHandler.getInstance().init(instance);
            //访问共享文件用到
            System.setProperty("jcifs.smb.client.dfs.disabled", "true");
            System.setProperty("jcifs.smb.client.soTimeout", "1000000");
            System.setProperty("jcifs.smb.client.responseTimeout", "30000");

            /**
             * 第三个参数为SDK调试模式开关，调试模式的行为特性如下：
             * 输出详细的Bugly SDK的Log；
             * 每一条Crash都会被立即上报；
             * 自定义日志将会在Logcat中输出。
             * 建议在测试阶段建议设置成true，发布时设置为false。
             */
            CrashReport.initCrashReport(getApplicationContext(), "30e93a44bd", !BuildConfig.API_ENV);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        androidx.multidex.MultiDex.install(this);
    }
}
