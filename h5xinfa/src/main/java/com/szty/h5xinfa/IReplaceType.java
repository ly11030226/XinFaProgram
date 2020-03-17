package com.szty.h5xinfa;

import android.app.Activity;

import java.io.File;

/**
 * 解压出来的文件替换源文件的方式
 */
public interface IReplaceType {
    /**
     *
     * 替换文件
     * @param unZipFile 解压缩出来的file
     * @param sourceFile 源文件的file
     * @return
     */
    boolean startReplace(Activity activity,File unZipFile, File sourceFile);
}
