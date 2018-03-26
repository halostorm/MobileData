package com.ustc.wsn.mydataapp.Listenter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.ustc.wsn.mydataapp.bean.Filter.BPF;
import com.ustc.wsn.mydataapp.bean.Filter.EKF;
import com.ustc.wsn.mydataapp.bean.Filter.FCF;
import com.ustc.wsn.mydataapp.bean.Filter.LPF_I;
import com.ustc.wsn.mydataapp.bean.Filter.LPF_II;
import com.ustc.wsn.mydataapp.bean.Filter.MeanFilter;
import com.ustc.wsn.mydataapp.bean.Filter.ekfParams;
import com.ustc.wsn.mydataapp.bean.Filter.ekfParamsHandle;
import com.ustc.wsn.mydataapp.bean.Log.myLog;
import com.ustc.wsn.mydataapp.bean.PhoneState;
import com.ustc.wsn.mydataapp.bean.math.myMath;
import com.ustc.wsn.mydataapp.bean.math.Quaternion;
import com.ustc.wsn.mydataapp.bean.outputFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by halo on 2018/1/28.
 */

public class TrackSensorListener implements SensorEventListener {
    private static int LAST_STATE = PhoneState.UNKONW_STATE;
    private static int NOW_STATE = PhoneState.UNKONW_STATE;
    private final float G = -9.806f;
    public final int windowSize = 25;//20*windowSize ms - 500ms
    public final int DurationWindow = 10;// 5s
    public final int sampleInterval = 20;//ms
    private final int stateParamsType = 1;
    private final String TAG = TrackSensorListener.this.toString();
    public int FRAME_TYPE;//0 - phone frame/ 1 - inertial frame
    private Context c = null;
    private float ACC_ABSOLUTE_STATIC_THRESHOLD;
    private float GYRO_ABSOLUTE_STATIC_THRESHOLD;
    private float ACC_STATIC_THRESHOLD;
    private float GYRO_STATIC_THRESHOLD;
    private float ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD;
    private float ACC_MEAN_STATIC_THRESHOLD;
    private float ACC_VAR_ABSOLUTE_STATIC_THRESHOLD;
    private float ACC_VAR_STATIC_THRESHOLD;
    private TrackSensorListener mContext = TrackSensorListener.this;
    //路径参数

    private volatile float[][] accSample = new float[windowSize][3];//加速度窗
    private volatile float[][] naccSample = new float[windowSize][3];//加速度窗

    private volatile float[][] laccSample = new float[windowSize][3];//线性加速度窗
    private volatile float[][] nlaccSample = new float[windowSize][3];//线性加速度窗

    private volatile float[][] gravitySample = new float[windowSize][3];//重力加速度窗

    private volatile float[][] gyroSample = new float[windowSize][3];//角速度窗
    private volatile float[][] magSample = new float[windowSize][3];//磁场窗
    private volatile float[] deltT = new float[windowSize];//积分时间

    private volatile float[][] gyroQueue = new float[DurationWindow * windowSize][3];//
    private volatile float[][] accQueue = new float[DurationWindow * windowSize][3];//
    //private volatile float[][] velocityQueue = new float[DurationWindow * windowSize][3];//
    private volatile float[][] positionQueue = new float[DurationWindow * windowSize][3];//位置队列
    private volatile int position_mark = DurationWindow * windowSize;
    private volatile float[] deltTQueue = new float[DurationWindow * windowSize];//积分步长
    private volatile long[] timeStamp = new long[DurationWindow * windowSize];//积分时间

    private long time;
    private long timeOld;

    //传感器参数
    private volatile float[] rawacc = new float[3];
    private volatile float[] acc = new float[3];
    private volatile float[] lacc = new float[3]; //phone frame
    private volatile float[] gravity = new float[3];
    private volatile float[] gyro = new float[3];
    private volatile float[] mag = new float[3];

