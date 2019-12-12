package com.gongw.remote.communication.client;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.gongw.remote.RemoteConst;
import com.gongw.remote.Tools;
import com.gongw.remote.communication.CommunicationKey;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientByteSocketManager {
    private static final String TAG = "ClientByteSocketManager";
    private static final int FLAG_SEND_STRING = 444;
    private static final int FLAG_SEND_BYTE = 555;

    private static final int FIRST = 1;
    private static final int SECOND = 2;
    private static final int THIRD = 3;
    private static final int FOUTH = 4;
    private static final int FIFTH = 5;
    private static final int SIXTH = 6;
    private static final int EIGHTH = 8;

    public static String currentDownloadName;

    private ClientByteSocketManager() {
    }

    ;
    private static ClientByteSocketManager instance;
    private ClientByteSocketRunnable clientByteSocketRunnable;
    private Thread thread;

    public static ClientByteSocketManager getInstance() {
        if (instance == null) {
            synchronized (ClientByteSocketManager.class) {
                if (instance == null) {
                    instance = new ClientByteSocketManager();
                }
            }
        }
        return instance;
    }

    public void createConn(String ip, Handler handler) {
        if (clientByteSocketRunnable == null) {
            clientByteSocketRunnable = new ClientByteSocketRunnable(ip, handler);
            Log.i(TAG, "createConn clientByteSocketRunnable==null , and ip ... " + ip);
        } else {
            Log.i(TAG, "createConn clientByteSocketRunnable!=null , and ip ... " + clientByteSocketRunnable.ip);
        }
        if (thread == null) {
            Log.i(TAG, "createConn thread == null");
            thread = new Thread(clientByteSocketRunnable);
            thread.start();
        } else {
            Log.i(TAG, "createConn thread != null");
        }
    }

    public void sendMsg(byte[] msg) {
        //        Log.i(TAG,"sendMsg msg ... "+msg);
        if (clientByteSocketRunnable != null) {
            clientByteSocketRunnable.sendMsg(msg);
        }
    }

    public void closeRunnable() {
        if (clientByteSocketRunnable != null) {
            clientByteSocketRunnable.close();
            clientByteSocketRunnable = null;
        }
        if (thread != null) {
            thread = null;
        }
    }

    public void addHandler(Handler handler) {
        if (clientByteSocketRunnable != null) {
            clientByteSocketRunnable.addControlHandler(handler);
        }
    }

    public void addDownloadFileHandler(Handler handler){
        if (clientByteSocketRunnable!=null) {
            clientByteSocketRunnable.addDownloadFileHandler(handler);
        }
    }

    private static class ClientByteSocketRunnable implements Runnable {

        private Socket socket;
        private String ip;
        private Handler uiHandler;
        private DataOutputStream dos;
        private DataInputStream dis;
        private Gson gson;
        private WriteHandlerThread writeHandlerThread;
        private WriteHandler writeHandler;
        private Handler resControlHandler;
        private Handler sendMsgHandler;
        private Handler downloafFileHandler;

        public ClientByteSocketRunnable(String ip, Handler handler) {
            this.ip = ip;
            this.uiHandler = handler;
            this.gson = new Gson();
        }

        private void addControlHandler(Handler handler) {
            this.resControlHandler = handler;
        }

        private void addDownloadFileHandler(Handler handler){
            this.downloafFileHandler = handler;
        }

        @Override
        public void run() {
            socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(ip, RemoteConst.COMMAND_RECEIVE_PORT));
                dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                initSendHandle();
                byte[] b = new byte[5];
                b[0] = FIRST;
                sendMsg(b);
                if (writeHandlerThread == null) {
                    writeHandlerThread = new WriteHandlerThread("write file");
                }
                writeHandlerThread.start();
                writeHandler = new WriteHandler(writeHandlerThread.getLooper(), dos);
                receiveMsgFromByte();
                close();
                if (uiHandler != null) {
                    uiHandler.sendEmptyMessage(RemoteConst.FLAG_SERVER_CLOSE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (uiHandler != null) {
                    uiHandler.sendEmptyMessage(RemoteConst.FLAG_CLOSE_SOCKET);
                }
            }
        }

        private void receiveMsgFromByte() throws IOException {
            byte[] buffer = new byte[5];
            while (dis.read(buffer) != -1) {
                int type = buffer[0];
                switch (type) {
                    case SECOND:
                        String resList = getResultString(buffer);
                        Log.i(TAG, "receiveMsgFromByte resList ... " + resList);
                        Message msg = uiHandler.obtainMessage();
                        msg.what = CommunicationKey.FLAG_CLIENT_READ_DATA;
                        msg.obj = resList;
                        uiHandler.sendMessage(msg);
                        break;
                    case FOUTH:
                        String path = getResultString(buffer);
                        transferFile(path);
                        break;
                    case SIXTH:
                        sendUploadFinishMsg();
                        break;
                    case EIGHTH:
                        int dataLength = Tools.bytes2IntExtra(buffer);
                        if (dataLength == 0) {
                            downloafFileHandler.sendEmptyMessage(CommunicationKey.FLAG_DOWNLOAD_FILE_IS_NULL);
                        }else{
                            Log.i(TAG,"EIGHTH dataLength ... "+dataLength);
                            byte[] b = new byte[1024];
                            int len = 0 ;
                            int total = 0;
                            String s = Environment
                                    .getExternalStorageDirectory().getPath()
                                    + File.separator
                                    + "SZTY" + File.separator + "FileDownloader" + File.separator;
                            if (TextUtils.isEmpty(currentDownloadName)) {
                                currentDownloadName = "temp.jpg";
                            }
                            String p = s+currentDownloadName;
                            File file = new File(s);
                            if (!file.exists()) {
                                file.mkdirs();
                            }
                            File f = new File(p);
                            FileOutputStream fos = new FileOutputStream(f);
                            while(total < dataLength){
                                len = dis.read(b,0,b.length);
                                Log.i(TAG,"read len ... "+len);
                                fos.write(b,0,len);
                                total+=len;
                                Log.i(TAG,"total ... "+total);
                            }
                            fos.close();
                            downloafFileHandler.sendEmptyMessage(CommunicationKey.FLAG_DOWNLOAD_FILE_IS_FINISH);
                        }
                        break;
                    default:
                        break;
                }
            }
            Log.i(TAG, "client receiveMsg run end");
        }

        private String getResultString(byte[] buffer) throws IOException {
            int datalen;
            datalen = Tools.bytes2IntExtra(buffer);
            byte[] temp = new byte[datalen];
            int len = dis.read(temp);
            int i = 0;
            Log.i(TAG, "dataLen ... " + datalen + " readLen ... " + len);
            while (len != datalen) {
                i = dis.read(temp, len, datalen - len);
                Log.i(TAG, "continue read len ... " + i);
                len += i;
            }
            String result = new String(temp, 0, temp.length, "UTF-8");
            return result;
        }

        private void initSendHandle() {
            SendHandlerThread sht = new SendHandlerThread("send msg thread");
            sht.start();
            sendMsgHandler = new Handler(sht.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    try {
                        if (msg.what == FLAG_SEND_STRING) {
                            String content = (String) msg.obj;
                            dos.write(content.getBytes("UTF-8"));
                            dos.flush();
                        } else if (msg.what == FLAG_SEND_BYTE) {
                            byte[] b = (byte[]) msg.obj;
                            Log.i(TAG, "send byte length ... " + b.length);
                            Log.i(TAG, "send byte type ... " + b[0]);
                            Log.i(TAG, "send byte content ... " + new String(b));
                            dos.write(b);
                            dos.flush();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
        }

        private void sendMsg(String msg) {
            if (sendMsgHandler != null) {
                Message m = sendMsgHandler.obtainMessage();
                m.obj = msg;
                m.what = FLAG_SEND_STRING;
                sendMsgHandler.sendMessageDelayed(m, 2000);
            }
        }

        private void sendMsg(byte[] b) {
            if (sendMsgHandler != null) {
                Message m = sendMsgHandler.obtainMessage();
                m.obj = b;
                m.what = FLAG_SEND_BYTE;
                sendMsgHandler.sendMessageDelayed(m, 2000);
            }
        }

        /**
         * 文件路径
         *
         * @param filePath
         */
        private synchronized void transferFile(final String filePath) {
            Log.i(TAG, "ClientSocketManager transferFile path ... " + filePath);
            //单线程去传输视图 run中加入for循环
            Message msg = writeHandler.obtainMessage();
            msg.what = 111;
            msg.obj = filePath;
            writeHandler.sendMessage(msg);
        }

        private void sendUploadFinishMsg() {
            Log.i(TAG, "sendUploadFinishMsg");
            if (resControlHandler != null) {
                resControlHandler.sendEmptyMessage(CommunicationKey.FLAG_CLIENT_UPDATE_SUCCESS);
            }
        }

        private void close() {
            Log.i(TAG, "close");
            try {
                if (uiHandler != null) {
                    uiHandler.sendEmptyMessage(RemoteConst.FLAG_CLOSE_SOCKET);
                }
                if (writeHandlerThread != null) {
                    writeHandlerThread.quit();
                    writeHandlerThread = null;
                }
                if (writeHandler != null) {
                    writeHandler.removeCallbacksAndMessages(null);
                    writeHandler = null;
                }
                if (dos != null) {
                    dos.close();
                    dos = null;
                }
                if (dis != null) {
                    dis.close();
                    dis = null;
                }
                if (socket != null) {
                    socket.close();
                    socket = null;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class SendHandlerThread extends HandlerThread {

        public SendHandlerThread(String name) {
            super(name);
        }
    }


    private static class WriteHandlerThread extends HandlerThread {
        public WriteHandlerThread(String name) {
            super(name);
        }
    }

    private static class WriteHandler extends Handler {
        private DataOutputStream dos;
        private Gson gson;
        private Object lock = new Object();

        public WriteHandler(Looper looper, DataOutputStream dos) {
            super(looper);
            this.dos = dos;
            this.gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setLenient().create();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                if (msg.what == 111) {
                    String filePath = (String) msg.obj;
                    writeFileByBytes(filePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void writeFileByBytes(String path) throws IOException {
            Log.i(TAG, "write file start  path ... " + path);
            FileInputStream fileInputStream = null;
            File file = new File(path);
            Log.i(TAG, "file is exist ... " + file.exists());
            try {
                if (file.exists()) {
                    fileInputStream = new FileInputStream(file);
                    byte[] bytes = new byte[1024 * 8];
                    int length = 0;
                    int i = 1;
                    byte[] b = new byte[5];
                    //组装数组前5个字节
                    b[0] = FIFTH;
                    int totalLen = (int) file.length();
                    Tools.int2BytesExtra(totalLen, b);
                    //将整个数组写入
                    dos.write(b);
                    dos.flush();
                    while ((length = fileInputStream.read(bytes, 0, bytes.length)) != -1) {
                        //                        Log.i(TAG,"length ... "+length+"  fileLength ... "+totalLen);
                        dos.write(bytes, 0, length);
                        dos.flush();
//                        Thread.sleep(200);
                    }
                    Log.i(TAG, "client file write end");
                    fileInputStream.close();
                } else {
                    Log.e(TAG, "writeFileByBytes file is not exist");
                }
            } catch (IOException e) {
                e.printStackTrace();
                dos.close();
            }
//            catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            Log.i(TAG, "write file end");
        }
    }
}
