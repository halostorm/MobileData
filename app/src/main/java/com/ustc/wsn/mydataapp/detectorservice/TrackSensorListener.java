package com.ustc.wsn.mydataapp.detectorservice;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.ustc.wsn.mydataapp.Application.AppResourceApplication;
import com.ustc.wsn.mydataapp.bean.AcceleratorData;
import com.ustc.wsn.mydataapp.bean.GyroData;
import com.ustc.wsn.mydataapp.bean.MagnetData;
import com.ustc.wsn.mydataapp.bean.PhoneState;
import com.ustc.wsn.mydataapp.quaternion.Quaternion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by halo on 2018/1/28.
 */

public class TrackSensorListener implements SensorEventListener {
    private static int LAST_STATE = PhoneState.UNKONW_STATE;
    private static int NOW_STATE = PhoneState.UNKONW_STATE;
    private final float G = 9.806f;
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
    private volatile float[][] laccSample = new float[windowSize][3];//线性加速度窗
    private volatile float[][] gravitySample = new float[windowSize][3];//重力加速度窗
    private volatile float[][] accSample = new float[windowSize][3];//加速度窗
    private volatile float[][] nlaccSample = new float[windowSize][3];//线性加速度窗
    private volatile float[][] naccSample = new float[windowSize][3];//加速度窗
    private volatile float[][] gyroSample = new float[windowSize][3];//角速度窗
    private volatile float[][] magSample = new float[windowSize][3];//磁场窗
    private volatile float[] deltT = new float[windowSize];//积分时间
    //private volatile long[] timeStamp = new long[windowSize];//积分时间

    //private volatile float[][] DcmQueue = new float[DurationWindow * windowSize][9];//
    private volatile float[][] gyroQueue = new float[DurationWindow * windowSize][3];//
    private volatile float[][] accQueue = new float[DurationWindow * windowSize][3];//
    //private volatile float[][] velocityQueue = new float[DurationWindow * windowSize][3];//
    private volatile float[][] positionQueue = new float[DurationWindow * windowSize][3];//位置队列
    private volatile int position_mark = DurationWindow * windowSize;
    private volatile float[] deltTQueue = new float[DurationWindow * windowSize];//积分时间
    private volatile long[] timeStamp = new long[DurationWindow*windowSize];//积分时间

    private long time;
    private long timeOld;

    //传感器参数
    private volatile float[] lacc = new float[3]; //phone frame
    private volatile float[] gravity = new float[3];
    private volatile float[] acc = new float[3];
    private volatile float[] accRaw = new float[3];
    private volatile float[] gyro = new float[3];
    private volatile float[] mag = new float[3];


    private float[] nlacc = new float[3];//inertial frame
    private float[] nacc = new float[3];
    private float[] naccRaw = new float[3];
    private float[] ngyro = new float[3];
    private float[] nmag = new float[3];


    //姿态参数
    private volatile float[] DCM = new float[]{1.f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f};
    private volatile Quaternion qAtt = new Quaternion();
    private volatile float[] DCM_static = new float[]{1.f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f};

    //滤波器参数
    private FCF fcf;

    private LPF_I accLPF;
    private LPF_I gyroLPF;
    private LPF_I magLPF;
    private BPF accBPF;
    private LPF_II accLPF2;
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

        accBPF = new BPF();
        accLPF2 = new LPF_II();

        accMF = new MeanFilter(5);
        gyroMF = new MeanFilter(5);
        magMF = new MeanFilter(5);

        time = System.nanoTime();
        timeOld = System.nanoTime();

        fcf = new FCF();