    private volatile float[] nrawacc = new float[3];
    private volatile float[] nacc = new float[3];
    private volatile float[] nlacc = new float[3];//inertial frame
    private volatile float[] ngravity = new float[3];
    private volatile float[] ngyro = new float[3];
    private volatile float[] nmag = new float[3];

    //姿态参数
    private volatile float[] androidDCM = new float[]{1.f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f};
    private volatile float[] DCM_static = new float[]{1.f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f};
    private volatile float[] Euler = {0f,0f,0f};

    //滤波器参数
    private EKF ekf;
    private ekfParams ekfP;
    private ekfParamsHandle ekfPH;

    private volatile float[] accOri;
    private volatile float[] magOri;
    private volatile float[] gyroOri;
    private volatile float[][] accOriSample = new float[windowSize][3];
    private volatile float[][] magOriSample = new float[windowSize][3];

    private boolean accOriOriNew = false;
    private boolean gyroOriNew = false;
    private boolean magOriNew = false;

    private LPF_I accLPF;
    private LPF_I gyroLPF;
    private LPF_I magLPF;

    private MeanFilter accMF;
    private MeanFilter gyroMF;
    private MeanFilter magMF;

    //加速度校准参数
    private static float[] params;

    //线程参数
    private boolean threadDisable_data_update = false;

    private File path;

