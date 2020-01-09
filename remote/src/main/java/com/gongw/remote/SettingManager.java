package com.gongw.remote;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
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
        String s = context.getExternalFilesDir("szty").getAbsolutePath()+File.separator+"Config";
        File f = new File(s);
        if (!f.exists()) {
            f.mkdirs();
        }
        //  /storage/emulated/0/Android/data/com.ads.xinfa/files/szty/Config
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

    /**
     * 修改本地文件的密码部分
     * @param psd
     */
    public void modifyPsd(Activity activity, String psd, Handler handler){
        try {
            File file = new File(activity.getExternalFilesDir("szty/Config"),SETTING_TXT);
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            StringBuilder sb = new StringBuilder();
            while((len = fis.read(buffer,0,buffer.length))!=-1){
                String str = new String(buffer,0,len);
                sb.append(str);
            }
            String readStr = sb.toString();
            Log.i(TAG,"modifyPsd 读出的文件内容 ... "+readStr);
            if (!TextUtils.isEmpty(readStr)) {
                String preStr = readStr.substring(0,readStr.lastIndexOf("-")+1);
                Log.i(TAG,"截取密码之前的字符串 ... "+preStr);
                String result = preStr + psd;
                Log.i(TAG,"最新写入文件的内容 ... "+result);
                file.delete();
                file.createNewFile();
                //写到本地文件中
                FileOutputStream fos = new FileOutputStream(file);
                byte[] b = result.getBytes("utf-8");
                fos.write(b);
                fos.flush();
                fos.close();
                if (setData(result)) {
                    handler.sendEmptyMessageDelayed(RemoteConst.MODIFY_PSD_SUCCESS,1500);
                }else{
                    handler.sendEmptyMessageDelayed(RemoteConst.MODIFY_PSD_FAIL,1500);
                }
            }else{
                handler.sendEmptyMessageDelayed(RemoteConst.MODIFY_PSD_FAIL,1500);
            }
        } catch (Exception e) {
            e.printStackTrace();
            handler.sendEmptyMessageDelayed(RemoteConst.MODIFY_PSD_FAIL,1500);
        }
    }

    private boolean setData(String d){
        if (TextUtils.isEmpty(d)) {
            return false;
        }
        try {
            String[] strs = d.split("\\|");
            for (int i = 0; i < strs.length; i++) {
                String[] s = strs[i].split("-");
                if (s[0].equals("url")) {
                    RemoteConst.URL_HTTP_DOWNLOAD = s[1];
                } else if (s[0].equals("udpPort")) {
                    RemoteConst.DEVICE_SEARCH_PORT = Integer.valueOf(s[1]);
                } else if (s[0].equals("serverPort")) {
                    RemoteConst.COMMAND_RECEIVE_PORT = Integer.valueOf(s[1]);
                } else if (s[0].equals("floatViewOpen")) {
                    RemoteConst.floatViewIsOpen = Boolean.parseBoolean(s[1]);
                } else if (s[0].equals("password")) {
                    RemoteConst.CONNECT_PSD = s[1];
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            setData(DEFAULT_SETTING);
            return false;
        }
    }
}
