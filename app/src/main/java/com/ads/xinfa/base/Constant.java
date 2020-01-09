package com.ads.xinfa.base;

/**
 * 常量
 */
public class Constant {
    //是否是单元测试
    public static boolean isUnitTest = false;

    //FTP
    public static final String FTP_USER = "admin";
    public static final String FTP_PWD = "123456";
    public static final int FTP_PORT = 2221;

    //URL
    public static final String URL = "http://192.168.0.2:16888/interface/interface1.aspx?rt=snapshot&es=131";
    public static final String URL_PREFIX = "http://";
    public static final String URL_PATH = "/interface/interface1.aspx?";

    //UDP收到的消息
    public static final int UPDATE_XML_FILE = 0X10;  //更新xml文件
    public static final int GET_SNAPSHOOT = 0x11;   //快照
    public static final int UPDATE_APK = 0X12;   //下载apk 更新
    public static final int INSTALL_APK = 0x13;  //安装apk

    //显示样式
    public static final String TYPE_SHOW_MARQUEE = "跑马灯";
    public static final String TYPE_SHOW_IMAGE_AND_VIDEO = "视图混播";
    public static final String TYPE_SHOW_IMAGE = "图片轮播";
    public static final String TYPE_SHOW_VIDEO = "视频轮播";

    //跑马灯运动轨迹
    public static final String MARQUEE_LEFT = "left";
    public static final String MARQUEE_RIGHT = "right";

    //UDP 发布过来的操作
    public static final String METHOD_UPDATE_XML = "更新";
    public static final String METHOD_GET_SNAPSHOOT = "快照";
    public static final String METHOD_UPDATE_APK = "升级";

    //下载apk存放路径
    public static final String NAME_DOWNLOAD_APK = "myxinfa.apk";
//    public static final String PATH_DOWNLOAD_APK = Environment
//            .getExternalStorageDirectory().getPath()+
//            File.separator+
//            "SZTY" +
//            File.separator+
//            "download";



    //指令集
    public static final String COMMAND_UPDATE = "更新";
    public static final String COMMAND_UPGRADE = "升级";
    public static final String COMMAND_SETTING = "配置";



    //端口号
    public static final int LOCAL_PORT = 6000;
    public static final int DEST_PORT = 6001;


    //本地端口
    public static final int RECEIVE_COMMAND_PORT = 5555;


    //ftp下载的apk名字
    public static final String APK_NAME = "xinfa_control_1.0.0.apk";
    public static final String URL_DOWNLOAD_APK = "http://www.sztiye.com/download/xinfa_control_1.0.0.apk";

    public static final String ACTION_JUMP_TO_VIDEO_ACTIVITY = "ACTION_JUMP_TO_VIDEO_ACTIVITY";

    //从哪个Activity跳转到LanConnectionHostActivity的标识
    public static final String ACTION_JUMP_FROM_WHERE = "ACTION_JUMP_FROM_WHERE";
    public static final String FROM_WELCOME = "FROM_WELCOME";
    public static final String FROM_HELP = "FROM_HELP";

    //处理Socket Data的Handler key
    public static final int KEY_UPDATE_NEW_LIST = 0X01;    //从控制端传输过来的新list
    public static final int KEY_LIST_IS_EMPTY = 0X02;      //从控制端传输过来的list是空的
    public static final int KEY_TRANFER_SUCCESS = 0X03;    //新list中的新文件已上传成功
    public static final int KEY_START_UPDATE_LIST = 0X04;  //开始更新list


}
