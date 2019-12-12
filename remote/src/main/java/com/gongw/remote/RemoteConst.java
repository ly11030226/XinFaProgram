package com.gongw.remote;

public class RemoteConst {

	/**
	 * 下载APK的url
	 */
	public static String URL_HTTP_DOWNLOAD;
	/**
     * 用于设备搜索的端口
	 */
	public static int DEVICE_SEARCH_PORT = 8100;
	/**
     * 用于接收命令的端口
	 */
	public static int COMMAND_RECEIVE_PORT = 60001;
	/**
	 * 设备搜索次数
	 */
	public static final int SEARCH_DEVICE_TIMES = 1;
	/**
	 * 搜索的最大设备数量
	 */
	public static final int SEARCH_DEVICE_MAX = 250;
	/**
     * 接收超时时间
	 */
	public static final int RECEIVE_TIME_OUT = 15*1000;

	/**
	 * udp数据包前缀
	 */
	public static final int PACKET_PREFIX = '$';
	/**
     * udp数据包类型：搜索类型
	 */
	public static final int PACKET_TYPE_SEARCH_DEVICE_REQ = 0x10;
	/**
	 * udp数据包类型：搜索应答类型
	 */
	public static final int PACKET_TYPE_SEARCH_DEVICE_RSP = 0x11;

	/**
	 * 关闭Socket的通知
	 */
	public static final int FLAG_CLOSE_SOCKET = 0X21;

	/**
	 * Server Socket已经关闭
	 */
	public static final int FLAG_SERVER_CLOSE = 0X22;

	/**
	 * 客户端Socket 连接超时时长
	 */
	public static final int TIME_OUT_30_SEC = 30 * 1000;
	/**
	 * 是否显示悬浮按钮
	 */
	public static boolean floatViewIsOpen;
	/**
	 * 连接机器的密码
	 */
	public static String CONNECT_PSD;
}
