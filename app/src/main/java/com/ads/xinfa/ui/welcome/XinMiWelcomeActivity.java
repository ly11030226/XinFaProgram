package com.ads.xinfa.ui.welcome;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.ads.xinfa.R;
import com.ads.xinfa.base.Constant;
import com.ads.xinfa.base.MyLogger;
import com.ads.xinfa.ui.lanConnection.LanConnectionHostActivity;
import com.gongw.remote.RemoteConst;

public class XinMiWelcomeActivity extends WelcomeActivity {

    private static final String TAG = "XinMiWelcomeActivity";
    @Override
    public int getLayoutId() {
        return R.layout.activity_welcome;
    }

    @Override
    public void doNext() {
        if (RemoteConst.floatViewIsOpen) {
            //检查是否已经授予权限，大于6.0的系统适用，小于6.0系统默认打开，无需理会
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M&&!Settings.canDrawOverlays(this)) {
                //没有权限，需要申请权限，因为是打开一个授权页面，所以拿不到返回状态的，所以建议是在onResume方法中从新执行一次校验
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 100);
            }else{
                jump();
            }
        }else{
            jump();
        }
    }

    private void jump(){
        Intent intent = new Intent(XinMiWelcomeActivity.this, LanConnectionHostActivity.class);
        intent.putExtra(Constant.ACTION_JUMP_FROM_WHERE, Constant.FROM_WELCOME);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        MyLogger.i(TAG,"onActivityResult");
        if (requestCode == 100) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(XinMiWelcomeActivity.this)) {
                    // SYSTEM_ALERT_WINDOW permission not granted...
                    Toast.makeText(XinMiWelcomeActivity.this,"请开启悬浮窗权限，否则应用无法使用",Toast.LENGTH_LONG).show();
                }else{
                    jump();
                }
            }
        }
    }
}
