package com.ads.clientconnection.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.ads.clientconnection.R;
import com.ads.clientconnection.base.BaseActivity;
import com.ads.clientconnection.base.MyLogger;
import com.ads.clientconnection.utils.BaseUtils;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import io.reactivex.annotations.NonNull;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class WelcomeActivity extends BaseActivity {
    private static final String TAG = "WelcomeActivity";
    private MaterialDialog noPermissionDialog, noAskDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        try {
            WelcomeActivityPermissionsDispatcher.checkPermissionWithPermissionCheck(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Handler handler = new Handler();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }


    ////////////////////////////权限相关///////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})
    void checkPermission() {
        try {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnShowRationale({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})
    void noPermission(PermissionRequest request) {
        MyLogger.i(TAG, "noPermission");
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
        MyLogger.i(TAG, "reject");
    }

    @OnNeverAskAgain({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})
    void rejectAndNOAsk() {
        MyLogger.i(TAG, "rejectAndNOAsk");
        if (noAskDialog==null) {
            noAskDialog = new MaterialDialog.Builder(this)
                    .title(R.string.dialog_title)
                    .content(R.string.res_no_permission)
                    .positiveText(R.string.dialog_confirm)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@androidx.annotation.NonNull MaterialDialog dialog, @androidx.annotation.NonNull DialogAction which) {
                            BaseUtils.jumpSystemSet(WelcomeActivity.this);
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



}
