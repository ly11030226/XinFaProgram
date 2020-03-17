package com.szty.h5xinfa;

import android.app.Activity;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 遍历解压后的目录，递归找到是文件的File，然后替换掉Page下面的的File，
 * 重名的先删除后写入，没有的直接添加
 * @author Ly
 */
public class NormalReplaceImpl implements IReplaceType {
    private static final String TAG = "NormalReplaceImpl";

    @Override
    public boolean startReplace(Activity activity, File unZipFile, File sourceFile) {
        if (activity == null || !unZipFile.exists() || !sourceFile.exists()) {
            return false;
        }
        try {
            checkIsDirectory(activity,unZipFile);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 查看是否是文件
     * @param file 解压后的文件存储路径
     */
    private void checkIsDirectory(Activity activity,File file) throws IOException {
        if (file.isDirectory()) {
            File[] f = file.listFiles();
            if (f != null && f.length > 0) {
                for (int i = 0; i < f.length; i++) {
                    checkIsDirectory(activity,f[i]);
                }
            }
        }else{
            writeToFile(activity, file);
        }
    }

    /**
     * 如果有重名的文件 则先删除 后替换
     * @param activity
     * @param file
     * @throws IOException
     */
    private void writeToFile(Activity activity, File file) throws IOException {
        String tempPath = file.getAbsolutePath();
        //获取对应page文件夹的文件路径
        String pagePath = getPagePath(tempPath);
        File ff = new File(pagePath);
        if (ff.exists()) {
            //重名文件存在则要先删除
            ff.delete();
            ff.createNewFile();
        }else{
            //父目录不存在 证明更新的是新文件 则要创建父目录 再写入
            if (!ff.getParentFile().exists()) {
                boolean mkdirs = ff.getParentFile().mkdirs();
                if (!mkdirs) {
                    throw new RuntimeException(activity.getString(R.string.create_file_error));
                }
            }
        }
        //写文件
        byte[] buffer = new byte[1024];
        int len;
        FileInputStream fis = new FileInputStream(file);
        FileOutputStream fps = new FileOutputStream(ff);
        while ((len = fis.read(buffer)) != -1) {
            fps.write(buffer, 0, len);
            fps.flush();
        }
        fis.close();
        fps.close();
    }

    /**
     * 获取page路径下的地址
     * @param tempPath
     * @return
     */
    private String getPagePath(String tempPath){
        String s = "";
        final String con = "/files/szty/temp/";
        final String result = "/files/szty/page/";
        if (tempPath.contains(con)) {
            s = tempPath.replace(con,result);
        }
        Log.i(TAG, "getPagePath: "+s);
        return s;
    }
}
