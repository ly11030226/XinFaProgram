package com.ads.xinfa.ui.wifiDirect;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.ads.xinfa.FtpService;
import com.ads.xinfa.R;
import com.ads.xinfa.base.BaseActivity;
import com.ads.xinfa.base.MyLogger;
import com.ads.xinfa.utils.BaseUtils;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WifiDirectActivity extends BaseActivity {
    private static final String TAG = "WifiDirectActivity";

    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private WifiDirectBroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private TimerTask mTimerTask;
    private Timer mTimer;
    @BindView(R.id.tv)
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_direct);
        ButterKnife.bind(this);
        try {
//            initWifiP2P();
//            initReceiver();
            String ip = BaseUtils.getHostIP();
            if(TextUtils.isEmpty(ip)){
                tv.setText("获取不到IP，请连接网络");
            }else{
                String str = "请在IE浏览器上输入网址访问FTP服务\n" +
                        "ftp://"+ip+":2221\n" +
                        "账号:admin\n" +
                        "密码:123456";
                tv.setText(str);
            }

            startService(new Intent(this, FtpService.class));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void discoverPeers() {
        mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //发现成功的时候处理
                MyLogger.d(TAG, "discoverPeers success");
            }

            @Override
            public void onFailure(int reason) {
                //发现失败时候的处理
                MyLogger.d(TAG, "discoverPeers fail");
            }
        });
    }

    private void executeTimer() {
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    discoverPeers();
                }
            };
        }
        if (mTimer == null) {
            mTimer = new Timer();
        }
        //        mTimer.schedule(mTimerTask,0,2*1000);
        mTimer.schedule(mTimerTask, 0);
    }

    private void cancelTimer() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void initReceiver() {
        mReceiver = new WifiDirectBroadcastReceiver(mWifiP2pManager, mChannel, WifiDirectActivity.this);
        mIntentFilter = new IntentFilter();
        //WIFI p2p状态改变
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        //WIFI p2p设备列表已经改变
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        //WIFI p2p连接状态发生改变
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        //WIFI p2p设备细节已经改变
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void initWifiP2P() {
        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(WifiDirectActivity.this, getMainLooper(), null);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() called");
        super.onResume();
//        registerReceiver(mReceiver, mIntentFilter);
//        executeTimer();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause() called");
        super.onPause();
//        unregisterReceiver(mReceiver);
//        cancelTimer();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, FtpService.class));
    }

}
