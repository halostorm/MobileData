package com.ustc.wsn.mydataapp.detectorservice;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.ustc.wsn.mydataapp.Application.AppResourceApplication;
import com.ustc.wsn.mydataapp.bean.AcceleratorData;
import com.ustc.wsn.mydataapp.bean.GyroData;
import com.ustc.wsn.mydataapp.bean.MagnetData;
import com.ustc.wsn.mydataapp.bean.PhoneState;

/**
 * Created by halo on 2018/1/28.
 */

public class TrackSensorListener implements SensorEventListener {
    private static int LAST_STATE = PhoneState.UNKONW_STATE;
    private static int NOW_STATE = PhoneState.UNKONW_STATE;
    private final float ACC_STATIC_THRESHOLD = 0.1f;
    private final float GYRO_STATIC_THRESHOLD = 0.1f;
    private final String TAG = TrackSensorListener.this.toString();
    private TrackSensorListener mContext = TrackSensorListener.this;
    public final int windowSize = 40;//
    public int FRAME_TYPE;//0 - phone frame/ 1 - inertial frame

    //路径参数
    private float[][] laccSample = new float[windowSize][3];//线性加速度窗
    private float[][] gravitySample = new float[windowSize][3];//重力加速度窗
    private float[][] accSample = new float[windowSize][3];//加速度窗
    private float[][] gyroSample = new float[windowSize][3];//角速度窗
    private float[][] DcmSample = new float[windowSize][9];//Dcm窗
    private float[][] magSample = new float[windowSize][3];//磁场窗
    private float[][] velocitySample = new float[windowSize][3];//速度窗
    private float[][] positionSample = new float[windowSize][3];//位置窗
    private float[] deltT = new float[windowSize];//积分时间
    private long time;
    private long timeOld;

    //传感器参数
    private float[] lacc = new float[3]; //phone frame
    private float[] gravity = new float[3];
    private float[] acc = new float[3];
    private float[] gyro = new float[3];
    private float[] mag = new float[3];


    private float[] nlacc = new float[3];//inertial frame
    private float[] ngravity = new float[3];
    private float[] nacc = new float[3];
    private float[] ngyro = new float[3];
    private float[] nmag = new float[3];


    //姿态参数
    private volatile float[] DCM_android = new float[]{1.f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f};
    private volatile float[] DCM_static = new float[]{1.f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f};
    private float[] gravityMeanOld = new float[3];
    private float[] magMeanOld = new float[3];

    //滤波器参数
    private LPF_I accLPF;
    private LPF_I gyroLPF;
    private LPF_I magLPF;
    private MeanFilter accMF;
    private MeanFilter gyroMF;
    private MeanFilter magMF;

    //线程参数
    private boolean threadDisable_data_update = false;

    //状态参数
    private float[] gyroMeanOld = new float[3];
    private float[] gyroVarOld = new float[3];
    private float[] laccMeanOld = new float[3];
    private float[] laccVarOld = new float[3];

