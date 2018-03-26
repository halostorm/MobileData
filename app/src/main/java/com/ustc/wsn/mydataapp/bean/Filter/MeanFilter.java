package com.ustc.wsn.mydataapp.bean.Filter;

import com.ustc.wsn.mydataapp.bean.math.myMath;

/**
 * Created by halo on 2018/1/23.
 */

public class MeanFilter {
    private int SAMPLE_NUMBER = 10;
    private float[][] sampleList;
    private float[] mean;

    public MeanFilter(int sample_number) {
        SAMPLE_NUMBER = sample_number;
        sampleList = new float[SAMPLE_NUMBER][3];
        mean = new float[3];
    }

    public MeanFilter() {
        sampleList = new float[SAMPLE_NUMBER][3];
        mean = new float[3];
    }

    public float[] filter(float[] values) {
        for (int i = SAMPLE_NUMBER - 1; i > 0; i--) {
            sampleList[i] = sampleList[i - 1].clone();
        }
        sampleList[0] = values.clone();
        mean = myMath.getMean(sampleList);
        return this.mean;
    }
}
