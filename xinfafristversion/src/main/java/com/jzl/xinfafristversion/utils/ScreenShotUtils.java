package com.jzl.xinfafristversion.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 截屏工具类
 * Created by Administrator on 2017/2/10.
 */
public class ScreenShotUtils {
    /**
     * 进行截取屏幕
     *
     * @param pActivity
     * @return
     */
    public static Bitmap  takeScreenShot(Activity pActivity) {
        // View是你需要截图的View
        View view = pActivity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();

        // 获取状态栏高度
        Rect frame = new Rect();
        pActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        Log.i("TAG", "" + statusBarHeight);

        // 获取屏幕长和高
        int width = pActivity.getWindowManager().getDefaultDisplay().getWidth();
        int height = pActivity.getWindowManager().getDefaultDisplay().getHeight();
        // 去掉标题栏
        // Bitmap b = Bitmap.createBitmap(b1, 0, 25, 320, 455);
        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return b;
    }

    // 保存到sdcard
    public static void savePic(Bitmap b, String strFileName) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(strFileName);
            if (null != fos) {
                b.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
