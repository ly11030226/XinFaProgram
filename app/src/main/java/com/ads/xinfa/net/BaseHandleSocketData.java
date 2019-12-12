package com.ads.xinfa.net;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.ads.xinfa.base.FileManager;
import com.ads.xinfa.base.MyLogger;
import com.ads.xinfa.entity.ImageAndVideoEntity;
import com.ads.xinfa.utils.Tools;
import com.gongw.remote.RemoteConst;
import com.gongw.remote.communication.CommunicationKey;
import com.gongw.remote.communication.server.ServerByteSocketManager;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 处理Socket数据的基类
 */
public abstract class BaseHandleSocketData{
    private static final String TAG = "BaseHandleSocketData";
    private Gson gson;
    private Handler writeHandler;
    private MyHandlerThread mht;
    private ArrayList<ImageAndVideoEntity.FileEntity> videoList = new ArrayList<>();
    private Context context;
    public BaseHandleSocketData(Context context) {
        this.context = context;
        this.gson = new Gson();
    }

    private void transfer(ImageAndVideoEntity.FileEntity fileEntity){
        try {
            byte[] transBytes = ServerByteSocketManager.getInstance().makeBytes
                    (fileEntity.getPath(),CommunicationKey.RESPONSE_UPDATE_LIST);
            //保存要upload的文件信息
            ServerByteSocketManager.getInstance().setFileInfo(
                    fileEntity.getName(),
                    fileEntity.getPath());
            ServerByteSocketManager.getInstance().sendMsgByte(transBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendFinishInfo(){
        try {
            MyLogger.i(TAG,"sendFinishInfo");
            //发送结束通知
            byte[] transBytes = ServerByteSocketManager.getInstance().makeBytes(
                    CommunicationKey.RESPONSE_OK,CommunicationKey.RESPONSE_UPLOAD_RES);
            ServerByteSocketManager.getInstance().sendMsgByte(transBytes);
            //更改本地的json文件内容
            this.mht = new MyHandlerThread("writeJsonTxt");
            this.mht.start();
            this.writeHandler = new Handler(mht.getLooper());
            writeHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        //这里用自己的videoList不用子类重写的makeJsonData方法
                        String writeStr = Tools.makeJsonData(context,videoList);
                        MyLogger.i(TAG,"writeStr ... "+writeStr);
                        writeData(writeStr.getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class MyHandlerThread extends HandlerThread {
        public MyHandlerThread(String name) {
            super(name);
        }
    }
    private void writeData(byte[] b) throws IOException {
        File f = new File(FileManager.UPLOAD_DIR + FileManager.JSON_DATA);
        if (f.exists()) {
            f.delete();
            f.createNewFile();
        }else{
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            f.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(b,0,b.length);
        fos.close();
    }
    public void createServerIfRunnableIsNull(){
        ServerByteSocketManager.getInstance().createServerIfRunnableIsNull(RemoteConst.COMMAND_RECEIVE_PORT, new ServerByteSocketManager.RequestListener() {
            @Override
            public void clientConn() {
                haveClientConn();
            }

            @Override
            public String jsonResult() {
                return makeJsondDataForResponse();
            }

            @Override
            public void updateList(String json) {
                MyLogger.i(TAG,"updateList ... "+json);
                //弹框提示加载中
                startUpdateList();
                ImageAndVideoEntity entity = gson.fromJson(json,ImageAndVideoEntity.class);
                ArrayList<ImageAndVideoEntity.FileEntity> fileList= entity.getFiles();
                videoList.clear();
                if (fileList!=null &&fileList.size()>0) {
                    videoList.addAll(fileList);
                    isNeedUploadFile();
                }else{
                    sendFinishInfo();
                    toastEmptyList();
                }
            }

            @Override
            public void transferring(String path, String progress) {
                //MyLogger.i(TAG,"正在传输"+file.getName()+" 已完成 "+progress);
            }

            @Override
            public void transferSuccess(String path) {
                File file = new File(path);
                if (file.exists()) {
                    MyLogger.i(TAG,"传输"+path+"成功!!!");
                    synchronized (videoList){
                        for (ImageAndVideoEntity.FileEntity fileEntity : videoList){
                            if (fileEntity.getName().equals(file.getName())) {
                                fileEntity.setPath(file.getPath());
                                fileEntity.setAdd(false);
                            }
                        }
                        isNeedUploadFile();
                    }
                }
            }
        });
    }

    private void isNeedUploadFile() {
        boolean hasNew = false;
        for (ImageAndVideoEntity.FileEntity fileEntity : videoList){
            File file = new File(fileEntity.getPath());
            //新添加的
            if (fileEntity.isAdd()) {
                if (file.exists()) {
                    //如果存在 直接加载本地
                    fileEntity.setAdd(false);
                }else{
                    hasNew = true;
                    transfer(fileEntity);
                    break;
                }
            }
            else {//证明控制端先添加了一个新的文件，然后提交，然后再移动刚提交文件的顺序，
                // 再次提交，控制端没有来得及更新path
                if (!file.exists()) {
                    String path = FileManager.UPLOAD_DIR + file.getName();
                    file = new File(path);
                    if (file.exists()) {
                        fileEntity.setPath(path);
                    }
                }
            }
        }
        //如果没有新添加的直接更新
        if (!hasNew) {
            sendFinishInfo();
            updateViewByNewList(videoList);
        }
    }

    public void clear(){
        if (writeHandler!=null) {
            writeHandler.removeCallbacksAndMessages(null);
        }
        if (mht!=null) {
            mht.quit();
        }
    }

    //为了响应获取Json data的请求
    public abstract String makeJsondDataForResponse();
    //得到了新的展示list
    public abstract void updateViewByNewList(ArrayList<ImageAndVideoEntity.FileEntity> videoList);
    //上传完用户添加视图后的操作
    public abstract void updateViewByUpload(ArrayList<ImageAndVideoEntity.FileEntity> videoList);
    //新的展示list为空
    public abstract void toastEmptyList();
    //开始显示list内容
    public abstract void startUpdateList();
    //当有客户端连接后
    public abstract void haveClientConn();


}
