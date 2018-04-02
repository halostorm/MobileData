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
import com.ustc.wsn.mydataapp.bean.Filter.GDF;
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
    private TrackSensorListener mContext = TrackSensorListener.this;
    private final String TAG = TrackSensorListener.this.toString();
    private static int LAST_STATE = PhoneState.ABSOLUTE_STATIC_STATE;
    private static int NOW_STATE = PhoneState.ABSOLUTE_STATIC_STATE;
    private final float G = -9.806f;
    public final int windowSize = 25;//20*windowSize ms - 500ms
    public final int DurationWindow = 10;// 5s
    public final int sampleInterval = 20;//ms
    public volatile int sampleIntervalReal = 20;//ms

    public int FRAME_TYPE;//0 - phone frame/ 1 - inertial frame
    private float ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD;
    private float ACC_MEAN_STATIC_THRESHOLD;
    private float ACC_VAR_ABSOLUTE_STATIC_THRESHOLD;
    private float ACC_VAR_STATIC_THRESHOLD;

    private static final int Attitude_ANDROID = 1;
    private static final int Attitude_EKF = 2;
    private static final int Attitude_FCF = 3;
    private static final int Attitude_GDF = 4;
    private static int AttitudeMode = Attitude_EKF;
    //路径参数
    /*
    private volatile float[] timeSample = new float[windowSize];
    private volatile float[][] accSample = new float[windowSize][3];//加速度窗
    private volatile float[][] naccSample = new float[windowSize][3];//加速度窗
    private volatile float[][] laccSample = new float[windowSize][3];//线性加速度窗
    private volatile float[][] nlaccSample = new float[windowSize][3];//线性加速度窗
    private volatile float[][] gyroSample = new float[windowSize][3];//角速度窗
    private volatile float[][] magSample = new float[windowSize][3];//磁场窗
    private volatile float[][] eulerSample = new float[windowSize][3];//姿态窗
    */
    private volatile float[][] gyroQueue = new float[DurationWindow * windowSize][3];//
    private volatile float[][] magQueue = new float[DurationWindow * windowSize][3];//
    private volatile float[][] accQueue = new float[DurationWindow * windowSize][3];//
    private volatile float[][] naccQueue = new float[DurationWindow * windowSize][3];
    //private volatile float[][] RotQueue = new float[DurationWindow * windowSize][3];//
    private volatile float[][] positionQueue = new float[DurationWindow * windowSize][3];//位置队列
    private volatile float[] deltTQueue = new float[DurationWindow * windowSize];//积分步长
    private volatile long[] timeStampQueue = new long[DurationWindow * windowSize];//积分时间
    private volatile int position_mark = DurationWindow * windowSize;
    private volatile int start_mark = 0;

    //传感器参数
    private volatile float[] rawacc = new float[3];
    private volatile float[] acc = new float[3];
    private volatile float[] gyro = new float[3];
    private volatile float[] mag = new float[3];

    private volatile float[] nrawacc = new float[3];
    private volatile float[] nacc = new float[3];
    private volatile float[] ngyro = new float[3];
    private volatile float[] nmag = new float[3];

    //姿态参数
    private volatile float[] androidDCM = new float[]{1.f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f};
    private volatile float[] DCM_Static = new float[]{1.f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f};
    private volatile float[] DCM_Static_Last = new float[]{1.f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f};
    private volatile float[] Euler = {0f, 0f, 0f};

    //滤波器参数
    private EKF ekf;
    private ekfParams ekfP;
    private ekfParamsHandle ekfPH;

    private GDF gdf;

    private boolean accOriOriNew = false;
    private boolean gyroOriNew = false;
    private boolean magOriNew = false;

    private LPF_I accLPF;
    private LPF_I gyroLPF;
    private LPF_I magLPF;

    private MeanFilter accMF;
    private MeanFilter gyroMF;
    private MeanFilter magMF;

    private MeanFilter timeMF;

    //加速度校准参数
    private static float[] params;

    //线程参数
    private boolean threadDisable_data_update = false;

    private File path;

    public TrackSensorListener(boolean enablePath) {
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

        timeMF = new MeanFilter(windowSize);

        ekfPH = new ekfParamsHandle();
        ekfP = new ekfParams();
        ekf = new EKF();
        //ekf.AttitudeEKF_initialize();

        gdf = new GDF();

        //加速度校准参数提取
        getAccCalibrateParams();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                long time = System.nanoTime();
                long timeOld = System.nanoTime();
                float dt = sampleIntervalReal / 1000f;
                float[] DCM = {1, 0, 0, 0, 1, 0, 0, 0, 1};
                float[] DCMOLD = {1, 0, 0, 0, 1, 0, 0, 0, 1};
                while (!threadDisable_data_update) {
                    try {
                        Thread.sleep(sampleIntervalReal);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //EKF
                    if (accOriOriNew && magOriNew && gyroOriNew) {
                        //myLog.log(TAG,"x_apo",ekf.x_apo);
                        //myLog.log(TAG,"P_apo",ekf.P_apo);
                        float[] accOri = acc;
                        float[] gyroOri = gyro;
                        float[] magOri = mag;

                        if (AttitudeMode == Attitude_GDF) {
                            //用EKF初始化GDF
                            if (gdf.InitCount < 100) {
                                ekfAtt(accOri, gyroOri, magOri);
                                Euler = ekf.euler.clone();
                                DCM = ekf.Rot_matrix.clone();
                                gdf.InitCount++;
                            } else {
                                gdfAtt(accOri, gyroOri, magOri, dt, false);
                                Euler = gdf.Euler.clone();
                                DCM = gdf.Rot_Matrix.clone();
                            }
                        }
                        if (AttitudeMode == Attitude_EKF) {
                            ekfAtt(accOri, gyroOri, magOri);
                            Euler = ekf.euler.clone();
                            DCM = ekf.Rot_matrix.clone();
                        }
                        if (AttitudeMode == Attitude_ANDROID) {
                            if (count++ == 100) {
                                androidAtt(accOri, magOri);
                                DCM = androidDCM.clone();
                                Euler = myMath.Rot2Euler(DCM);
                                count = 0;
                            } else {
                                float[] Matrix_W = new float[]{1f, -gyro[2] * dt, gyro[1] * dt,//
                                        gyro[2] * dt, 1f, -gyro[0] * dt, //
                                        -gyro[1] * dt, gyro[0] * dt, 1f};
                                DCM = myMath.matrixMultiply(DCMOLD, Matrix_W, 3);//获取新DCM
                                Euler = myMath.Rot2Euler(DCM);
                            }
                            DCMOLD = DCM;
                        }
                    }
                    /*
                    myLog.log(TAG,ekf.Rot_matrix,"ekf RotMatrix",3);
                    myLog.log(TAG,androidDCM,"androidDCM",3);
                    myLog.log(TAG,gdf.Rot_Matrix,"gdf RotMatrix",3);
                    */

                    time = System.nanoTime();
                    dt = (time - timeOld) / 1000000000f;
                    sampleIntervalReal = sampleInterval - ((int) timeMF.filter(dt * 1000) - sampleInterval);//测量计算时间，对delay进行补偿
                    //Log.d(TAG, "DT Thread 1:\t" + dt);
                    //Log.d(TAG, "DsampleIntervalRealT Thread 1:\t" + sampleIntervalReal);
                    timeOld = time;
                    //全部转入NED坐标系基准
                    nrawacc = myMath.coordinatesTransform(DCM, rawacc);
                    nacc = myMath.coordinatesTransform(DCM, acc);
                    ngyro = myMath.coordinatesTransform(DCM, gyro);
                    nmag = myMath.coordinatesTransform(DCM, mag);
                    /*
                    myMath.addData(timeSample, dt);
                    myMath.addData(accSample, acc);
                    myMath.addData(naccSample, nacc);
                    myMath.addData(laccSample, lacc);
                    myMath.addData(nlaccSample, nlacc);
                    myMath.addData(gyroSample, gyro);
                    myMath.addData(magSample, mag);
                    myMath.addData(eulerSample,Euler);
                    */
                    if(NOW_STATE == PhoneState.UNKONW_STATE&& start_mark>0) {
                        start_mark--;
                    }
                    myMath.addData(deltTQueue, dt);
                    myMath.addData(timeStampQueue, System.currentTimeMillis());
                    myMath.addData(accQueue, acc);
                    myMath.addData(naccQueue, nacc);
                    myMath.addData(magQueue, mag);
                    myMath.addData(gyroQueue, gyro);
                    //myMath.addData(RotQueue, DCM);
                }
            }
        }).start();


        if (enablePath) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long time = System.nanoTime();
                    long timeOld = System.nanoTime();
                    float dt = sampleIntervalReal / 1000f;
                    int beginFlag = 0;
                    int stopFlag = 0;
                    int StartWindow = 0;
                    int StopWindow = 0;
                    while (!threadDisable_data_update) {
                        try {
                            Thread.sleep(windowSize * sampleInterval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        updateThresHoldParams();//更新状态阈值参数

                        //Var参数
                        float[] naccSum = new float[windowSize];
                        for (int i = (DurationWindow - 1) * windowSize; i < DurationWindow * windowSize; i++) {
                            naccSum[i - (DurationWindow - 1) * windowSize]
                                    = naccQueue[i][0] * naccQueue[i][0] + naccQueue[i][1] * naccQueue[i][1];
                        }
                        float accSumMean = myMath.getMean(naccSum);
                        float accSumVar = myMath.getVar(naccSum);
                        NOW_STATE = stateRecognizeUseAccel(accSumMean, accSumVar);
                        //Log.d(TAG, "laccSumMean:" + laccSumMean);
                        //Log.d(TAG, "laccSumVar:" + laccSumVar);

                        time = System.nanoTime();
                        dt = (time - timeOld)/1000000000f;
                        timeOld = time;
                        //myLog.log(TAG,"Thread 2 dt\t",dt+"");
                        //若进入Path过程：动静切换
                        if ((LAST_STATE == PhoneState.USER_STATIC_STATE || LAST_STATE == PhoneState.ABSOLUTE_STATIC_STATE)
                                && NOW_STATE == PhoneState.UNKONW_STATE) {
                            //开始位置
                            start_mark = (DurationWindow-2)*windowSize;

                            Log.d(TAG, "laccSumMean:" + accSumMean);
                            Log.d(TAG, "laccSumVar:" + accSumVar);
                            // 获取静止初始静止DCM和位置，作为轨迹起点，velocityQueue[0] =0
                            LAST_STATE = NOW_STATE;
                            float[][] positionQ = new float[DurationWindow * windowSize][3];
                            float[][] velocityQueue = new float[DurationWindow * windowSize][3];
                            float[][] DcmQueue = new float[DurationWindow * windowSize][3];

                            //等待全部5秒数据
                            while (start_mark > 0) {
                                try {
                                    Thread.sleep(2);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            time = System.nanoTime();
                            dt = (time - timeOld)/1000000000f;
                            timeOld = time;
                            myLog.log(TAG,"Thread 2 in path dt\t",dt+"");
                            //path数据填充完成
                            long[] timeWindow = timeStampQueue.clone();
                            float[][] gyroWindow = gyroQueue.clone();
                            float[] deltTWindow = deltTQueue.clone();
                            float[][] accWindow = accQueue.clone();
                            float[][] naccWindow = naccQueue.clone();
                            float[][] magWindow = magQueue.clone();

                            float[] accStartWindow = new float[2*windowSize];//开始窗口
                            float[] accStopWindow = new float[2*windowSize];//结束窗口


                            for(int i =0;i<2*windowSize;i++){//记录开始窗口
                                myMath.addData(accStartWindow,naccWindow[i][0]*naccWindow[i][0]
                                        +naccWindow[i][1]*naccWindow[i][1]);
                            }
                            //找出开始点
                            float[] accSlideWindow = new float[5];
                            for(int i = 0;i<2*windowSize-5;i++){
                                for(int j =0;j<5;j++) {
                                    accSlideWindow[j] = accStartWindow[i+j];
                                }
                                accSumMean = myMath.getMean(accSlideWindow);
                                accSumVar = myMath.getVar(accSlideWindow);

                                Log.d(TAG,"startSlideWindowID\t"+i);
                                Log.d(TAG, "laccSumMean:" + accSumMean);
                                Log.d(TAG, "laccSumVar:" + accSumVar);

                                NOW_STATE = stateRecognizeUseAccel(accSumMean,accSumVar);
                                if(NOW_STATE == PhoneState.UNKONW_STATE){
                                    beginFlag = i;
                                    break;
                                }
                                beginFlag = 0;
                            }

                            //找出结束窗口
                            for(int i = 2;i<DurationWindow;i++){
                                naccSum = new float[windowSize];
                                for(int j = 0;j<windowSize;j++){
                                    naccSum[j] = naccWindow[i*windowSize+j][0] * naccWindow[i*windowSize+j][0]
                                            + naccWindow[i*windowSize+j][1] * naccWindow[i*windowSize+j][1];

                                    myMath.addData(accStopWindow,naccSum[j]);//记录停止窗口

                                }

                                accSumMean = myMath.getMean(naccSum);
                                accSumVar = myMath.getVar(naccSum);

                                Log.d(TAG,"windowID\t"+i);
                                Log.d(TAG, "laccSumMean:" + accSumMean);
                                Log.d(TAG, "laccSumVar:" + accSumVar);

                                NOW_STATE = stateRecognizeUseAccel(accSumMean, accSumVar);

                                if ((NOW_STATE == PhoneState.USER_STATIC_STATE || NOW_STATE == PhoneState.ABSOLUTE_STATIC_STATE)
                                        && LAST_STATE == PhoneState.UNKONW_STATE) {
                                    StopWindow = i;
                                    break;
                                }
                                LAST_STATE = NOW_STATE;
                                //运动时间超过5s
                                StopWindow = i;
                            }
                            //找出结束点
                            accSlideWindow = new float[5];
                            for(int i = 0;i<2*windowSize-5;i++){
                                for(int j =0;j<5;j++) {
                                    accSlideWindow[j] = accStopWindow[i+j];
                                }
                                accSumMean = myMath.getMean(accSlideWindow);
                                accSumVar = myMath.getVar(accSlideWindow);
                                Log.d(TAG,"stopSlideWindowID\t"+i);
                                Log.d(TAG, "laccSumMean:" + accSumMean);
                                Log.d(TAG, "laccSumVar:" + accSumVar);

                                NOW_STATE = stateRecognizeUseAccel(accSumMean,accSumVar);
                                if(NOW_STATE == PhoneState.USER_STATIC_STATE||NOW_STATE == PhoneState.ABSOLUTE_STATIC_STATE){
                                    stopFlag = i;
                                    break;
                                }
                                stopFlag = i;
                            }

                            float[] _accOri = myMath.getMean(accWindow,0,windowSize);
                            float[] _magOri = myMath.getMean(magWindow,0,windowSize);
                            androidAtt(_accOri,_magOri);
                            DCM_Static = androidDCM.clone();
                            DcmQueue[beginFlag] = DCM_Static;

                            float[] accNow = myMath.coordinatesTransform(DcmQueue[beginFlag], accWindow[beginFlag]);//得到一次新惯性加速度
                            float[] accLast = accNow.clone();
                            Log.d(TAG, "accNow[0]:" + ":\t" + accNow[0]);
                            Log.d(TAG, "accNow[1]:" + ":\t" + accNow[1]);
                            Log.d(TAG, "accNow[2]:" + ":\t" + (accNow[2]));
                            //申明path文件
                            StringBuffer pathOut = new StringBuffer();
                            //开始计算Path
                            for (int i = beginFlag+1; i < (StopWindow-2) * windowSize + stopFlag; i++) {
                                //when i = 0, velocitySample[i] =0; positionSample[i] =0;
                                pathOut.append(timeWindow[i] + "\t");

                                float[] W = myMath.matrixDivide(myMath.matrixAdd(gyroWindow[i],gyroWindow[i-1]),2);
                                //float[] W = gyroWindow[i].clone();

                                pathOut.append(W[0]);
                                pathOut.append(W[1] + "\t");
                                pathOut.append(W[2] + "\t");

                                float[] Matrix_W = new float[]{1f, -W[2] * deltTWindow[i], W[1] * deltTWindow[i],//
                                        W[2] * deltTWindow[i], 1f, -W[0] * deltTWindow[i], //
                                        -W[1] * deltTWindow[i], W[0] * deltTWindow[i], 1f};

                                DcmQueue[i] = myMath.matrixMultiply(DcmQueue[i - 1], Matrix_W, 3);//获取新DCM
                                myLog.log(TAG, DcmQueue[i], "gyro DCM", 3);


                                Log.d(TAG, "accWindow[i][0]:" + String.valueOf(i) + ":\t" + accWindow[i][0]);
                                Log.d(TAG, "accWindow[i][1]:" + String.valueOf(i) + ":\t" + accWindow[i][1]);
                                Log.d(TAG, "accWindow[i][2]:" + String.valueOf(i) + ":\t" + accWindow[i][2]);


                                pathOut.append(accWindow[i][0] + "\t");
                                pathOut.append(accWindow[i][1] + "\t");
                                pathOut.append(accWindow[i][2] + "\t");

                                accNow = myMath.coordinatesTransform(DcmQueue[i], accWindow[i]);//得到一次新惯性加速度

                                Log.d(TAG, "accNow[0]:" + String.valueOf(i) + ":\t" + accNow[0]);
                                Log.d(TAG, "accNow[1]:" + String.valueOf(i) + ":\t" + accNow[1]);
                                Log.d(TAG, "accNow[2]:" + String.valueOf(i) + ":\t" + (accNow[2] - G));

                                Log.d(TAG, "accLast[0]:" + String.valueOf(i) + ":\t" + accLast[0]);
                                Log.d(TAG, "accLast[1]:" + String.valueOf(i) + ":\t" + accLast[1]);
                                Log.d(TAG, "accLast[2]:" + String.valueOf(i) + ":\t" + (accLast[2] - G));

                                pathOut.append((accNow[0] + accLast[0]) / 2 + "\t");
                                pathOut.append((accNow[1] + accLast[1]) / 2 + "\t");
                                pathOut.append((accNow[2] + accLast[2] - 2 * G) / 2 + "\t");

                                velocityQueue[i][0] = velocityQueue[i - 1][0] + 0.5f * (accNow[0] + accLast[0]) * deltTWindow[i];
                                velocityQueue[i][1] = velocityQueue[i - 1][1] + 0.5f * (accNow[1] + accLast[1]) * deltTWindow[i];
                                velocityQueue[i][2] = velocityQueue[i - 1][2] + 0.5f * ((accNow[2] - G) + (accLast[2] - G)) * deltTWindow[i];

                                accLast = accNow.clone();//记录上一次惯性加速度

                                pathOut.append(velocityQueue[i][0] + "\t");
                                pathOut.append(velocityQueue[i][1] + "\t");
                                pathOut.append(velocityQueue[i][2] + "\t");

                                Log.d(TAG, "velocityQueue[0]:" + String.valueOf(i) + ":\t" + velocityQueue[i][0]);
                                Log.d(TAG, "velocityQueue[1]:" + String.valueOf(i) + ":\t" + velocityQueue[i][1]);
                                Log.d(TAG, "velocityQueue[2]:" + String.valueOf(i) + ":\t" + velocityQueue[i][2]);

                                positionQ[i][0] = positionQ[i - 1][0] + 0.5f * (velocityQueue[i][0] + velocityQueue[i - 1][0]) * deltTWindow[i];
                                positionQ[i][1] = positionQ[i - 1][1] + 0.5f * (velocityQueue[i][1] + velocityQueue[i - 1][1]) * deltTWindow[i];
                                positionQ[i][2] = positionQ[i - 1][2] + 0.5f * (velocityQueue[i][2] + velocityQueue[i - 1][2]) * deltTWindow[i]; //- freeFallPosition;

                                pathOut.append(positionQ[i][0] + "\t");
                                pathOut.append(positionQ[i][1] + "\t");
                                pathOut.append(positionQ[i][2] + "\n");

                                Log.d(TAG, "position[0]" + String.valueOf(i) + ":\t" + positionQ[i][0]);
                                Log.d(TAG, "position[1]" + String.valueOf(i) + ":\t" + positionQ[i][1]);
                                Log.d(TAG, "position[2]" + String.valueOf(i) + ":\t" + positionQ[i][2]);
                            }
                            //
                            pathOut.append("\n");
                            try {
                                FileWriter writer = new FileWriter(path);
                                Log.d(TAG, "path write");
                                writer.write(pathOut.toString());
                                writer.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            positionQueue = positionQ.clone();
                            //position_mark = (DurationWindow - w_count) * windowSize;
                            //position_mark = (DurationWindow) * windowSize;
                        }//结束Path

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
        ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD = PhoneState.ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD;
        ACC_MEAN_STATIC_THRESHOLD = PhoneState.ACC_MEAN_STATIC_THRESHOLD;
        ACC_VAR_ABSOLUTE_STATIC_THRESHOLD = PhoneState.ACC_VAR_ABSOLUTE_STATIC_THRESHOLD;
        ACC_VAR_STATIC_THRESHOLD = PhoneState.ACC_VAR_STATIC_THRESHOLD;
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

    public float[] readEuler() {
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
                    float[] _acc = AccCalibrate(event.values);
                    rawacc = myMath.V_android2Ned(_rawacc);
                    acc = myMath.V_android2Ned(_acc);
                    accOriOriNew = true;
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                if (event.values != null) {
                    float[] _gyro = event.values.clone();
                    gyro = myMath.V_android2Ned(_gyro);
                    gyroOriNew = true;

                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                if (event.values != null) {
                    float[] _mag = event.values.clone();
                    //myLog.log(TAG,"mag raw:",_mag);
                    mag = myMath.V_android2Ned(_mag);
                    magOriNew = true;
                }
                break;
        }
    }

    private float[] AccCalibrate(float[] rData) {
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
        return aData;
    }

    private void ekfAtt(float[] accOri, float[] gyroOri, float[] magOri) {
        ekf.update_vect[0] = 1;
        ekf.update_vect[1] = 1;
        ekf.update_vect[2] = 1;

        ekf.z_k[0] = gyroOri[0];
        ekf.z_k[1] = gyroOri[1];
        ekf.z_k[2] = gyroOri[2];

        ekf.z_k[3] = accOri[0];
        ekf.z_k[4] = accOri[1];
        ekf.z_k[5] = accOri[2];

        ekf.z_k[6] = magOri[0] / 100.f;
        ekf.z_k[7] = magOri[1] / 100.f;
        ekf.z_k[8] = magOri[2] / 100.f;

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
                ekfP.use_moment_inertia, ekf.update_vect, ekf.dt, ekf.z_k, ekfP.q0, // q_rotSpeed,
                ekfP.q1, // q_rotAcc
                ekfP.q2, // q_acc
                ekfP.q3, // q_mag
                ekfP.r0, // r_gyro
                ekfP.r1, // r_accel
                ekfP.r2, // r_mag
                ekfP.moment_inertia_J, ekf.x_aposteriori, ekf.P_aposteriori, ekf.Rot_matrix, ekf.euler, ekf.debugOutput, ekf.euler_pre);
        ekf.time = System.nanoTime();
        ekf.q = myMath.Rot2Q(ekf.Rot_matrix);
    }

    private void gdfAtt(float[] accOri, float[] gyroOri, float[] magOri, float dt, boolean gyroIMU) {
        gdf.Filter(gyroOri[0], -gyroOri[1], -gyroOri[2], accOri[0], -accOri[1], -accOri[2], magOri[0], -magOri[1], -magOri[2], dt, gyroIMU);
    }

    private void androidAtt(float[] accOri, float[] magOri) {
        float[] _accOri = myMath.V_android2Ned(accOri);
        float[] _magOri = myMath.V_android2Ned(magOri);

        float[] aDCM = new float[9];
        SensorManager.getRotationMatrix(aDCM, null, _accOri, _magOri);
        androidDCM = myMath.R_android2Ned(aDCM);
    }

    private int stateRecognizeUseAccel(float laccSumMean, float laccSumVar) {
        if (laccSumMean < ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD && laccSumVar < ACC_VAR_ABSOLUTE_STATIC_THRESHOLD) {//gyroSumMean < GYRO_ABSOLUTE_STATIC_THRESHOLD && laccSumMean < ACC_ABSOLUTE_STATIC_THRESHOLD && gyroSumVar < GYRO_ABSOLUTE_STATIC_THRESHOLD && laccSumVar < ACC_ABSOLUTE_STATIC_THRESHOLD) {
            return PhoneState.ABSOLUTE_STATIC_STATE;
        } else if (laccSumMean < ACC_MEAN_STATIC_THRESHOLD && laccSumVar < ACC_VAR_STATIC_THRESHOLD) {//laccSumMean < ACC_STATIC_THRESHOLD && gyroSumVar < GYRO_STATIC_THRESHOLD && laccSumVar < ACC_STATIC_THRESHOLD) {gyroSumMean < GYRO_STATIC_THRESHOLD && laccSumMean < ACC_STATIC_THRESHOLD && gyroSumVar < GYRO_STATIC_THRESHOLD && laccSumVar < ACC_STATIC_THRESHOLD) {
            return PhoneState.USER_STATIC_STATE;
        } else {
            return PhoneState.UNKONW_STATE;
        }
    }

    private int stateRecognizeUseEuler(float[] eulerDelt) {
        if (Math.abs(eulerDelt[0]) + Math.abs(eulerDelt[1]) < PhoneState.EULER_ABSOLUTE_STATIC_THRESHOLD) {
            return PhoneState.ABSOLUTE_STATIC_STATE;
        } else if (Math.abs(eulerDelt[0]) + Math.abs(eulerDelt[1]) < PhoneState.EULER_STATIC_THRESHOLD) {
            return PhoneState.USER_STATIC_STATE;
        } else {
            return PhoneState.UNKONW_STATE;
        }
    }
}
