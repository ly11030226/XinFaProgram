package com.ads.xinfa.net;

import android.os.Handler;
import android.os.Message;

import com.ads.xinfa.base.Constant;
import com.ads.xinfa.base.MyLogger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPConnectRunnable implements Runnable {
    private static final String TAG = "UDPConnectRunnable";
    private Handler mHandler;
    private DatagramSocket socket;
    public UDPConnectRunnable(Handler mHandler) {
        this.mHandler = mHandler;
    }
    @Override
    public void run() {
        try {
            socket = new DatagramSocket(4444);
            while(true){
                byte[] buf = new byte[1024];
                DatagramPacket inPacket=new DatagramPacket(buf,buf.length);
                socket.receive(inPacket);
                String receiveInfo = new String (inPacket.getData(),inPacket.getOffset(),inPacket.getLength());
                MyLogger.i(TAG,"receiveInfo ... "+receiveInfo);
                Message message = mHandler.obtainMessage();
                if(receiveInfo.contains(Constant.METHOD_UPDATE_XML)) {
                    message.what= Constant.UPDATE_XML_FILE;
                }else if(receiveInfo.contains(Constant.METHOD_GET_SNAPSHOOT)){
                    message.what=Constant.GET_SNAPSHOOT;
                }else if(receiveInfo.contains(Constant.METHOD_UPDATE_APK)){
                    message.what=Constant.UPDATE_APK;
                }
                mHandler.sendMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            MyLogger.e(TAG,"UDP thread exit");
        }
    }
    public void close(){
        if (socket!=null) {
            socket.close();
        }
    }
}
