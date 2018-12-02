package com.ustc.wsn.mobileData.bean;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by halo on 2018/1/24.
 */

public class PhoneState {
    private static final String TAG = PhoneState.class.toString();
    public int PHONE_STATE = 0;
    //手机状态
    public static final int ABSOLUTE_STATIC_STATE = 1;
    public static final int USER_STATIC_STATE = 2;
    public static final int WALK_STATE = 3;
    public static final int RUN_STATE = 4;
    public static final int BIKE_STATE = 5;
    public static final int CAR_STATE = 6;
    public static final int UNKONW_STATE = 5;

    //姿态解算模式
    public static final int Attitude_GYRO = 0;
    public static final int Attitude_ANDROID = 1;
    public static final int Attitude_EKF = 2;
    public static final int Attitude_FCF = 3;
    public static final int Attitude_GDF = 4;

    //状态判定参数
    public static float ACC_ABSOLUTE_STATIC_THRESHOLD = 0.01f;
    public static float GYRO_ABSOLUTE_STATIC_THRESHOLD = 0.01f;
    public static float ACC_STATIC_THRESHOLD = 0.1f;
    public static float GYRO_STATIC_THRESHOLD = 0.1f;

    public static float PHONE_USE_ON_VEHICLE_EULER_THRESHOLD = -5;

    public static float EULER_ABSOLUTE_STATIC_THRESHOLD = 0.01f;
    public static float EULER_STATIC_THRESHOLD = 0.1f;

    public static float ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD_DEFAULT = 0.01f;
    public static float ACC_VAR_ABSOLUTE_STATIC_THRESHOLD_DEFAULT = 0.001f;
    public static float ACC_MEAN_STATIC_THRESHOLD_DEFAULT = 2f;
    public static float ACC_VAR_STATIC_THRESHOLD_DEFAULT = 1.0f;

    public static float ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD = 0.01f;
    public static float ACC_VAR_ABSOLUTE_STATIC_THRESHOLD = 0.001f;
    public static float ACC_MEAN_STATIC_THRESHOLD = 2f;
    public static float ACC_VAR_STATIC_THRESHOLD = 1.0f;

    public static float AMPDB_THRESHOLD_DEFAULT = -11;
    public static float PEAK_FRE_THRESHOLD_DEFAULT = 10;
    public static float VEHICLE_PROBABILITY_THRESHOLD_DEFAULT = 0.3f;

    public static float AMPDB_THRESHOLD = -11;
    public static float PEAK_FRE_THRESHOLD = 10;
    public static float VEHICLE_PROBABILITY_THRESHOLD = 0.3f;

    //加速度校准参数
    private static float[] params = {1,0,0, 0,1,0, 0,0,1, 0,0,0};

    //
    public static float[] Euler = {0.0f,0f,0f};
    public static float[] Quarternion = {1.0f,0f,0f,0f};

    public volatile static String HAR ="Unknow";

    public synchronized static void initAccCalibrateParams(){

        File accparams = outputFile.getAccParamsFile();
        if (accparams.exists()) {
            try {
                BufferedReader bf = new BufferedReader(new FileReader(accparams));
                Log.d(TAG, "read params");
                String values = new String();
                values = bf.readLine();
                if (values.length() != 0) {
                    String[] v = new String[10];
                    v = values.split("\t");
                    for(int i = 0;i<params.length;i++) {
                        params[i] = Float.parseFloat(v[i]);
                        Log.d(TAG, "params0:"+params[i]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized static void initStateParams() {
        File params = outputFile.getParamsFile();
        if (params.exists()) {
            try {
                BufferedReader bf = new BufferedReader(new FileReader(params));
                Log.d(TAG, "read params");
                String values = new String();
                values = bf.readLine();
                if (values.length() != 0) {
                    String[] v = new String[10];
                    v = values.split("\t");
                    ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD = Float.parseFloat(v[0]);
                    ACC_VAR_ABSOLUTE_STATIC_THRESHOLD = Float.parseFloat(v[1]);
                    ACC_MEAN_STATIC_THRESHOLD = Float.parseFloat(v[2]);
                    ACC_VAR_STATIC_THRESHOLD = Float.parseFloat(v[3]);

                    AMPDB_THRESHOLD = Float.parseFloat(v[4]);
                    PEAK_FRE_THRESHOLD = Float.parseFloat(v[5]);
                    VEHICLE_PROBABILITY_THRESHOLD = Float.parseFloat(v[6]);

                    Log.d(TAG, "params0:"+v[0]);
                    Log.d(TAG, "params1:"+v[1]);
                    Log.d(TAG, "params2:"+v[2]);
                    Log.d(TAG, "params3:"+v[3]);
                    Log.d(TAG, "params4:"+v[4]);
                    Log.d(TAG, "params5:"+v[5]);
                    Log.d(TAG, "params6:"+v[6]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public synchronized static float[] getCalibrateParams() {
        return params;
    }

}
