package com.gongw.remote.communication.client;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.gongw.remote.RemoteConst;
import com.gongw.remote.communication.Base64Utils;
import com.gongw.remote.communication.CommunicationKey;
import com.gongw.remote.communication.Transmission;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 发送请求管理者
 */
public class ClientSocketManager {
    private static final String TAG = "ClientSocketManager";
    private CommandRunnable commandRunnable;
    private Thread thread;

    private ClientSocketManager() {
    }

    private static ClientSocketManager instance;

    public static ClientSocketManager getInstance() {
        if (instance == null) {
            synchronized (ClientSocketManager.class) {
                if (instance == null) {
                    instance = new ClientSocketManager();
                }
            }
        }
        return instance;
    }

    public void createConn(Transmission transmission, String ip, Handler handler) {
        if (commandRunnable==null) {
            commandRunnable = new CommandRunnable(transmission, ip, handler);
            Log.i(TAG,"createConn commandRunnable==null , and ip ... "+ip);
        }else{
            Log.i(TAG,"createConn commandRunnable!=null , and ip ... "+commandRunnable.ip);
        }
        if (thread==null) {
            Log.i(TAG,"createConn thread == null");
            thread = new Thread(commandRunnable);
            thread.start();
        }else{
            Log.i(TAG,"createConn thread != null");
        }
    }

    public void sendMsg(Transmission transmission) {
        if (commandRunnable != null) {
            commandRunnable.sendMsg(transmission);
        }
    }
    public void addHandler(Handler handler){
        if (commandRunnable!=null) {
            commandRunnable.addControlHandler(handler);
        }
    }

    public void closeRunnable(){
        if (commandRunnable!=null) {
            commandRunnable.close();
            commandRunnable = null;
        }
        if (thread!=null) {
            thread = null;
        }
    }


    private static class CommandRunnable implements Runnable {

        private BufferedReader br;
        private PrintWriter pw;
        private Transmission transmission;
        private String ip;
        private Handler readHandler;
        private Socket socket;
        private Gson gson;
        private Handler resControlHandler;
        private WriteHandlerThread writeHandlerThread;
        private WriteHandler writeHandler;

        public CommandRunnable(Transmission transmission, String ip, Handler handler) {
            this.transmission = transmission;
            this.ip = ip;
            this.readHandler = handler;
            this.gson = new Gson();
        }

        private void addControlHandler(Handler handler){
            this.resControlHandler = handler;
        }

        private void sendMsg(final Transmission transmission) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //发送命令内容
                    Gson gson = new Gson();
                    String json = gson.toJson(transmission);
                    pw.write(json + CommunicationKey.EOF);
                    pw.flush();
                }
            }).start();
        }

        @Override
        public void run() {
            socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(ip, RemoteConst.COMMAND_RECEIVE_PORT));
                if (socket.isClosed() || !socket.isConnected()||socket.isOutputShutdown()) {
                    if (readHandler!=null) {
                        readHandler.sendEmptyMessage(RemoteConst.FLAG_CLOSE_SOCKET);
                    }
                    return;
                }
                OutputStream os = socket.getOutputStream();
                InputStream is = socket.getInputStream();
                br = new BufferedReader(new InputStreamReader(is));
                pw = new PrintWriter(new OutputStreamWriter(os));
                Transmission transmission = this.transmission;
                transmission.transmissionType = CommunicationKey.REQUEST_GET_LIST;
                sendMsg(transmission);
                String content;
                if (writeHandlerThread==null) {
                    writeHandlerThread = new WriteHandlerThread("write file");
                }
                writeHandlerThread.start();
                writeHandler = new WriteHandler(writeHandlerThread.getLooper(),pw);
                //读取应答内容
                while ((content = br.readLine()) != null) {
                    Transmission transmission1 = gson.fromJson(content, Transmission.class);
                    if (transmission1.transmissionType == CommunicationKey.RESPONSE_GET_LIST) {
                        String result = transmission1.content;
                        Message msg = readHandler.obtainMessage();
                        msg.what = CommunicationKey.FLAG_CLIENT_READ_DATA;
                        msg.obj = result;
                        readHandler.sendMessage(msg);
                    } else if (transmission1.transmissionType == CommunicationKey.RESPONSE_UPDATE_LIST) {
                        String path = transmission1.content;
                        transferFile(path);
                    } else if (transmission1.transmissionType == CommunicationKey.RESPONSE_UPLOAD_RES) {
                        sendUploadFinishMsg();
                    }
                }
                Log.i(TAG,"run end");
                close();
                if (readHandler!=null) {
                    readHandler.sendEmptyMessage(RemoteConst.FLAG_SERVER_CLOSE);
                }
            }catch (IOException e) {
                e.printStackTrace();
                if (readHandler!=null) {
                    readHandler.sendEmptyMessage(RemoteConst.FLAG_CLOSE_SOCKET);
                }
            }
        }

        private void close(){
            Log.i(TAG,"close");
            try {
                if (writeHandlerThread!=null) {
                    writeHandlerThread.quit();
                    writeHandlerThread = null;
                }
                if (writeHandler!=null) {
                    writeHandler.removeCallbacksAndMessages(null);
                    writeHandler = null;
                }
                if (pw != null) {
                    pw.close();
                    pw = null;
                }
                if (br != null) {
                    br.close();
                    br = null;
                }
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 文件路径
         *
         * @param filePath
         */
        private synchronized void transferFile(final String filePath) {
            Log.i(TAG,"ClientSocketManager transferFile path ... "+filePath);
            //单线程去传输视图 run中加入for循环
            Message msg = writeHandler.obtainMessage();
            msg.what = 111;
            msg.obj = filePath;
            writeHandler.sendMessage(msg);
        }

        private void sendUploadFinishMsg(){
            Log.i(TAG,"sendUploadFinishMsg");
            if (resControlHandler!=null) {
                resControlHandler.sendEmptyMessage(CommunicationKey.FLAG_CLIENT_UPDATE_SUCCESS);
            }
        }
    }
    private static class WriteHandlerThread extends HandlerThread{
        private String path;
        public WriteHandlerThread(String name) {
            super(name);
        }
    }

    private static class WriteHandler extends Handler{
        private Gson gson;
        private PrintWriter pw;
        public WriteHandler(Looper looper,PrintWriter pw) {
            super(looper);
            this.gson = new Gson();
            this.pw = pw;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                if (msg.what == 111) {
                    String filePath = (String) msg.obj;
                    writeFile(filePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private void writeFile(String path) {
            FileInputStream fileInputStream = null;
            File file = new File(path);
            Log.i(TAG, "WriteThread path ... " + path);
            try {
                fileInputStream = new FileInputStream(file);
                Transmission trans = new Transmission();
                trans.transmissionType = CommunicationKey.REQUEST_UPLOAD_RES;
                trans.fileName = file.getName();
                trans.fileLength = file.length();
                trans.transLength = 0;
                byte[] bytes = new byte[1024];
                int length = 0;
                while ((length = fileInputStream.read(bytes, 0, bytes.length)) != -1) {
                    trans.transLength += length;
                    trans.content = Base64Utils.encode(bytes);
                    pw.write(gson.toJson(trans) + CommunicationKey.EOF);
                    pw.flush();
                }
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                pw.close();
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
}
