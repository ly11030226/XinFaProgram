package com.szty.h5xinfa.ui;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.szty.h5xinfa.Constant;
import com.szty.h5xinfa.R;
import com.szty.h5xinfa.XmlManager;
import com.szty.h5xinfa.util.BaseUtils;

import java.io.File;
import java.lang.ref.WeakReference;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.annotations.NonNull;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

@RuntimePermissions
public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";
    private MyHandler handler;
    private MaterialDialog noPermissionDialog, noAskDialog;
    private static int handleCode;
    private TextView tvVersion;

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        try {
            /**
             * MyHandler 是用来跳转到 MainActivity的
             * 并且将obj封装的what属性传递给MainActivity，what包括了在启动界面处理逻辑后的所有结果
             */
            handler = new MyHandler(WelcomeActivity.this);
            //显示版本号
            showVersion();
            //获取 android/data/packagename/files/szty 目录
            File file = WelcomeActivity.this.getExternalFilesDir(Constant.PATH_SZTY);
            File configF = new File(file,Constant.PATH_CONFIG);
            //如果android/data/packagename/files/szty/config 目录不存在 则要创建各级文件夹
            if (!configF.exists()) {
                configF.mkdirs();
                //发送消息
                handler.sendEmptyMessage(Constant.FILE_NOT_EXIST);
            }else{
                //单独开辟一个线程用来加载config目录下面的xml数据
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            XmlManager.getInstance().loadXmlData(handler,WelcomeActivity.this);
                        } catch (Exception e) {
                            e.printStackTrace();
                            handler.sendEmptyMessage(Constant.LOAD_XML_ERROR);
                        }
                    }
                }).start();
            }
            //开启EXO模式
            PlayerFactory.setPlayManager(Exo2PlayerManager.class);
            //ijk关闭log
            IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT);
            //切换渲染模式
            GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 显示版本号
     */
    private void showVersion() {
        tvVersion = findViewById(R.id.tv_version);
        String version = BaseUtils.getVersionCode(WelcomeActivity.this);
        if (TextUtils.isEmpty(version)) {
            tvVersion.setVisibility(View.GONE);
        }else{
            tvVersion.setVisibility(View.VISIBLE);
            tvVersion.setText("v "+version);
        }
        Log.d(TAG, "showVersion: "+version);
    }

    private void requestPermissions(){
        WelcomeActivityPermissionsDispatcher.checkPermissionWithPermissionCheck(this);
    }


    private static class MyHandler extends Handler{
        WeakReference<WelcomeActivity> activity;

        public MyHandler(WelcomeActivity a) {
            activity = new WeakReference<WelcomeActivity>(a);
        }

        @Override
        public void handleMessage(@androidx.annotation.NonNull Message msg) {
            WelcomeActivity a = activity.get();
            if (a != null) {
                handleCode = msg.what;
                a.requestPermissions();
            }
        }
    }



    ////////////////////////////权限相关///////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})
    void checkPermission() {
        try {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(WelcomeActivity.this, IndexActivity.class);
                    intent.putExtra(Constant.KEY_HANDLE_CODE,handleCode);
                    startActivity(intent);
                    finish();
                }
            }, 2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnShowRationale({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})
    void noPermission(PermissionRequest request) {
        Log.i(TAG, "noPermission");
        if (noPermissionDialog==null) {
            noPermissionDialog = new MaterialDialog.Builder(this)
                    .title(R.string.dialog_title)
                    .content(R.string.res_no_permission)
                    .positiveText(R.string.dialog_commit)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@androidx.annotation.NonNull MaterialDialog dialog, @androidx.annotation.NonNull DialogAction which) {
                            request.proceed();
                        }
                    })
                    .negativeText(R.string.dialog_cancel)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@androidx.annotation.NonNull MaterialDialog dialog, @androidx.annotation.NonNull DialogAction which) {
                            request.cancel();
                        }
                    })
                    .build();
        }
        noPermissionDialog.show();
    }

    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})
    void reject() {
        Log.i(TAG, "reject");
    }

    @OnNeverAskAgain({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})
    void rejectAndNOAsk() {
        Log.i(TAG, "rejectAndNOAsk");
        if (noAskDialog==null) {
            noAskDialog = new MaterialDialog.Builder(this)
                    .title(R.string.dialog_title)
                    .content(R.string.res_no_permission)
                    .positiveText(R.string.dialog_confirm)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@androidx.annotation.NonNull MaterialDialog dialog, @androidx.annotation.NonNull DialogAction which) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", WelcomeActivity.this.getPackageName(), null);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    })
                    .negativeText(R.string.dialog_do_not)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@androidx.annotation.NonNull MaterialDialog dialog, @androidx.annotation.NonNull DialogAction which) {
                            noAskDialog.dismiss();
                        }
                    })
                    .build();
        }
        noAskDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        WelcomeActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    ////////////////////////////权限相关///////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        return;
    }

}
