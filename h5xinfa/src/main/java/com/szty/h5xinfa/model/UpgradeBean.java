package com.szty.h5xinfa.model;

import java.io.Serializable;

public class UpgradeBean implements Serializable {
    private int id;
    private String filepath;

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
}