    public TrackSensorListener(boolean ifPath) {
        // TODO Auto-generated constructor stub
        super();
        updateThresHoldParams();
        path = outputFile.getPathFile();
        accLPF = new LPF_I();
        gyroLPF = new LPF_I();
        magLPF = new LPF_I();

        accMF = new MeanFilter();
        gyroMF = new MeanFilter();
        magMF = new MeanFilter();


        time = System.nanoTime();
        timeOld = System.nanoTime();

        ekfPH = new ekfParamsHandle();
        ekfP = new ekfParams();
        ekf = new EKF();
        //ekf.AttitudeEKF_initialize();
        //加速度校准参数提取
        getAccCalibrateParams();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                float[] DCM = {1,0,0, 0,1,0, 0,0,1};
                while (!threadDisable_data_update) {
                    try {
                        Thread.sleep(sampleInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //EKF
                    if (accOriOriNew && magOriNew && gyroOriNew) {
                        //myLog.log(TAG,"x_apo",ekf.x_apo);
                        //myLog.log(TAG,"P_apo",ekf.P_apo);
                        ekf.update_vect[0] = 1;
                        ekf.update_vect[1] = 1;
                        ekf.update_vect[2] = 1;
                        /*
                        float[] _accOri = accMF.filter(accOri);
                        float[] _gyroOri = gyroMF.filter(gyroOri);
                        float[] _magOri = magMF.filter(magOri);
                        */

                        float[] _accOri = accOri;
                        float[] _gyroOri = gyroOri;
                        float[] _magOri = magOri;
                        ekf.z_k[0] = _gyroOri[1];
                        ekf.z_k[1] = _gyroOri[0];
                        ekf.z_k[2] = -_gyroOri[2];

                        ekf.z_k[3] = _accOri[1];
                        ekf.z_k[4] = _accOri[0];
                        ekf.z_k[5] = -_accOri[2];

                        ekf.z_k[6] = _magOri[1] / 100.f;
                        ekf.z_k[7] = _magOri[0] / 100.f;
                        ekf.z_k[8] = -_magOri[2] / 100.f;

                        ekf.x_aposteriori_k[0] = ekf.z_k[0];
                        ekf.x_aposteriori_k[1] = ekf.z_k[1];
                        ekf.x_aposteriori_k[2] = ekf.z_k[2];
                        ekf.x_aposteriori_k[3] = 0.0f;
                        ekf.x_aposteriori_k[4] = 0.0f;
                        ekf.x_aposteriori_k[5] = 0.0f;
                        ekf.x_aposteriori_k[6] = ekf.z_k[3];
                        ekf.x_aposteriori_k[7] = ekf.z_k[4];
                        ekf.x_aposteriori_k[8] = ekf.z_k[5];
                        ekf.x_aposteriori_k[9] = ekf.z_k[6];
                        ekf.x_aposteriori_k[10] = ekf.z_k[7];
                        ekf.x_aposteriori_k[11] = ekf.z_k[8];
                        //Log.d(TAG, "calculateOrientation");
                        ekfP.parameters_update(ekfPH);
                        ekf.dt = (System.nanoTime() - ekf.time) / 1000000000.f;
                        ekf.AttitudeEKF(false, // approx_prediction
                                ekfP.use_moment_inertia, ekf.update_vect, ekf.dt, ekf.z_k, ekfP.q[0], // q_rotSpeed,
                                ekfP.q[1], // q_rotAcc
                                ekfP.q[2], // q_acc
                                ekfP.q[3], // q_mag
                                ekfP.r[0], // r_gyro
                                ekfP.r[1], // r_accel
                                ekfP.r[2], // r_mag
                                ekfP.moment_inertia_J, ekf.x_aposteriori_k, ekf.P_aposteriori_k, ekf.Rot_matrix, ekf.euler, ekf.debugOutput, ekf.euler_pre);
                        ekf.time = System.nanoTime();
                        Euler = ekf.euler.clone();
                        readEuler();
                        SensorManager.getRotationMatrix(androidDCM, null, accOri, magOri);
                        androidDCM = myMath.R_android2Ned(androidDCM);
                    }
                    //myLog.log(TAG,ekf.Rot_matrix,"RotMatrix",3);
                    //myLog.log(TAG,androidDCM,"androidDCM",3);

                    time = System.nanoTime();
                    float dt = (time - timeOld) / 1000000000f;
                    timeOld = time;
                    if((count++)%100 == 0) {
                        //DCM = ekf.Rot_matrix.clone();
                        DCM = androidDCM.clone();
                    }
                    float[] Matrix_W = new float[]{
                            1f, -gyro[2] * dt, gyro[1] * dt,//
                            gyro[2] * dt, 1f, -gyro[0] * dt, //
                            -gyro[1] * dt, gyro[0] * dt, 1f};

                    DCM = myMath.matrixMultiply(DCM, Matrix_W);//获取新DCM
                    //全部转入NED坐标系基准
                    nrawacc = myMath.coordinatesTransform( DCM, rawacc);//得到一次理想加速度
                    nacc = myMath.coordinatesTransform( DCM, acc);//得到一次理想加速度
                    nlacc = myMath.coordinatesTransform( DCM, lacc);
                    ngyro = myMath.coordinatesTransform( DCM, gyro);
                    nmag = myMath.coordinatesTransform( DCM, mag);

                    myMath.addData(deltT, dt);

                    myMath.addData(accSample, acc);
                    myMath.addData(naccSample, nacc);

                    myMath.addData(laccSample, lacc);
                    myMath.addData(nlaccSample, nlacc);

                    myMath.addData(gyroSample, gyro);

                    myMath.addData(magSample, mag);

                    myMath.addData(gravitySample, gravity);


                    if (accOriOriNew && magOriNew && gyroOriNew) {
                        myMath.addData(accOriSample, accOri);
                        myMath.addData(magOriSample, magOri);
                    }

                    myMath.addData(deltTQueue, dt);
                    myMath.addData(timeStamp, System.currentTimeMillis());
                    myMath.addData(accQueue, acc);
                    myMath.addData(gyroQueue, gyro);
                }
            }
        }).start();


