package com.nulana.nchart3d.example.DifferentCharts;

/**
 * Created by halo on 2018/1/17.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.nulana.NChart.NChartView;
import com.ustc.wsn.mydataapp.Application.AppResourceApplication;
import com.ustc.wsn.mydataapp.detectorservice.TrackSensorListener;
import com.ustc.wsn.mydataapp.service.TrackService;

import detector.wsn.ustc.com.mydataapp.R;
public class ChartingDemoActivity extends Activity {
    NChartView mNChartView;
    TrackService track;
    private boolean threadDisable = false;
    private TrackSensorListener sensorListener;
    private SensorManager sm;
    private boolean ACCELERATOR_EXIST = false;
    private boolean GYROSCROPE_EXIST = false;
    private boolean MAGNETIC_EXIST = false;

    private Sensor accelerator;
    private Sensor gyroscrope;
    private Sensor magnetic;
    private Toast t;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.nchart);
        mNChartView = (NChartView) findViewById(R.id.surface);
        initSensor();
        loadView();
    }


    private void loadView() {
        // Paste your license key here.
        mNChartView.getChart().setLicenseKey("bWCo+E65fg+Cjg+0M0BAWhfIORkIsDwDBIO5mODAznWdtIQHirZztXtFaRWLwUiALjmPjEv/oyXerwe3dnDCAiTAO/IFiddoYA3ljKOvgx58NfwdUXXNgSmGiAKvetyNlWs6s3vFvFKc/OsdUk7uzc5WpKQcWFNbYGdJJ3cFNHSmeF2KvSDjJL4YaJhvkFoAQ96igwBEbgexORYX5vpVIlibW/F6Kr2oVcCQ3Wb7S9d4XkvkvD8kqIa6bRcnhu4U+Ky/zJ07B/ohuGE0EMGogozgRitI5Am6ZFNb8LwZwJXaekeZLar8+tG+GajUn7+X0CShuTEIZUxfs1IFEGz8aauu5ki/5HY+sDKufs745/jeqYDL4d/lxYEFSkniDSvUUa2rd3x6WBxciXG65Pr8jIDZYPjtrvvc/D7F1eEzp+53os/wBGxSs8FRfWXRqQQjNjeVHTbYRaVkaFTAvXeGWvKJfiYyZQt5OJgq5rIdXZKJh+/JdN8TaYRkZTDnoj8cX8gs4KYDrnvgN+Yp34FdTKBgHA0IGn31KaKN6MFapNypo9rRTlIhPOKeVmuieormClpgzxegrfjHE0uAcNdSpEUhH1O42RU33/XbjkQkYNm0YvTgF94B9eIkLpb4vC7xseYHTN8J/DPudE9ZOMMUgJJP2HCXgskm6UgyyS42Nho=");

        track = new TrackService(true, mNChartView, this,40);
        track.initView();
        track.updateData();
        new Thread(new Runnable() {
            @Override
            public void run() {
                float[][] position;
                while (!threadDisable) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    position = sensorListener.getPosition();
                    if (position != null) {
                        track.setPosition(position);
                        track.updateData();
                    }
                }
            }
        }).start();
    }

    @SuppressLint("InlinedApi")
    public void initSensor() {
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
        gyroscrope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscrope != null) {
            GYROSCROPE_EXIST = true;
        } else {
            t = Toast.makeText(this, "您的手机不支持陀螺仪", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }
        magnetic = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magnetic != null) {
            MAGNETIC_EXIST = true;
        } else {
            t = Toast.makeText(this, "您的手机不支持电子罗盘", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }
        //rotation = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorListener = new TrackSensorListener((AppResourceApplication) getApplicationContext());
        if (ACCELERATOR_EXIST) {
            sm.registerListener(sensorListener, accelerator, SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (GYROSCROPE_EXIST) {
            sm.registerListener(sensorListener, gyroscrope, SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (MAGNETIC_EXIST) {
            sm.registerListener(sensorListener, magnetic, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        threadDisable = true;
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
}
