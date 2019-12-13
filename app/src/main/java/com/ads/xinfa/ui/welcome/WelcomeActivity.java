package com.ads.xinfa.ui.welcome;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;

import com.ads.xinfa.R;
import com.ads.xinfa.base.BaseActivity;
import com.ads.xinfa.base.FileManager;
import com.ads.xinfa.base.MyLogger;
import com.ads.xinfa.utils.BaseUtils;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.gongw.remote.SettingManager;

import io.reactivex.annotations.NonNull;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * 基类 欢迎界面
 * @author Ly
 */
@RuntimePermissions
public abstract class WelcomeActivity extends BaseActivity {
    private static final String TAG = "WelcomeActivity";
    public Handler handler = new Handler();
    private MaterialDialog noPermissionDialog, noAskDialog;
    public boolean isCanWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        try {
            createFile();
            WelcomeActivityPermissionsDispatcher.checkPermissionWithPermissionCheck(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract int getLayoutId();
    public abstract void doNext();

    private void createFile() {
        FileManager.init(WelcomeActivity.this);
    }



    /**
     * 屏蔽用户按手机back键
     */
    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (noPermissionDialog != null) {
            noPermissionDialog = null;
        }
        if (noAskDialog != null) {
            noAskDialog = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    ////////////////////////////权限相关///////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    @NeedsPermission({
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA})
    void checkPermission() {
        try {
            MyLogger.i(TAG, "checkPermission");
            isCanWrite = true;
            SettingManager.getInstance().writeConfigTxt(WelcomeActivity.this);
            doNext();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @OnShowRationale({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})
    void noPermission(PermissionRequest request) {
        MyLogger.i(TAG, "noPermission");
        if (noPermissionDialog == null) {
            noPermissionDialog = new MaterialDialog.Builder(this).title(R.string.dialog_title).content(R.string.no_permission).positiveText(R.string.dialog_commit).onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@androidx.annotation.NonNull MaterialDialog dialog, @androidx.annotation.NonNull DialogAction which) {
                    request.proceed();
                }
            }).negativeText(R.string.dialog_cancel).onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@androidx.annotation.NonNull MaterialDialog dialog, @androidx.annotation.NonNull DialogAction which) {
                    request.cancel();
                }
            }).build();
        }
        noPermissionDialog.show();
    }

    @OnPermissionDenied({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})
    void reject() {
        MyLogger.i(TAG, "reject");
    }

    @OnNeverAskAgain({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})
    void rejectAndNOAsk() {
        MyLogger.i(TAG, "rejectAndNOAsk");
        if (noAskDialog == null) {
            noAskDialog = new MaterialDialog.Builder(this).title(R.string.dialog_title).content(R.string.no_permission).positiveText(R.string.dialog_confirm).onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@androidx.annotation.NonNull MaterialDialog dialog, @androidx.annotation.NonNull DialogAction which) {
                    BaseUtils.jumpSystemSet(WelcomeActivity.this);
                }
            }).negativeText(R.string.dialog_do_not).onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@androidx.annotation.NonNull MaterialDialog dialog, @androidx.annotation.NonNull DialogAction which) {
                    noAskDialog.dismiss();
                }
            }).build();
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


}