        if (ifPath) {
            new Thread(new Runnable() {
                float[][] gyroSampleBegin = new float[windowSize][3];
                float[][] accSampleBegin = new float[windowSize][3];
                float[] deltBegin = new float[windowSize];
                long[] timeStampBegin = new long[windowSize];

                @Override
                public void run() {
                    while (!threadDisable_data_update) {
                        try {
                            Thread.sleep(windowSize * sampleInterval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        updateThresHoldParams();//更新状态阈值参数

                        //状态参数更新
                        if (stateParamsType == 0) {
                            //xyz参数
                            float[] gyroMean = myMath.getMean(gyroSample);
                            float[] gyroVar = myMath.getVar(gyroSample);
                            float[] laccMean = myMath.getMean(laccSample);
                            float[] laccVar = myMath.getVar(laccSample);

                            if (laccMean[0] < ACC_ABSOLUTE_STATIC_THRESHOLD && gyroMean[0] < GYRO_ABSOLUTE_STATIC_THRESHOLD && laccVar[0] < ACC_ABSOLUTE_STATIC_THRESHOLD && gyroVar[0] < GYRO_ABSOLUTE_STATIC_THRESHOLD && laccMean[1] < ACC_ABSOLUTE_STATIC_THRESHOLD && gyroMean[1] < GYRO_ABSOLUTE_STATIC_THRESHOLD && laccVar[1] < ACC_ABSOLUTE_STATIC_THRESHOLD && gyroVar[1] < GYRO_ABSOLUTE_STATIC_THRESHOLD && laccMean[2] < ACC_ABSOLUTE_STATIC_THRESHOLD && gyroMean[2] < GYRO_ABSOLUTE_STATIC_THRESHOLD && laccVar[2] < ACC_ABSOLUTE_STATIC_THRESHOLD && gyroVar[2] < GYRO_ABSOLUTE_STATIC_THRESHOLD) {
                                NOW_STATE = PhoneState.ABSOLUTE_STATIC_STATE;
                                //Log.d(TAG, "当前状态是:绝对静止");
                            } else if (laccMean[0] < ACC_STATIC_THRESHOLD && gyroMean[0] < GYRO_STATIC_THRESHOLD && laccVar[0] < ACC_STATIC_THRESHOLD && gyroVar[0] < GYRO_STATIC_THRESHOLD && laccMean[1] < ACC_STATIC_THRESHOLD && gyroMean[1] < GYRO_STATIC_THRESHOLD && laccVar[1] < ACC_STATIC_THRESHOLD && gyroVar[1] < GYRO_STATIC_THRESHOLD && laccMean[2] < ACC_STATIC_THRESHOLD && gyroMean[2] < GYRO_STATIC_THRESHOLD && laccVar[2] < ACC_STATIC_THRESHOLD && gyroVar[2] < GYRO_STATIC_THRESHOLD) {
                                NOW_STATE = PhoneState.USER_STATIC_STATE;
                                //Log.d(TAG, "当前状态是:相对静止");
                            } else {
                                NOW_STATE = PhoneState.UNKONW_STATE;
                                //Log.d(TAG, "当前状态是:其他");
                            }
                        } else if (stateParamsType == 1) {
                            //Var参数
                            //float[] gyroSum = new float[windowSize];
                            float[] nlaccSum = new float[windowSize];
                            for (int i = 0; i < windowSize; i++) {
                                //gyroSum[i] = gyroSample[i][0] * gyroSample[i][0] + gyroSample[i][1] * gyroSample[i][1] + gyroSample[i][2] * gyroSample[i][2];
                                nlaccSum[i] = nlaccSample[i][0] * nlaccSample[i][0] + nlaccSample[i][1] * nlaccSample[i][1];// + laccSample[i][2] * laccSample[i][2];
                            }
                            float laccSumMean = myMath.getMean(nlaccSum);
                            float laccSumVar = myMath.getVar(nlaccSum);

                            Log.d(TAG, "laccSumMean:" + laccSumMean);
                            Log.d(TAG, "laccSumVar:" + laccSumVar);
                            if (laccSumMean < ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD && laccSumVar < ACC_VAR_ABSOLUTE_STATIC_THRESHOLD) {//gyroSumMean < GYRO_ABSOLUTE_STATIC_THRESHOLD && laccSumMean < ACC_ABSOLUTE_STATIC_THRESHOLD && gyroSumVar < GYRO_ABSOLUTE_STATIC_THRESHOLD && laccSumVar < ACC_ABSOLUTE_STATIC_THRESHOLD) {
                                NOW_STATE = PhoneState.ABSOLUTE_STATIC_STATE;
                                //Log.d(TAG, "当前状态是:绝对静止");
                            } else if (laccSumMean < ACC_MEAN_STATIC_THRESHOLD && laccSumVar < ACC_VAR_STATIC_THRESHOLD) {//laccSumMean < ACC_STATIC_THRESHOLD && gyroSumVar < GYRO_STATIC_THRESHOLD && laccSumVar < ACC_STATIC_THRESHOLD) {gyroSumMean < GYRO_STATIC_THRESHOLD && laccSumMean < ACC_STATIC_THRESHOLD && gyroSumVar < GYRO_STATIC_THRESHOLD && laccSumVar < ACC_STATIC_THRESHOLD) {
                                NOW_STATE = PhoneState.USER_STATIC_STATE;
                                //Log.d(TAG, "当前状态是:相对静止");
                            } else {
                                NOW_STATE = PhoneState.UNKONW_STATE;
                                //Log.d(TAG, "当前状态是:其他");
                            }
                        }

                        //计算初始DCM
                        if (NOW_STATE == PhoneState.ABSOLUTE_STATIC_STATE || NOW_STATE == PhoneState.USER_STATIC_STATE) {
                            float[] accOriMean = myMath.getMean(accOriSample);
                            float[] magOriMean = myMath.getMean(magOriSample);
                            SensorManager.getRotationMatrix(DCM_static, null, accOriMean, magOriMean);
                            DCM_static = myMath.R_android2Ned(DCM_static);
                            //jing静止记录上一次积分变量
                            gyroSampleBegin = gyroSample.clone();
                            accSampleBegin = accSample.clone();
                            deltBegin = deltT.clone();
                        }

                        //若进入Path过程
                        if ((LAST_STATE == PhoneState.USER_STATIC_STATE || LAST_STATE == PhoneState.ABSOLUTE_STATIC_STATE) && NOW_STATE == PhoneState.UNKONW_STATE) {
                            // 获取静止初始静止DCM和位置，作为轨迹起点，velocityQueue[0] =0
                            float[][] positionQ = new float[DurationWindow * windowSize][3];
                            float[][] velocityQueue = new float[DurationWindow * windowSize][3];
                            float[][] DcmQueue = new float[DurationWindow * windowSize][3];

                            //获取path传感器数据
                            long[] time = new long[DurationWindow * windowSize];
                            float[][] gyroWindow = new float[DurationWindow * windowSize][3];//;
                            float[] deltTWindow = new float[DurationWindow * windowSize];//;
                            float[][] accWindow = new float[DurationWindow * windowSize][3];//;

                            int w_count = 0;
                            //添加上一次变量
                            myMath.addData(time, timeStampBegin);
                            myMath.addData(gyroWindow, gyroSampleBegin);
                            myMath.addData(deltTWindow, deltBegin);
                            myMath.addData(accWindow, accSampleBegin);
                            w_count++;
                            //添加本次变量
                            myMath.addData(time, timeStamp);
                            myMath.addData(gyroWindow, gyroSample);
                            myMath.addData(deltTWindow, deltT);
                            myMath.addData(accWindow, accSample);
                            w_count++;//w_count = 2;
                            //判断后续窗口状态，后续窗口最多DurationWindow-2个
                            for (int w = 1; w < DurationWindow - 1; w++) {
                                //暂停1个窗口查是否停止动作
                                try {
                                    Thread.sleep(windowSize * sampleInterval);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                float[] nlaccSum = new float[windowSize];
                                for (int i = 0; i < windowSize; i++) {
                                    //gyroSum[i] = gyroSample[i][0] * gyroSample[i][0] + gyroSample[i][1] * gyroSample[i][1] + gyroSample[i][2] * gyroSample[i][2];
                                    nlaccSum[i] = nlaccSample[i][0] * nlaccSample[i][0] + nlaccSample[i][1] * nlaccSample[i][1];// + laccSample[i][2] * laccSample[i][2];
                                }
                                float laccSumMean = myMath.getMean(nlaccSum);
                                float laccSumVar = myMath.getVar(nlaccSum);

                                // Log.d(TAG, "laccSumMean:" + laccSumMean);
                                // Log.d(TAG, "laccSumVar:" + laccSumVar);

                                if (laccSumMean < ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD && laccSumVar < ACC_VAR_ABSOLUTE_STATIC_THRESHOLD) {//gyroSumMean < GYRO_ABSOLUTE_STATIC_THRESHOLD && laccSumMean < ACC_ABSOLUTE_STATIC_THRESHOLD && gyroSumVar < GYRO_ABSOLUTE_STATIC_THRESHOLD && laccSumVar < ACC_ABSOLUTE_STATIC_THRESHOLD) {
                                    NOW_STATE = PhoneState.ABSOLUTE_STATIC_STATE;
                                    //Log.d(TAG, "当前状态是:绝对静止");
                                } else if (laccSumMean < ACC_MEAN_STATIC_THRESHOLD && laccSumVar < ACC_VAR_STATIC_THRESHOLD) {//laccSumMean < ACC_STATIC_THRESHOLD && gyroSumVar < GYRO_STATIC_THRESHOLD && laccSumVar < ACC_STATIC_THRESHOLD) {gyroSumMean < GYRO_STATIC_THRESHOLD && laccSumMean < ACC_STATIC_THRESHOLD && gyroSumVar < GYRO_STATIC_THRESHOLD && laccSumVar < ACC_STATIC_THRESHOLD) {
                                    NOW_STATE = PhoneState.USER_STATIC_STATE;
                                    //Log.d(TAG, "当前状态是:相对静止");
                                } else {
                                    NOW_STATE = PhoneState.UNKONW_STATE;
                                    //Log.d(TAG, "当前状态是:其他");
                                }
                                w_count = w + 2;
                                //添加当前窗口，添加窗口总数为 w+2 = 静+动+(w-1)*动+静；窗口总数最少为3：
                                if (NOW_STATE == PhoneState.ABSOLUTE_STATIC_STATE || NOW_STATE == PhoneState.USER_STATIC_STATE) {
                                    //若当前窗口已经停止，跳出，不再添加
                                    break;
                                }
                                myMath.addData(time, timeStamp);
                                myMath.addData(gyroWindow, gyroSample);
                                myMath.addData(deltTWindow, deltT);
                                myMath.addData(accWindow, accSample);
                            }

                            DcmQueue[(DurationWindow - w_count) * windowSize] = DCM_static.clone();
                            time = timeStamp.clone();
                            gyroWindow = gyroQueue.clone();//;
                            deltTWindow = deltTQueue.clone();//;
                            accWindow = accQueue.clone();//;

                            String pathOut = new String();

                            for (int i = ((DurationWindow - w_count) * windowSize + 1); i < (DurationWindow - 1) * windowSize; i++) { //when i = 0, velocitySample[i] =0; positionSample[i] =0;
                                float[] W = gyroWindow[i].clone();
                                //Log.d(TAG,"gyroWindow[0]:"+String.valueOf(i)+":\t"+W[0]);
                                //Log.d(TAG,"gyroWindow[1]:"+String.valueOf(i)+":\t"+W[1]);
                                //Log.d(TAG,"gyroWindow[2]:"+String.valueOf(i)+":\t"+W[2]);

                                float[] Matrix_W = new float[]{
                                        1f, -W[2] * deltTWindow[i], W[1] * deltTWindow[i],//
                                        W[2] * deltTWindow[i], 1f, -W[0] * deltTWindow[i], //
                                        -W[1] * deltTWindow[i], W[0] * deltTWindow[i], 1f};

                                DcmQueue[i] = myMath.matrixMultiply(DcmQueue[i - 1], Matrix_W);//获取新DCM

                                //Log.d(TAG, "euler[0]\t" + i +"\t"+ euler[0]/3.1415*180);
                                //Log.d(TAG, "euler[1]\t" + i +"\t"+ euler[1]/3.1415*180);
                                //Log.d(TAG, "euler[2]\t" + i +"\t"+ euler[2]/3.1415*180);
                                Log.d(TAG, "accWindow[i][0]:" + String.valueOf(i) + ":\t" + accWindow[i][0]);
                                Log.d(TAG, "accWindow[i][1]:" + String.valueOf(i) + ":\t" + accWindow[i][1]);
                                Log.d(TAG, "accWindow[i][2]:" + String.valueOf(i) + ":\t" + accWindow[i][2]);
                                pathOut += time[i] + "\t";
                                pathOut += accWindow[i][0] + "\t";
                                pathOut += accWindow[i][1] + "\t";
                                pathOut += accWindow[i][2] + "\t";
                                float[] accNow = myMath.coordinatesTransform(DcmQueue[i], accWindow[i]);//得到一次新惯性加速度
                                float[] accLast = myMath.coordinatesTransform(DcmQueue[i - 1], accWindow[i - 1]);//记录上一次惯性加速度
                                Log.d(TAG, "accNow[0]:" + String.valueOf(i) + ":\t" + accNow[0]);
                                Log.d(TAG, "accNow[1]:" + String.valueOf(i) + ":\t" + accNow[1]);
                                Log.d(TAG, "accNow[2]:" + String.valueOf(i) + ":\t" + (accNow[2] - G));
                                pathOut += accNow[0] + "\t";
                                pathOut += accNow[1] + "\t";
                                pathOut += accNow[2] + "\t";
                                velocityQueue[i][0] = velocityQueue[i - 1][0] + 0.5f * (accNow[0] + accLast[0]) * deltTWindow[i];
                                velocityQueue[i][1] = velocityQueue[i - 1][1] + 0.5f * (accNow[1] + accLast[1]) * deltTWindow[i];
                                velocityQueue[i][2] = velocityQueue[i - 1][2] + 0.5f * ((accNow[2] - G) + (accLast[2] - G)) * deltTWindow[i];
                                pathOut += velocityQueue[i][0] + "\t";
                                pathOut += velocityQueue[i][1] + "\t";
                                pathOut += velocityQueue[i][2] + "\t";
                                Log.d(TAG, "velocityQueue[0]:" + String.valueOf(i) + ":\t" + velocityQueue[i][0]);
                                Log.d(TAG, "velocityQueue[1]:" + String.valueOf(i) + ":\t" + velocityQueue[i][1]);
                                Log.d(TAG, "velocityQueue[2]:" + String.valueOf(i) + ":\t" + velocityQueue[i][2]);

                                positionQ[i][0] = positionQ[i - 1][0] + 0.5f * (velocityQueue[i][0] + velocityQueue[i - 1][0]) * deltTWindow[i];
                                positionQ[i][1] = positionQ[i - 1][1] + 0.5f * (velocityQueue[i][1] + velocityQueue[i - 1][1]) * deltTWindow[i];
                                positionQ[i][2] = positionQ[i - 1][2] + 0.5f * (velocityQueue[i][2] + velocityQueue[i - 1][2]) * deltTWindow[i];

                                pathOut += positionQ[i][0] + "\t";
                                pathOut += positionQ[i][1] + "\t";
                                pathOut += positionQ[i][2] + "\n";

                                Log.d(TAG, "position[0]" + String.valueOf(i) + ":\t" + positionQ[i][0]);
                                Log.d(TAG, "position[1]" + String.valueOf(i) + ":\t" + positionQ[i][1]);
                                Log.d(TAG, "position[2]" + String.valueOf(i) + ":\t" + positionQ[i][2]);
                            }
                            //
                            try {
                                FileWriter writer = new FileWriter(path);
                                Log.d(TAG, "path write");
                                writer.write(pathOut);
                                writer.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            positionQueue = positionQ.clone();
                            position_mark = (DurationWindow - w_count) * windowSize;
                        }

                        LAST_STATE = NOW_STATE;
                    }

                }
            }).start();
        }
    }

    private void getAccCalibrateParams() {
        PhoneState.initAccCalibrateParams();
        params = PhoneState.getCalibrateParams();
    }

    private void updateThresHoldParams() {
        if (stateParamsType == 0) {
            ACC_ABSOLUTE_STATIC_THRESHOLD = PhoneState.ACC_ABSOLUTE_STATIC_THRESHOLD;
            GYRO_ABSOLUTE_STATIC_THRESHOLD = PhoneState.GYRO_ABSOLUTE_STATIC_THRESHOLD;
            ACC_STATIC_THRESHOLD = PhoneState.ACC_STATIC_THRESHOLD;
            GYRO_STATIC_THRESHOLD = PhoneState.GYRO_STATIC_THRESHOLD;
        } else if (stateParamsType == 1) {
            ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD = PhoneState.ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD;
            ACC_MEAN_STATIC_THRESHOLD = PhoneState.ACC_MEAN_STATIC_THRESHOLD;
            ACC_VAR_ABSOLUTE_STATIC_THRESHOLD = PhoneState.ACC_VAR_ABSOLUTE_STATIC_THRESHOLD;
            ACC_VAR_STATIC_THRESHOLD = PhoneState.ACC_VAR_STATIC_THRESHOLD;
        }
    }

    public float[] readLinearAccData(int TYPE) {
        if (TYPE == 0) {
            return this.lacc;
        } else return this.nlacc;
    }

    public float[] readRawAccData(int TYPE) {
        if (TYPE == 0) {
            return this.rawacc;
        } else return this.nrawacc;
    }

    public float[] readAccData(int TYPE) {
        if (TYPE == 0) {
            return this.acc;
        } else return this.nacc;
    }

    public float[][] getAccSample() {
        return this.accSample;
    }

    public float[] readGyroData(int TYPE) {

        if (TYPE == 0) {
            return this.gyro;
        } else return this.ngyro;
    }

    public float[] readMagData(int TYPE) {

        if (TYPE == 0) {
            return this.mag;
        } else return this.nmag;
    }

    public float[] readEuler(){
        PhoneState.Euler = this.Euler;
        return this.Euler;
    }

    public float[][] getPosition() {

        return positionQueue;
    }

    public int getPosition_mark() {
        return position_mark;
    }

    public int getNowState() {
        return NOW_STATE;
    }


    public void closeSensorThread() {
        threadDisable_data_update = true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    private long timeCount = 0;
    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        // storeData = new StoreData();
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                if (event.values != null) {
                    //Log.d(TAG,System.currentTimeMillis()+"\t"+timeCount++);
                    float[] _rawacc = event.values.clone();
                    float[] _acc = doAccCalibrate(event.values);
                    float[] _gravity = accLPF.filter(event.values);
                    float[] _lacc = myMath.matrixSub(_acc, _gravity);

                    rawacc = myMath.android2Ned(_rawacc);
                    acc = myMath.android2Ned(_acc);
                    lacc = myMath.android2Ned(_lacc);
                    gravity = myMath.android2Ned(_gravity);

                    accOri = _acc;
                    accOriOriNew = true;
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                if (event.values != null) {
                    float[] _gyro = event.values.clone();
                    gyro = myMath.android2Ned(_gyro);

                    gyroOri = _gyro.clone();
                    gyroOriNew = true;

                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                if (event.values != null) {
                    float[] _mag = event.values.clone();
                    mag = myMath.android2Ned(_mag);

                    magOri = _mag.clone();
                    magOriNew = true;
                }
                break;
        }
    }

    private float[] doAccCalibrate(float[] rData) {
        float[] data = new float[3];
        data[0] = rData[0] - params[9];
        data[1] = rData[1] - params[10];
        data[2] = rData[2] - params[11];
        //减去shift
        float[] aData = new float[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                aData[i] += params[i * 3 + j] * data[j];
            }
        }
        //乘以比例系数
        return aData;
    }
}
