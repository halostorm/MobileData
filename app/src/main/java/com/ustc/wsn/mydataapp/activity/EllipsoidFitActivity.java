package com.ustc.wsn.mydataapp.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ustc.wsn.mydataapp.Listenter.TrackSensorListener;
import com.ustc.wsn.mydataapp.R;
import com.ustc.wsn.mydataapp.bean.ellipsoidFit.EllipsoidFit;
import com.ustc.wsn.mydataapp.bean.ellipsoidFit.ThreeSpacePoint;
import com.ustc.wsn.mydataapp.bean.outputFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class EllipsoidFitActivity extends Activity implements View.OnClickListener {
    private final String TAG = EllipsoidFitActivity.class.toString();
    private boolean ACCELERATOR_EXIST = false;
    private boolean GYROSCROPE_EXIST = false;
    private boolean MAGNETIC_EXIST = false;

    private Toast t;
    private SensorManager sm;
    private Sensor accelerator;
    private Sensor gyroscrope;
    private Sensor magnetic;
    private TrackSensorListener sensorListener;

    private boolean threadDisable_data_update = false;
    private boolean ifCollect = false;
    private boolean ifFit = false;

    private Button start;
    private Button stop;
    private Button save;

    private EllipsoidFit fit;
    private float[] params = new float[12];//系数矩阵-9 + 偏移向量-3 = 12
    private ArrayList<ThreeSpacePoint> sample = new ArrayList<ThreeSpacePoint>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ellipsoid_fit);
        start = (Button) findViewById(R.id.btnStartEllipsoidFit);
        start.setOnClickListener(this);

        stop = (Button) findViewById(R.id.btnStopEllipsoidFit);
        stop.setOnClickListener(this);

        save = (Button) findViewById(R.id.btnSaveEllipsoidFit);
        save.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnStartEllipsoidFit:
                if (!ifCollect) {
                    start.setTextColor(Color.BLUE);
                    collectSample();
                    ifCollect = true;
                }
                break;
            case R.id.btnStopEllipsoidFit:
                if (ifCollect) {
                    try {
                        start.setTextColor(Color.BLACK);
                        threadDisable_data_update = true;
                        fit = new EllipsoidFit(sample);
                        ifFit = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btnSaveEllipsoidFit:
                if (ifFit) {
                    params = fit.getParams();
                    String out = "";
                    for (int i = 0; i < params.length; i++) {
                        out += params[i] + "\t";
                    }
                    File accParams = outputFile.getAccParamsFile();
                    try {
                        FileWriter writer = new FileWriter(accParams);
                        writer.write(out);
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void collectSample() {
        initSensor();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!threadDisable_data_update) {
                    try {
                        Thread.sleep(sensorListener.sampleIntervalReal);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    float[] acc = sensorListener.readRawAccData(0);
                    ThreeSpacePoint tsp = new ThreeSpacePoint(acc[0], acc[1], acc[2]);
                    sample.add(tsp);
                }
            }
        }).start();
    }

    @SuppressLint("InlinedApi")
    public void initSensor() {
        Log.d("Sensor", "InitSensor Over");
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerator = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        float accMax = accelerator.getMaximumRange();
        Log.d(TAG, "accMaxRange\t" + accMax);
        if (accelerator != null) {
            ACCELERATOR_EXIST = true;
        } else {
            t = Toast.makeText(this, "您的手机不支持加速度计", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }
        gyroscrope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        float gyroMax = gyroscrope.getMaximumRange();
        Log.d(TAG, "gyroMaxRange\t" + gyroMax);
        if (gyroscrope != null) {
            GYROSCROPE_EXIST = true;
        } else {
            t = Toast.makeText(this, "您的手机不支持陀螺仪", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }
        magnetic = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        float magMax = magnetic.getMaximumRange();
        Log.d(TAG, "magMaxRange\t" + magMax);
        if (magnetic != null) {
            MAGNETIC_EXIST = true;
        } else {
            t = Toast.makeText(this, "您的手机不支持电子罗盘", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }
        sensorListener = new TrackSensorListener(accMax, gyroMax, magMax, false);
        if (ACCELERATOR_EXIST) {
            sm.registerListener(sensorListener, accelerator, SensorManager.SENSOR_DELAY_GAME);
        }
        if (GYROSCROPE_EXIST) {
            sm.registerListener(sensorListener, gyroscrope, SensorManager.SENSOR_DELAY_GAME);
        }
        if (MAGNETIC_EXIST) {
            sm.registerListener(sensorListener, magnetic, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        threadDisable_data_update = true;
        if (sensorListener != null) {
            sensorListener.closeSensorThread();
            sm.unregisterListener(sensorListener);
        }
    }

    @Override
    public void onStop(){
        // TODO Auto-generated method stub
        super.onStop();
        onDestroy();
    }
}
