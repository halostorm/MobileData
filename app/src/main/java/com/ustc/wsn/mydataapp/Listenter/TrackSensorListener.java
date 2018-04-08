package com.ustc.wsn.mydataapp.Listenter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.ustc.wsn.mydataapp.bean.Filter.EKF;
import com.ustc.wsn.mydataapp.bean.Filter.FCF;
import com.ustc.wsn.mydataapp.bean.Filter.GDF;
import com.ustc.wsn.mydataapp.bean.Filter.LPF_I;
import com.ustc.wsn.mydataapp.bean.Filter.MeanFilter;
import com.ustc.wsn.mydataapp.bean.Filter.ekfParams;
import com.ustc.wsn.mydataapp.bean.Filter.ekfParamsHandle;
import com.ustc.wsn.mydataapp.bean.Log.myLog;
import com.ustc.wsn.mydataapp.bean.PathData;
import com.ustc.wsn.mydataapp.bean.PhoneState;
import com.ustc.wsn.mydataapp.bean.math.PathIntegration;
import com.ustc.wsn.mydataapp.bean.math.myMath;

import java.util.ArrayList;

/**
 * Created by halo on 2018/1/28.
 */

public class TrackSensorListener implements SensorEventListener {
    private final String TAG = TrackSensorListener.this.toString();

    //数据窗口参数
    private final float G = -9.806f;
    public final int windowSize = 25;//20*windowSize ms - 500ms
    public final int DurationWindow = 10;// 5s
    public final int sampleInterval = 20;//ms
    public volatile int sampleIntervalReal = 20;//ms

    //姿态滤波器选择
    private static int AttitudeMode = PhoneState.Attitude_EKF;

    //状态参数
    private int Window_STATE = PhoneState.ABSOLUTE_STATIC_STATE;
    private volatile int GLOBAL_NOW_STATE = PhoneState.ABSOLUTE_STATIC_STATE;

    private float[] stateValues = {0, 0};

    private float ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD;
    private float ACC_MEAN_STATIC_THRESHOLD;
    private float ACC_VAR_ABSOLUTE_STATIC_THRESHOLD;
    private float ACC_VAR_STATIC_THRESHOLD;

    //路径参数
    public boolean ifNewPath = false;

    private StringBuffer positionBuffer;
    private StringBuffer InterpositionBuffer;

    private volatile float[][] gyroQueue = new float[DurationWindow * windowSize][3];//
    private volatile float[][] magQueue = new float[DurationWindow * windowSize][3];//
    private volatile float[][] accQueue = new float[DurationWindow * windowSize][3];//
    private volatile float[][] naccQueue = new float[DurationWindow * windowSize][3];
    private volatile float[][] positionQueue = new float[DurationWindow * windowSize][3];//位置队列
    private volatile float[] deltTQueue = new float[DurationWindow * windowSize];//积分步长
    private volatile long[] timeStampQueue = new long[DurationWindow * windowSize];//积分时间
    private volatile int position_mark = DurationWindow * windowSize;
    private volatile int RemainingDataSize = 0;

    //传感器参数

    private float AccRange;
    private float GyroRange;
    private float MagRange;
    private float RangeK = 0.8f;

    private boolean AccOriOriNew = false;
    private boolean GyroOriNew = false;
    private boolean MagOriNew = false;

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
    private volatile float[] Euler = {0f, 0f, 0f};

    //姿态滤波器
    private EKF ekf;
    private ekfParams ekfP;
    private ekfParamsHandle ekfPH;

    private GDF gdf;

    private FCF fcf;

    //简单滤波器
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

