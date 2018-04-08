package com.ustc.wsn.mydataapp.bean.math;

/**
 * Created by halo on 2018/3/26.
 */

public class myMath {
    //统计库
    public static final float PI = 3.1416f;
    public static final float G = -9.807f;

    public static final int N = 10;

    public static float getMoulding(float[] value){
        float temp = 0;
        for(int i=0;i<value.length;i++){
            temp += value[i]*value[i];
        }
        return (float) Math.sqrt(temp);
    }

    public static float getVar(float[] x) {
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

    public static float[] getVar(float[][] x) {
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
        return dVar;
    }

    public static float getMean(float[] x) {
        int m = x.length;
        float sum = 0;
        for (int i = 0; i < m; i++) {// 求和
            sum += x[i];
        }
        float dAve = sum / m;// 求平均值
        return dAve;
    }

    public static float[] getMean(float[][] x) {
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
        return dAve;
    }

    public static float[] getMean(float[][] x,int start,int stop) {
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
        return dAve;
    }

    //矩阵库
    public static float[] coordinatesTransform(float[] DCM, float[] values) {
        float[] valuesEarth = new float[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                valuesEarth[i] += values[j] * DCM[3 * i + j];
            }
        }
        return valuesEarth;
    }

    public static float[] matrixMultiply(float[] A, float[] B, int N) {
        float[] values = new float[N*N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                float temp = 0;
                for (int k = 0; k < N; k++) {
                    temp += A[i * N + k] * B[N * k + j];
                }
                values[i * N + j] = temp;
            }
        }
        return values;
    }

    public static float[] matrixMultiply(float[] A, float scale) {
        float[] values = new float[A.length];
        for (int i = 0; i < A.length; i++) {
                values[i] = A[i]*scale;
            }
        return values;
    }

    public static float[] matrixDivide(float[] A, float scale) {
        float[] values = new float[A.length];
        for (int i = 0; i < A.length; i++) {
            values[i] = A[i]/scale;
        }
        return values;
    }

    public static float[] matrixAdd(float[] A, float[] B) {
        float[] values = new float[A.length];
        for (int i = 0; i < A.length; i++) {
                values[i] = A[i] + B[i];
        }
        return values;
    }

    public static float[] matrixSub(float[] A, float[] B) {
        float[] values = new float[A.length];
        for (int i = 0; i < A.length; i++) {
            values[i] = A[i] - B[i];
        }
        return values;
    }

    public static float[] matrixTranspose(float[] A) {
        float[] values = new float[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                values[i * 3 + j] = A[j * 3 + i];
            }
        }
        return values;
    }

//队列库
     /*
    //前入栈
    public static void addData(float[][] sample, float[] values) {
        for (int i = windowSize - 1; i > 0; i--) {
            sample[i] = sample[i - 1].clone();
        }
        sample[0] = values.clone();
    }

    public static void addData(float[] sample, float values) {
        for (int i = windowSize - 1; i > 0; i--) {
            sample[i] = sample[i - 1];
        }
        sample[0] = values;
    }
    */

    //后入栈
    public static void addData(float[][] sample, float[] values) {
        for (int i = 0; i < sample.length - 1; i++) {
            sample[i] = sample[i + 1].clone();
        }
        sample[sample.length - 1] = values.clone();
    }

    public static void addData(float[][] sample, float[][] values) {
        for (int k = 0; k < values.length; k++) {
            for (int i = 0; i < sample.length - 1; i++) {
                sample[i] = sample[i + 1].clone();
            }
            sample[sample.length - 1] = values[k].clone();
        }
    }

    public static void addData(float[] sample, float values) {
        for (int i = 0; i < sample.length - 1; i++) {
            sample[i] = sample[i + 1];
        }
        sample[sample.length - 1] = values;
    }

    public static void addData(long[] sample, long values) {
        for (int i = 0; i < sample.length - 1; i++) {
            sample[i] = sample[i + 1];
        }
        sample[sample.length - 1] = values;
    }

    public static void addData(long[] sample, long[] values) {
        for (int k = 0; k < values.length; k++) {
            for (int i = 0; i < sample.length - 1; i++) {
                sample[i] = sample[i + 1];
            }
            sample[sample.length - 1] = values[k];
        }
    }

    public static void addData(float[] sample, float[] values) {
        for (int k = 0; k < values.length; k++) {
            for (int i = 0; i < sample.length - 1; i++) {
                sample[i] = sample[i + 1];
            }
            sample[sample.length - 1] = values[k];
        }
    }


    public static void DeleteData2K(float[][] sample, int K) {
        for (int i = 0; i < sample.length-K; i++) {
            sample[i] = sample[K+i].clone();
        }
    }


    public static float[] V_android2Ned(float[] data) {
        float[] nData = new float[data.length];
        nData[0] = data[1];
        nData[1] = data[0];
        nData[2] = -data[2];
        return nData;
    }

    public static float[] R_android2Ned(float[] data) {
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
        return nData;
    }

    public static float[] Q2Rot(float q[]){

        float[] matrix = new float[9];
        float aSq = q[0] * q[0];
        float bSq = q[1] * q[1];
        float cSq = q[2] * q[2];
        float dSq = q[3] * q[3];
        matrix[0] = aSq + bSq - cSq - dSq;
        matrix[1] = -2.0f * (q[1] * q[2] - q[0] * q[3]);
        matrix[2] = -2.0f * (q[0] * q[2] + q[1] * q[3]);
        matrix[3] = -2.0f * (q[1] * q[2] + q[0] * q[3]);
        matrix[4] = aSq - bSq + cSq - dSq;
        matrix[5] = 2.0f * (q[2] * q[3] - q[0] * q[1]);
        matrix[6] = -2.0f * (q[1] * q[3] - q[0] * q[2]);
        matrix[7] = 2.0f * (q[0] * q[1] + q[2] * q[3]);
        matrix[8] = aSq - bSq - cSq + dSq;

        return matrix.clone();
    }

    public static float[] Rot2Euler(float[] Rot_matrix) {
        float[] eulerAngles = {0, 0, 0};
        eulerAngles[0] = (float) Math.atan2(Rot_matrix[7], Rot_matrix[8]);
        //Log.d((String) TAG, "eulerAngles0：" + eulerAngles[0]);
        eulerAngles[1] = -(float) Math.asin(Rot_matrix[6]);
        eulerAngles[2] = (float) Math.atan2(Rot_matrix[3], Rot_matrix[0]);
        return eulerAngles;
    }

    public static float[] Rot2Q(float[] dcm) {
        float[] _q = new float[4];
        _q[0] = (float) ((0.5) * (Math.sqrt((1) + dcm[0] + dcm[4] + dcm[8])));
        _q[1] = ((dcm[7] - dcm[5]) / ((4) * _q[0]));
        _q[2] = ((dcm[2] - dcm[6]) / ((4) * _q[0]));
        _q[3] = ((dcm[3] - dcm[1]) / ((4) * _q[0]));
        return _q.clone();
    }
}
