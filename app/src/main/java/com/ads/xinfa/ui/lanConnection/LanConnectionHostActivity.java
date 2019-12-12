package com.ads.xinfa.ui.lanConnection;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.ads.xinfa.FtpService;
import com.ads.xinfa.R;
import com.ads.xinfa.base.BaseActivity;
import com.ads.xinfa.base.Constant;
import com.ads.xinfa.floatView.FloatViewService;
import com.ads.xinfa.ui.displayVideoAndImage.DisplayVideoAndImageFragment;
import com.ads.xinfa.utils.SystemUtil;
import com.gongw.remote.RemoteConst;
import com.gongw.remote.search.DeviceSearchResponser;

import butterknife.ButterKnife;

/**
 * 用于搜索连接的客户端
 * @author Ly
 */
public class LanConnectionHostActivity extends BaseActivity {

    private static final String TAG = "LanConnectionHost";
    DisplayVideoAndImageFragment mDisplayVideoAndImageFragment;
    private String action;
    public static final int TIME_EXIT_APP = 2*1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_server);
        ButterKnife.bind(this);
        try {
//            createBarcode();
//            startService();
            initIntent();
            initFragment();
            //将从哪跳转过来的数据传递给Fragment
            mDisplayVideoAndImageFragment.getAction(action);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startService() {
        if (RemoteConst.floatViewIsOpen) {
            Intent intent = new Intent(LanConnectionHostActivity.this, FloatViewService.class);
            //启动FloatViewService
            startService(intent);
        }
    }

    private void initIntent() {
        if (getIntent()!=null && getIntent().hasExtra(Constant.ACTION_JUMP_FROM_WHERE)) {
            action = getIntent().getStringExtra(Constant.ACTION_JUMP_FROM_WHERE);
            if (Constant.FROM_WELCOME.equals(action)) {
                //开始响应搜索
                DeviceSearchResponser.open(SystemUtil.getSystemModelExtra());
            }
        }
    }

    private void initFragment() {
        mDisplayVideoAndImageFragment = new DisplayVideoAndImageFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fl_,mDisplayVideoAndImageFragment)
                .show(mDisplayVideoAndImageFragment)
                .commit();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, FtpService.class));
        stopService(new Intent(this,FloatViewService.class));
    }
    public interface ActionListener{
        void getAction(String action);
    }

    private long firstTime = 0;
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();
                //如果两次按键时间间隔大于2秒，则不退出
                if (secondTime - firstTime > TIME_EXIT_APP) {
                    Toast.makeText(this, R.string.exit_app, Toast.LENGTH_SHORT).show();
                    //更新firstTime
                    firstTime = secondTime;
                    return true;
                } else {
                    //两次按键小于2秒时，退出应用
                    System.exit(0);
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }
}
