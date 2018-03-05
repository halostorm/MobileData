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
    private final int sampleSize = 100;
    private final int sampleInteral = 40;//ms
    private float[] Sample = {0f, 0f, 0f};

    private float x_K = 0;
    private float y_K = 0;
    private float z_K = 0;
    private float x_B = 0;
    private float y_B = 0;
    private float z_B = 0;

    private float[] xP = {0f, 0f, 0f};
    private float[] xI = {0f, 0f, 0f};
    private float[] zP = {0f, 0f, 0f};
    private float[] zI = {0f, 0f, 0f};
    private float[] yP = {0f, 0f, 0f};
    private float[] yI = {0f, 0f, 0f};

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
                delay(10,1);//等待10s
                break;
            case R.id.btnLeft:
                left.setText("请放置");
                left.setTextColor(Color.BLUE);
                delay(10,2);//等待10s
                break;
            case R.id.btnForward:
                forward.setText("请放置");
                forward.setTextColor(Color.BLUE);
                delay(10,3);//等待10s
                break;
            case R.id.btnBack:
                back.setText("请放置");
                back.setTextColor(Color.BLUE);
                delay(10,4);//等待10s
                break;
            case R.id.btnUp:
                up.setText("请放置");
                up.setTextColor(Color.BLUE);
                delay(10,5);//等待10s
                //getSample(5);//屏幕朝上
                break;
            case R.id.btnDown:
                down.setText("请放置");
                down.setTextColor(Color.BLUE);
                delay(10,6);//等待10s
                break;
            case R.id.btnCalCalibration:
                calibration();
                break;
        }
    }

    public void getSample( int ori) {
        Log.d(TAG,"getSample\t"+ori);
        handler_i = 0;
        final int orientation = ori;
        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (handler_i < sampleSize) {
                        float[] acc = sensorListener.readAccData(0);
                        Sample[0] += acc[0];
                        Sample[1] += acc[1];
                        Sample[2] += acc[2];
                        Log.d(TAG, "accSample[0]\t" + Sample[0] + "\t" + handler_i);
                        Log.d(TAG, "accSample[1]\t" + Sample[1] + "\t" + handler_i);
                        Log.d(TAG, "accSample[2]\t" + Sample[2] + "\t" + handler_i);
                        handler_i++;
                    } else if (handler_i == sampleSize) {
                        setSample(msg.arg1);
                        Log.d(TAG,"msg.arg1\t"+msg.arg1);
                        handler_i++;
                    }
                }
            };
            @Override
            public void run() {
                Message msg = new Message();
                msg.arg1 = orientation;
                handler.sendMessage(msg);
            }
        }, 0, sampleInteral);
    }

    //private

    public void setSample(int orientation) {
        Sample[0] /= sampleSize;
        Sample[1] /= sampleSize;
        Sample[2] /= sampleSize;

        switch (orientation) {
            case 1:
                right.setText("校准完成");
                right.setTextColor(Color.BLUE);
                break;
            case 2:
                left.setText("校准完成");
                left.setTextColor(Color.BLUE);
                break;
            case 3:
                forward.setText("校准完成");
                forward.setTextColor(Color.BLUE);
                break;
            case 4:
                back.setText("校准完成");
                back.setTextColor(Color.BLUE);
                break;
            case 5:
                up.setText("校准完成");
                up.setTextColor(Color.BLUE);
                break;
            case 6:
                down.setText("校准完成");
                down.setTextColor(Color.BLUE);
                break;
        }

        Log.d(TAG, "meanSample[0]\t" + Sample[0]);
        Log.d(TAG, "meanSample[1]\t" + Sample[1]);
        Log.d(TAG, "meanSample[2]\t" + Sample[2]);

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
        x_K = 2 * G / (xP[0] - xI[0]);
        y_K = 2 * G / (yP[1] - yI[1]);
        z_K = 2 * G / (zP[2] - zI[2]);

        x_B = 2 * G * (xP[0] + xI[0]) / (xP[0] - xI[0]);
        y_B = 2 * G * (yP[1] + yI[1]) / (yP[1] - yI[1]);
        z_B = 2 * G * (zP[2] + zI[2]) / (zP[2] - zI[2]);

        Log.d(TAG,"x_k\t"+x_K);
        Log.d(TAG,"y_K\t"+y_K);
        Log.d(TAG,"z_k\t"+z_K);
        Log.d(TAG,"x_B\t"+x_B);
        Log.d(TAG,"y_B\t"+y_B);
        Log.d(TAG,"z_B\t"+z_B);

        String out = new String();
        out += x_K+"\t";
        out += y_K+"\t";
        out += z_K+"\t";
        out += x_B+"\t";
        out += y_B+"\t";
        out += z_B;

        File accParams = outputFile.getParamsFile();
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
        sensorListener = new TrackSensorListener((AppResourceApplication) getApplicationContext());
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

    public void delay( int delay, final int orientation) { // S
        Log.d(TAG,"delay\t"+orientation);
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
