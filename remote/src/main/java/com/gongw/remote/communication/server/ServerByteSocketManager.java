package com.gongw.remote.communication.server;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.gongw.remote.Tools;
import com.gongw.remote.communication.CommunicationKey;
import com.gongw.remote.communication.Transmission;
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
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerByteSocketManager {
    private static final String TAG = "ServerByteSocketManager";
    public ServerByteSocketManager.RequestListener listener;
    private static final String SAVE_PATH =
            Environment.getExternalStorageDirectory().getPath()
                    + File.separator+
                    "SZTY"+File.separator+"Upload"+File.separator;
    private ServerByteSocketRunnable serverSocketRunnable;
    private static final int FLAG_SEND_MSG = 0x33;
    private static final int FLAG_SEND_MSG_BYTE = 0x34;
    //当前需要上传file的路径
    private static String CURRENT_FILE_PATH;
    private static String CURRENT_FILE_NAME;


    private static final int FIRST = 1;
    private static final int SECOND = 2;
    private static final int THIRD = 3;
    private static final int FOUTH = 4;
    private static final int FIFTH = 5;
    private static final int SIXTH = 6;
    private static final int SEVENTH = 7;


    private ServerByteSocketManager(){
        File file = new File(SAVE_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
    private static ServerByteSocketManager instance;
    public static ServerByteSocketManager getInstance(){
        if (instance == null) {
            synchronized (ServerByteSocketManager.class){
                if (instance == null) {
                    instance = new ServerByteSocketManager();
                }
            }
        }
        return instance;
    }
    public void createServerIfRunnableIsNull(int port, ServerByteSocketManager.RequestListener mListener){
        this.listener = mListener;
        if (serverSocketRunnable == null) {
            serverSocketRunnable = new ServerByteSocketRunnable(port);
            new Thread(serverSocketRunnable).start();
        }else{
            Log.e(TAG,"serverSocketRunnable must be null then start thread");
        }
    }
    public void sendMsg(String msg) {
        if (serverSocketRunnable != null) {
//            serverSocketRunnable.sendMsg(msg);
        }
    }
    public void sendMsgByte(byte[] b) {
        if (serverSocketRunnable != null) {
            serverSocketRunnable.sendMsgByte(b);
        }
    }

    public void setFileInfo(String name,String path){
        CURRENT_FILE_NAME = name;
        CURRENT_FILE_PATH = path;
    }

    private class ServerByteSocketRunnable implements Runnable{
        private ServerSocket serverSocket;
        private int port;
        private DataInputStream dis;
        private DataOutputStream dos;
        private boolean isOpen;
        private Gson gson;
        boolean mCreateFile = true;
        FileOutputStream mFos = null;
        private HandlerThread ht;
        private Handler sendMsgHandler;

        public ServerByteSocketRunnable(int port) {
            this.port = port;
            this.gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .setLenient()
                    .create();
        }

        @Override
        public void run() {
            try {
                isOpen = true;
                serverSocket = new ServerSocket(port);
                while (isOpen){
                    Socket clientSocket = serverSocket.accept();
                    dos = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
                    dis = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                    ht = new HandlerThread("server send msg");
                    ht.start();
                    sendMsgHandler = new Handler(ht.getLooper()){
                        @Override
                        public void handleMessage(Message msg) {
                            try {
                                if (msg.what == FLAG_SEND_MSG) {
                                    String m = (String) msg.obj;
                                    byte[] b = m.getBytes("UTF-8");
                                    Log.i(TAG,"sendMsgHandler get msg length ... "+b.length);
                                    Log.i(TAG,"sendMsgHandler get msg ... "+m);
                                    dos.write(b);
                                    dos.flush();
                                }else if (msg.what == FLAG_SEND_MSG_BYTE) {
                                    byte[] bb = (byte[]) msg.obj;
                                    dos.write(bb);
                                    dos.flush();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    if (listener!=null) {
                        listener.clientConn();
                    }
                    receiveMsgFromByte();
                    clientSocket.close();
                    Log.i(TAG,"server run end");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        private void receiveMsgFromByte() throws IOException, InterruptedException {
            byte [] buffer = new byte[5];
            int len = 0;
            int readFileLength = 0;
            int fileLength = 0;
            String path = "";
            int surplus = 0; //接收剩余没读完的数据
            while((len = dis.read(buffer)) != -1){
                int type = buffer[0];
                Log.i(TAG,"type ... "+type);
                switch (type) {
                    case FIRST:
                        if (listener != null) {
                            String resultStr = listener.jsonResult();
                            byte[] b = ServerByteSocketManager.getInstance().makeBytes(
                                    resultStr,
                                    SECOND);
                            Log.i(TAG,"res list ... "+resultStr);
                            dos.write(b);
                            dos.flush();
                        }
                        break;
                    case THIRD:
                        int temp;
                        String s = getResultString(buffer);
                        if (listener != null) {
                            listener.updateList(s);
                        }
                        break;
                    case FIFTH:
                        int currentFileLength = Tools.bytes2IntExtra(buffer);
                        byte[] b = new byte[1024*8];
                        int cycleIndex = currentFileLength/b.length;
                        int lastIndexByte = currentFileLength%b.length;
//                        Log.i(TAG,"一共"+(cycleIndex+1)+"次循环，最后一次循环读"+lastIndexByte+"字节");
                        if (fileLength == 0) {
                            fileLength = currentFileLength;
                            path = SAVE_PATH+CURRENT_FILE_NAME;
                            mFos = new FileOutputStream(new File(path));
                        }
                        int j = 0;
                        int num;
                        int total = 0;
                        while((readFileLength = dis.read(b))!=-1){
//                            Log.i(TAG,"1 === get write file info");
                            while (readFileLength < b.length){
                                if (j == cycleIndex) {
                                    if (readFileLength<lastIndexByte) {
                                        while(readFileLength < lastIndexByte){
                                            num = dis.read(b,readFileLength,lastIndexByte - readFileLength);
                                            if (num > 0){
                                                readFileLength = readFileLength + num;
                                            }else{
                                                break;
                                            }
                                        }
                                    }
                                    break;
                                }else{
                                    num = dis.read(b, readFileLength, b.length - readFileLength);
//                                    Log.i(TAG,"num ... "+num);
                                    if (num > 0){
                                        readFileLength = readFileLength + num;
                                    }else{
                                        break;
                                    }
                                }
                            }
//                            Log.i(TAG,"2 === start write file , readFileLength ... "+readFileLength);
                            mFos.write(b, 0, readFileLength);
                            mFos.flush();
                            j++;
//                            Log.i(TAG,"3 === 第"+j+"次 write");
                            total+=readFileLength;
//                            Log.i(TAG,"4 === readFileLength ... "+readFileLength+"  currentFileLength ... "+currentFileLength);
                            if (total >= fileLength) {
                                if (listener != null) {
                                    listener.transferSuccess(path);
                                }
                                fileLength = 0;
                                mFos.close();
                                mFos = null;
                                break;
                            }
                        }
                        break;
                    case SEVENTH: //通过路径上传文件
                        int length = Tools.bytes2IntExtra(buffer);
                        byte[] _b = new byte[length];
                        int _length = 0;
                        _length = dis.read(_b,0,length);
                        String _fileName = new String(_b,0,_length);
                        String _path = Environment.getExternalStorageDirectory().getPath()
                                +File.separator+
                                "SZTY"+File.separator+"Upload"+File.separator;
                        File file = new File(_path);
                        File _file = new File(_path+_fileName);
                        if (!file.exists()) {
                            sendEmptyByte();
                        }else{
                            if (!_file.exists()) {
                                sendEmptyByte();
                            }else{
                                sendResData(_file);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        private void sendResData(File file) throws IOException, InterruptedException {
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[1024 * 8];
            int length = 0;
            int i = 1;
            byte[] b = new byte[5];
            //组装数组前5个字节
            b[0] = CommunicationKey.RESPONSE_DOWNLOAD_FILE;
            int totalLen = (int) file.length();
            Tools.int2BytesExtra(totalLen, b);
            //将整个数组写入
            dos.write(b);
            dos.flush();
            while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
                //                        Log.i(TAG,"length ... "+length+"  fileLength ... "+totalLen);
                dos.write(bytes, 0, length);
                dos.flush();
//                Thread.sleep(200);
            }
            Log.i(TAG, "client file write end");
            fis.close();
        }

        private void sendEmptyByte() {
            byte[] _bb = new byte[5];
            _bb[0] = CommunicationKey.RESPONSE_DOWNLOAD_FILE;
            _bb[1] = 0;
            _bb[2] = 0;
            _bb[3] = 0;
            _bb[4] = 0;
            sendMsgByte(_bb);
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
                Log.i(TAG,"continue read len ... "+i);
                len += i;
            }
            return new String(temp, 0, temp.length, "UTF-8");
        }
        private void receiveMsg() throws IOException {
            byte[] buffer = new byte[1024];
            int len = 0;
            boolean isTransFile = false;
            long fileLength = 0;
            long transLength = 0;
            String path = "";
            int tempLen = 0;
            StringBuilder sb = null;
            while ((len = dis.read(buffer)) != -1) {
//                Log.i(TAG,"get buffer");
                if (isTransFile) {
                    mFos.write(buffer, 0, len);
                    mFos.flush();
                    transLength+=len;
//                    Log.i(TAG,"start trans file: transLength ... "+transLength+" fileLength ... "+fileLength);
                    if (fileLength == transLength) {
                        if (listener != null) {
                            listener.transferSuccess(path);
                        }
                        fileLength = 0;
                        transLength = 0;
                        isTransFile = false;
                        mFos.close();
                        mFos = null;
                    }
                }else{
                    if (sb==null) {
                        sb = new StringBuilder();
                    }
                    String result = new String(buffer, 0, len,"UTF-8");
                    sb.append(result);
                    Log.i(TAG,"server read byte len ... "+len);
                    if (len == 1024) {

                    }else{
                        String finish = sb.toString();
                        Log.d(TAG,"buffer length ... "+finish.getBytes("UTF-8").length);
                        Log.i(TAG,"server receiveMsg msg ... " + finish);
                        Transmission transmission = gson.fromJson(finish, Transmission.class);
                        if (transmission.transmissionType == CommunicationKey.REQUEST_GET_LIST) {
                            if (listener != null) {
                                String str = listener.jsonResult();
                                int dataLength = str.getBytes().length;
                                Transmission transmission1 = new Transmission();
                                transmission1.transmissionType = CommunicationKey.RESPONSE_GET_LIST;
                                transmission1.dataLength = dataLength;
                                String json = gson.toJson(transmission1);
                                //                            Log.i(TAG, "json ... " + json);
                                dos.write(json.getBytes("UTF-8"));
                                dos.flush();
                                dos.write(str.getBytes("UTF-8"));
                                dos.flush();
                            }
                        } else if (transmission.transmissionType == CommunicationKey.REQUEST_UPDATE_LIST) {
//                            byte[] b = new byte[transmission.dataLength];
                            //                        int l = dis.read(b);
//                            dis.readFully(b);
                            //                        while (len1<transmission.dataLength){
                            //                            int temp = dis.read(b);
                            //                            len1+=temp;
                            //                        }
//                            String ss = new String(b,"UTF-8");
                            //                        Log.i(TAG,"updateList ... "+ss);
                            String content = transmission.content;
                            if (listener != null) {
                                listener.updateList(content);
                            }
                        } else if (transmission.transmissionType == CommunicationKey.REQUEST_UPLOAD_RES) {
                            //                        Log.i(TAG,"receive trans file");
                            isTransFile = true;
                            fileLength = transmission.fileLength;
                            path = SAVE_PATH + transmission.fileName;
                            //                        Log.i(TAG, "file path ... " + path);
                            mFos = new FileOutputStream(new File(path));
                            //Log.i(TAG,"接收文件进度" + 100 * transLength / fileLength + "%...");
                            if (listener != null) {
                                listener.transferring(SAVE_PATH + transmission.fileName, 100 * transLength / fileLength + "%...");
                            }
                        }
                        sb = null;
                    }
                }
            }
            Log.i(TAG, "server receiveMsg run end");
            dos.close();
            dis.close();
            serverSocket.close();
        }

        private void sendMsg(String msg){
//            Message m = sendMsgHandler.obtainMessage();
//            m.what = FLAG_SEND_MSG;
//            m.obj = msg;
//            sendMsgHandler.sendMessage(m);
        }
        private void sendMsgByte(byte[] b){
            Message m = sendMsgHandler.obtainMessage();
            m.what = FLAG_SEND_MSG_BYTE;
            m.obj = b;
            sendMsgHandler.sendMessage(m);
        }

        private void close(){
            try {
                if (dis!=null) {
                    dis.close();
                    dis = null;
                }
                if (dos!=null) {
                    dos.close();
                    dos = null;
                }
                if (serverSocket!=null) {
                    serverSocket.close();
                    serverSocket = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface RequestListener{
        void clientConn();
        String jsonResult();
        void updateList(String json);
        void transferring(String path,String progress);
        void transferSuccess(String path);
    }
    public void closeSerVer(){
        if (serverSocketRunnable!=null) {
            serverSocketRunnable.close();
            serverSocketRunnable = null;
        }
    }


    public byte[] makeBytes(String content,int type) throws UnsupportedEncodingException {
        byte[] data = content.getBytes("UTF-8");
        int dataLength = data.length;
        int byteAllLength = 5+dataLength;
        byte[] transBytes = new byte[byteAllLength];
        System.arraycopy(data, 0, transBytes, 5, dataLength);
        //填充操作type
        transBytes[0] = (byte) type;
        //填充dataLength
        com.gongw.remote.Tools.int2BytesExtra(dataLength,transBytes);
        return transBytes;
    }
}
