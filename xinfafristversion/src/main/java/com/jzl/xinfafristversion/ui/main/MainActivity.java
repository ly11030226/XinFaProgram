package com.jzl.xinfafristversion.ui.main;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jzl.xinfafristversion.HideNavigationBar;
import com.jzl.xinfafristversion.R;
import com.jzl.xinfafristversion.XMLDataManager;
import com.jzl.xinfafristversion.base.BaseActivity;
import com.jzl.xinfafristversion.base.Constant;
import com.jzl.xinfafristversion.base.FileManager;
import com.jzl.xinfafristversion.base.MyLogger;
import com.jzl.xinfafristversion.bean.ConfigBean;
import com.jzl.xinfafristversion.bean.MyBean.GroupsBean.GroupBean.AreasBean.AreaBean;
import com.jzl.xinfafristversion.bean.MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.InfoBean;
import com.jzl.xinfafristversion.bean.MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean;
import com.jzl.xinfafristversion.download.DownloadInfo;
import com.jzl.xinfafristversion.installtools.AutoInstallUtil;
import com.jzl.xinfafristversion.net.HeartBeatService;
import com.jzl.xinfafristversion.net.OkHttp3Manager;
import com.jzl.xinfafristversion.utils.BaseUtils;
import com.jzl.xinfafristversion.view.ResLoopView;
import com.jzl.xinfafristversion.view.ResViewGroup;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

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

@RuntimePermissions
public class MainActivity extends BaseActivity implements MainContract.MainView {
    private static final String TAG = "MainActivity";
    @BindView(R.id.tv)
    TextView tv;
    @BindView(R.id.main_progress1)
    ProgressBar progress;
    @BindView(R.id.ll)
    LinearLayout ll;
    @BindView(R.id.rvg)
    ResViewGroup rvg;


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
//            //TODO test uploadfile
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
                mHeartBeatService.sendHeartBeatData();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                MyLogger.i(TAG,"HeartBeatService Disconnected");
            }
        };
        Intent intent = new Intent(MainActivity.this,HeartBeatService.class);
        bindService(intent,mServiceConnection,BIND_AUTO_CREATE);
    }

    @Override
    public void showDialog() {

    }

    @Override
    public void hideDialog() {

    }

    @Override
    public void showDownloadNum(String num) {
    }

    @Override
    public void updateProgress(DownloadInfo value) {
    }

    @Override
    public void downloadFileSuccess(String results[]) {
        String configString = results[0];
        String dataString = results[1];
        MyLogger.i(TAG,"downloadFileSuccess configString ... "+configString);
        MyLogger.i(TAG,"downloadFileSuccess    ... "+dataString);
        mMainPresenter.writeTxtToFile(configString, FileManager.XML_DIR, FileManager.XML_CONFIG);//将xml写入到本地存储目录中
        mMainPresenter.writeTxtToFile(dataString, FileManager.XML_DIR, FileManager.XML_DATA);//将xml写入到本地存储目录中
        ll.setVisibility(View.GONE);
        rvg.removeAllViews();
        XMLDataManager.getInstance().getDataFromXML(MainActivity.this);
        if (XMLDataManager.getInstance().isDefaultData()) {
            setDefaultData();
        }else{
            mMainPresenter.playView(rvg);
        }
    }

    @Override
    public void downloadFileFail() {
        ll.setVisibility(View.GONE);
    }

    @Override
    public void setDefaultData() {
        rvg.removeAllViews();
        ResLoopView resLoopView = new ResLoopView(MainActivity.this);
        AreaBean areaBean = new AreaBean();

        InfoBean infoBean = new InfoBean();
        infoBean.setLeft("0");
        infoBean.setTop("0");
        infoBean.setWidth("1920");
        infoBean.setHeight("1080");
        infoBean.setTname("图片轮播");

        FilesBean fileBean = new FilesBean();
        ArrayList<FilesBean.FileBean> list = new ArrayList<>();
        FilesBean.FileBean fileBean1 = new FilesBean.FileBean();
        fileBean1.setTime("10");
        fileBean1.setFormat("图片");
        fileBean1.setId("101");
        list.add(fileBean1);

        FilesBean.FileBean fileBean2 = new FilesBean.FileBean();
        fileBean2.setTime("10");
        fileBean2.setFormat("图片");
        fileBean2.setId("102");
        list.add(fileBean2);

        FilesBean.FileBean fileBean3 = new FilesBean.FileBean();
        fileBean3.setTime("10");
        fileBean3.setFormat("图片");
        fileBean3.setId("103");
        list.add(fileBean3);
        fileBean.setFile(list);

        areaBean.setInfo(infoBean);
        areaBean.setFiles(fileBean);
        resLoopView.initData(areaBean);
        rvg.addView(resLoopView);
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
                            ArrayList<ConfigBean.Commands.Command> commands = XMLDataManager.getInstance().getConfigBean().getCommands().getCommand();
                            String url = "";
                            for (int i = 0; i < commands.size(); i++) {
                               ConfigBean.Commands.Command command = commands.get(i);
                                if (command.getType().contains(Constant.METHOD_UPDATE_APK)) {
                                    url = command.getUrl();
                                    break;
                                }
                            }
                            if (!TextUtils.isEmpty(url)) {
                                url = url + "&es=" + XMLDataManager.getInstance().getConfigBean().getSetting().getConnect().getId();
                                //下载apk
                                mMainActivity.mMainPresenter.downloadApk(url);
                            }
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
    protected void onStart() {
        super.onStart();
        rvg.startFlipping();
    }

    @Override
    protected void onStop() {
        super.onStop();
        rvg.stopFlipping();
    }

    @Override
    protected void onDestroy() {
        OkHttp3Manager.getOkHttpClient().dispatcher().cancelAll();
        mMainPresenter.clear();
        mHandler.removeCallbacksAndMessages(null);
        //清除子view
        clear();
        unbindService(mServiceConnection);
        super.onDestroy();
    }

    private void clear(){
        int count = rvg.getChildCount();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                View view = rvg.getChildAt(i);
                if (view instanceof ResLoopView) {
                    ((ResLoopView) view).clear();
                }
            }
        }
    }


    /************************************权限相关********************************/

    private AlertDialog mAlertDialog;
    private AlertDialog notAskDialog;

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void checkStore(){
        MyLogger.i(TAG,"checkContactPermission");
        mMainPresenter.playView(rvg);
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
