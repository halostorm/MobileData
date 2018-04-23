package com.ustc.wsn.mobileData.bean;

/**
 * Created by chong on 2017/8/28.
 */

public class FileBean {


    private String fileName;

    private boolean isUpload;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }


    public FileBean(String fileName) {
        this.fileName = fileName;
    }

    public FileBean() {
    }
}
