package com.ads.xinfa.ui.displayVideoAndImage;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.ads.xinfa.base.Constant;
import com.ads.xinfa.entity.ImageAndVideoEntity;
import com.ads.xinfa.net.BaseHandleSocketData;
import com.ads.xinfa.utils.Tools;

import java.util.ArrayList;

public class DisplayHandleSocketData extends BaseHandleSocketData {

    private Context context;
    private ArrayList<ImageAndVideoEntity.FileEntity> fileEntityArrayList;
    private Handler handler;
    public DisplayHandleSocketData(Context context,ArrayList<ImageAndVideoEntity.FileEntity> list,Handler handler) {
        super(context);
        this.context = context;
        this.fileEntityArrayList = list;
        this.handler = handler;
    }

    @Override
    public String makeJsondDataForResponse() {
        return Tools.makeJsonData(context,fileEntityArrayList);
    }

    @Override
    public void updateViewByNewList(ArrayList<ImageAndVideoEntity.FileEntity> videoList) {
        Message msg = handler.obtainMessage();
        msg.what = Constant.KEY_UPDATE_NEW_LIST;
        msg.obj = videoList;
        handler.sendMessage(msg);
    }

    @Override
    public void updateViewByUpload(ArrayList<ImageAndVideoEntity.FileEntity> videoList) {
        Message msg = handler.obtainMessage();
        msg.what = Constant.KEY_TRANFER_SUCCESS;
        msg.obj = videoList;
        handler.sendMessage(msg);
    }

    @Override
    public void toastEmptyList() {
        Message msg = handler.obtainMessage();
        msg.what = Constant.KEY_LIST_IS_EMPTY;
        handler.sendMessage(msg);
    }

    @Override
    public void startUpdateList() {
        Message msg = handler.obtainMessage();
        msg.what = Constant.KEY_START_UPDATE_LIST;
        handler.sendMessage(msg);
    }

    @Override
    public void haveClientConn() {
//        try {
//            File f = new File(FileManager.UPLOAD_DIR + FileManager.JSON_DATA);
//            FileInputStream fis = new FileInputStream(f);
//            byte[] b = new byte[1024*8];
//            String str = "";
//            int len;
//            while ((len = fis.read(b))!= -1) {
//                str = new String(b,0,len);
//            }
//            fis.close();
//            if (!TextUtils.isEmpty(str)) {
//                Gson gson = new Gson();
//                ImageAndVideoEntity entity = gson.fromJson(str,ImageAndVideoEntity.class);
//                ArrayList<ImageAndVideoEntity.FileEntity> list = entity.getFiles();
//                if (list != null && list.size()>0) {
//                    Message msg = handler.obtainMessage();
//                    msg.what = Constant.KEY_UPDATE_NEW_LIST;
//                    msg.obj = list;
//                    handler.sendMessage(msg);
//
//
//                    fileEntityArrayList.clear();
//                    fileEntityArrayList.addAll(list);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public ArrayList<ImageAndVideoEntity.FileEntity> getFileEntityArrayList(){
        return this.fileEntityArrayList;
    }

}