    public TrackSensorListener(AppResourceApplication resource) {
        // TODO Auto-generated constructor stub
        super();
        accLPF = new LPF_I();
        gyroLPF = new LPF_I();
        magLPF = new LPF_I();

        accMF = new MeanFilter();
        gyroMF = new MeanFilter();
        magMF = new MeanFilter();

        time = System.nanoTime();
        timeOld = System.nanoTime();

        //
        new Thread(new Runnable() {
            @Override
            public void run() {
                float[] dcm = DCM_static.clone();
                float[] dcmOld = dcm.clone();
                int i = 0;
                while (!threadDisable_data_update) {
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (i++ < windowSize) {
                        time = System.nanoTime();
                        float dt = (time - timeOld) / 1000000000f;
                        timeOld = time;
                        //Log.d(TAG,"deltT:"+String.valueOf(dt));

                        float[] W = gyro.clone();
                        float[] Matrix_W = new float[]{1f, -W[2] * dt, W[1] * dt,//
                                W[2] * dt, 1f, -W[0] * dt, //
                                -W[1] * dt, W[0] * dt, 1f};
                        dcm = DcmMultiply(dcmOld, Matrix_W);//获取DCM
                        dcmOld = dcm.clone();
                        nacc = phoneToEarth(dcm, acc);//得到一次理想加速度
                        nlacc = phoneToEarth(dcm, lacc);
                        ngyro = phoneToEarth(dcm, gyro);
                        nmag = phoneToEarth(dcm, mag);

                        addData(deltT, dt);
                        addData(laccSample, lacc);
                        addData(accSample, acc);
                        addData(gyroSample, gyro);
                        addData(magSample, mag);
                        addData(gravitySample, gravity);
                    } else {
                        dcmOld = DCM_static.clone();
                        i = 0;
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!threadDisable_data_update) {
                    try {
                        Thread.sleep(windowSize * 25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //状态参数更新
                    float[] gyroMean = getMean(gyroSample);
                    float[] gyroVar = getVar(gyroSample);
                    float[] laccMean = getMean(laccSample);
                    float[] laccVar = getVar(laccSample);
                    /*
                    Log.d(TAG,"gyroMean[0]:"+gyroMean[0]);
                    Log.d(TAG,"gyroMean[1]:"+gyroMean[1]);
                    Log.d(TAG,"gyroMean[2]:"+gyroMean[2]);
                    Log.d(TAG,"gyroVar[0]:"+gyroVar[0]);
                    Log.d(TAG,"gyroVar[1]:"+gyroVar[1]);
                    Log.d(TAG,"gyroVar[2]:"+gyroVar[2]);

                    Log.d(TAG,"laccMean[0]:"+laccMean[0]);
                    Log.d(TAG,"laccMean[1]:"+laccMean[1]);
                    Log.d(TAG,"laccMean[2]:"+laccMean[2]);
                    Log.d(TAG,"laccVar[0]:"+laccVar[0]);
                    Log.d(TAG,"laccVar[1]:"+laccVar[1]);
                    Log.d(TAG,"laccVar[2]:"+laccVar[2]);
                    */

                    if (laccMean[0] < ACC_STATIC_THRESHOLD && gyroMean[0] < GYRO_STATIC_THRESHOLD && laccVar[0] < ACC_STATIC_THRESHOLD && gyroVar[0] < GYRO_STATIC_THRESHOLD && laccMean[1] < ACC_STATIC_THRESHOLD && gyroMean[1] < GYRO_STATIC_THRESHOLD && laccVar[1] < ACC_STATIC_THRESHOLD && gyroVar[1] < GYRO_STATIC_THRESHOLD && laccMean[2] < ACC_STATIC_THRESHOLD && gyroMean[2] < GYRO_STATIC_THRESHOLD && laccVar[2] < ACC_STATIC_THRESHOLD && gyroVar[2] < GYRO_STATIC_THRESHOLD) {
                        NOW_STATE = PhoneState.ABSOLUTE_STATIC_STATE;
                    } else {
                        NOW_STATE = PhoneState.UNKONW_STATE;
                    }
                    //姿态参数更新
                    float[] gravityMean = getMean(gravitySample);
                    float[] magMean = getMean(magSample);

                    if (LAST_STATE == PhoneState.ABSOLUTE_STATIC_STATE && NOW_STATE == PhoneState.UNKONW_STATE) {
                        // 静止DCM
                        SensorManager.getRotationMatrix(DCM_static, null, gravityMeanOld, magMeanOld);
                        DcmSample[0] = DCM_static.clone();
                        //动态DCM及轨迹
                        for (int i = 1; i < windowSize; i++) { //when i = 0, velocitySample[i] =0; positionSample[i] =0;
                            float[] W = gyroSample[i].clone();
                            float[] Matrix_W = new float[]{1f, -W[2] * deltT[i], W[1] * deltT[i],//
                                    W[2] * deltT[i], 1f, -W[0] * deltT[i], //
                                    -W[1] * deltT[i], W[0] * deltT[i], 1f};

                            DcmSample[i] = DcmMultiply(DcmSample[i - 1], Matrix_W);//获取DCM

                            float[] accNow = phoneToEarth(DcmSample[i], accSample[i]);//得到一次理想加速度
                            //Log.d(TAG,"accNow[0]:"+String.valueOf(i)+":\t"+accNow[0]);
                            //Log.d(TAG,"accNow[1]:"+String.valueOf(i)+":\t"+accNow[1]);
                            //Log.d(TAG,"accNow[2]:"+String.valueOf(i)+":\t"+accNow[2]);
                            velocitySample[i][0] = velocitySample[i - 1][0] + accNow[0] * deltT[i];
                            velocitySample[i][1] = velocitySample[i - 1][1] + accNow[1] * deltT[i];
                            velocitySample[i][2] = velocitySample[i - 1][2] + accNow[2] * deltT[i];
                            //Log.d(TAG,"velocityNow:"+String.valueOf(i)+":\t"+velocitySample[i][0]);

                            positionSample[i][0] = positionSample[i - 1][0] + 0.5f * (velocitySample[i][0] + velocitySample[i - 1][0]) * deltT[i];
                            positionSample[i][1] = positionSample[i - 1][1] + 0.5f * (velocitySample[i][1] + velocitySample[i - 1][1]) * deltT[i];
                            positionSample[i][2] = positionSample[i - 1][2] + 0.5f * (velocitySample[i][2] + velocitySample[i - 1][2]) * deltT[i];

                            //Log.d(TAG,"position"+String.valueOf(i)+":\t"+positionSample[i][0]);
                        }
                    }
                    gravityMeanOld = gravityMean.clone();//记录姿态参数old
                    magMeanOld = magMean.clone();

                    gyroMeanOld = gyroMean.clone();//记录状态参数old
                    laccMeanOld = laccMean.clone();
                    gyroVarOld = gyroVar.clone();
                    laccVarOld = laccVar.clone();
                    LAST_STATE = NOW_STATE;
                }
            }
        }).start();

    }

    public float[] readLinearAccData(int TYPE) {
        if(TYPE==0) {
            return this.lacc;
        }else
            return this.nlacc;
    }

    public float[] readAccData(int TYPE) {
        if(TYPE==0) {
            return this.acc;
        }else
            return this.nacc;
    }

    public float[] readGyroData(int TYPE) {

        if(TYPE==0) {
            return this.gyro;
        }else
            return this.ngyro;
    }

    public float[] readMagData(int TYPE) {

        if(TYPE==0) {
            return this.mag;
        }else
            return this.nmag;
    }

    public float[][] getPosition() {

        return positionSample;
    }

    public void addData(float[][] sample, float[] values) {
        for (int i = windowSize - 1; i > 0; i--) {
            sample[i] = sample[i - 1].clone();
        }
        sample[0] = values.clone();
    }

    public void addData(float[] sample, float values) {
        for (int i = windowSize - 1; i > 0; i--) {
            sample[i] = sample[i - 1];
        }
        sample[0] = values;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        // storeData = new StoreData();
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                if (event.values != null) {
                    acc = event.values.clone();
                    gravity = accLPF.filter(event.values);
                    lacc[0] = event.values[0] - gravity[0];
                    lacc[1] = event.values[1] - gravity[1];
                    lacc[2] = event.values[2] - gravity[2];
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                if (event.values != null) {
                    gyro = event.values.clone();
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                if (event.values != null) {
                    mag = event.values.clone();
                }
                break;
        }
    }

    public float[] phoneToEarth(float[] DCM, float[] values) {
        float[] valuesEarth = new float[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                valuesEarth[i] += values[j] * DCM[3 * i + j];
            }
        }
        return valuesEarth;
    }

    public float[] DcmMultiply(float[] A, float[] B) {
        float[] values = new float[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                float temp = 0;
                for (int k = 0; k < 3; k++) {
                    temp += A[i * 3 + k] * B[3 * k + j];
                }
                values[i * 3 + j] = temp;
            }
        }
        return values;
    }

    public float getVar(float[] x) {
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

    public float[] getVar(float[][] x) {
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

    public float getMean(float[] x) {
        int m = x.length;
        float sum = 0;
        for (int i = 0; i < m; i++) {// 求和
            sum += x[i];
        }
        float dAve = sum / m;// 求平均值
        return dAve;
    }

    public float[] getMean(float[][] x) {
        int m = x.length;
        float[] sum = new float[3];
        for (int i = 0; i < m; i++) {// 求和
            sum[0] += x[i][0];
            sum[1] += x[i][1];
            sum[2] += x[i][2];
        }
        float dAve[] = new float[3];
        dAve[0] = sum[0] / m;// 求平均值
        dAve[1] = sum[1] / m;// 求平均值
        dAve[2] = sum[2] / m;// 求平均值
        return dAve;
    }

    public void eulerAnglesToQuaternion(float angle[], float q[]) {
        float cosRoll = (float) Math.cos(angle[2] * 0.5f);
        float sinRoll = (float) Math.sin(angle[2] * 0.5f);

        float cosPitch = (float) Math.cos(angle[1] * 0.5f);
        float sinPitch = (float) Math.sin(angle[1] * 0.5f);

        float cosHeading = (float) Math.cos(angle[0] * 0.5f);
        float sinHeading = (float) Math.sin(angle[0] * 0.5f);

        q[0] = cosRoll * cosPitch * cosHeading + sinRoll * sinPitch * sinHeading;
        q[1] = sinRoll * cosPitch * cosHeading - cosRoll * sinPitch * sinHeading;
        q[2] = cosRoll * sinPitch * cosHeading + sinRoll * cosPitch * sinHeading;
        q[3] = cosRoll * cosPitch * sinHeading - sinRoll * sinPitch * cosHeading;
    }

    public void quaternionToRotationMatrix(float q[], float rMat[]) {
        float q1q1 = (float) Math.sqrt(q[1]);
        float q2q2 = (float) Math.sqrt(q[2]);
        float q3q3 = (float) Math.sqrt(q[3]);

        float q0q1 = q[0] * q[1];
        float q0q2 = q[0] * q[2];
        float q0q3 = q[0] * q[3];
        float q1q2 = q[1] * q[2];
        float q1q3 = q[1] * q[3];
        float q2q3 = q[2] * q[3];

        rMat[0] = 1.0f - 2.0f * q2q2 - 2.0f * q3q3;
        rMat[1] = 2.0f * (q1q2 + q0q3);
        rMat[2] = 2.0f * (q1q3 - q0q2);

        rMat[3] = 2.0f * (q1q2 - q0q3);
        rMat[4] = 1.0f - 2.0f * q1q1 - 2.0f * q3q3;
        rMat[5] = 2.0f * (q2q3 + q0q1);

        rMat[6] = 2.0f * (q1q3 + q0q2);
        rMat[7] = 2.0f * (q2q3 - q0q1);
        rMat[8] = 1.0f - 2.0f * q1q1 - 2.0f * q2q2;
    }
}
