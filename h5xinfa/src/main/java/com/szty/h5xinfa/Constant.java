package com.szty.h5xinfa;

/**
 * 静态常量
 * @author Ly
 */
public class Constant {
    /**
     * 有文件的目录
     */
    public static final String PATH_SZTY = "szty";
    public static final String PATH_CONFIG = "config";
    public static final String PATH_PAGE = "page";
    public static final String PATH_DOWNLOAD = "download";
    public static final String ZIP_NAME = "resource.zip";
    public static final String APK_NAME = "update.apk";
    public static final String HTML_INDEX = "index.html";
    public static final String PATH_TEMP = "temp";

    public static final String XML_CONFIG = "config.xml";


    public static final int LOAD_XML_FINISH = 0x01;
    public static final int LOAD_XML_ERROR = 0x02;
    public static final int FILE_NOT_EXIST = 0x03;

    public static final String KEY_HANDLE_CODE = "KEY_HANDLE_CODE";

    public static final String HTTP_CODE = "http://";

    //请求更新
    public static final int REQUEST_UPDATE = 0X11;
    //请求升级
    public static final int REQUEST_UPGRADE = 0X12;
    //发送心跳
    public static final int SEND_HEARTBEAT = 0x13;
}
