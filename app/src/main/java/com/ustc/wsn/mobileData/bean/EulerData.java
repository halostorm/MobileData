package com.ustc.wsn.mobileData.bean;

/**
 * Created by halo on 2019/1/4.
 */

public class EulerData {

    private float x;
    private float y;
    private float z;
    //private long time;

    public EulerData(float[] values) {
        x = values[0];
        y = values[1];
        z = values[2];
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return x + "\t" + y + "\t" + z ;
    }

}
