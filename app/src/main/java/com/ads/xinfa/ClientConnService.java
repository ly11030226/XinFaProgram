package com.ads.xinfa;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.ads.xinfa.base.Constant;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientConnService extends Service {
    private ClientConnBinder mClientConnBinder = new ClientConnBinder();
    public ClientConnService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket mServerSocket = new ServerSocket(Constant.RECEIVE_COMMAND_PORT,1);
                    Socket mClientSocket = mServerSocket.accept();
                    InputStream is = mClientSocket.getInputStream();
                    DataInputStream dis = new DataInputStream(is);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                }


            }
        }).start();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mClientConnBinder;
    }



    public class ClientConnBinder extends Binder{
        public ClientConnService getService(){
            return ClientConnService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }



}
