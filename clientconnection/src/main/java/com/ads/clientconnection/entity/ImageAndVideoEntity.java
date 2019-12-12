package com.ads.clientconnection.entity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 视图轮播实体类
 */
public class ImageAndVideoEntity implements Serializable {

    private Info info;
    private ArrayList<FileEntity> files;

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public ArrayList<FileEntity> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<FileEntity> files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return "ImageAndVideoEntity{" + "info=" + info + ", files=" + files + '}';
    }

    public static class Info implements Serializable{
        private String cIp;
        private String cPort;
        private String cName;
        private String width;
        private String height;
        private String volume;
        private String audioPath;
        private String udpPort;
        private String httpPath;
        private String ftpPath;

        public String getFtpPath() {
            return ftpPath;
        }

        public void setFtpPath(String ftpPath) {
            this.ftpPath = ftpPath;
        }

        public String getUdpPort() {
            return udpPort;
        }

        public void setUdpPort(String udpPort) {
            this.udpPort = udpPort;
        }

        public String getHttpPath() {
            return httpPath;
        }

        public void setHttpPath(String httpPath) {
            this.httpPath = httpPath;
        }

        public String getcIp() {
            return cIp;
        }

        public void setcIp(String cIp) {
            this.cIp = cIp;
        }

        public String getcPort() {
            return cPort;
        }

        public void setcPort(String cPort) {
            this.cPort = cPort;
        }

        public String getcName() {
            return cName;
        }

        public void setcName(String cName) {
            this.cName = cName;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getVolume() {
            return volume;
        }

        public void setVolume(String volume) {
            this.volume = volume;
        }

        public String getAudioPath() {
            return audioPath;
        }

        public void setAudioPath(String audioPath) {
            this.audioPath = audioPath;
        }

        @Override
        public String toString() {
            return "Info{" + "cIp='" + cIp + '\'' + ", cPort='" + cPort + '\'' + ", cName='" + cName + '\'' + ", width='" + width + '\'' + ", height='" + height + '\'' + ", volume='" + volume + '\'' + ", audioPath='" + audioPath + '\'' + '}';
        }
    }
    public static class FileEntity implements Serializable{
        private String format; //图片 or 视频
        private String path;
        private String time; //停留时长
        private String name;
        private String size;
        private String playTime; //播放时长
        private boolean isAdd;  //是否是新添加的


        public boolean isAdd() {
            return isAdd;
        }

        public void setAdd(boolean add) {
            isAdd = add;
        }

        public String getPlayTime() {
            return playTime;
        }

        public void setPlayTime(String playTime) {
            this.playTime = playTime;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        @Override
        public String toString() {
            return "FileEntity{" + "format='" + format + '\'' + ", path='" + path + '\'' + ", time='" + time + '\'' + ", name='" + name + '\'' + ", size='" + size + '\'' + ", playTime='" + playTime + '\'' + ", isAdd=" + isAdd + '}';
        }
    }
}
