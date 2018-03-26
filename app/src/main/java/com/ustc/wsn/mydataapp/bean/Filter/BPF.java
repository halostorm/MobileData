package com.ustc.wsn.mydataapp.bean.Filter;

/**
 * Created by halo on 2018/2/2.
 */

public class BPF {
    private static final int NZEROS = 4;
    private static final int NPOLES = 4;
    private static final float GAIN = 2.728347287e+01f;

    private float[][] xv;
    private float[][] yv;

    public  BPF() {
        xv = new float[NZEROS + 1][3];
        yv = new float[NPOLES + 1][3];
    }

    public float[] filter(float[] values) {
        xv[0] = xv[1].clone();
        xv[1] = xv[2].clone();
        xv[2] = xv[3].clone();
        xv[3] = xv[4].clone();

        xv[4][0] = values[0] / GAIN;
        xv[4][1] = values[1] / GAIN;
        xv[4][2] = values[2] / GAIN;

        yv[0] = yv[1].clone();
        yv[1] = yv[2].clone();
        yv[2] = yv[3].clone();
        yv[3] = yv[4].clone();

        yv[4][0] = (xv[0][0] + xv[4][0]) - 2 * xv[2][0] + (-0.5371946248f * yv[0][0]) + (2.3183598952f * yv[1][0]) + (-3.9803720379f * yv[2][0]) + (3.1888066187f * yv[3][0]);
        yv[4][1] = (xv[0][1] + xv[4][1]) - 2 * xv[2][1] + (-0.5371946248f * yv[0][1]) + (2.3183598952f * yv[1][1]) + (-3.9803720379f * yv[2][1]) + (3.1888066187f * yv[3][1]);
        yv[4][2] = (xv[0][2] + xv[4][2]) - 2 * xv[2][2] + (-0.5371946248f * yv[0][2]) + (2.3183598952f * yv[1][2]) + (-3.9803720379f * yv[2][2]) + (3.1888066187f * yv[3][2]);

        return yv[4];
    }
}
