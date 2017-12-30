package com.ustc.wsn.detector.service;
/**
 * Created by halo on 2017/7/1.
 */

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.os.Binder;

import com.ustc.wsn.detector.Application.AppResourceApplication;
import com.ustc.wsn.detector.bean.CellInfo;
import com.ustc.wsn.detector.detectorservice.DetectorSensorListener;
import com.ustc.wsn.detector.bean.StoreData;
import com.ustc.wsn.detector.detectorservice.gps;
import com.ustc.wsn.detector.detectorservice.outputFile;
import com.ustc.wsn.detector.utils.z7Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import detector.wsn.ustc.com.detectorservice.R;

public class DetectorService extends Service {

    protected static final String TAG = null;
    public volatile int stateLabel = 0;
    // private static final boolean false = false;
    ArrayList<CellInfo> cellIds = null;
    private static int windowSize = 256;// 256
    private static int sampleSize = 150;// 150
    private static int windowSamplingSize = 50;
    private boolean threadDisable_sensor = false;
    private boolean threadDisable_gps = false;
    // private boolean threadDisable_sensorInit = false;
    private boolean threadDisable_sensorPackage;
    // private final static String TAG = DetectorService.class.getSimpleName();

    // private volatile boolean rawFileWriteFlag = false;
    private volatile boolean rawFileReadFlag = false;

    private Context mContext = DetectorService.this;
    private SensorManager sm;
    private Sensor accelerator;
    private Sensor gyroscrope;
    private Sensor magnetic;
    // private Sensor rotation;
    private DetectorSensorListener sensorListener;

    // private TelephonyManager tm;
    // private PhoneStateListener psl;

    // private LocationListener locationListener;

    // private AppResource resource;m
    private StoreData sd;
    private gps sgps;
    private String location;
    // private LocationData loc;
    // private long timeBase = 0;

    // private String[] accData = new String[windowSize];// =
    // sensorListener.getAccData();
    // private String[] gyroData = new String[windowSize];// =
    // sensorListener.getGyroData();
    // private String[] magData = new String[windowSize];

    // private String[] accDataTrans = new String[windowSize];// =
    // sensorListener.getAccData();
    // private String[] gyroDataTrans = new String[windowSize];// =
    // sensorListener.getGyroData();
    // private String[] magDataTrans = new String[windowSize];

    private float[] accDataNorm = new float[windowSize];
    private float[] gyroDataNorm = new float[windowSize];
    private float[] magDataNorm = new float[windowSize];
    // private float[] X_angle = new float[windowSize];
    // private float[] Y_angle = new float[windowSize];
    // private float[] Z_angle = new float[windowSize];

    // private RotationData rotationData;
    // private String[] rotationDatatest = new String[windowSize];

    /*
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }
    */
    /**
     * 返回一个Binder对象
     */
    @Override
    public IBinder onBind(Intent intent) {
        return new MsgBinder();
    }

