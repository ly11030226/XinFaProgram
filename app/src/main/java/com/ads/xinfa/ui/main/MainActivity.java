package com.ads.xinfa.ui.main;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ads.xinfa.HideNavigationBar;
import com.ads.xinfa.R;
import com.ads.xinfa.base.BaseActivity;
import com.ads.xinfa.base.Constant;
import com.ads.xinfa.base.FileManager;
import com.ads.xinfa.base.MyLogger;
import com.ads.xinfa.download.DownloadInfo;
import com.ads.xinfa.installtools.AutoInstallUtil;
import com.ads.xinfa.net.HeartBeatService;
import com.ads.xinfa.net.OkHttp3Manager;
import com.ads.xinfa.utils.BaseUtils;

import java.io.File;
import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * 主界面
 * @author Ly
 */
@RuntimePermissions
public class MainActivity extends BaseActivity implements MainContract.MainView {
    private static final String TAG = "MainActivity";
    @BindView(R.id.tv)
    TextView tv;
    @BindView(R.id.main_progress1)
    ProgressBar progress;
    @BindView(R.id.ll)
    LinearLayout ll;
    @BindView(R.id.rl)
    RelativeLayout rl;
    private MainContract.MainPresenter mMainPresenter;
    private HeartBeatService mHeartBeatService;
    private ServiceConnection mServiceConnection;
    private MyHandler mHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        try {
            new HideNavigationBar().hideNavigationBar(this);
            mMainPresenter = new MainPresenterImpl(this,mHandler);
            MainActivityPermissionsDispatcher.checkStoreWithPermissionCheck(this);
            initService();
            //Test
//            mMainPresenter.uploadFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initService() {
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MyLogger.i(TAG,"HeartBeatService Connected");
                HeartBeatService.HeartBeatBinder binder = (HeartBeatService.HeartBeatBinder) service;
                mHeartBeatService = binder.getService();
                //连接成功 发送心跳数据
                MainPresenterImpl mpi = (MainPresenterImpl) mMainPresenter;
                mHeartBeatService.sendHeartBeatData(mpi.getmXmlDataOperator());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                MyLogger.i(TAG,"HeartBeatService Disconnected");
            }
        };
        Intent intent = new Intent(MainActivity.this,HeartBeatService.class);
        MyLogger.i(TAG,"start bind service");
        bindService(intent,mServiceConnection,BIND_AUTO_CREATE);
    }

    @Override
    public void showDialog() {

    }

    @Override
    public void hideDialog() {

    }

    @Override
    public void updateView(ViewGroup vg) {
        rl.addView(vg);
    }

    @Override
    public void showDownloadNum(String num) {
        tv.setText(num);
    }

    @Override
    public void updateProgress(DownloadInfo value) {
        progress.setMax((int)(value.getTotal()/1024));
        progress.setProgress((int)(value.getProgress()/1024));
    }

    @Override
    public void downloadFileSuccess(String results[]) {
        if (progress.getProgress()>=progress.getMax()){
            String configString = results[0];
            String dataString = results[1];
            //将xml写入到本地存储目录中
            mMainPresenter.writeTxtToFile(configString, FileManager.XML_DIR, FileManager.XML_CONFIG);
            //将xml写入到本地存储目录中
            mMainPresenter.writeTxtToFile(dataString, FileManager.XML_DIR, FileManager.XML_DATA);//将xml写入到本地存储目录中
            ll.setVisibility(View.GONE);
            rl.removeAllViews();
            mMainPresenter.playView();
        }
    }

    @Override
    public void downloadFileFail() {
        ll.setVisibility(View.GONE);
    }

    @Override
    public void setPresenter(MainContract.MainPresenter presenter) {
        this.mMainPresenter = presenter;
    }

    @Override
    public void showTip(String msg) {

    }

    @Override
    public void showError(String error) {

    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public Context getContext() {
        return this;
    }



    static class MyHandler extends android.os.Handler{
        private final WeakReference<MainActivity> actiity;

        public MyHandler(MainActivity activity) {
            this.actiity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity mMainActivity = actiity.get();
            if (mMainActivity!=null) {
                try {
                    switch (msg.what) {
                        case Constant.UPDATE_XML_FILE:
                            mMainActivity.ll.setVisibility(View.VISIBLE);
                            //下载xml文件
                            mMainActivity.mMainPresenter.downloadFile();
                            break;
                        case Constant.GET_SNAPSHOOT:
                            //上传文件
                            mMainActivity.mMainPresenter.uploadFile();
                            break;
                        case Constant.UPDATE_APK:
                            //下载apk
                            mMainActivity.mMainPresenter.downloadApk();
                            break;
                        case Constant.INSTALL_APK:
                            //安装apk
                            AutoInstallUtil.install(
                                    mMainActivity,
                                    Constant.PATH_DOWNLOAD_APK+ File.separator+Constant.NAME_DOWNLOAD_APK);
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        OkHttp3Manager.getOkHttpClient().dispatcher().cancelAll();
        mMainPresenter.clear();
        mHandler.removeCallbacksAndMessages(null);
        unbindService(mServiceConnection);
        super.onDestroy();
    }

    /************************************权限相关********************************/

    private AlertDialog mAlertDialog;

    private AlertDialog notAskDialog;
    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void checkStore(){
        MyLogger.i(TAG,"checkContactPermission");
        mMainPresenter.playView();
        mMainPresenter.doUDPConnect();
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showRationaleForContact(final PermissionRequest request) {
        if (mAlertDialog==null) {
            mAlertDialog = new AlertDialog.Builder(this)
                    .setMessage(BaseUtils.getStringByResouceId(R.string.auth_contact_permission_remind))
                    .setPositiveButton(R.string.auth_contact_permission_open, (dialog, button) -> request.proceed())
                    .setNegativeButton(R.string.auth_contact_permission_cancel, (dialog, button) -> request.cancel())
                    .setCancelable(false)
                    .create();
            mAlertDialog.setCanceledOnTouchOutside(false);
        }
        mAlertDialog.show();
    }
    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showDeniedForContact() {
        MyLogger.i(TAG,"READ_CONTACTS Denied");
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showNeverAskForContact() {
        MyLogger.i(TAG,"READ_CONTACTS NeverAskFor");
        if (notAskDialog==null) {
            notAskDialog = new AlertDialog.Builder(this)
                    .setMessage(BaseUtils.getStringByResouceId(R.string.auth_contact_permission_not_ask))
                    .setPositiveButton(R.string.auth_contact_permission_cancel,(dialog,button)-> BaseUtils.jumpSystemSet(MainActivity.this))
                    .setNegativeButton(R.string.auth_contact_permission_cancel, (dialog, button) -> notAskDialog.dismiss())
                    .setCancelable(false)
                    .create();
            notAskDialog.setCanceledOnTouchOutside(false);
        }
        notAskDialog.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


/************************************权限相关********************************/

}
