package com.ads.xinfa.base;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

/**
 *
 * @author Ly
 */
public class FileManager {
    private static final String TAG = "FileManager";
    public static final String XML_DATA = "data.xml";
    public static final String XML_CONFIG = "Config.xml";
    public static final String TEST_XML_CONFIG = "test_Config.xml";
    public static final String TEST_XML_DATA = "test_data.xml";
    public static final String JSON_DATA = "json.txt";
    private static String basePath;

    public static void init(Context context) {
        if (context != null) {
            MyLogger.d(TAG, "create file");
            File file = context.getExternalFilesDir("szty");
            if (!file.exists()) {
                file.mkdirs();
            }
            basePath = file.getAbsolutePath();
            screenShotPath = basePath + File.separator + "ScreenShot.jpg";
            XML_DIR = basePath + File.separator + "Xml" + File.separator;
            Resource_DIR = basePath + File.separator + "FileDownloader" + File.separator;
            TEMP_DIR = basePath + File.separator + "Temp" + File.separator;
            UPLOAD_DIR = basePath + File.separator + "Upload" + File.separator;
        }
    }

    public static String screenShotPath;
    public static String XML_DIR;
    public static String Resource_DIR;
    public static String TEMP_DIR;
    public static String UPLOAD_DIR;

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
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
