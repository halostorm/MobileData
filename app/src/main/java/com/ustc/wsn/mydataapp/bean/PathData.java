package com.ustc.wsn.mydataapp.bean;

/**
 * Created by halo on 2018/4/5.
 */

public class PathData {
    public float[] acc;
    public float[] gyro;
    public float timestamp;

    public PathData(float[] accV,float[]gyroV,float time) {
        acc = accV.clone();
        gyro = gyroV.clone();
        timestamp = time;
    }
}
