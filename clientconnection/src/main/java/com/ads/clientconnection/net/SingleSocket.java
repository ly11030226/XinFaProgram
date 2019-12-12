package com.ads.clientconnection.net;

import java.net.Socket;

public class SingleSocket extends Socket {
    private static SingleSocket instance;
    public static SingleSocket getInstance(){
        if (instance==null) {
            synchronized (SingleSocket.class){
                if (instance == null) {
                    instance = new SingleSocket();
                }
            }
        }
        return instance;
    }

}
