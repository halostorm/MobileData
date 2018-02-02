package com.ustc.wsn.mydataapp.detectorservice;

/**
 * Created by halo on 2018/1/23.
 */

public class MeanFilter {
    private int SAMPLE_NUMBER = 1;
    private float[][] sampleList;
    private float[] mean;

    public MeanFilter(int sample_number) {
        SAMPLE_NUMBER = sample_number;
        sampleList = new float[SAMPLE_NUMBER][3];
        mean = new float[3];
    }

    public float[] filter(float[] values) {
        for (int i = SAMPLE_NUMBER - 1; i > 0; i--) {
            sampleList[i] = sampleList[i - 1].clone();
        }
        sampleList[0] = values.clone();
        calMean();
        return this.mean;
    }
    public void calMean() {
        for (int i = 0; i < SAMPLE_NUMBER; i++) {
            mean[0] += sampleList[i][0];
            mean[1] += sampleList[i][1];
            mean[2] += sampleList[i][2];
        }
        mean[0] /= SAMPLE_NUMBER;
        mean[1] /= SAMPLE_NUMBER;
        mean[2] /= SAMPLE_NUMBER;
    }
    public float calStdVar(float[] x) {
        int m = x.length;
        float sum = 0;
        for (int i = 0; i < m; i++) {// 求和
            sum += x[i];
        }
        float dAve = sum / m;// 求平均值
        float dVar = 0;
        for (int i = 0; i < m; i++) {// 求方差
            dVar += (x[i] - dAve) * (x[i] - dAve);
        }
        return (float) Math.sqrt(dVar / m);
    }
}
