package com.ads.clientconnection.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.ads.clientconnection.application.ClientApplication;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import androidx.documentfile.provider.DocumentFile;

/**
 * @author Ly
 */
public class BaseUtils {
    private static final String TAG = "BaseUtils";
    /**
     * 获取本机ip
     *
     * @return
     */
    public static String getHostIP() {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hostIp;
    }

    /**
     * 通过resouceid获取字符串
     *
     * @param id
     * @return
     */
    public static String getStringByResouceId(int id) {
        return ClientApplication.getInstance().getResources().getString(id);
    }

    /**
     * dp转px
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dpToPx(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * px转dp
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int pxToDp(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @return
     */
    public static int pxToSp(Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param context
     * @param spValue
     * @return
     */
    public static int spToPx(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static void jumpSystemSet(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", ClientApplication.getInstance().getPackageName(), null);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    /**
     * 根据视频播放时长 定义停留时长
     *
     * @param playTime
     * @return
     */
    public static String getStayTimeFromPlayTime(String playTime) {
        if (playTime.contains(":")) {
            String[] strs = playTime.split(":");
            int right = Integer.valueOf(strs[1]);
            String left = strs[0];
            if (left.startsWith("00")) {
                return right + "";
            } else if (left.startsWith("0")) {
                String temp = left.substring(0);
                int result = Integer.valueOf(temp) * 60 + right;
                return result + "";
            } else {
                int result = Integer.valueOf(left) * 60 + right;
                return result + "";
            }
        } else {
            return "10";
        }
    }


    /**
     * 通过Uri获取mimetype
     *
     * @param uri
     * @return
     */
    public static String getMimeTypeByUri(Context context, Uri uri) {
        String mimeType = "";
        if (context == null || uri == null) {
            Log.e(TAG, "getMimeTypeByUri: context is null or uri is null",null);
            return mimeType;
        }
        ContentResolver cr = context.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        if (mime!=null) {
             mimeType = mime.getExtensionFromMimeType(cr.getType(uri));
        }else{
            Log.e(TAG, "getMimeTypeByUri: mime is empty",null);
        }
        return mimeType;
    }

    /**
     * 通过Uri获取文件大小
     *
     * @return
     */
    public static long getFileLengthByUri(Context context, Uri uri) {
        long length = 0;
        if (context == null || uri == null) {
            Log.e(TAG, "getFileLengthByUri: context is null or uri is null",null);
            return length;
        }
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
        if (documentFile != null && documentFile.exists()) {
            length = documentFile.length();
        } else {
            length = 0;
            Log.e(TAG, "getFileLengthByUri: DocumentFile is null or no exists",null);
        }
        return length;
    }
}
