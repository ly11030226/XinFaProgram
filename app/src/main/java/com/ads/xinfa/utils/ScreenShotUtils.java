package com.ads.xinfa.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;

import com.ads.xinfa.base.FileManager;

import java.io.File;
import java.io.FileOutputStream;

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
    public static void takeScreenShot(Activity pActivity) {
        View view = pActivity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        view.setDrawingCacheEnabled(false);
        if (bitmap != null) {
            try {
                File file = new File(FileManager.screenShotPath);
                if (file.exists()) {
                    file.delete();
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                FileOutputStream os = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.flush();
                os.close();
            } catch (Exception e) {
            }
        }
    }

}
