package com.ads.clientconnection.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 播放列表实体
 */
public class PlayListEntity implements Serializable {
    //名字
    private String name;
    //创建时间
    private String createTime;
    //当前是否被选中
    private boolean isChoice;
    //对应的资源列表
    private List<ImageAndVideoEntity.FileEntity> fileEntityList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public boolean isChoice() {
        return isChoice;
    }

    public void setChoice(boolean choice) {
        isChoice = choice;
    }

    public List<ImageAndVideoEntity.FileEntity> getFileEntityList() {
        return fileEntityList;
    }

    public void setFileEntityList(List<ImageAndVideoEntity.FileEntity> fileEntityList) {
        this.fileEntityList = fileEntityList;
    }
}