        //加速度校准参数提取
        getAccCalibrateParams();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Quaternion Q = qAtt;
                int count=0;
                while (!threadDisable_data_update) {
                    try {
                        Thread.sleep(sampleInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    time = System.nanoTime();
                    float dt = (time - timeOld) / 1000000000f;
                    timeOld = time;

                    float[] _w = gyro.clone();
                    if(count++ ==windowSize) {
                        Q = qAtt;
                        count = 0;
                    }

                    //Log.d(TAG,"gyroWindow[0]:"+String.valueOf(i)+":\t"+W[0]);
                    //Log.d(TAG,"gyroWindow[1]:"+String.valueOf(i)+":\t"+W[1]);
                    //Log.d(TAG,"gyroWindow[2]:"+String.valueOf(i)+":\t"+W[2]);

                    Q = Q.update(_w,dt);
                    nacc = Q.rotate(acc);//得到一次理想加速度
                    naccRaw = Q.rotate(accRaw);//得到一次理想加速度

                    nlacc = Q.rotate(lacc);
                    ngyro = Q.rotate(gyro);
                    nmag = Q.rotate(mag);
                    /*
                    nacc = phoneToEarth(dcm, acc);//得到一次理想加速度
                    naccRaw = phoneToEarth(dcm, accRaw);//得到一次理想加速度

                    nlacc = phoneToEarth(dcm, lacc);
                    ngyro = phoneToEarth(dcm, gyro);
                    nmag = phoneToEarth(dcm, mag);
                    */

                    addData(deltT, dt);
                    addData(laccSample, lacc);
                    addData(accSample, acc);
                    addData(nlaccSample, lacc);
                    addData(naccSample, nacc);
                    addData(gyroSample, gyro);
                    addData(magSample, mag);
                    addData(gravitySample, gravity);

                    addData(deltTQueue, dt);
                    addData(timeStamp, System.currentTimeMillis());
                    addData(accQueue, acc);
                    addData(gyroQueue, gyro);
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!threadDisable_data_update) {
                    try {
                        Thread.sleep(windowSize*sampleInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    float[] accMean = getMean(accSample);
                    float[] magMean = getMean(magSample);
                    SensorManager.getRotationMatrix(DCM, null, accMean, magMean);
                    qAtt = qAtt.fromDcm(DCM);
                }
            }
        }).start();

        if(ifPath) {
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
                            float[] gyroMean = getMean(gyroSample);
                            float[] gyroVar = getVar(gyroSample);
                            float[] laccMean = getMean(laccSample);
                            float[] laccVar = getVar(laccSample);

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
                            float laccSumMean = getMean(nlaccSum);
                            float laccSumVar = getVar(nlaccSum);

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

                        //姿态参数更新
                        float[] gravityMean = getMean(gravitySample);
                        float[] accMean = getMean(accSample);
                        float[] magMean = getMean(magSample);
                        //初始化上一次积分变量

                        //计算初始DCM
                        if (NOW_STATE == PhoneState.ABSOLUTE_STATIC_STATE || NOW_STATE == PhoneState.USER_STATIC_STATE) {
                            SensorManager.getRotationMatrix(DCM_static, null, accMean, magMean);
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
                            addData(time, timeStampBegin);
                            addData(gyroWindow, gyroSampleBegin);
                            addData(deltTWindow, deltBegin);
                            addData(accWindow, accSampleBegin);
                            w_count++;
                            //添加本次变量
                            addData(time, timeStamp);
                            addData(gyroWindow, gyroSample);
                            addData(deltTWindow, deltT);
                            addData(accWindow, accSample);
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
                                float laccSumMean = getMean(nlaccSum);
                                float laccSumVar = getVar(nlaccSum);

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
                                w_count = w + 2;
                                //添加当前窗口，添加窗口总数为 w+2 = 静+动+(w-1)*动+静；窗口总数最少为3：
                                if (NOW_STATE == PhoneState.ABSOLUTE_STATIC_STATE || NOW_STATE == PhoneState.USER_STATIC_STATE) {
                                    //若当前窗口已经停止，跳出，不再添加
                                    break;
                                }
                                addData(time, timeStamp);
                                addData(gyroWindow, gyroSample);
                                addData(deltTWindow, deltT);
                                addData(accWindow, accSample);
                            }

                            DcmQueue[(DurationWindow - w_count) * windowSize] = DCM_static.clone();
                            time = timeStamp.clone();
                            gyroWindow = gyroQueue.clone();//;
                            deltTWindow = deltTQueue.clone();//;
                            accWindow = accQueue.clone();//;

                            String pathOut = new String();

                            for (int i = ((DurationWindow - w_count) * windowSize + 1); i < (DurationWindow - 1) * windowSize; i++) { //when i = 0, velocitySample[i] =0; positionSample[i] =0;
                                float[] _w = gyroWindow[i].clone();
                                float[] W = phoneToEarth(DcmQueue[i-1],_w);
                                //Log.d(TAG,"gyroWindow[0]:"+String.valueOf(i)+":\t"+W[0]);
                                //Log.d(TAG,"gyroWindow[1]:"+String.valueOf(i)+":\t"+W[1]);
                                //Log.d(TAG,"gyroWindow[2]:"+String.valueOf(i)+":\t"+W[2]);

                                float[] Matrix_W = new float[]{1f, -W[2] * deltTWindow[i], W[1] * deltTWindow[i],//
                                        W[2] * deltTWindow[i], 1f, -W[0] * deltTWindow[i], //
                                        -W[1] * deltTWindow[i], W[0] * deltTWindow[i], 1f};
                                float[] euler = new float[3];

                                DcmQueue[i] = mMultiply(DcmQueue[i - 1], Matrix_W);//获取新DCM
                                SensorManager.getOrientation(DcmQueue[i], euler);//获取欧拉角

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
                                float[] accNow = phoneToEarth(DcmQueue[i], accWindow[i]);//得到一次新惯性加速度
                                float[] accLast = phoneToEarth(DcmQueue[i - 1], accWindow[i - 1]);//记录上一次惯性加速度
                                Log.d(TAG, "accNow[0]:" + String.valueOf(i) + ":\t" + accNow[0]);
                                Log.d(TAG, "accNow[1]:" + String.valueOf(i) + ":\t" + accNow[1]);
                                Log.d(TAG, "accNow[2]:" + String.valueOf(i) + ":\t" + (accNow[2]-G));
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

    private void getAccCalibrateParams(){
        PhoneState.initAccCalibrateParams();
        params = PhoneState.getCalibrateParams();
    }

    public void updateThresHoldParams() {
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

    public float[] readAccData(int TYPE) {
        if (TYPE == 0) {
            return this.acc;
        } else return this.nacc;
    }

    public float[] readRawAccData(int TYPE) {
        if (TYPE == 0) {
            return this.accRaw;
        } else return this.naccRaw;
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

    public float[][] getPosition() {

        return positionQueue;
    }

    public int getPosition_mark() {
        return position_mark;
    }

    public int getNowState() {
        return NOW_STATE;
    }

    /*
    //前入栈
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
    */

    //后入栈
    public void addData(float[][] sample, float[] values) {
        for (int i = 0; i < sample.length - 1; i++) {
            sample[i] = sample[i + 1].clone();
        }
        sample[sample.length - 1] = values.clone();
    }

    public void addData(float[][] sample, float[][] values) {
        for (int k = 0; k < values.length; k++) {
            for (int i = 0; i < sample.length - 1; i++) {
                sample[i] = sample[i + 1].clone();
            }
            sample[sample.length - 1] = values[k].clone();
        }
    }

    public void addData(float[] sample, float values) {
        for (int i = 0; i < sample.length - 1; i++) {
            sample[i] = sample[i + 1];
        }
        sample[sample.length - 1] = values;
    }

    public void addData(long[] sample, long values) {
        for (int i = 0; i < sample.length - 1; i++) {
            sample[i] = sample[i + 1];
        }
        sample[sample.length - 1] = values;
    }

    public void addData(long[] sample, long[] values) {
        for (int k = 0; k < values.length; k++) {
            for (int i = 0; i < sample.length - 1; i++) {
                sample[i] = sample[i + 1];
            }
            sample[sample.length - 1] = values[k];
        }
    }

    public void addData(float[] sample, float[] values) {
        for (int k = 0; k < values.length; k++) {
            for (int i = 0; i < sample.length - 1; i++) {
                sample[i] = sample[i + 1];
            }
            sample[sample.length - 1] = values[k];
        }
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
        // storeData = new StoreData();
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                if (event.values != null) {
                    accRaw = event.values.clone();
                    acc = accCalibrate(event.values);
                    gravity = accLPF.filter(acc);
                    lacc[0] = event.values[0] - gravity[0];
                    lacc[1] = event.values[1] - gravity[1];
                    lacc[2] = event.values[2] - gravity[2];
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                if (event.values != null) {
                    //gyro = gyroMF.filter(event.values);
                    //gyro = gyroLPF.filter(event.values);
                    gyro = event.values.clone();
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                if (event.values != null) {
                    //mag = magMF.filter(event.values);
                    //mag = magLPF.filter(event.values);
                    mag = event.values.clone();
                }
                break;
        }
    }

    public float[] accCalibrate(float[] rData){
        float[] data = new float[3];
        data[0] = rData[0] - params[9];
        data[1] = rData[1] - params[10];
        data[2] = rData[2] - params[11];
        //减去shift
        float[] aData = new float[3];
        for(int i =0;i<3;i++) {
            for (int j = 0; j < 3; j++) {
                aData[i] += params[i*3+j]*data[j];
            }
        }
        //乘以比例系数
        return  aData;
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

    public float[] phoneToEarth(float[] DCM, float[] values) {
        float[] valuesEarth = new float[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                valuesEarth[i] += values[j] * DCM[3 * i + j];
            }
        }
        return valuesEarth;
    }

    public float[] mMultiply(float[] A, float[] B) {
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


    /*
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
        float q1q1 = q[1]*q[1];
        float q2q2 = q[2]*q[2];
        float q3q3 = q[3]*q[3];

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

    public void DcmToQ(float[] data,float[][] mData) {
        float tr = mData[0][0] + mData[1][1] + mData[2][2];
        if (tr > 0.0f) {
            float s = (float)Math.sqrt(tr + 1.0f);
            data[0] = s * 0.5f;
            s = 0.5f / s;
            data[1] = (mData[2][1] - mData[1][2]) * s;
            data[2] = (mData[0][2] - mData[2][0]) * s;
            data[3] = (mData[1][0] - mData[0][1]) * s;
        } else {
            int dcm_i = 0;
            for (int i = 1; i < 3; i++) {
                if (mData[i][i] > mData[dcm_i][dcm_i]) {
                    dcm_i = i;
                }
            }
            int dcm_j = (dcm_i + 1) % 3;
            int dcm_k = (dcm_i + 2) % 3;
            float s = (float)Math.sqrt((mData[dcm_i][dcm_i] - mData[dcm_j][dcm_j] -
                    mData[dcm_k][dcm_k]) + 1.0f);
            data[dcm_i + 1] = s * 0.5f;
            s = 0.5f / s;
            data[dcm_j + 1] = (mData[dcm_i][dcm_j] + mData[dcm_j][dcm_i]) * s;
            data[dcm_k + 1] = (mData[dcm_k][dcm_i] + mData[dcm_i][dcm_k]) * s;
            data[0] = (mData[dcm_k][dcm_j] - mData[dcm_j][dcm_k]) * s;
        }
    }
    */

}
