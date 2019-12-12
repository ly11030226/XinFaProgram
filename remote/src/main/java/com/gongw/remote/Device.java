package com.gongw.remote;

/**
 * 局域网中的设备
 */
public class Device {
    //ip地址
    private String ip;
    //端口号
    private int port;
    //唯一id
    private String uuid;
    //设备名称
    private String name;
    //设备系统
    private String os;

    public static final String WINDOWS = "WINDOWS";
    public static final String ANDROID = "ANDROID";

    public Device(String ip, int port, String uuid,String name,String os) {
        super();
        this.ip = ip;
        this.port = port;
        this.uuid = uuid;
        this.name = name;
        this.os = os;
    }
    public Device(String ip,int port){
        super();
        this.ip = ip;
        this.port = port;
    }
    public Device(String ip,int port,String name,String os){
        super();
        this.ip = ip;
        this.port = port;
        this.name = name;
        this.os = os;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }
}
