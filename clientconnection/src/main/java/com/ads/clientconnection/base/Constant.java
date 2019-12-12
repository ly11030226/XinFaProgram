package com.ads.clientconnection.base;

import android.os.Environment;

import java.io.File;

public class Constant {
    public static final String TEMP_DIR =
            Environment.getExternalStorageDirectory().getPath()+
            File.separator+
            "SZTY" +
            File.separator +
            "Temp" +
            File.separator;

    public static final String JUMP_RESOURCE_CONTROL = "JUMP_RESOURCE_CONTROL";


    public static final String ACTION_BACK = "ACTION_BACK";
    public static final String ACTION_IS_VIDEO = "ACTION_IS_VIDEO";

    public static final int FLAG_JUMP_TO_RES_CONTROL = 0X11;
    public static final int FLAG_CLOSE_PROGRESSBAR = 0x23;

    public static final String ACTION_FINISH_ACTIVITY = "ACTION_FINISH_ACTIVITY";
    public static final String ACTION_SHOW_SETTING_FROM_RESMANAGER ="ACTION_SHOW_SETTING_FROM_RESMANAGER";
    public static final String ACTION_SHOW_SETTING_FROM_RESLIST ="ACTION_SHOW_SETTING_FROM_RESLIST";
    public static final String ACTION_ADD_IMAGE = "ACTION_ADD_IMAGE";
    public static final String ACTION_SHOW_PROGRESSBAR = "ACTION_SHOW_PROGRESSBAR";
    public static final String ACTION_REMOVE_RES = "ACTION_REMOVE_RES";


    public static final String FLAG_PLAY_TYPE = "FLAG_PLAY_TYPE"; //播放类型
    public static final String PLAY_TYPE_VIDEO = "PLAY_TYPE_VIDEO";
    public static final String PLAY_TYPE_IMAGE = "PLAY_TYPE_IMAGE";

    public static final String FLAG_PLAY_DATA = "FLAG_PLAY_DATA";
    public static final String FLAG_REMOVE_IMAGE = "FLAG_REMOVE_IMAGE";

    public static final int UPLOAD_VIDEO_MAX_LENGTH = 100;
    public static final int UPLOAD_IMAGE_MAX_LENGTH = 5;


    /**资源管理器*/
    public static final String KEY_PLAY_DATA = "KEY_PLAY_DATA"; //保存SharePreference文件的key
    public static final String PLAY_LIST_DEFAULT_NAME = "DEFAULT"; //展示端的播放列表默认名字
    public static final String KEY_SHOW_SETTING = "KEY_SHOW_SETTING";

    public static final String RES_MANAGER_USE_ADAPTER = "RES_MANAGER_USE_ADAPTER";
    public static final String RES_LIST_USE_ADAPTER = "RES_LIST_USE_ADAPTER";
}
