package com.ustc.wsn.mydataapp.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ustc.wsn.mydataapp.Application.AppResourceApplication;
import com.ustc.wsn.mydataapp.detectorservice.TrackSensorListener;
import com.ustc.wsn.mydataapp.detectorservice.outputFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import detector.wsn.ustc.com.mydataapp.R;

public class AccCalibrateActivity extends Activity implements View.OnClickListener {
    private final String TAG = AccCalibrateActivity.this.toString();
    private TrackSensorListener sensorListener;
    private SensorManager sm;
    private boolean ACCELERATOR_EXIST = false;
    private Sensor accelerator;
    private Toast t;
    private int handler_i = 0;

    private Button up;
    private Button down;
    private Button right;
    private Button left;
    private Button forward;
    private Button back;

    private final float G = 9.806f;
    private float[] Sample = {0f, 0f, 0f};

    private static float x_K = 1;
    private static float y_K = 1;
    private static float z_K = 1;
    private static float x_B = 0;
    private static float y_B = 0;
    private static float z_B = 0;

    private float[] xP = {0f, 0f, 0f};
    private float[] xI = {0f, 0f, 0f};
    private float[] zP = {0f, 0f, 0f};
    private float[] zI = {0f, 0f, 0f};
    private float[] yP = {0f, 0f, 0f};
    private float[] yI = {0f, 0f, 0f};

    private static boolean is_xP = false;
    private static boolean is_yP = false;
    private static boolean is_zP = false;
    private static boolean is_xI = false;
    private static boolean is_yI = false;
    private static boolean is_zI = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_calibrate);
        initAccel();

        up = (Button) findViewById(R.id.btnUp);
        up.setOnClickListener(this);

        down = (Button) findViewById(R.id.btnDown);
        down.setOnClickListener(this);

        forward = (Button) findViewById(R.id.btnForward);
        forward.setOnClickListener(this);

        back = (Button) findViewById(R.id.btnBack);
        back.setOnClickListener(this);

        right = (Button) findViewById(R.id.btnRight);
        right.setOnClickListener(this);

        left = (Button) findViewById(R.id.btnLeft);
        left.setOnClickListener(this);

        Button cal = (Button) findViewById(R.id.btnCalCalibration);
        cal.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnRight:
                right.setText("请放置");
                right.setTextColor(Color.BLUE);
                delay(10, 1);//等待10s
                break;
            case R.id.btnLeft:
                left.setText("请放置");
                left.setTextColor(Color.BLUE);
                delay(10, 2);//等待10s
                break;
            case R.id.btnForward:
                forward.setText("请放置");
                forward.setTextColor(Color.BLUE);
                delay(10, 3);//等待10s
                break;
            case R.id.btnBack:
                back.setText("请放置");
                back.setTextColor(Color.BLUE);
                delay(10, 4);//等待10s
                break;
            case R.id.btnUp:
                up.setText("请放置");
                up.setTextColor(Color.BLUE);
                delay(10, 5);//等待10s
                //getSample(5);//屏幕朝上
                break;
            case R.id.btnDown:
                down.setText("请放置");
                down.setTextColor(Color.BLUE);
                delay(10, 6);//等待10s
                break;
            case R.id.btnCalCalibration:
                calibration();
                break;
        }
    }

    public void getSample(int ori) {
        final int _ori = ori;
        Log.d(TAG, "getSample\t" + ori);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                float[][] acc = sensorListener.getAccSample();
                Sample= getMean(acc);
                Log.d(TAG, "accSample[0]\t" + Sample[0]);
                Log.d(TAG, "accSample[1]\t" + Sample[1]);
                Log.d(TAG, "accSample[2]\t" + Sample[2]);
                setSample(_ori);
            }
        }, 2 * 1000);
    }

    public void setSample(int orientation) {

        switch (orientation) {
            case 1:
                right.setText("校准完成");
                right.setTextColor(Color.BLUE);
                is_xP = true;
                break;
            case 2:
                left.setText("校准完成");
                left.setTextColor(Color.BLUE);
                is_xI = true;
                break;
            case 3:
                forward.setText("校准完成");
                forward.setTextColor(Color.BLUE);
                is_yP = true;
                break;
            case 4:
                back.setText("校准完成");
                back.setTextColor(Color.BLUE);
                is_yI = true;
                break;
            case 5:
                up.setText("校准完成");
                up.setTextColor(Color.BLUE);
                is_zP = true;
                break;
            case 6:
                down.setText("校准完成");
                down.setTextColor(Color.BLUE);
                is_zI = true;
                break;
        }

        switch (orientation) {
            case 1:
                xP = Sample.clone();
                break;
            case 2:
                xI = Sample.clone();
                break;
            case 3:
                yP = Sample.clone();
                break;
            case 4:
                yI = Sample.clone();
                break;
            case 5:
                zP = Sample.clone();
                break;
            case 6:
                zI = Sample.clone();
                break;
        }
        Sample = new float[3];//Sample置0
    }

    public void calibration() {
        if(is_xP&&is_xI) {
            x_K = 2 * G / (xP[0] - xI[0]);
            x_B = 2 * G * (xP[0] + xI[0]) / (xP[0] - xI[0]);
        }
        if(is_yI&&is_yP) {
            y_K = 2 * G / (yP[1] - yI[1]);
            y_B = 2 * G * (yP[1] + yI[1]) / (yP[1] - yI[1]);
        }
        if(is_zI&&is_zP) {
            z_K = 2 * G / (zP[2] - zI[2]);
            z_B = 2 * G * (zP[2] + zI[2]) / (zP[2] - zI[2]);
        }

        Log.d(TAG, "x_k\t" + x_K);
        Log.d(TAG, "y_K\t" + y_K);
        Log.d(TAG, "z_k\t" + z_K);
        Log.d(TAG, "x_B\t" + x_B);
        Log.d(TAG, "y_B\t" + y_B);
        Log.d(TAG, "z_B\t" + z_B);

        String out = new String();
        out += x_K + "\t";
        out += y_K + "\t";
        out += z_K + "\t";
        out += x_B + "\t";
        out += y_B + "\t";
        out += z_B;

        File accParams = outputFile.getAccParamsFile();
        try {
            FileWriter writer = new FileWriter(accParams);
            Log.d(TAG, "come in");
            writer.write(out);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("InlinedApi")
    public void initAccel() {
        Log.d("Sensor", "InitSensor Over");
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerator = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerator != null) {
            ACCELERATOR_EXIST = true;
        } else {
            t = Toast.makeText(this, "您的手机不支持加速度计", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }
        sensorListener = new TrackSensorListener(false);
        if (ACCELERATOR_EXIST) {
            sm.registerListener(sensorListener, accelerator, SensorManager.SENSOR_DELAY_GAME);
        }
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

    public void delay(int delay, final int orientation) { // S
        Log.d(TAG, "delay\t" + orientation);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                switch (orientation) {
                    case 1:
                        right.setText("校准中");
                        break;
                    case 2:
                        left.setText("校准中");
                        break;
                    case 3:
                        forward.setText("校准中");
                        break;
                    case 4:
                        back.setText("校准中");
                        break;
                    case 5:
                        up.setText("校准中");
                        break;
                    case 6:
                        down.setText("校准中");
                        break;
                }
                getSample(orientation);
            }
        }, delay * 1000);
    }

    public void delayMs(int delay) { // mS
        new Handler().postDelayed(new Runnable() {
            public void run() {
                //execute the task
            }
        }, delay);
    }
}
