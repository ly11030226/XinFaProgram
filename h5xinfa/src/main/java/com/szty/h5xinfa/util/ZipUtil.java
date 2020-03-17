package com.szty.h5xinfa.util;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Ly
 */

public class ZipUtil {
    private static final String TAG = "ZipUtil";

    /**
     * Java 自带的解压缩方法
     * @param zipFile
     * @param unZipSavePath
     * @throws IOException
     */
    public static void unZipFileByJava(File zipFile, String unZipSavePath) throws IOException {
        //如果解压缩文件没有删除 先删除
        File oldFile = new File(unZipSavePath);
        if (oldFile.exists()) {
            File[] fileList = oldFile.listFiles();
            if (fileList != null && fileList.length > 0) {
                oldFile.delete();
            }
        }

        ZipInputStream zis = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            zis = new ZipInputStream(new FileInputStream(zipFile), Charset.forName("GBK"));
        }else{
            zis = new ZipInputStream(new FileInputStream(zipFile));
        }
        ZipEntry zipEntry;
        String szName = "";
        try {
            while((zipEntry = zis.getNextEntry())!=null){
                szName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    szName = szName.substring(0,szName.length() - 1);
                    Log.i(TAG,"szName ... "+szName);
                    File folder = new File(unZipSavePath + File.separator + szName);
                    folder.mkdirs();
                }else{
                    Log.i(TAG,"szName ... "+szName);
                    File file = new File(unZipSavePath + File.separator + szName);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }else{
                        file.delete();
                        file.createNewFile();
                    }

                    //写文件
                    byte[] buffer = new byte[1024];
                    int len;
                    FileOutputStream fps = new FileOutputStream(file);
                    while((len = zis.read(buffer))!=-1){
                        fps.write(buffer,0,len);
                        fps.flush();
                    }
                    fps.close();
                }
            }
            zis.close();
        } catch (UTFDataFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * Apache 的解压缩方法
     * 将zipFile文件解压到folderPath目录下.
     *
     * @param zipFile    zip文件
     * @param folderPath 解压到的地址
     * @throws IOException
     */
    public static void upZipFileByApache(File zipFile, String folderPath) throws IOException {
        BufferedInputStream bi;
        org.apache.tools.zip.ZipFile zf = new org.apache.tools.zip.ZipFile(zipFile, "GBK");
        Enumeration e = zf.getEntries();
        while (e.hasMoreElements()){
            org.apache.tools.zip.ZipEntry ze2 = (org.apache.tools.zip.ZipEntry) e.nextElement();
            String entryName = ze2.getName();
            String path = folderPath + "/" + entryName;
            if (ze2.isDirectory()){
                System.out.println("正在创建解压目录 - " + entryName);
                File decompressDirFile = new File(path);
                if (!decompressDirFile.exists()){
                    decompressDirFile.mkdirs();
                }
            } else{
                System.out.println("正在创建解压文件 - " + entryName);
                String fileDir = path.substring(0, path.lastIndexOf("/"));
                File fileDirFile = new File(fileDir);
                if (!fileDirFile.exists()){
                    fileDirFile.mkdirs();
                }
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(folderPath + "/" + entryName));
                bi = new BufferedInputStream(zf.getInputStream(ze2));
                byte[] readContent = new byte[1024];
                int readCount = bi.read(readContent);
                while (readCount != -1){
                    bos.write(readContent, 0, readCount);
                    readCount = bi.read(readContent);
                }
                bos.close();
            }
        }
        zf.close();
    }
}
