package com.ads.xinfa.ui.fragmentMain;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.widget.FrameLayout;

import com.ads.xinfa.MyFragmentManager;
import com.ads.xinfa.R;
import com.ads.xinfa.base.BaseActivity;
import com.ads.xinfa.base.Constant;
import com.ads.xinfa.base.MyLogger;
import com.ads.xinfa.bean.MyBean;
import com.ads.xinfa.installtools.AutoInstallUtil;
import com.ads.xinfa.net.HeartBeatService;
import com.ads.xinfa.net.OkHttp3Manager;
import com.ads.xinfa.utils.BaseUtils;
import com.ads.xinfa.utils.ToastUtils;
import com.shuyu.gsyvideoplayer.GSYVideoManager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;

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
 * 含有Fragment的MainActivity
 */
@RuntimePermissions
public class FragmentMainActivity extends BaseActivity implements FragmentMainContract.FragmentMainView{
    private static final String TAG = "FragmentMainActivity";
    @BindView(R.id.fl_main)
    FrameLayout mFlMain;
    private MainHandler mHandler = new MainHandler(this);
    private FragmentMainContract.FragmentMainPresenter mFragmentMainPresenter;
    private ServiceConnection mServiceConnection;
    private HeartBeatService mHeartBeatService;
    private MyFragmentManager mMyFragmentManager = new MyFragmentManager(getSupportFragmentManager());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_fragment_main);
        ButterKnife.bind(this);
        try {
            MyLogger.i(TAG,"ip ... "+BaseUtils.getHostIP());
            mFragmentMainPresenter = new FragmentMainPresenterImpl(this, mHandler);
            initService();
            FragmentMainActivityPermissionsDispatcher.checkStoreWithPermissionCheck(this);
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
                FragmentMainPresenterImpl mpi = (FragmentMainPresenterImpl) mFragmentMainPresenter;
                mHeartBeatService.sendHeartBeatData(mpi.getmXmlDataOperator());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                MyLogger.i(TAG,"HeartBeatService Disconnected");
            }
        };
        Intent intent = new Intent(FragmentMainActivity.this, HeartBeatService.class);
        bindService(intent,mServiceConnection,BIND_AUTO_CREATE);
    }

    @Override
    public void setPresenter(FragmentMainContract.FragmentMainPresenter presenter) {
        this.mFragmentMainPresenter = presenter;
    }

    @Override
    public void showTip(String msg) {
        ToastUtils.showToast(this,msg);
    }

    @Override
    public void showError(String error) {
        showTip(error);
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void updateView(HashMap<String, MyBean.GroupsBean.GroupBean.AreasBean.AreaBean> map) {
        try {
            //当本地有xml数据文件的时候再去开启UDP
            mFragmentMainPresenter.doUDPConnect();
            if (mMyFragmentManager!=null) {
                mMyFragmentManager.showFragment(R.id.fl_main,map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void downloadFileSuccess(MyBean myBean) {
        //成功下载完xml数据文件，然后下载xml文件中所需要用到的图片或者是视频
        mFragmentMainPresenter.downloadImageOrVideo(myBean);
    }

    @Override
    public void downloadFileFail(String str) {
        ToastUtils.showToast(this,str);
    }

    @Override
    public void downloadImageAndVideoFail() {

    }

    @Override
    public void downloadImageAndVideoSuccess() {

    }

    static class MainHandler extends android.os.Handler {
        private final WeakReference<FragmentMainActivity> actiity;

        public MainHandler(FragmentMainActivity activity) {
            this.actiity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FragmentMainActivity mMainActivity = actiity.get();
            if (mMainActivity != null) {
                try {
                    switch (msg.what) {
                        case Constant.UPDATE_XML_FILE:
                            //下载xml文件
                            mMainActivity.mFragmentMainPresenter.downloadFile();
                            break;
                        case Constant.GET_SNAPSHOOT:
                            //上传文件
                            mMainActivity.mFragmentMainPresenter.uploadFile();
                            break;
                        case Constant.UPDATE_APK:
                            //下载apk
                            mMainActivity.mFragmentMainPresenter.downloadApk();
                            break;
                        case Constant.INSTALL_APK:
                            //安装apk
                            AutoInstallUtil.install(mMainActivity, Constant.PATH_DOWNLOAD_APK + File.separator + Constant.NAME_DOWNLOAD_APK);
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
        mFragmentMainPresenter.clear();
        mHandler.removeCallbacksAndMessages(null);
        unbindService(mServiceConnection);
        super.onDestroy();
    }

    public interface InitListener{
        void doInit(MyBean.GroupsBean.GroupBean.AreasBean.AreaBean areaBean);
    }


    @Override
    public void onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }



    /************************************权限相关********************************/

    private AlertDialog mAlertDialog;
    private AlertDialog notAskDialog;

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void checkStore(){
        MyLogger.i(TAG,"checkContactPermission");
        mFragmentMainPresenter.playView();
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
                    .setPositiveButton(R.string.auth_contact_permission_cancel,(dialog,button)-> BaseUtils.jumpSystemSet(FragmentMainActivity.this))
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
        FragmentMainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    /************************************权限相关********************************/

}
