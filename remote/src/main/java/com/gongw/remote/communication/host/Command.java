package com.gongw.remote.communication.host;


public class Command {
    private String destIp;
    private String content;
    private Callback callback;
    private int commandType;


    public static final String GET_LIST = "GET_LIST";  //获取资源列表的请求
    public static final String UPLOAD_FILE = "UPLOAD_FILE";  //上传文件


    public Command(String command, Callback callback){
        this.content = command;
        this.callback = callback;
    }

    public int getCommandType() {
        return commandType;
    }

    public void setCommandType(int commandType) {
        this.commandType = commandType;
    }

    public String getDestIp() {
        return destIp;
    }

    public void setDestIp(String destIp) {
        this.destIp = destIp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onRequest(String msg);
        void onSuccess(String msg);
        void onError(String msg);
        void onEcho(String msg);
    }



}
