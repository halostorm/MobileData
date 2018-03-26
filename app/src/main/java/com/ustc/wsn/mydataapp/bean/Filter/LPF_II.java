package com.ustc.wsn.mydataapp.bean.Filter;

/**
 * Created by halo on 2018/2/2.
 */

public class LPF_II {//100-10hz
    private static final int NZEROS = 2;
    private static final int NPOLES = 2;
    private static final float GAIN = 1.482463775e+01f;
    private float[][] xv;
    private float[][] yv;

    public LPF_II() {
        xv = new float[NZEROS + 1][3];
        yv = new float[NPOLES + 1][3];
        yv = new float[NPOLES + 1][3];
    }

    public float[] filter(float[] values) {

        xv[0] = xv[1].clone();
        xv[1] = xv[2].clone();

        xv[2][0] = values[0] /GAIN;
        xv[2][1] = values[1] /GAIN;
        xv[2][2] = values[2] /GAIN;

        yv[0] = yv[1].clone();
        yv[1] = yv[2].clone();

        yv[2][0] = (xv[0][0] + xv[2][0]) + 2 * xv[1][0] + (-0.4128015981f  * yv[0][0]) + (1.1429805025f  * yv[1][0]);
        yv[2][1] = (xv[0][1] + xv[2][1]) + 2 * xv[1][1] + (-0.4128015981f  * yv[0][1]) + (1.1429805025f  * yv[1][1]);
        yv[2][2] = (xv[0][2] + xv[2][2]) + 2 * xv[1][2] + (-0.4128015981f  * yv[0][2]) + (1.1429805025f  * yv[1][2]);

        return yv[2];
    }
}