    //
    public TrackSensorListener(float accMaxRange, float gyroMaxRange, float magMaxRange, boolean enablePath) {
        // TODO Auto-generated constructor stub
        super();
        //传感器量程
        AccRange = accMaxRange;
        GyroRange = gyroMaxRange;
        MagRange = magMaxRange;

        updateStateThreshold();

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

        fcf = new FCF();

        //加速度校准参数提取
        getAccCalibrateParams();

        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                long time = 0;
                long timeOld = System.nanoTime();
                float dt = sampleIntervalReal / 1000f;
                float[] DCM = {1, 0, 0, 0, 1, 0, 0, 0, 1};
                float[] DCM_LAST = {1, 0, 0, 0, 1, 0, 0, 0, 1};
                while (!threadDisable_data_update) {
                    try {
                        Thread.sleep(sampleIntervalReal);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    updateStateThreshold();
                    //EKF
                    if (AccOriOriNew && MagOriNew && GyroOriNew) {
                        //myLog.log(TAG,"x_apo",ekf.x_apo);
                        //myLog.log(TAG,"P_apo",ekf.P_apo);
                        float[] accOri = acc;
                        float[] gyroOri = gyro;
                        float[] magOri = mag;

                        if (AttitudeMode == PhoneState.Attitude_GDF) {
                            //用EKF初始化GDF
                            if (gdf.InitCount < 100) {
                                ekfAtt(accOri, gyroOri, magOri, dt);
                                Euler = ekf.euler.clone();
                                DCM = ekf.Rot_matrix.clone();
                                gdf.InitCount++;
                                gdf.q = ekf.q.clone();
                            } else {
                                gdfAtt(accOri, gyroOri, magOri, dt, false);
                                Euler = gdf.Euler.clone();
                                DCM = gdf.Rot_Matrix.clone();
                            }
                        }
                        if (AttitudeMode == PhoneState.Attitude_EKF) {
                            ekfAtt(accOri, gyroOri, magOri, dt);
                            Euler = ekf.euler.clone();
                            DCM = ekf.Rot_matrix.clone();
                            //myLog.log(TAG, "ekf q:", ekf.q);
                        }
                        if (AttitudeMode == PhoneState.Attitude_FCF) {
                            fcfAtt(accOri, gyroOri, magOri, dt);
                            Euler = fcf.euler.clone();
                            DCM = fcf.Rot_matrix.clone();
                            //myLog.log(TAG, "fcf q:", fcf.q);
                        }

                        if (AttitudeMode == PhoneState.Attitude_ANDROID) {
                            androidAtt(accOri, magOri);
                            DCM = androidDCM.clone();
                            Euler = myMath.Rot2Euler(DCM);
                        }
                        if (AttitudeMode == PhoneState.Attitude_GYRO) {
                            if (count++ == DurationWindow * windowSize) {
                                androidAtt(accOri, magOri);
                                DCM = androidDCM.clone();
                                Euler = myMath.Rot2Euler(DCM);
                                count = 0;
                            } else {
                                float[] Matrix_W = new float[]{1f, -gyro[2] * dt, gyro[1] * dt,//
                                        gyro[2] * dt, 1f, -gyro[0] * dt, //
                                        -gyro[1] * dt, gyro[0] * dt, 1f};
                                DCM = myMath.matrixMultiply(DCM_LAST, Matrix_W, 3);//获取新DCM
                                Euler = myMath.Rot2Euler(DCM);
                            }
                            DCM_LAST = DCM;
                        }
                    }

                    time = System.nanoTime();
                    dt = (time - timeOld) / 1000000000f;
                    sampleIntervalReal = sampleInterval - ((int) timeMF.filter(dt * 1000) - sampleInterval);
                    //测量计算时间，对delay进行补偿

                    timeOld = time;
                    //全部转入NED坐标系基准
                    nrawacc = myMath.coordinatesTransform(DCM, rawacc);
                    nacc = myMath.coordinatesTransform(DCM, acc);
                    ngyro = myMath.coordinatesTransform(DCM, gyro);
                    nmag = myMath.coordinatesTransform(DCM, mag);

                    if (GLOBAL_NOW_STATE == PhoneState.UNKONW_STATE && RemainingDataSize > 0) {
                        RemainingDataSize--;
                    }

                    myMath.addData(deltTQueue, dt);
                    myMath.addData(timeStampQueue, System.currentTimeMillis());
                    myMath.addData(accQueue, acc);
                    myMath.addData(naccQueue, nacc);
                    myMath.addData(magQueue, mag);
                    myMath.addData(gyroQueue, gyro);

                    float[] naccSum = new float[windowSize];
                    for (int i = (DurationWindow - 1) * windowSize; i < DurationWindow * windowSize; i++) {
                        naccSum[i - (DurationWindow - 1) * windowSize] = naccQueue[i][0] * naccQueue[i][0] + naccQueue[i][1] * naccQueue[i][1];
                    }
                    stateValues[0] = myMath.getMean(naccSum);
                    stateValues[1] = myMath.getVar(naccSum);

                    Window_STATE = stateRecognizeUseAccel(stateValues[0], stateValues[1]);
                }
            }
        }).start();

        if (enablePath) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int NOW_STATE = PhoneState.ABSOLUTE_STATIC_STATE;
                    int LAST_STATE = PhoneState.ABSOLUTE_STATIC_STATE;
                    long time = 0;
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
                        //更新状态阈值参数
                        updateStateThreshold();

                        //对最新数据窗口判断状态（数据队列最后一个窗口）
                        float[] naccSum = new float[windowSize];
                        for (int i = (DurationWindow - 1) * windowSize; i < DurationWindow * windowSize; i++) {
                            naccSum[i - (DurationWindow - 1) * windowSize] = naccQueue[i][0] * naccQueue[i][0] + naccQueue[i][1] * naccQueue[i][1];
                        }
                        float accSumMean = myMath.getMean(naccSum);
                        float accSumVar = myMath.getVar(naccSum);
                        NOW_STATE = stateRecognizeUseAccel(accSumMean, accSumVar);
                        //进入Path过程（动静切换）
                        if ((LAST_STATE == PhoneState.USER_STATIC_STATE || LAST_STATE == PhoneState.ABSOLUTE_STATIC_STATE) && NOW_STATE == PhoneState.UNKONW_STATE) {

                            Log.d(TAG, "laccSumMean:" + accSumMean);
                            Log.d(TAG, "laccSumVar:" + accSumVar);

                            LAST_STATE = NOW_STATE;
                            //定义path参数缓存
                            float[][] positionQ = new float[DurationWindow * windowSize][3];
                            float[][] velocityQueue = new float[DurationWindow * windowSize][3];
                            float[][] DcmQueue = new float[DurationWindow * windowSize][3];
                            GLOBAL_NOW_STATE = NOW_STATE;
                            RemainingDataSize = (DurationWindow - 2) * windowSize;
                            //等待全部5秒数据到达
                            while (RemainingDataSize > 0) {
                                try {
                                    Thread.sleep(2);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            //path数据填充完成
                            long[] timeWindow = timeStampQueue.clone();
                            float[][] gyroWindow = gyroQueue.clone();
                            float[] deltTWindow = deltTQueue.clone();
                            float[][] accWindow = accQueue.clone();
                            float[][] naccWindow = naccQueue.clone();
                            float[][] magWindow = magQueue.clone();

                            //申明首末窗口，大小为 2*windowSize
                            float[] accStartWindow = new float[2 * windowSize];//开始窗口
                            float[] accStopWindow = new float[2 * windowSize];//结束窗口


                            for (int i = 0; i < 2 * windowSize; i++) {//提取开始窗口数据
                                myMath.addData(accStartWindow, naccWindow[i][0] * naccWindow[i][0] + naccWindow[i][1] * naccWindow[i][1]);
                            }

                            //在开始窗口找出开始点
                            float[] accSlideWindow = new float[5];
                            for (int i = 0; i < 2 * windowSize - 5; i++) {
                                for (int j = 0; j < 5; j++) {
                                    accSlideWindow[j] = accStartWindow[i + j];
                                }
                                accSumMean = myMath.getMean(accSlideWindow);
                                accSumVar = myMath.getVar(accSlideWindow);

                                Log.d(TAG, "startSlideWindowID\t" + i);
                                Log.d(TAG, "laccSumMean:" + accSumMean);
                                Log.d(TAG, "laccSumVar:" + accSumVar);

                                NOW_STATE = stateRecognizeUseAccel(accSumMean, accSumVar);
                                if (NOW_STATE == PhoneState.UNKONW_STATE) {
                                    beginFlag = i;
                                    break;
                                }
                                beginFlag = 0;
                            }

                            //找出结束窗口，并提取结束窗口
                            for (int i = 2; i < DurationWindow; i++) {
                                naccSum = new float[windowSize];
                                for (int j = 0; j < windowSize; j++) {
                                    naccSum[j] = naccWindow[i * windowSize + j][0] * naccWindow[i * windowSize + j][0] + naccWindow[i * windowSize + j][1] * naccWindow[i * windowSize + j][1];

                                    myMath.addData(accStopWindow, naccSum[j]);//提取停止窗口

                                }

                                accSumMean = myMath.getMean(naccSum);
                                accSumVar = myMath.getVar(naccSum);

                                Log.d(TAG, "windowID\t" + i);
                                Log.d(TAG, "laccSumMean:" + accSumMean);
                                Log.d(TAG, "laccSumVar:" + accSumVar);

                                NOW_STATE = stateRecognizeUseAccel(accSumMean, accSumVar);

                                if ((NOW_STATE == PhoneState.USER_STATIC_STATE || NOW_STATE == PhoneState.ABSOLUTE_STATIC_STATE) && LAST_STATE == PhoneState.UNKONW_STATE) {
                                    StopWindow = i;
                                    break;
                                }
                                LAST_STATE = NOW_STATE;
                                //运动时间超过5s
                                StopWindow = i;
                            }

                            //在结束窗口找出结束点
                            accSlideWindow = new float[5];
                            for (int i = 0; i < 2 * windowSize - 5; i++) {
                                for (int j = 0; j < 5; j++) {
                                    accSlideWindow[j] = accStopWindow[i + j];
                                }
                                accSumMean = myMath.getMean(accSlideWindow);
                                accSumVar = myMath.getVar(accSlideWindow);
                                Log.d(TAG, "stopSlideWindowID\t" + i);
                                Log.d(TAG, "laccSumMean:" + accSumMean);
                                Log.d(TAG, "laccSumVar:" + accSumVar);

                                NOW_STATE = stateRecognizeUseAccel(accSumMean, accSumVar);
                                if (NOW_STATE == PhoneState.USER_STATIC_STATE || NOW_STATE == PhoneState.ABSOLUTE_STATIC_STATE) {
                                    stopFlag = i;
                                    break;
                                }
                                stopFlag = i;
                            }

                            Log.d(TAG, "beginFlag" + ":\t" + beginFlag);
                            Log.d(TAG, "stopFlag:" + ":\t" + stopFlag);
                            Log.d(TAG, "stopWindow:" + ":\t" + StopWindow);

                            //从0到beginFlag 为平稳静止，计算初始姿态
                            int InitialSize = windowSize;
                            if (beginFlag < windowSize && beginFlag > 5) {
                                InitialSize = beginFlag;
                            }
                            //初始姿态
                            float[] _accOri = myMath.getMean(accWindow, 0, InitialSize);
                            float[] _magOri = myMath.getMean(magWindow, 0, InitialSize);
                            androidAtt(_accOri, _magOri);
                            DCM_Static = androidDCM.clone();
                            DcmQueue[beginFlag] = DCM_Static;

                            //初始加速度
                            float[] accNow = myMath.coordinatesTransform(DcmQueue[beginFlag], accWindow[beginFlag]);
                            float[] accLast = accNow.clone();
                            Log.d(TAG, "accNow[0]:" + ":\t" + accNow[0]);
                            Log.d(TAG, "accNow[1]:" + ":\t" + accNow[1]);
                            Log.d(TAG, "accNow[2]:" + ":\t" + (accNow[2]));
                            //path输出数据缓存
                            StringBuffer pathOut = new StringBuffer();

                            //path数据提取
                            ArrayList<PathData> Path = new ArrayList<PathData>();
                            PathData pathValue = new PathData(accWindow[beginFlag],gyroWindow[beginFlag],0f);
                            Path.add(pathValue);

                            float time0 = 0;
                            int PathLength = 1;
                            boolean ifInterpolation = false;
                            //开始计算Path
                            for (int i = beginFlag + 1; i < (StopWindow - 2) * windowSize + stopFlag; i++) {
                                time0 += deltTWindow[i];
                                //when i = 0, velocitySample[i] =0; positionSample[i] =0;
                                pathValue = new PathData(accWindow[i],gyroWindow[i],time0);
                                Path.add(pathValue);

                                pathOut.append(timeWindow[i] + "\t");

                                //角速度插值
                                float[] W = myMath.matrixDivide(myMath.matrixAdd(gyroWindow[i], gyroWindow[i - 1]), 2);
                                //float[] W = gyroWindow[i].clone();

                                pathOut.append(W[0]+ "\t");
                                pathOut.append(W[1] + "\t");
                                pathOut.append(W[2] + "\t");

                                //旋转矩阵导数
                                float[] Matrix_W = new float[]{1f, -W[2] * deltTWindow[i], W[1] * deltTWindow[i],//
                                        W[2] * deltTWindow[i], 1f, -W[0] * deltTWindow[i], //
                                        -W[1] * deltTWindow[i], W[0] * deltTWindow[i], 1f};

                                //新时刻旋转矩阵
                                DcmQueue[i] = myMath.matrixMultiply(DcmQueue[i - 1], Matrix_W, 3);

                                myLog.log(TAG, DcmQueue[i], "gyro DCM", 3);

                                Log.d(TAG, "accWindow[i][0]:" + String.valueOf(i) + ":\t" + accWindow[i][0]);
                                Log.d(TAG, "accWindow[i][1]:" + String.valueOf(i) + ":\t" + accWindow[i][1]);
                                Log.d(TAG, "accWindow[i][2]:" + String.valueOf(i) + ":\t" + accWindow[i][2]);

                                pathOut.append(accWindow[i][0] + "\t");
                                pathOut.append(accWindow[i][1] + "\t");
                                pathOut.append(accWindow[i][2] + "\t");

                                ///新惯性加速度
                                accNow = myMath.coordinatesTransform(DcmQueue[i], accWindow[i]);

                                Log.d(TAG, "accNow[0]:" + String.valueOf(i) + ":\t" + accNow[0]);
                                Log.d(TAG, "accNow[1]:" + String.valueOf(i) + ":\t" + accNow[1]);
                                Log.d(TAG, "accNow[2]:" + String.valueOf(i) + ":\t" + (accNow[2] - G));

                                Log.d(TAG, "accLast[0]:" + String.valueOf(i) + ":\t" + accLast[0]);
                                Log.d(TAG, "accLast[1]:" + String.valueOf(i) + ":\t" + accLast[1]);
                                Log.d(TAG, "accLast[2]:" + String.valueOf(i) + ":\t" + (accLast[2] - G));

                                pathOut.append((accNow[0] + accLast[0]) / 2 + "\t");
                                pathOut.append((accNow[1] + accLast[1]) / 2 + "\t");
                                pathOut.append((accNow[2] + accLast[2] - 2 * G) / 2 + "\t");

                                //新速度
                                velocityQueue[i][0] = velocityQueue[i - 1][0] + 0.5f * (accNow[0] + accLast[0]) * deltTWindow[i];
                                velocityQueue[i][1] = velocityQueue[i - 1][1] + 0.5f * (accNow[1] + accLast[1]) * deltTWindow[i];
                                velocityQueue[i][2] = velocityQueue[i - 1][2] + 0.5f * ((accNow[2] - G) + (accLast[2] - G)) * deltTWindow[i];

                                //记录上一次加速度
                                accLast = accNow.clone();

                                pathOut.append(velocityQueue[i][0] + "\t");
                                pathOut.append(velocityQueue[i][1] + "\t");
                                pathOut.append(velocityQueue[i][2] + "\t");

                                Log.d(TAG, "velocityQueue[0]:" + String.valueOf(i) + ":\t" + velocityQueue[i][0]);
                                Log.d(TAG, "velocityQueue[1]:" + String.valueOf(i) + ":\t" + velocityQueue[i][1]);
                                Log.d(TAG, "velocityQueue[2]:" + String.valueOf(i) + ":\t" + velocityQueue[i][2]);

                                //新位置
                                positionQ[i][0] = positionQ[i - 1][0] + 0.5f * (velocityQueue[i][0] + velocityQueue[i - 1][0]) * deltTWindow[i];
                                positionQ[i][1] = positionQ[i - 1][1] + 0.5f * (velocityQueue[i][1] + velocityQueue[i - 1][1]) * deltTWindow[i];
                                positionQ[i][2] = positionQ[i - 1][2] + 0.5f * (velocityQueue[i][2] + velocityQueue[i - 1][2]) * deltTWindow[i]; //- freeFallPosition;

                                pathOut.append(positionQ[i][0] + "\t");
                                pathOut.append(positionQ[i][1] + "\t");
                                pathOut.append(positionQ[i][2] + "\n");

                                Log.d(TAG, "position[0]" + String.valueOf(i) + ":\t" + positionQ[i][0]);
                                Log.d(TAG, "position[1]" + String.valueOf(i) + ":\t" + positionQ[i][1]);
                                Log.d(TAG, "position[2]" + String.valueOf(i) + ":\t" + positionQ[i][2]);
                                PathLength++;

                                ifInterpolation = true;
                            }

                            //输出path数据
                            pathOut.append("\n");
                            positionBuffer = pathOut;
                            //positionQueue = new float[DurationWindow * windowSize*myMath.N][3];//位置队列
                            Log.d(TAG,"PathLength\t"+PathLength);

                            if(ifInterpolation) {
                                PathIntegration pathTest = new PathIntegration(Path, PathLength);
                                pathTest.setRotMatrix0(DCM_Static);
                                pathTest.GenerateDataQueue();
                                pathTest.CalPath(positionQueue);
                                InterpositionBuffer = pathTest.getPathBuffer();
                                ifInterpolation = false;
                            }

                            positionQueue = positionQ.clone();
                            ifNewPath = true;
                        }//结束Path

                        LAST_STATE = NOW_STATE;
                    }
                }
            }).start();
        }
    }

    public void setAttitudeMode(int mode) {
        AttitudeMode = mode;
    }

    private void getAccCalibrateParams() {
        PhoneState.initAccCalibrateParams();
        params = PhoneState.getCalibrateParams();
    }

    private void updateStateThreshold() {
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

    public StringBuffer getPositionString(){
        return positionBuffer;
    }

    public StringBuffer getInterPositionString(){
        return InterpositionBuffer;
    }

    public boolean ifNewPath(){
        return ifNewPath;
    }

    public int getPosition_mark() {
        return position_mark;
    }

    public int getNowState() {
        return Window_STATE;
    }

    public float[] getNowStateValues() {
        return stateValues;
    }

    public void closeSensorThread() {
        threadDisable_data_update = true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                if (event.values != null) {
                    if (myMath.getMoulding(event.values) < RangeK * AccRange) {
                        float[] _rawacc = event.values.clone();
                        float[] _acc = AccCalibrate(event.values);
                        rawacc = myMath.V_android2Ned(_rawacc);
                        acc = myMath.V_android2Ned(_acc);
                        AccOriOriNew = true;
                    }
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                if (event.values != null) {
                    if (myMath.getMoulding(event.values) < RangeK * GyroRange) {
                        float[] _gyro = event.values.clone();
                        gyro = myMath.V_android2Ned(_gyro);
                        GyroOriNew = true;
                    }
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                if (event.values != null) {
                    if (myMath.getMoulding(event.values) < RangeK * MagRange) {
                        float[] _mag = event.values.clone();
                        //myLog.log(TAG,"mag raw:",_mag);
                        mag = myMath.V_android2Ned(_mag);
                        MagOriNew = true;
                    }
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

    private void ekfAtt(float[] accOri, float[] gyroOri, float[] magOri, float dt) {
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
        ekf.dt = dt;
        ekf.AttitudeEKF(false, // approx_prediction
                ekfP.use_moment_inertia, ekf.update_vect, ekf.dt, ekf.z_k, ekfP.q0, // q_rotSpeed,
                ekfP.q1, // q_rotAcc
                ekfP.q2, // q_acc
                ekfP.q3, // q_mag
                ekfP.r0, // r_gyro
                ekfP.r1, // r_accel
                ekfP.r2, // r_mag
                ekfP.moment_inertia_J, ekf.x_aposteriori, ekf.P_aposteriori, ekf.Rot_matrix, ekf.euler, ekf.debugOutput, ekf.euler_pre);
        //ekf.time = System.nanoTime();
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

    private void fcfAtt(float[] accOri, float[] gyroOri, float[] magOri, float dt) {

        accOri[0] *= -1;
        accOri[2] *= -1;
        gyroOri[0] *= -1;
        gyroOri[2] *= -1;
        magOri[0] *= -1;
        magOri[2] *= -1;
        fcf.acc = accOri.clone();
        fcf.gyro = gyroOri.clone();
        fcf.mag = magOri.clone();
        fcf.dt = dt;
        fcf.attitude(dt);
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
