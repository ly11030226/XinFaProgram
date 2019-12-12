package com.jzl.xinfafristversion.base;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

public class FileManager {
    private static final String TAG = "FileManager";
    public static final String XML_DATA = "data.xml";
    public static final String XML_CONFIG = "Config.xml";
    public static final String TEST_XML_CONFIG = "test_Config.xml";
//    public static final String TEST_XML_DATA = "test_data.xml";
    public static final String JSON_DATA = "json.txt";

    public static final String screenShotPath = Environment
            .getExternalStorageDirectory().getPath()
            + File.separator
            + "SZTY" + File.separator + "ScreenShot" + File.separator;
    public static final String XML_DIR = Environment
            .getExternalStorageDirectory().getPath()
            + File.separator
            + "SZTY" + File.separator;
    public static final String Resource_DIR = Environment
            .getExternalStorageDirectory().getPath()
            + File.separator
            + "SZTY" + File.separator + "FileDownloader" + File.separator;

    public static final String TEMP_DIR = Environment
            .getExternalStorageDirectory().getPath()
            + File.separator
            + "SZTY" + File.separator + "Temp" + File.separator;
    public static final String UPLOAD_DIR = Environment.getExternalStorageDirectory().getPath()
            +File.separator+
            "SZTY"+File.separator+"Upload"+File.separator;


    public static String loadXMLFromSDCard(String fileName){
        String result = null;
        try {
            File f = new File(XML_DIR+fileName);
            if(f.exists()){
                int length = (int) f.length();
                byte[] buff = new byte[length];
                FileInputStream fin = new FileInputStream(f);
                fin.read(buff);
                fin.close();
                result = new String(buff, "UTF-8");
            }else{
                MyLogger.e(TAG,XML_DIR+fileName+" no found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 将最新的xml数据写入到文件
     * @param content
     * @param path
     * @param name
     * @return
     */
    public static boolean writeTxtToFile(String content,String path,String name){
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            // 每次写入时，都换行写
            String strContent = content + "\r\n";
            File txt = new File(path,name);
            if (txt.exists()) {
                txt.delete();
                txt.getParentFile().mkdirs();
                txt.createNewFile();
            }else{
                txt.getParentFile().mkdirs();
                txt.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(txt, "rwd");
            raf.seek(txt.length());
            raf.write(strContent.getBytes());
            raf.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
