package com.ads.xinfa.ui.welcome;

import android.content.Intent;
import android.content.res.AssetManager;
import android.media.MediaMetadataRetriever;
import android.util.DisplayMetrics;

import com.ads.utillibrary.utils.ConvertUtils;
import com.ads.xinfa.FtpService;
import com.ads.xinfa.R;
import com.ads.xinfa.base.Constant;
import com.ads.xinfa.base.FileManager;
import com.ads.xinfa.base.MyLogger;
import com.ads.xinfa.entity.ImageAndVideoEntity;
import com.ads.xinfa.ui.help.HelpActivity;
import com.ads.xinfa.ui.lanConnection.LanConnectionHostActivity;
import com.ads.xinfa.utils.BaseUtils;
import com.ads.xinfa.utils.SystemUtil;
import com.gongw.remote.RemoteConst;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class CustomWelcomeActivity extends WelcomeActivity {

    private static final String TAG = "CustomWelcomeActivity";
    private static final int JUMP_TO_LAN_CONN = 1;
    private static final int JUMP_TO_HELP = 2;
    private boolean isHasFile = false;
    private ArrayList<ImageAndVideoEntity.FileEntity> mVideoList = new ArrayList<>();

    @Override
    public int getLayoutId() {
        return R.layout.activity_welcome;
    }

    @Override
    public void doNext() {
        File file = new File(FileManager.UPLOAD_DIR);
        File[] files = null;
        if (file.exists()) {
            files = file.listFiles();
            if (files != null && files.length > 0) {
                isHasFile = true;
            } else {
                isHasFile = false;
            }
        } else {
            file.mkdirs();
            isHasFile = false;
        }
        if (isHasFile) {
            File f = new File(FileManager.UPLOAD_DIR + FileManager.JSON_DATA);
            // upload 文件夹存在 视图资源 但是没有json.txt文档，新建文档
            if (!f.exists()) {
                if (isCanWrite) {
                    new Thread(new WriteFileRunnable(files)).start();
                } else {
                    MyLogger.e(TAG, "permission is tiny,can not write file");
                }
            } else {
                new Thread(new ReadFileRunnable()).start();
            }
        } else {
//            writeFtpServiceApk(JUMP_TO_HELP);
            jumpToWhere(JUMP_TO_HELP);
        }
    }

    private void jumpToWhere(int type){
        if (type == JUMP_TO_HELP) {
            sendMsgToHelpActivity();
        } else if (type == JUMP_TO_LAN_CONN) {
            sendMsgToLanConnectionHostActivity();
        }
        //开启
    }

    private class ReadFileRunnable implements Runnable {

        FileInputStream fis;

        @Override
        public void run() {
            File f = new File(FileManager.UPLOAD_DIR + FileManager.JSON_DATA);
            try {
                fis = new FileInputStream(f);
                byte[] b = new byte[1024];
                int len;
                StringBuilder sb = new StringBuilder();
                while ((len = fis.read(b, 0, b.length)) != -1) {
                    String str = new String(b, 0, len);
                    sb.append(str);
                }
                Gson gson = new Gson();
                ImageAndVideoEntity imageAndVideoEntity = gson.fromJson(sb.toString(), ImageAndVideoEntity.class);
                imageAndVideoEntity.getInfo().setHttpPath(RemoteConst.URL_HTTP_DOWNLOAD);
                imageAndVideoEntity.getInfo().setcPort(RemoteConst.COMMAND_RECEIVE_PORT + "");
                imageAndVideoEntity.getInfo().setUdpPort(RemoteConst.DEVICE_SEARCH_PORT + "");
                imageAndVideoEntity.getInfo().setFtpPath("");
                String jsonStr = gson.toJson(imageAndVideoEntity);
                writeData(jsonStr.getBytes());
//                writeFtpServiceApk(JUMP_TO_LAN_CONN);
                jumpToWhere(JUMP_TO_LAN_CONN);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private class WriteFileRunnable implements Runnable {

        private File[] files;

        public WriteFileRunnable(File[] files) {
            this.files = files;
        }

        @Override
        public void run() {
            try {
                if (files != null && files.length > 0) {
                    //如果upload文件夹下有超过10个文件，且没有json.txt文件，则只播放前10个文件
                    int j = 0;
                    if (files.length > 3) {
                        j = 3;
                    } else {
                        j = files.length;
                    }
                    mVideoList.clear();
                    for (int i = 0; i < j; i++) {
                        ImageAndVideoEntity.FileEntity fileEntity = getVideoEntity(files[i]);
                        mVideoList.add(fileEntity);
                    }
                    String json = makeJsonData();
                    MyLogger.i(TAG, "json ... " + json);
                    writeData(json.getBytes());
//                    writeFtpServiceApk(JUMP_TO_LAN_CONN);
                    jumpToWhere(JUMP_TO_LAN_CONN);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void writeFtpServiceApk(int type) {
        //下载ftpservice包
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MyLogger.i(TAG, "Open AssetManager");
                    AssetManager am = getAssets();
                    InputStream is = am.open(Constant.APK_NAME);
                    File file = new File(FileManager.Resource_DIR + Constant.APK_NAME);
                    if (file.exists()) {
                        file.delete();
                    }
                    writeApk(is, file);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (type == JUMP_TO_HELP) {
                        sendMsgToHelpActivity();
                    } else if (type == JUMP_TO_LAN_CONN) {
                        sendMsgToLanConnectionHostActivity();
                    }
                    //开启ftp服务
                    startService(new Intent(CustomWelcomeActivity.this, FtpService.class));
                }
            }
        }).start();
    }

    private void writeApk(InputStream is, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        byte[] b = new byte[1024];
        int len;
        MyLogger.i(TAG, "write start");
        while ((len = is.read(b, 0, b.length)) != -1) {
            fos.write(b, 0, len);
        }
        is.close();
        fos.close();
        MyLogger.i(TAG, "write end");
    }

    private void sendMsgToLanConnectionHostActivity() {
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(CustomWelcomeActivity.this, LanConnectionHostActivity.class);
                intent.putExtra(Constant.ACTION_JUMP_FROM_WHERE, Constant.FROM_WELCOME);
                startActivity(intent);
                finish();
            }
        }, 3 * 1000);
    }

    private void sendMsgToHelpActivity() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(CustomWelcomeActivity.this, HelpActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3 * 1000);
    }

    private void writeData(byte[] b) throws IOException {
        File f = new File(FileManager.UPLOAD_DIR + FileManager.JSON_DATA);
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        if (!f.exists()) {
            f.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(b, 0, b.length);
        fos.close();
    }

    private ImageAndVideoEntity.FileEntity getVideoEntity(File file) {
        ImageAndVideoEntity.FileEntity fileEntity = null;
        try {
            String path = file.getAbsolutePath();
            MyLogger.i(TAG, "File path ... " + file.getAbsolutePath());
            FileInputStream fis = new FileInputStream(file);
            ;
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while (fis.read(buffer) != -1) {
                bos.write(buffer, 0, buffer.length);
            }
            byte[] results = bos.toByteArray();
            fileEntity = new ImageAndVideoEntity.FileEntity();
            fileEntity.setAdd(false);
            fileEntity.setPath(path);
            String name = file.getName();
            String format = "";
            int minute = 0, sec = 0;
            String finalTime = "";
            if (name.contains(".mp4")) {
                format = "视频";
                long time = getMediaLength(path);
                minute = (int) (time / 1000 / 60);
                sec = (int) (time / 1000 % 60);
                //        MyLogger.i(TAG, "minute: " + minute + "  sec: " + sec);
                String minuteStr = String.valueOf(minute);
                if (minuteStr.length() == 1) {
                    minuteStr = "0" + minuteStr;
                }
                String secStr = String.valueOf(sec);
                if (secStr.length() == 1) {
                    secStr = "0" + secStr;
                }
                finalTime = minuteStr + ":" + secStr;
                fileEntity.setPlayTime(finalTime);
                //停留时长
                MyLogger.i(TAG, "time ... " + time);
                fileEntity.setTime((time / 1000) + "");
            } else if (name.contains(".jpg")) {
                format = "图片";
                //停留时长 默认10秒
                fileEntity.setTime("10");
            } else if (name.contains(".jpeg")) {
                format = "图片";
                //停留时长
                fileEntity.setTime("10");
            }
            fileEntity.setName(name);
            fileEntity.setFormat(format);
            fileEntity.setSize(ConvertUtils.byte2FitMemorySize(results.length));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileEntity;
    }

    public long getMediaLength(String strMediaPath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(strMediaPath);
        String time = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInmillisec = Long.parseLong(time);
        return timeInmillisec;
    }

    private String makeJsonData() {
        DisplayMetrics displayMetrics = CustomWelcomeActivity.this.getResources().getDisplayMetrics();
        ImageAndVideoEntity entity = new ImageAndVideoEntity();
        ImageAndVideoEntity.Info info = new ImageAndVideoEntity.Info();
        info.setcIp(BaseUtils.getHostIP());
        info.setcPort(RemoteConst.COMMAND_RECEIVE_PORT + "");
        info.setVolume("1");
        info.setcName(SystemUtil.getSystemModel());
        info.setWidth(displayMetrics.widthPixels + "");
        info.setHeight(displayMetrics.heightPixels + "");
        info.setFtpPath("");
        info.setUdpPort(RemoteConst.DEVICE_SEARCH_PORT + "");
        info.setHttpPath(RemoteConst.URL_HTTP_DOWNLOAD);
        entity.setInfo(info);
        entity.setFiles(mVideoList);
        Gson gson = new Gson();
        String result = gson.toJson(entity);
        return result;
    }

}
