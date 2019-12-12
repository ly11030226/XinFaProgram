package com.gongw.remote.communication.server;

import android.os.Environment;
import android.util.Log;

import com.gongw.remote.communication.Base64Utils;
import com.gongw.remote.communication.CommunicationKey;
import com.gongw.remote.communication.Transmission;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketManager {
    private static final String TAG = "ServerSocketManager";
    private static volatile boolean isOpen;
    private static volatile boolean isConnectClient = false;
    private static RequestListener listener;
    private static final String SAVE_PATH =
            Environment.getExternalStorageDirectory().getPath()
            +File.separator+
            "SZTY"+File.separator+"Upload"+File.separator;

    private ServerSocketManager(){
        File file = new File(SAVE_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
    public boolean isConnectClient(){
        return isConnectClient;
    }

    private static ServerSocketManager instance;
    public static ServerSocketManager getInstance(){
        if (instance == null) {
            synchronized (ServerSocketManager.class){
                if (instance == null) {
                    instance = new ServerSocketManager();
                }
            }
        }
        return instance;
    }
    private ServerSocketRunnable serverSocketRunnable;

    public void createServerIfRunnableIsNull(int port, RequestListener mListener){
        this.listener = mListener;
        if (serverSocketRunnable == null) {
            serverSocketRunnable = new ServerSocketRunnable(port);
            new Thread(serverSocketRunnable).start();
        }else{
            Log.e(TAG,"serverSocketRunnable must be null then start thread");
        }
    }
    public void sendMsg(String msg) {
        if (serverSocketRunnable != null) {
            serverSocketRunnable.sendMsg(msg);
        }
    }

    private static class ServerSocketRunnable implements Runnable{
        private int port;
        ServerSocket serverSocket = null;

        private PrintWriter mPw;
        private BufferedReader mBr;
        private Gson mGson;
        boolean mCreateFile = true;
        FileOutputStream mFos = null;

        public ServerSocketRunnable(int mPort) {
            this.port = mPort;
            this.mGson = new Gson();
        }

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(port);
                isOpen = true;
                while (isOpen){
                    final Socket clientSocket = serverSocket.accept();
                    mPw = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                    mBr = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    isConnectClient = true;
                    receiveMsg();
                    clientSocket.close();
                    isConnectClient = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }
        }
        private void close(){
            try {
                if (mPw!=null) {
                    mPw.close();
                    mPw = null;
                }
                if (mBr!=null) {
                    mBr.close();
                    mBr = null;
                }
                if (serverSocket!=null) {
                    serverSocket.close();
                    serverSocket = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private void receiveMsg() throws IOException {
            String content;
            String oldPath = "";
            while((content = mBr.readLine())!=null){
                Transmission transmission = mGson.fromJson(content,Transmission.class);
                if (transmission.transmissionType == CommunicationKey.REQUEST_GET_LIST) {
                    if (listener!=null) {
                        String str = listener.jsonResult();
                        Transmission transmission1 = new Transmission();
                        transmission1.transmissionType = CommunicationKey.RESPONSE_GET_LIST;
                        transmission1.content = str;
                        String json = mGson.toJson(transmission1);
                        Log.i(TAG,"json ... "+json);
                        sendMsg(json);
                    }
                }else if (transmission.transmissionType == CommunicationKey.REQUEST_UPDATE_LIST) {
                    String json = transmission.content;
                    //                    Log.i(TAG,"REQUEST_UPDATE_LIST json ... "+json);
                    if (listener!=null) {
                        listener.updateList(json);
                    }
                }else if (transmission.transmissionType == CommunicationKey.REQUEST_UPLOAD_RES) {
                    long fileLength = transmission.fileLength;
                    long transLength = transmission.transLength;
//                    Log.i(TAG,"file path ... "+SAVE_PATH + transmission.fileName);
                    if (mCreateFile) {
                        mCreateFile = false;
                        mFos = new FileOutputStream(new File(SAVE_PATH + transmission.fileName));
                    }
                    byte[] b = Base64Utils.decode(transmission.content.getBytes());
                    mFos.write(b, 0, b.length);
                    //                    Log.i(TAG,"接收文件进度" + 100 * transLength / fileLength + "%...");
                    if (listener != null) {
                        listener.transferring(SAVE_PATH + transmission.fileName, 100 * transLength / fileLength + "%...");
                    }
                    if (transLength == fileLength) {
                        mCreateFile = true;
                        if (listener != null) {
                            Log.i(TAG, "transLength == fileLength  filename ... " + transmission.fileName);
                            listener.transferSuccess(SAVE_PATH + transmission.fileName);
                        }
                        mFos.flush();
                        mFos.close();
                    }
                }
            }
        }
        private void sendMsg(String msg){
            Log.i(TAG,"send msg ... "+msg);
            if (mPw!=null) {
                mPw.write(msg+ CommunicationKey.EOF);
                mPw.flush();
            }else{
                Log.e(TAG,"sendMsg pw is null");
            }
        }

    }

    public void closeSerVer(){
        if (serverSocketRunnable!=null) {
            serverSocketRunnable.close();
            serverSocketRunnable = null;
        }
    }

    public interface RequestListener{
        String jsonResult();
        void updateList(String json);
        void transferring(String path,String progress);
        void transferSuccess(String path);
    }
}
