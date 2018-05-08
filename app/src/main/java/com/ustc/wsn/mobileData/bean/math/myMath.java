package com.ustc.wsn.mobileData.bean.math;

import android.util.Log;

import com.ustc.wsn.mobileData.bean.outputFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by halo on 2018/3/26.
 */

public class myMath {
    private static final String TAG = myMath.class.toString();
    //统计库
    public static final float PI = 3.141593f;
    public static final float G_INIT = -9.7932f;
    public static float G = G_INIT;

    public static final int N = 10;

    public static float DECLINATION = -5.2989f;

    public synchronized static void updateGravity(double latitude, double altitude) {
        latitude = latitude / 180 * Math.PI;
        G = -(float) (9.780327 * (1 + 0.005302 * Math.sin(latitude) * Math.sin(latitude) - 0.000005 * Math.sin(2 * latitude) * Math.sin(2 * latitude)) - 3.08769 * 10e-6 * (1 - 0.0014437 * Math.sin(2 * latitude) * Math.sin(2 * latitude)) * altitude);
        Log.d(TAG, "local Gravity:\t" + G);
    }

    public synchronized static void updateDeclination(float dec) {
        DECLINATION = dec;
    }

    public synchronized static void updateGeographicalParams() {
        File f = outputFile.getGeographicalParamsFile();
        String out = G + "\t" + DECLINATION;
        try {
            FileWriter writer = new FileWriter(f);
            writer.write(out);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void getGeographicalParams() {
        File f = outputFile.getGeographicalParamsFile();
        if (f.exists()) {
            try {
                BufferedReader bf = new BufferedReader(new FileReader(f));

                String values = new String();
                values = bf.readLine();
                if (values.length() != 0) {
                    String[] v = new String[10];
                    v = values.split("\t");
                    G = Float.parseFloat(v[0]);
                    DECLINATION = Float.parseFloat(v[1]);

                    Log.d(TAG, "Gravity & DECLINATION\t" + G + "\t" + DECLINATION);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized static int log2(int n){
        if(n <= 0) throw new IllegalArgumentException();
        return 31 - Integer.numberOfLeadingZeros(n);
    }

    public synchronized static float getMoulding(float[] value) {
        float temp = 0;
        for (int i = 0; i < value.length; i++) {
            temp += value[i] * value[i];
        }
        return (float) Math.sqrt(temp);
    }

    public synchronized static float getVar(float[] x) {
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
        return dVar / m;
    }

    public synchronized static float[] getVar(float[][] x) {
        int m = x.length;
        float[] sum = new float[3];
        for (int i = 0; i < m; i++) {// 求和
            sum[0] += x[i][0];
            sum[1] += x[i][1];
            sum[2] += x[i][2];
        }
        float[] dAve = new float[3];
        dAve[0] = sum[0] / m;// 求平均值
        dAve[1] = sum[1] / m;// 求平均值
        dAve[2] = sum[2] / m;// 求平均值
        float[] dVar = new float[3];
        for (int i = 0; i < m; i++) {// 求方差
            dVar[0] += (x[i][0] - dAve[0]) * (x[i][0] - dAve[0]);
            dVar[1] += (x[i][1] - dAve[1]) * (x[i][1] - dAve[1]);
            dVar[2] += (x[i][2] - dAve[2]) * (x[i][2] - dAve[2]);
        }
        dVar[0] = dVar[0] / m;
        dVar[1] = dVar[1] / m;
        dVar[2] = dVar[2] / m;
        return dVar.clone();
    }

    public synchronized static float getMean(float[] x) {
        int m = x.length;
        float sum = 0;
        for (int i = 0; i < m; i++) {// 求和
            sum += x[i];
        }
        float dAve = sum / m;// 求平均值
        return dAve;
    }

    public synchronized static float[] getMean(float[][] x) {
        int m = x.length;
        float[] sum = new float[x[0].length];
        for (int i = 0; i < m; i++) {// 求和
            for (int j = 0; j < x[0].length; j++) {
                sum[j] += x[i][j];
            }
        }
        float dAve[] = new float[x[0].length];
        for (int j = 0; j < x[0].length; j++) {
            dAve[j] = sum[j] / m;// 求平均值
        }
        return dAve.clone();
    }

    public synchronized static float[] getMean(float[][] x, int start, int stop) {
        int m = stop - start;
        float[] sum = new float[x[0].length];
        for (int i = start; i < stop; i++) {// 求和
            for (int j = 0; j < x[0].length; j++) {
                sum[j] += x[i][j];
            }
        }
        float dAve[] = new float[x[0].length];
        for (int j = 0; j < x[0].length; j++) {
            dAve[j] = sum[j] / m;// 求平均值
        }
        return dAve.clone();
    }

    public static float[] dataRandom(float[] rawData,int sampleSize) {
        int windowSize = rawData.length;
        float[] dataList = new float[sampleSize];
        int i = 0;
        boolean inArray;

        while (i < sampleSize) {
            int temp;
            inArray = false;
            temp = (int) (Math.random() * windowSize);

            for (int j = 0; j < i; j++) {
                if (dataList[i] == dataList[j]) {
                    inArray = true;
                }
            }

            if (inArray == false) {
                dataList[i] = rawData[temp];
                i++;
            }
        }

        return dataList;
    }


    public synchronized static float[] matrixMultiply(float[] A, float[] B, int N) {
        float[] values = new float[N * N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                float temp = 0;
                for (int k = 0; k < N; k++) {
                    temp += A[i * N + k] * B[N * k + j];
                }
                values[i * N + j] = temp;
            }
        }
        return values.clone();
    }

    public synchronized static float[] matrixMultiply(float[] A, float scale) {
        float[] values = new float[A.length];
        for (int i = 0; i < A.length; i++) {
            values[i] = A[i] * scale;
        }
        return values.clone();
    }

    public synchronized static float[] matrixDivide(float[] A, float scale) {
        float[] values = new float[A.length];
        for (int i = 0; i < A.length; i++) {
            values[i] = A[i] / scale;
        }
        return values.clone();
    }

    public synchronized static float[] matrixAdd(float[] A, float[] B) {
        float[] values = new float[A.length];
        for (int i = 0; i < A.length; i++) {
            values[i] = A[i] + B[i];
        }
        return values.clone();
    }

    public synchronized static float[] matrixSub(float[] A, float[] B) {
        float[] values = new float[A.length];
        for (int i = 0; i < A.length; i++) {
            values[i] = A[i] - B[i];
        }
        return values.clone();
    }

    public synchronized static float[] matrixTranspose(float[] A) {
        float[] values = new float[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                values[i * 3 + j] = A[j * 3 + i];
            }
        }
        return values.clone();
    }

//队列库
     /*
    //前入栈
    public synchronized static void addData(float[][] sample, float[] values) {
        for (int i = windowSize - 1; i > 0; i--) {
            sample[i] = sample[i - 1].clone();
        }
        sample[0] = values.clone();
    }

    public synchronized static void addData(float[] sample, float values) {
        for (int i = windowSize - 1; i > 0; i--) {
            sample[i] = sample[i - 1];
        }
        sample[0] = values;
    }
    */

    //后入栈
    public synchronized static void addData(float[][] sample, float[] values) {
        for (int i = 0; i < sample.length - 1; i++) {
            sample[i] = sample[i + 1].clone();
        }
        sample[sample.length - 1] = values.clone();
    }

    public synchronized static void addData(float[][] sample, float[][] values) {
        for (int k = 0; k < values.length; k++) {
            for (int i = 0; i < sample.length - 1; i++) {
                sample[i] = sample[i + 1].clone();
            }
            sample[sample.length - 1] = values[k].clone();
        }
    }

    public synchronized static void addData(float[] sample, float values) {
        for (int i = 0; i < sample.length - 1; i++) {
            sample[i] = sample[i + 1];
        }
        sample[sample.length - 1] = values;
    }

    public synchronized static void addData(long[] sample, long values) {
        for (int i = 0; i < sample.length - 1; i++) {
            sample[i] = sample[i + 1];
        }
        sample[sample.length - 1] = values;
    }

    public synchronized static void addData(long[] sample, long[] values) {
        for (int k = 0; k < values.length; k++) {
            for (int i = 0; i < sample.length - 1; i++) {
                sample[i] = sample[i + 1];
            }
            sample[sample.length - 1] = values[k];
        }
    }

    public synchronized static void addData(float[] sample, float[] values) {
        for (int k = 0; k < values.length; k++) {
            for (int i = 0; i < sample.length - 1; i++) {
                sample[i] = sample[i + 1];
            }
            sample[sample.length - 1] = values[k];
        }
    }

    public synchronized static float[] V_android2Ned(float[] data) {
        float[] nData = new float[data.length];
        nData[0] = data[1];
        nData[1] = data[0];
        nData[2] = -data[2];
        return nData.clone();
    }

    public synchronized static float[] R_android2Ned(float[] data) {
        float[] nData = new float[data.length];
        nData[0] = data[4];
        nData[1] = data[3];
        nData[2] = -data[5];
        nData[3] = data[1];
        nData[4] = data[0];
        nData[5] = -data[2];
        nData[6] = -data[7];
        nData[7] = -data[6];
        nData[8] = data[8];
        return nData.clone();
    }

    public synchronized static float[] Q2Rot(float q[]) {

        float[] matrix = new float[9];
        float aSq = q[0] * q[0];
        float bSq = q[1] * q[1];
        float cSq = q[2] * q[2];
        float dSq = q[3] * q[3];
        matrix[0] = aSq + bSq - cSq - dSq;
        matrix[1] = 2.0f * (q[1] * q[2] - q[0] * q[3]);
        matrix[2] = 2.0f * (q[0] * q[2] + q[1] * q[3]);
        matrix[3] = 2.0f * (q[1] * q[2] + q[0] * q[3]);
        matrix[4] = aSq - bSq + cSq - dSq;
        matrix[5] = 2.0f * (q[2] * q[3] - q[0] * q[1]);
        matrix[6] = 2.0f * (q[1] * q[3] - q[0] * q[2]);
        matrix[7] = 2.0f * (q[0] * q[1] + q[2] * q[3]);
        matrix[8] = aSq - bSq - cSq + dSq;

        return matrix.clone();
    }

    public synchronized static float[] Q2Euler(float[] q) {
        float[] eulerAngles = {0, 0, 0};
        eulerAngles[0] = (float) Math.atan2(2.0f * (q[0] * q[1] + q[2] * q[3]), 1.0f - 2.0f * (q[1] * q[1] + q[2] * q[2]));
        eulerAngles[1] = (float) Math.asin(2.0f * (q[0] * q[2] - q[3] * q[1]));
        eulerAngles[2] = (float) Math.atan2(2.0f * (q[0] * q[3] + q[1] * q[2]), 1.0f - 2.0f * (q[2] * q[2] + q[3] * q[3]));

        return eulerAngles.clone();
    }

    public synchronized static float[] Rot2Euler(float[] Rot_matrix) {
        float[] eulerAngles = {0, 0, 0};
        eulerAngles[0] = (float) Math.atan2(Rot_matrix[7], Rot_matrix[8]);
        //Log.d((String) TAG, "eulerAngles0：" + eulerAngles[0]);
        eulerAngles[1] = -(float) Math.asin(Rot_matrix[6]);
        eulerAngles[2] = (float) Math.atan2(Rot_matrix[3], Rot_matrix[0]);
        return eulerAngles.clone();
    }

    /*
    public synchronized static float[] Rot2Q(float[] dcm) {
        float[] _q = new float[4];
        _q[0] = (float) ((0.5) * (Math.sqrt(Math.abs(1 + dcm[0] + dcm[4] + dcm[8]))));
        _q[1] = ((dcm[7] - dcm[5]) / ((4) * _q[0]));
        _q[2] = ((dcm[2] - dcm[6]) / ((4) * _q[0]));
        _q[3] = ((dcm[3] - dcm[1]) / ((4) * _q[0]));
        return _q.clone();
    }
    */
    public synchronized static float[] Rot2Q(float[] dcm) {
        float[] _q = new float[4];
        float tr = dcm[0] + dcm[4] + dcm[8];
        if (tr > 0.0f) {
            float s = (float) Math.sqrt(tr + 1.0f);
            _q[0] = s * 0.5f;
            s = 0.5f / s;
            _q[1] = (dcm[7] - dcm[5]) * s;
            _q[2] = (dcm[2] - dcm[6]) * s;
            _q[3] = (dcm[3] - dcm[1]) * s;
        } else {
            /* Find maximum diagonal element in dcm
            * store index in dcm_i */
            int dcm_i = 0;
            for (int i = 1; i < 3; i++) {
                if (dcm[i * 3 + i] > dcm[dcm_i * 3 + dcm_i]) {
                    dcm_i = i;
                }
            }
            int dcm_j = (dcm_i + 1) % 3;
            int dcm_k = (dcm_i + 2) % 3;
            float s = (float) Math.sqrt((dcm[dcm_i * 3 + dcm_i] - dcm[dcm_j * 3 + dcm_j] - dcm[dcm_k * 3 + dcm_k]) + 1.0f);
            _q[dcm_i + 1] = s * 0.5f;
            s = 0.5f / s;
            _q[dcm_j + 1] = (dcm[dcm_i * 3 + dcm_j] + dcm[dcm_j * 3 + dcm_i]) * s;
            _q[dcm_k + 1] = (dcm[dcm_k * 3 + dcm_i] + dcm[dcm_i * 3 + dcm_k]) * s;
            _q[0] = (dcm[dcm_k * 3 + dcm_j] - dcm[dcm_j * 3 + dcm_k]) * s;
        }
        return _q.clone();
    }

    //矩阵坐标系变换
    public synchronized static float[] Rot_coordinatesTransform(float[] DCM, float[] values) {
        float[] valuesEarth = new float[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                valuesEarth[i] += values[j] * DCM[3 * i + j];
            }
        }
        return valuesEarth.clone();
    }

    public synchronized static float[] Q_coordinatesTransform(float[] Q, float[] values) {
        float[] valuesEarth = new float[3];
        float q0q0 = Q[0] * Q[0];
        float q1q1 = Q[1] * Q[1];
        float q2q2 = Q[2] * Q[2];
        float q3q3 = Q[3] * Q[3];

        valuesEarth[0] = values[0] * (q0q0 + q1q1 - q2q2 - q3q3) + values[1] * 2.0f * (Q[1] * Q[2] - Q[0] * Q[3]) + values[2] * 2.0f * (Q[0] * Q[2] + Q[1] * Q[3]);

        valuesEarth[1] = values[0] * 2.0f * (Q[1] * Q[2] + Q[0] * Q[3]) + values[1] * (q0q0 - q1q1 + q2q2 - q3q3) + values[2] * 2.0f * (Q[2] * Q[3] - Q[0] * Q[1]);

        valuesEarth[2] = values[0] * 2.0f * (Q[1] * Q[3] - Q[0] * Q[2]) + values[1] * 2.0f * (Q[0] * Q[1] + Q[2] * Q[3]) + values[2] * (q0q0 - q1q1 - q2q2 + q3q3);
        return valuesEarth.clone();
    }

}
