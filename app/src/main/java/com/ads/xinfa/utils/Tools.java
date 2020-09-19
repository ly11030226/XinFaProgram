package com.ads.xinfa.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.DisplayMetrics;

import com.ads.xinfa.base.MyLogger;
import com.ads.xinfa.entity.ImageAndVideoEntity;
import com.gongw.remote.RemoteConst;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Tools {
    private static final String TAG = "Tools";
    public static <T> boolean notEmpty(List<T> list) {
        return !isEmpty(list);
    }

    public static <T> boolean isEmpty(List<T> list) {
        if (list == null || list.size() == 0) {
            return true;
        }
        return false;
    }

    // 将px值转换为dip或dp值
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    // 将dip或dp值转换为px值
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    // 将px值转换为sp值
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    // 将sp值转换为px值
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    // 屏幕宽度（像素）
    public static int getWindowWidth(Activity context) {
        DisplayMetrics metric = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;
    }

    // 屏幕高度（像素）
    public static int getWindowHeight(Activity context) {
        DisplayMetrics metric = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metric);
        return metric.heightPixels;
    }

    // 根据Unicode编码判断中文汉字和符号
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    // 判断中文汉字和符号
    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取网络视频第一帧
     * @param videoUrl
     * @return
     */
    public static Bitmap getNetVideoBitmap(String videoUrl) {
        Bitmap bitmap = null;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            //根据url获取缩略图
            retriever.setDataSource(videoUrl, new HashMap());
            //获得第一帧图片
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return bitmap;
    }
    /**
     * 获取本地视频的第一帧
     *
     * @param localPath
     * @return
     */
    public static Bitmap getLocalVideoBitmap(String localPath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {

            //根据文件路径获取缩略图
            retriever.setDataSource(localPath);
            //获得第一帧图片
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return bitmap;
    }

    /**
     * 获取本地视频的第一帧
     *
     * @param context
     * @return
     */
    public static Bitmap getLocalVideoBitmap(Context context, Uri uri) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            //根据文件路径获取缩略图
            retriever.setDataSource(context,uri);
            //获得第一帧图片
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return bitmap;
    }

    /**
     * 生成Json字符串
     * @param context
     * @param mVideoList
     * @return
     */
    public static String makeJsonData(Context context,ArrayList<ImageAndVideoEntity.FileEntity> mVideoList){
        if (context==null) {
            MyLogger.e(TAG,"************* makeJsonData context is null *************");
            return "";
        }
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        ImageAndVideoEntity entity = new ImageAndVideoEntity();
        ImageAndVideoEntity.Info info = new ImageAndVideoEntity.Info();
        info.setcIp(BaseUtils.getHostIP());
        info.setcPort(RemoteConst.COMMAND_RECEIVE_PORT+"");
        info.setVolume("1");
        info.setcName(SystemUtil.getSystemModel());
        info.setWidth(displayMetrics.widthPixels+"");
        info.setHeight(displayMetrics.heightPixels+"");
        info.setUdpPort(RemoteConst.DEVICE_SEARCH_PORT+"");
        info.setFtpPath("");
        info.setHttpPath(RemoteConst.URL_HTTP_DOWNLOAD);

        entity.setInfo(info);
        entity.setFiles(mVideoList);
        Gson gson = new Gson();
        String result = gson.toJson(entity);
        return result;
    }

    /**
     * 根据视频播放时长 定义停留时长
     * @param playTime
     * @return
     */
    public static String setStayTimeFromPlayTime(String playTime){
        if (playTime.contains(":")) {
            String[] strs =  playTime.split(":");
            int right = Integer.valueOf(strs[1]);
            String left = strs[0];
            if (left.startsWith("00")) {
                return right+"";
            }else if (left.startsWith("0")) {
                String temp = left.substring(0);
                int result = Integer.valueOf(temp)*60 + right;
                return result+"";
            }else{
                int result = Integer.valueOf(left)*60 + right;
                return result+"";
            }
        }else{
            return "10";
        }
    }
    /**
     * 获取版本名称
     *
     * @param context 上下文
     *
     * @return 版本名称
     */
    public static String getVersionName(Context context) {
        //获取包管理器
        PackageManager pm = context.getPackageManager();
        //获取包信息
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            //返回版本号
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
