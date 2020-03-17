package com.szty.h5xinfa;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.szty.h5xinfa.model.ConfigBean;
import com.thoughtworks.xstream.XStream;

import java.io.File;
import java.io.FileInputStream;

/**
 * xml 配置文件的管理者
 *
 * @author Ly
 */
public class XmlManager {
    private static final String TAG = "XmlManager";
    private ConfigBean configBean;
    private XmlManager() {
    }

    private static XmlManager instance;

    public static XmlManager getInstance() {
        if (instance == null) {
            synchronized (XmlManager.class) {
                if (instance == null) {
                    instance = new XmlManager();
                }
            }
        }
        return instance;
    }

    public ConfigBean getConfigBean() {
        return configBean;
    }

    public void loadXmlData(Handler handler, Context context) throws Exception {
        String p = context.getExternalFilesDir(Constant.PATH_SZTY).getAbsolutePath();
        String path = p + File.separator + Constant.PATH_CONFIG;
        Log.i(TAG, "loadXmlData: path ... " + path);
        //获取 android/data/pagename/files/szty/config/config.xml 路径
        File file = new File(path, Constant.XML_CONFIG);
        //config.xml文件存在
        if (file.exists()) {
            int length = (int) file.length();
            byte[] buff = new byte[length];
            FileInputStream fin = new FileInputStream(file);
            fin.read(buff);
            fin.close();
            String result = new String(buff, "UTF-8");
            XStream xStream = new XStream();
            xStream.processAnnotations(ConfigBean.class);
            configBean = (ConfigBean) xStream.fromXML(result);
            handler.sendEmptyMessage(Constant.LOAD_XML_FINISH);
        }else{
            //config.xml文件不存在 发送消息
            Log.e(TAG, "loadXmlData: xml file is not exists");
            handler.sendEmptyMessage(Constant.FILE_NOT_EXIST);
        }
    }
}
