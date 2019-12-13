package com.gongw.remote;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 本地配置文件config.txt
 * @author Ly
 */
public class SettingManager {
    private static final String TAG = "SettingManager";
    public static final String SETTING_TXT = "setting.txt";
    public static final String DEFAULT_SETTING = "url-http://www.sztiye.com/download/xinfa_control_1.0.0.apk|udpPort-8100|serverPort-60001|floatViewOpen-true|password-123456";

    private SettingManager(){}
    private static SettingManager instance;
    public static SettingManager getInstance(){
        if (instance == null) {
            synchronized (SettingManager.class){
                if (instance == null) {
                    instance = new SettingManager();
                }
            }
        }
        return instance;
    }

    public void writeConfigTxt(Context context) throws IOException {
        File f = context.getExternalFilesDir("szty/Config");
        //  /storage/emulated/0/Android/data/com.ads.xinfa/files/config
        String path = f.getAbsolutePath();
        File file = new File(f,SETTING_TXT);
        if (!file.exists()) {
            file.createNewFile();
            byte[] b = DEFAULT_SETTING.getBytes("UTF-8");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(b);
            fos.flush();
            fos.close();
            setData(DEFAULT_SETTING);
        }else{
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            StringBuilder sb = new StringBuilder();
            while((len = fis.read(buffer,0,buffer.length))!=-1){
                String str = new String(buffer,0,len);
                sb.append(str);
            }
            setData(sb.toString());
        }
    }

    private void setData(String d){
        if (TextUtils.isEmpty(d)) {
            return;
        }
        try {
            String[] strs = d.split("\\|");
            for (int i = 0; i < strs.length; i++) {
                String[] s = strs[i].split("-");
                if (s[0].equals("url")) {
                    RemoteConst.URL_HTTP_DOWNLOAD = s[1];
                }else if (s[0].equals("udpPort")) {
                    RemoteConst.DEVICE_SEARCH_PORT= Integer.valueOf(s[1]);
                }else if (s[0].equals("serverPort")) {
                    RemoteConst.COMMAND_RECEIVE_PORT = Integer.valueOf(s[1]);
                }else if (s[0].equals("floatViewOpen")) {
                    RemoteConst.floatViewIsOpen = Boolean.parseBoolean(s[1]);
                }else if (s[0].equals("password")) {
                    RemoteConst.CONNECT_PSD = s[1];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"setData is error");
            setData(DEFAULT_SETTING);
        }
    }
}