    public class MsgBinder extends Binder {
        /**
         * 获取当前Service的实例
         *
         * @return
         */
        public DetectorService getService() {
            return DetectorService.this;
        }
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        //stateLabel();//监听标签
        outputFile f = new outputFile();
        sd = new StoreData();
        location = new String();
        initSensor();// start sensor
        // initTelephony();
		/*
		 * try { TimeInit(); } catch (InterruptedException e2) { // TODO
		 * Auto-generated catch block e2.printStackTrace(); }
		 * 
		 */
        sgps = new gps(mContext);// start gps
        // GPS更新线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!threadDisable_gps) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (sgps != null) {
                        location = sgps.getLocation();
                        if (location != null) { // 当结束服务时gps为空
                            // loc = new LocationData(location.getLongitude(),
                            // location.getLatitude(),
                            // System.currentTimeMillis(), location.getSpeed());
                            // resource.updateLocationData(loc);
                            // StoreData sd = new StoreData();
                            try {
                                sd.storeLocation(location);
                            } catch (IOException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                        }
                    }

                }
            }
        }).start();

        dataHandle();

        try {
            DataPackage();
        } catch (InterruptedException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

    }

    public void dataHandle() {
        // Sensor更新线程
        new Thread(new Runnable() {

            float meanOfMeanPre = 0;
            float meanOfStdPre = 0;
            float stdOfMeanPre = 0;
            float stdOfStdPre = 0;
            // 初始化统计量
            float meanOfMeanNow = 0;
            float meanOfStdNow = 0;
            float stdOfMeanNow = 0;
            float stdOfStdNow = 0;

            int stateSize = 0;// 状态包含的数据量 = 250*窗口数

            @Override
            public void run() {
                while (!threadDisable_sensor) {
                    try {
                        Thread.sleep(6400);// 6400
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
					/*
					 * while (threadDisable_sensorInit != true) { try {
					 * Thread.sleep(1000); } catch (InterruptedException e) {
					 * e.printStackTrace(); } }
					 */
                    int i = 0;
                    // String[] outStoreCombine = new String[windowSize];
                    // String[] outStoreAttitude = new String[windowSize];
                    String[] outStoreRaw = new String[windowSize];

                    String[] windowTime = new String[windowSize];

                    float[] accX = new float[windowSize];
                    float[] accY = new float[windowSize];
                    float[] accZ = new float[windowSize];

                    float[] gyroX = new float[windowSize];
                    float[] gyroY = new float[windowSize];
                    float[] gyroZ = new float[windowSize];

                    float[] magX = new float[windowSize];
                    float[] magY = new float[windowSize];
                    float[] magZ = new float[windowSize];
                    String[] bear = new String[windowSize];

                    String accData;
                    String gyroData;
                    String magData;
                    String bearData;
                    int cLabel = stateLabel;
                    while (i < windowSize) {
                        accData = sensorListener.getAccData();
                        gyroData = sensorListener.getGyroData();
                        magData = sensorListener.getMagData();
                        bearData = sensorListener.getBearData();

                        // rotationData = sensorListener.getRotationData();
                        // 转化数据
                        if (accData == null)
                            Log.d(TAG, "accNull");
                        if (gyroData == null)
                            Log.d(TAG, "gyroNull");
                        if (magData == null)
                            Log.d(TAG, "magNull");
                        // if (rotationData == null)
                        // Log.d(TAG, "rotNull");

                        if (accData != null && gyroData != null && magData != null) {

                            if (bearData == null) {
                                bear[i] = String.valueOf(-1);
                            } else {
                                bear[i] = bearData;
                                //Log.d(TAG,"bear:"+bearData);
                            }
                            //Log.d(TAG, "bear:" + bear[i]);
                            // rotationDatatest[i] = rotationData.toString();
                            float[] accTemp = new float[3];
                            float[] gyroTemp = new float[3];
                            float[] magTemp = new float[3];

                            String accTransTime = new String();
                            // String gyroTransTime = new String();
                            // String magTransTime = new String();

                            // float[] accDataTemp = new float[3];// =
                            // sensorListener.getAccData();
                            // float[] gyroDataTemp = new float[3];// =
                            // sensorListener.getGyroData();
                            // float[] magDataTemp = new float[3];

                            // float[][] T = new float[3][3];
                            // float[] Q = new float[4];// 获取四元数
							/*
							 * Q[0] = rotationData.getX(); Q[1] =
							 * rotationData.getY(); Q[2] = rotationData.getZ();
							 * Q[3] = rotationData.getW(); double sqrtMean =
							 * Math.sqrt(Q[0] * Q[0] + Q[1] * Q[1] + Q[2] * Q[2]
							 * + Q[3] * Q[3]); // 获取转换矩阵 Q[0] = -(float) (Q[0] /
							 * sqrtMean); Q[1] = -(float) (Q[1] / sqrtMean);
							 * Q[2] = -(float) (Q[2] / sqrtMean); Q[3] = (float)
							 * (Q[3] / sqrtMean);
							 * 
							 * X_angle[i] = (float) Math.atan2(2 * Q[0] * Q[3] +
							 * 2 * Q[1] * Q[2], 1 - 2 * Q[0] * Q[0] - 2 * Q[1] *
							 * Q[1]); Y_angle[i] = (float) Math.asin(2 * Q[1] *
							 * Q[3] - 2 * Q[0] * Q[2]); Z_angle[i] = (float)
							 * Math.atan2(2 * Q[0] * Q[1] + 2 * Q[2] * Q[3], 1 -
							 * 2 * Q[1] * Q[1] - 2 * Q[2] * Q[2]);
							 * 
							 * // Log.d(TAG,String.valueOf(Q[0])+" //
							 * "+String.valueOf(Q[1])+" "+String.valueOf(Q[2])
							 * // +" "+String.valueOf(Q[3]));
							 * 
							 * T[0][0] = Q[0] * Q[0] - Q[1] * Q[1] - Q[2] * Q[2]
							 * + Q[3] * Q[3]; T[0][1] = 2 * (Q[0] * Q[1] + Q[2]
							 * * Q[3]); T[0][2] = 2 * (Q[0] * Q[2] - Q[1] *
							 * Q[3]);
							 * 
							 * T[1][0] = 2 * (Q[0] * Q[1] - Q[2] * Q[3]);
							 * T[1][1] = Q[3] * Q[3] + Q[1] * Q[1] - Q[0] * Q[0]
							 * - Q[2] * Q[2]; T[1][2] = 2 * (Q[1] * Q[2] + Q[0]
							 * * Q[3]);
							 * 
							 * T[2][0] = 2 * (Q[1] * Q[3] + Q[0] * Q[2]);
							 * T[2][1] = 2 * (Q[1] * Q[2] - Q[0] * Q[3]);
							 * T[2][2] = Q[2] * Q[2] + Q[3] * Q[3] - Q[0] * Q[0]
							 * - Q[1] * Q[1];
							 */
							/*
							 * T[0][0] = Q[0] * Q[0] + Q[1] * Q[1] - Q[2] * Q[2]
							 * - Q[3] * Q[3]; T[0][1] = 2 * (Q[1] * Q[2] - Q[0]
							 * * Q[3]); T[0][2] = 2 * (Q[1] * Q[3] + Q[0] *
							 * Q[2]);
							 * 
							 * T[1][0] = 2 * (Q[1] * Q[2] + Q[0] * Q[3]);
							 * T[1][1] = Q[0] * Q[0] - Q[1] * Q[1] + Q[2] * Q[2]
							 * - Q[3] * Q[3]; T[1][2] = 2 * (Q[2] * Q[3] - Q[0]
							 * * Q[1]);
							 * 
							 * T[2][0] = 2 * (Q[1] * Q[3] - Q[0] * Q[2]);
							 * T[2][1] = 2 * (Q[2] * Q[3] + Q[0] * Q[1]);
							 * T[2][2] = Q[0] * Q[0] - Q[1] * Q[1] - Q[2] * Q[2]
							 * + Q[3] * Q[3];
							 */

							/*
							 * Log.d(TAG, String.valueOf(T[0][0])
							 * +" "+String.valueOf(T[0][1])
							 * +" "+String.valueOf(T[0][2])
							 * +" "+String.valueOf(T[1][0])
							 * +" "+String.valueOf(T[1][1])
							 * +" "+String.valueOf(T[1][2])
							 * +" "+String.valueOf(T[2][0])
							 * +" "+String.valueOf(T[2][1])
							 * +" "+String.valueOf(T[2][2]));
							 */

                            String[] accArray = new String[5];
                            accArray = accData.split("\t");
                            // get raw data
                            accTransTime = accArray[1];
                            windowTime[i] = accTransTime;
                            accTemp[0] = Float.parseFloat(accArray[2]);
                            accTemp[1] = Float.parseFloat(accArray[3]);
                            accTemp[2] = Float.parseFloat(accArray[4]);

                            accX[i] = accTemp[0];
                            accY[i] = accTemp[1];
                            accZ[i] = accTemp[2];

                            accDataNorm[i] = (float) Math
                                    .sqrt(accTemp[0] * accTemp[0] + accTemp[1] * accTemp[1] + accTemp[2] * accTemp[2]);
							/*
							 * for (int j = 0; j < 3; j++) { for (int k = 0; k <
							 * 3; k++) { accDataTemp[j] += T[j][k] * accTemp[k];
							 * } }
							 */
							/*
							 * accDataTrans[i] = accTransTime + "\t" +
							 * String.valueOf(accDataTemp[0]) + "\t" +
							 * String.valueOf(accDataTemp[1]) + "\t" +
							 * String.valueOf(accDataTemp[2]);
							 */
                            String[] gyroArray = new String[5];
                            gyroArray = gyroData.split("\t");

                            // gyroTransTime = gyroArray[1];
                            gyroTemp[0] = Float.parseFloat(gyroArray[2]);
                            gyroTemp[1] = Float.parseFloat(gyroArray[3]);
                            gyroTemp[2] = Float.parseFloat(gyroArray[4]);

                            gyroDataNorm[i] = (float) Math.sqrt(
                                    gyroTemp[0] * gyroTemp[0] + gyroTemp[1] * gyroTemp[1] + gyroTemp[2] * gyroTemp[2]);

                            gyroX[i] = gyroTemp[0];
                            gyroY[i] = gyroTemp[1];
                            gyroZ[i] = gyroTemp[2];
							/*
							 * for (int j = 0; j < 3; j++) { for (int k = 0; k <
							 * 3; k++) { gyroDataTemp[j] += T[j][k] *
							 * gyroTemp[k]; } } gyroDataTrans[i] = gyroTransTime
							 * + "\t" + String.valueOf(gyroDataTemp[0]) + "\t" +
							 * String.valueOf(gyroDataTemp[1]) + "\t" +
							 * String.valueOf(gyroDataTemp[2]);
							 */
                            String[] magArray = new String[5];
                            magArray = magData.split("\t");

                            // magTransTime = magArray[1];
                            magTemp[0] = Float.parseFloat(magArray[2]);
                            magTemp[1] = Float.parseFloat(magArray[3]);
                            magTemp[2] = Float.parseFloat(magArray[4]);

                            magDataNorm[i] = (float) Math
                                    .sqrt(magTemp[0] * magTemp[0] + magTemp[1] * magTemp[1] + magTemp[2] * magTemp[2]);

                            magX[i] = magTemp[0];
                            magY[i] = magTemp[1];
                            magZ[i] = magTemp[2];
							/*
							 * for (int j = 0; j < 3; j++) { for (int k = 0; k <
							 * 3; k++) { magDataTemp[j] += T[j][k] * magTemp[k];
							 * } } magDataTrans[i] = magTransTime + "\t" +
							 * String.valueOf(magDataTemp[0]) + "\t" +
							 * String.valueOf(magDataTemp[1]) + "\t" +
							 * String.valueOf(magDataTemp[2]);
							 * 
							 * outStoreCombine[i] = accTransTime + "\t" +
							 * String.valueOf(accDataTemp[0]) + "\t" +
							 * String.valueOf(accDataTemp[1]) + "\t" +
							 * String.valueOf(accDataTemp[2]) + "\t" +
							 * String.valueOf(gyroDataTemp[0]) + "\t" +
							 * String.valueOf(gyroDataTemp[1]) + "\t" +
							 * String.valueOf(gyroDataTemp[2]) + "\t" +
							 * String.valueOf(magDataTemp[0]) + "\t" +
							 * String.valueOf(magDataTemp[1]) + "\t" +
							 * String.valueOf(magDataTemp[2]);
							 * 
							 * outStoreAttitude[i] = accTransTime + "\t" +
							 * String.valueOf(Q[0]) + "\t" +
							 * String.valueOf(Q[1]) + "\t" +
							 * String.valueOf(Q[2]) + "\t" +
							 * String.valueOf(Q[3]) + "\t" +
							 * String.valueOf(X_angle[i]) + "\t" +
							 * String.valueOf(Y_angle[i]) + "\t" +
							 * String.valueOf(Z_angle[i]) + "\t"; for (int r =
							 * 0; r < 3; r++) { for (int c = 0; c < 3; c++) {
							 * outStoreAttitude[i] += String.valueOf(T[r][c]) +
							 * "\t"; } }
							 */
                            if (i == 0) {
                                // windowTime = accTransTime;
                            }
                            i++;
                        }
                        // 如果出現緩衝池空，則停止讀取，等待5s
                        else {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    // 均值压缩
                    float accMeanX = getMean(accX);
                    float accMeanY = getMean(accY);
                    float accMeanZ = getMean(accZ);
                    float gyroMeanX = getMean(gyroX);
                    float gyroMeanY = getMean(gyroY);
                    float gyroMeanZ = getMean(gyroZ);
                    float magMeanX = getMean(magX);
                    float magMeanY = getMean(magY);
                    float magMeanZ = getMean(magZ);

                    for (int i_1 = 0; i_1 < windowSize; i_1++) {
                        if (Math.abs(accMeanX - accX[i_1]) < 0.01) {
                            accX[i_1] = 0;
                        } else {
                            accX[i_1] = (float) Math.round((accX[i_1] - accMeanX) * 1000) / 1000;
                        }

                        if (Math.abs(accMeanY - accY[i_1]) < 0.01) {
                            accY[i_1] = 0;
                        } else {
                            accY[i_1] = (float) Math.round((accY[i_1] - accMeanY) * 1000) / 1000;
                        }

                        if (Math.abs(accMeanZ - accZ[i_1]) < 0.01) {
                            accZ[i_1] = 0;
                        } else {
                            accZ[i_1] = (float) Math.round((accZ[i_1] - accMeanZ) * 1000) / 1000;
                        }
                        ///////// acc
                        if (Math.abs(gyroMeanX - gyroX[i_1]) < 0.01) {
                            gyroX[i_1] = 0;
                        } else {
                            gyroX[i_1] = (float) Math.round((gyroX[i_1] - gyroMeanX) * 1000) / 1000;
                        }

                        if (Math.abs(gyroMeanY - gyroY[i_1]) < 0.01) {
                            gyroY[i_1] = 0;
                        } else {
                            gyroY[i_1] = (float) Math.round((gyroY[i_1] - gyroMeanY) * 1000) / 1000;
                        }

                        if (Math.abs(gyroMeanZ - gyroZ[i_1]) < 0.01) {
                            gyroZ[i_1] = 0;
                        } else {
                            gyroZ[i_1] = (float) Math.round((gyroZ[i_1] - gyroMeanZ) * 1000) / 1000;
                        }
                        ///////// gyro
                        if (Math.abs(magMeanX - magX[i_1]) < 0.01) {
                            magX[i_1] = 0;
                        } else {
                            magX[i_1] = (float) Math.round((magX[i_1] - magMeanX) * 1000) / 1000;
                        }

                        if (Math.abs(magMeanY - magY[i_1]) < 0.01) {
                            magY[i_1] = 0;
                        } else {
                            magY[i_1] = (float) Math.round((magY[i_1] - magMeanY) * 1000) / 1000;
                        }

                        if (Math.abs(magMeanZ - magZ[i_1]) < 0.01) {
                            magZ[i_1] = 0;
                        } else {
                            magZ[i_1] = (float) Math.round((magZ[i_1] - magMeanZ) * 1000) / 1000;
                        }

                    }

                    for (int i_2 = 0; i_2 < windowSize; i_2++) {
                        if (i_2 == 0) {
                            outStoreRaw[i_2] = windowTime[i_2] + "\t" + cLabel+"\t"+ String.valueOf(accMeanX) + "\t"
                                    + String.valueOf(accMeanY) + "\t" + String.valueOf(accMeanZ) + "\t"
                                    + String.valueOf(gyroMeanX) + "\t" + String.valueOf(gyroMeanY) + "\t"
                                    + String.valueOf(gyroMeanZ) + "\t" + String.valueOf(magMeanX) + "\t"
                                    + String.valueOf(magMeanY) + "\t" + String.valueOf(magMeanZ) + "\n"
                                    + windowTime[i_2] + "\t" + String.valueOf(accX[i_2]) + "\t"
                                    + String.valueOf(accY[i_2]) + "\t" + String.valueOf(accZ[i_2]) + "\t"
                                    + String.valueOf(gyroX[i_2]) + "\t" + String.valueOf(gyroY[i_2]) + "\t"
                                    + String.valueOf(gyroZ[i_2]) + "\t" + String.valueOf(magX[i_2]) + "\t"
                                    + String.valueOf(magY[i_2]) + "\t" + String.valueOf(magZ[i_2]) + "\t" + bear[i_2];
                        } else {
                            outStoreRaw[i_2] = windowTime[i_2] + "\t" + cLabel+"\t"+  String.valueOf(accX[i_2]) + "\t"
                                    + String.valueOf(accY[i_2]) + "\t" + String.valueOf(accZ[i_2]) + "\t"
                                    + String.valueOf(gyroX[i_2]) + "\t" + String.valueOf(gyroY[i_2]) + "\t"
                                    + String.valueOf(gyroZ[i_2]) + "\t" + String.valueOf(magX[i_2]) + "\t"
                                    + String.valueOf(magY[i_2]) + "\t" + String.valueOf(magZ[i_2]) + "\t" + bear[i_2];
                        }
                    }

                    // 获取均值/标准差序列
                    float[] meanListNow = new float[windowSamplingSize];
                    float[] stdVarListNow = new float[windowSamplingSize];

                    for (int iList = 0; iList < windowSamplingSize; iList++) {
                        float[] t = new float[sampleSize];
                        t = dataRandom(accDataNorm);
                        meanListNow[iList] = getMean(t);
                        stdVarListNow[iList] = getStdVar(t);
                    }

                    // 计算检验统计量
                    meanOfMeanPre = meanOfMeanNow;
                    meanOfStdPre = meanOfStdNow;
                    stdOfMeanPre = stdOfMeanNow;
                    stdOfStdPre = stdOfStdNow;

                    meanOfMeanNow = getMean(meanListNow);
                    meanOfStdNow = getMean(stdVarListNow);
                    stdOfMeanNow = getStdVar(meanListNow);
                    stdOfStdNow = getStdVar(stdVarListNow);

                    //.d(TAG, String.valueOf(meanOfStdPre));
                    //Log.d(TAG, String.valueOf(meanOfStdNow));

                    //Log.d(TAG, String.valueOf(Math.abs(meanOfStdPre - meanOfStdNow)));
                    //Log.d(TAG, String.valueOf(stdOfStdPre) + "--" + String.valueOf(stdOfStdNow));

                    //Log.d(TAG, String.valueOf(Math.abs(meanOfMeanPre - meanOfMeanNow)));
                    //
                    // Log.d(TAG, String.valueOf(stdOfMeanPre) + "--" + String.valueOf(stdOfMeanNow));

                    // Log.d(TAG, "第一次定位1");
                    // 大数定理-假设检验,差异明显则切分
                    if ((meanOfStdPre > 0.1 || meanOfStdNow > 0.1)// 条件层1
                            && (Math.abs(meanOfStdPre - meanOfStdNow) > 5 * Math.min(stdOfStdPre, stdOfStdNow))) {
                        if ((Math.abs(meanOfStdPre - meanOfStdNow) > 10 * Math.min(stdOfStdPre, stdOfStdNow))
                                || ((Math.abs(meanOfMeanPre - meanOfMeanNow) > 5// 条件层2
                                * Math.min(stdOfMeanPre, stdOfMeanNow)))) {

                            // 若判断切分
                            if (stateSize != 0) {
                                Log.d(TAG, "切分");
                                String[] stateCutFlag = new String[1];
                                stateCutFlag[0] = String.valueOf(stateSize);
                                stateSize = 0;

								/*
								 * try { sd.storeDataCombine(stateCutFlag); }
								 * catch (IOException e1) { // TODO
								 * Auto-generated catch block
								 * e1.printStackTrace(); }
								 */
                                while (rawFileReadFlag == true) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                try {
                                    sd.storeDataRaw(stateCutFlag);
                                } catch (IOException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }
                    stateSize += 1;
                    // 原始数据
					/*
					 * if (accData != null) { // 当结束服务时gps为空 try {
					 * sd.storeDataAccelerator(accData); } catch (IOException
					 * e1) { // TODO Auto-generated catch block
					 * e1.printStackTrace(); } } if (gyroData != null) { //
					 * 当结束服务时gps为空 try { sd.storeDataGyroscope(gyroData); }
					 * catch (IOException e1) { // TODO Auto-generated catch
					 * block e1.printStackTrace(); } }
					 * 
					 * if (magData != null) { // 当结束服务时gps为空 try {
					 * sd.storeDataMagnetic(magData); } catch (IOException e1) {
					 * // TODO Auto-generated catch block e1.printStackTrace();
					 * } } try { sd.storeDataCombine(outStoreCombine); } catch
					 * (IOException e1) { // TODO Auto-generated catch block
					 * e1.printStackTrace(); } try {
					 * sd.storeDataAttitude(outStoreAttitude); } catch
					 * (IOException e1) { // TODO Auto-generated catch block
					 * e1.printStackTrace(); }
					 */
                    Log.d(TAG, "window");
                    while (rawFileReadFlag == true) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        sd.storeDataRaw(outStoreRaw);
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                }
            }

        }).start();

    }

    /*
	 * public void TimeInit() throws InterruptedException {
	 * 
	 * new Thread(new Runnable() {
	 * 
	 * @Override public void run() { while (!threadDisable_sensorInit) { try {
	 * Thread.sleep(2000); } catch (InterruptedException e) {
	 * e.printStackTrace(); } String[] acc = new String[5]; String[] gyro = new
	 * String[5]; String[] mag = new String[5];
	 * 
	 * String accTemp = new String(); String gyroTemp = new String(); String
	 * magTemp = new String();
	 * 
	 * float[] vector = { 0, 0, 0, 1 }; RotationData rot = new
	 * RotationData(vector, 0);
	 * 
	 * while (true) { accTemp = sensorListener.getAccData(); gyroTemp =
	 * sensorListener.getGyroData(); magTemp = sensorListener.getMagData(); //
	 * rot = sensorListener.getRotationData();
	 * 
	 * if (accTemp != null) { acc = accTemp.split("\t"); timeBase =
	 * Long.parseLong(acc[1]); } if (gyroTemp != null) { gyro =
	 * gyroTemp.split("\t"); if (timeBase < Long.parseLong(gyro[1]))
	 * 
	 * timeBase = Long.parseLong(gyro[1]); } if (magTemp != null) { mag =
	 * magTemp.split("\t"); if (timeBase < Long.parseLong(mag[1])) timeBase =
	 * Long.parseLong(mag[1]); } if (rot != null) { if (timeBase <
	 * rot.getTime())
	 * 
	 * timeBase = rot.getTime(); } if (accTemp != null && gyroTemp != null &&
	 * magTemp != null && rot != null) break; }
	 * 
	 * timeBase = timeBase + 1000; while (accTemp != null) { if
	 * (Long.parseLong(acc[1]) < timeBase) { accTemp =
	 * sensorListener.getAccData(); if (accTemp != null) acc =
	 * accTemp.split("\t"); } } while (gyroTemp != null) { if
	 * (Long.parseLong(gyro[1]) < timeBase) { gyroTemp =
	 * sensorListener.getGyroData(); if (gyroTemp != null) gyro =
	 * gyroTemp.split("\t"); } } while (magTemp != null) { if
	 * (Long.parseLong(mag[1]) < timeBase) { magTemp =
	 * sensorListener.getMagData(); if (magTemp != null) mag =
	 * magTemp.split("\t"); } } while (rot != null) { if (rot.getTime() <
	 * timeBase) { rot = sensorListener.getRotationData(); } }
	 * threadDisable_sensorInit = true; } Log.d(TAG, "init complete");
	 * threadDisable_sensorInit = true; } }).start(); threadDisable_sensorInit =
	 * true; }
	 */
    public void DataPackage() throws InterruptedException {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!threadDisable_sensorPackage) {
                    try {
                        Thread.sleep(5 * 60 * 1000);//
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    File inputFileRaw = sd.getRawDataFile(); //当前使用的txt文件
                    // File inputFileCombine = sd.getCombineDataFile();

                    if (inputFileRaw.length() > 5 * 1024 * 1024) {
                        Log.d(TAG, "begin");
                        File outputFile = sd.getz7RawDataFile();//当前7z文件
                        sd.getNewz7RawDataFile();//创建新的7z文件备下次使用
                        Log.d(TAG, "package");
                        String inputPath = inputFileRaw.getPath();
                        String outputPath = outputFile.getPath();
                        rawFileReadFlag = true;
                        sd.newRawDataFile();//创建新的txt文件供写入
                        rawFileReadFlag = false;
                        try {
                            z7Test.z7(inputPath, outputPath);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        inputFileRaw.delete();//删除压缩完的txt文件
                    }
					/*
					 * if (inputFileCombine.length() > 12*1024* 1024) { File
					 * outputFile = sd.getz7CombineDataFile(); Log.d(TAG,
					 * "package"); String inputPath =
					 * inputFileCombine.getPath(); String outputPath =
					 * outputFile.getPath();
					 * 
					 * sd.newCombineDataFile();
					 * 
					 * try { z7Test.z7(inputPath, outputPath); } catch
					 * (IOException e) { // TODO Auto-generated catch block
					 * e.printStackTrace(); } inputFileCombine.delete(); }
					 */

                }
            }
        }).start();
    }

    /*
	 * public void store(Location location) { LocationData loc = new
	 * LocationData(location.getLongitude(), location.getLatitude(),
	 * System.currentTimeMillis(),location.getSpeed()); StoreData sd = new
	 * StoreData(); sd.storeLocation(loc); }
	 */
    @SuppressLint("InlinedApi")
    public void initSensor() {
        Log.d("Sensor", "InitSensor Over");
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerator = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscrope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetic = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        // rotation = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorListener = new DetectorSensorListener((AppResourceApplication) getApplicationContext());
        sm.registerListener(sensorListener, accelerator, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(sensorListener, gyroscrope, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(sensorListener, magnetic, SensorManager.SENSOR_DELAY_GAME);
        // sm.registerListener(sensorListener,
        // rotation,SensorManager.SENSOR_DELAY_GAME);
    }
	/*
	 * public void initTelephony() { tm = (TelephonyManager)
	 * getSystemService(Context.TELEPHONY_SERVICE); psl = new
	 * DetectorTelListener(tm, (AppResource) getApplicationContext());
	 * tm.listen(psl, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS); }
	 */
    // editText = (EditText) findViewById(R.id.editText);

    public float[] dataRandom(float[] rawData) {
        // int[] index = new int[windowSize];
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

    public float getStdVar(float[] x) {
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

    public float getMean(float[] x) {
        int m = x.length;
        float sum = 0;
        for (int i = 0; i < m; i++) {// 求和
            sum += x[i];
        }
        float dAve = sum / m;// 求平均值
        return dAve;
    }

    public float getMean(float[][] x, int k) {
        int m = x.length;
        float sum = 0;
        for (int i = 0; i < m; i++) {// 求和
            sum += x[i][k];
        }
        float dAve = sum / m;// 求平均值
        return dAve;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        sensorListener.closeSensorThread();
        sm.unregisterListener(sensorListener);
        // tm.listen(psl, PhoneStateListener.LISTEN_NONE);
        // lm.removeUpdates(locationListener);
        threadDisable_gps = true;
        threadDisable_sensor = true;
        // threadDisable_sensorInit = true;
        threadDisable_sensorPackage = true;
        if (sgps != null) {
            sgps.closeLocation();
            sgps = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        // resource = (AppResource) getApplicationContext();
        // resource = new AppResource();

        // return super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
        //return START_REDELIVER_INTENT;
    }

}
