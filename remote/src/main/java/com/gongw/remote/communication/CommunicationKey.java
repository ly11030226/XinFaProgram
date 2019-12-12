package com.gongw.remote.communication;

/**
 * 定义通信用的一些标识
 * Created by gw on 2017/7/28.
 */
public class CommunicationKey {
    public static final String RESPONSE_OK = "ok";
    public static final String RESPONSE_ECHO = "echo:";
    public static final String RESPONSE_ERROR = "error:";
    public static final String EOF = "\r";


    //请求获取展示文件列表
    public static final int REQUEST_GET_LIST = 1;
    //响应展示文件列表
    public static final int RESPONSE_GET_LIST = 2;
    //请求更新文件列表
    public static final int REQUEST_UPDATE_LIST = 3;
    //响应更新文件列表
    public static final int RESPONSE_UPDATE_LIST = 4;
    //请求上传文件
    public static final int REQUEST_UPLOAD_RES = 5;
    //响应上传文件
    public static final int RESPONSE_UPLOAD_RES = 6;
    //请求下载文件
    public static final int REQUEST_DOWNLOAD_FILE = 7;
    //响应下载文件
    public static final int RESPONSE_DOWNLOAD_FILE = 8;


    public static final int FLAG_CLIENT_READ_DATA = 0x111;
    public static final int FLAG_CLIENT_UPDATE_SUCCESS = 0x112;
    public static final int FLAG_JSON_TXT_IS_NULL = 0x113;
    public static final int FLAG_DOWNLOAD_FILE_IS_NULL = 0x114;
    public static final int FLAG_DOWNLOAD_FILE_IS_FINISH = 0x115;
    public static final int FLAG_FILE_IS_EXIST = 0x116;



    public static final int RESPONSE_OK_STATE = 111;
    public static final int RESPONSE_ERROR_STATE = 112;


}
