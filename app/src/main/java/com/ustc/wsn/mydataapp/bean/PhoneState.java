package com.ustc.wsn.mydataapp.bean;

import android.util.Log;

import com.ustc.wsn.mydataapp.detectorservice.outputFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by halo on 2018/1/24.
 */

public class PhoneState {
    private static final String TAG = "PhoneStateClass";
    public int PHONE_STATE = 0;
    public static final int ABSOLUTE_STATIC_STATE = 1;
    public static final int USER_STATIC_STATE = 2;
    public static final int WALK_STATE = 3;
    public static final int RUN_STATE = 4;
    public static final int BIKE_STATE = 5;
    public static final int CAR_STATE = 6;
    public static final int UNKONW_STATE = 5;

    public static float ACC_ABSOLUTE_STATIC_THRESHOLD = 0.01f;
    public static float GYRO_ABSOLUTE_STATIC_THRESHOLD = 0.01f;
    public static float ACC_STATIC_THRESHOLD = 0.1f;
    public static float GYRO_STATIC_THRESHOLD = 0.1f;

    public static void initParams() {
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
                    ACC_ABSOLUTE_STATIC_THRESHOLD = Float.parseFloat(v[0]);
                    GYRO_ABSOLUTE_STATIC_THRESHOLD = Float.parseFloat(v[1]);
                    ACC_STATIC_THRESHOLD = Float.parseFloat(v[2]);
                    GYRO_STATIC_THRESHOLD = Float.parseFloat(v[3]);
                    Log.d(TAG, "params0:"+v[0]);
                    Log.d(TAG, "params1:"+v[1]);
                    Log.d(TAG, "params2:"+v[2]);
                    Log.d(TAG, "params3:"+v[3]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
