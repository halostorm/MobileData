package com.ustc.wsn.mydataapp.bean.Filter;

import com.ustc.wsn.mydataapp.bean.math.myMath;

/**
 * Created by halo on 2018/1/23.
 */

public class MeanFilter {
    private int SAMPLE_NUMBER = 10;
    private float[][] sampleList3D;
    private float[] mean3D;

    private float[] sampleList1D;
    private float mean1D;

    public MeanFilter(int sample_number) {
        SAMPLE_NUMBER = sample_number;
        sampleList3D = new float[SAMPLE_NUMBER][3];
        sampleList1D = new float[SAMPLE_NUMBER];
        mean3D = new float[3];
        mean1D = 0;
    }

    public MeanFilter() {
        sampleList3D = new float[SAMPLE_NUMBER][3];
        sampleList1D = new float[SAMPLE_NUMBER];
        mean3D = new float[3];
        mean1D = 0;
    }

    public float[] filter(float[] values) {
        for (int i = SAMPLE_NUMBER - 1; i > 0; i--) {
            sampleList3D[i] = sampleList3D[i - 1].clone();
        }
        sampleList3D[0] = values.clone();
        mean3D = myMath.getMean(sampleList3D);
        return this.mean3D;
    }

    public float filter(float values) {
        for (int i = SAMPLE_NUMBER - 1; i > 0; i--) {
            sampleList1D[i] = sampleList1D[i - 1];
        }
        sampleList1D[0] = values;
        mean1D = myMath.getMean(sampleList1D);
        return this.mean1D;
    }
}
