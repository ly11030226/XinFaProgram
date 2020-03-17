package com.szty.h5xinfa.model;

import java.io.Serializable;

/**
 * 更新实体类
 */
public class UpdateBean implements Serializable {
    private int id;
    private String filepath;
    private int length;
    private boolean del;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isDel() {
        return del;
    }

    public void setDel(boolean del) {
        this.del = del;
    }
}
