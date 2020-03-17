package com.szty.h5xinfa.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("root")
public class ConfigBean {
    @XStreamAlias("id")
    public String id;
    @XStreamAlias("clientIp")
    public String clientIp;
    @XStreamAlias("clientPort")
    public String clientPort;
    @XStreamAlias("serverIp")
    public String serverIp;
    @XStreamAlias("serverPort")
    public String serverPort;
    @XStreamAlias("heartTime")
    public String heartTime;
    @XStreamAlias("planFolder")
    public String planFolder;
    @XStreamAlias("top")
    public String top;
    @XStreamAlias("left")
    public String left;
    @XStreamAlias("width")
    public String width;
    @XStreamAlias("height")
    public String height;
    @XStreamAlias("versionNumber")
    public String versionNumber;
    @XStreamAlias("heartUrl")
    public String heartUrl;
    @XStreamAlias("updateUrl")
    public String updateUrl;
    @XStreamAlias("updateEndUrl")
    public String updateEndUrl;
    @XStreamAlias("upgradeUrl")
    public String upgradeUrl;
    @XStreamAlias("upgradeEndUrl")
    public String upgradeEndUrl;
    @XStreamAlias("snapshotUrl")
    public String snapshotUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getClientPort() {
        return clientPort;
    }

    public void setClientPort(String clientPort) {
        this.clientPort = clientPort;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public String getHeartTime() {
        return heartTime;
    }

    public void setHeartTime(String heartTime) {
        this.heartTime = heartTime;
    }

    public String getPlanFolder() {
        return planFolder;
    }

    public void setPlanFolder(String planFolder) {
        this.planFolder = planFolder;
    }

    public String getTop() {
        return top;
    }

    public void setTop(String top) {
        this.top = top;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
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

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getHeartUrl() {
        return heartUrl;
    }

    public void setHeartUrl(String heartUrl) {
        this.heartUrl = heartUrl;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public String getUpdateEndUrl() {
        return updateEndUrl;
    }

    public void setUpdateEndUrl(String updateEndUrl) {
        this.updateEndUrl = updateEndUrl;
    }

    public String getUpgradeUrl() {
        return upgradeUrl;
    }

    public void setUpgradeUrl(String upgradeUrl) {
        this.upgradeUrl = upgradeUrl;
    }

    public String getUpgradeEndUrl() {
        return upgradeEndUrl;
    }

    public void setUpgradeEndUrl(String upgradeEndUrl) {
        this.upgradeEndUrl = upgradeEndUrl;
    }

    public String getSnapshotUrl() {
        return snapshotUrl;
    }

    public void setSnapshotUrl(String snapshotUrl) {
        this.snapshotUrl = snapshotUrl;
    }

    @Override
    public String toString() {
        return "ConfigBean{" + "id='" + id + '\'' + ", clientIp='" + clientIp + '\'' + ", clientPort='" + clientPort + '\'' + ", serverIp='" + serverIp + '\'' + ", serverPort='" + serverPort + '\'' + ", versionNumber='" + versionNumber + '\'' + '}';
    }
}
