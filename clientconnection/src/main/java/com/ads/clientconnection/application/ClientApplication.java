package com.ads.clientconnection.application;

import android.app.Application;
import android.content.Context;

import com.ads.clientconnection.BuildConfig;
import com.ads.clientconnection.R;
import com.ads.clientconnection.ui.ErrorActivity;
import com.ads.clientconnection.ui.WelcomeActivity;
import com.tamsiree.rxkit.RxTool;
import com.tamsiree.rxkit.TLog;
import com.tamsiree.rxkit.crash.TCrashProfile;
import com.tamsiree.rxkit.crash.TCrashTool;
import com.tencent.bugly.crashreport.CrashReport;

public class ClientApplication extends Application {
    public static final String TAG = "ClientApplication";
    private static Context instance;

    public static Context getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = getApplicationContext();
        try {
//            CrashHandler.getInstance().init(instance);
            //访问共享文件用到
            System.setProperty("jcifs.smb.client.dfs.disabled", "true");
            System.setProperty("jcifs.smb.client.soTimeout", "1000000");
            System.setProperty("jcifs.smb.client.responseTimeout", "30000");

            initCrashTool();
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

    private void initCrashTool() {
        RxTool.init(this)
                .debugLog(true)
                .crashLogFile(true)
                .crashProfile()
                //以下为崩溃配置
                .backgroundMode(TCrashProfile.BACKGROUND_MODE_SILENT)
                .enabled(true)
                .showErrorDetails(true)
                .showRestartButton(true)
                .logErrorOnRestart(true)
                .trackActivities(true)
                .minTimeBetweenCrashesMs(2000)
                .errorDrawable(R.drawable.crash_logo)
                .restartActivity(WelcomeActivity.class)
                .errorActivity(ErrorActivity.class).
                eventListener(new TCrashTool.EventListener() {
            @Override
            public void onLaunchErrorActivity() {
                TLog.e(TAG,"奔溃回调 onLaunchErrorActivity");
            }

            @Override
            public void onRestartAppFromErrorActivity() {
                TLog.e(TAG,"奔溃回调 onRestartAppFromErrorActivity");
            }

            @Override
            public void onCloseAppFromErrorActivity() {
                TLog.e(TAG,"奔溃回调 onCloseAppFromErrorActivity");
            }
        }).apply();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        androidx.multidex.MultiDex.install(this);
    }
}
