package com.ads.utillibrary.utils;

import android.content.Context;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Tools {
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

    /**
     * 验证字符串是否符合json格式
     * @param jsonStr
     * @return
     */
    public static boolean validateJson(String jsonStr) {
        JsonElement jsonElement;
        try {
            jsonElement = new JsonParser().parse(jsonStr);
        } catch (Exception e) {
            return false;
        }
        if (jsonElement == null) {
            return false;
        }
        if (!jsonElement.isJsonObject()) {
            return false;
        }
        return true;
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
                String temp = left.substring(1);
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

}
