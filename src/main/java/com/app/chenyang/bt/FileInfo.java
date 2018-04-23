package com.app.chenyang.bt;

import java.io.Serializable;

/**
 * Created by chenyang on 2018/4/16.
 */

public class FileInfo implements Serializable{
    private static final long serialVersionUID = 10101L;

    private String name;

    private String path;

    private long length;

    private String md5;

    public FileInfo(String name, long length, String md5, String path) {
        this.name = name;
        this.length = length;
        this.md5 = md5;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "name='" + name + '\'' +
                ", length=" + length +
                ", md5='" + md5 + '\'' +
                '}';
    }
}
