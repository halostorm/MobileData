package com.ustc.wsn.mobileData.bean;

/**
 * Created by halo on 2018/4/20.
 */

public class PathBasicData {
    public float[] acc;
    public float timestamp;

    public PathBasicData(float[] accV,float time) {
        acc = accV.clone();
        timestamp = time;
    }
}
