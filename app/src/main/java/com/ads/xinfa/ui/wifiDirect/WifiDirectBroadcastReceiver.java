package com.ads.xinfa.ui.wifiDirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;

import com.ads.xinfa.base.FileManager;
import com.ads.xinfa.base.MyLogger;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import androidx.core.content.FileProvider;


/**
 * wifi直连的广播
 */
public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiDirectBroadcast";
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private WifiDirectActivity mActivity;
    private WifiP2pManager.PeerListListener mPeerListListener;
    private static final String APK_NAME = "app-debug.apk";

    public WifiDirectBroadcastReceiver() {
    }

    public WifiDirectBroadcastReceiver(WifiP2pManager mWifiP2pManager, WifiP2pManager.Channel mChannel, WifiDirectActivity mActivity) {
        this.mWifiP2pManager = mWifiP2pManager;
        this.mChannel = mChannel;
        this.mActivity = mActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        MyLogger.i(TAG,"onReceive action ... "+action);
        //WIFI_P2P_STATE_CHANGED_ACTION 当设备的WiFi直连功能打开或者关闭时进行广播
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            //查看wifi p2p 网络的状态值
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1);
            //查看wifi直连状态
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                //wifi直连 enabled
                MyLogger.d(TAG,"wifi direct open");
            }else{
                //wifi直连 not enabled
                MyLogger.d(TAG,"wifi direct close");
            }
        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            mPeerListListener = new WifiP2pManager.PeerListListener(){

                @Override
                public void onPeersAvailable(WifiP2pDeviceList peers) {
                    Collection<WifiP2pDevice> wifiP2pDeviceList = peers.getDeviceList();
                    MyLogger.i(TAG,"wifiP2pDeviceList size ... "+wifiP2pDeviceList.size());
                    Iterator<WifiP2pDevice> ib= (Iterator<WifiP2pDevice>) wifiP2pDeviceList.iterator();
                    while(ib.hasNext()){
                        WifiP2pDevice device = ib.next();
                        String address = device.deviceAddress;
                        String name = device.deviceName;
                        int status = device.status;
                        MyLogger.i(TAG,"address ... "+address+"   name ... "+name+"   status ... "+status);
                        if (name.contains("xinfa-")) {
                            WifiP2pConfig config = new WifiP2pConfig();
                            config.deviceAddress = address;
                            if (status!=WifiP2pDevice.CONNECTED) {
                                mWifiP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        //成功连接
                                        MyLogger.d(TAG, "onSuccess() called");
                                        shareFile(mActivity);
                                    }

                                    @Override
                                    public void onFailure(int reason) {
                                        //连接失败
                                        MyLogger.d(TAG, "onFailure() called with: reason = [" + reason + "]");
                                    }
                                });
                            }
                        }
                    }
                }
            };
            if (mWifiP2pManager!=null) {
                mWifiP2pManager.requestPeers(mChannel,mPeerListListener);
            }
        }//当设备的WiFi连接信息状态改变时进行广播
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

        }//当设备的详细信息改变的时候进行广播，比如设备的名称
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

        }
    }
    private void shareFile(Context context) {
        File file = new File(FileManager.Resource_DIR + APK_NAME);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, "com.ads.xinfa.demo.fileprovider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra("subject", ""); //
        intent.putExtra("body", ""); // 正文
        intent.putExtra(Intent.EXTRA_STREAM, uri); // 添加附件，附件为file对象
        intent.setType("application/octet-stream"); // 其他的均使用流当做二进制数据来发送
        context.startActivity(intent);
    }
}
