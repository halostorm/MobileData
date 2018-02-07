package com.ustc.wsn.mydataapp.activity;
/**
 * Created by halo on 2018/1/17.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.animation.RotateAnimation;
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
    private int WindowSize;
    private int sampleInterval;
    private Sensor accelerator;
    private Sensor gyroscrope;
    private Sensor magnetic;
    private Toast t;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.nchart);
        mNChartView = (NChartView) findViewById(R.id.surface);
        initSensor();
        WindowSize = sensorListener.windowSize*sensorListener.DurationWindow;
        sampleInterval = sensorListener.sampleInterval;
        loadView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.path_params_setting, menu);
        //setIconEnable(menu,true);
        return true;
    }

    private void showHelpDialog() {
        Dialog helpDialog = new Dialog(this);
        helpDialog.setCancelable(true);
        helpDialog.setCanceledOnTouchOutside(true);

        helpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        helpDialog.setContentView(getLayoutInflater().inflate(R.layout.help, null));

        helpDialog.show();
    }

    private void calibrate_state(){
        Intent intent = new Intent(this,CalibrateStateActivity.class);
        startActivity(intent);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings_help:
                showHelpDialog();
                return true;
            case R.id.calibrate_state:
                calibrate_state();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadView() {
        // Paste your license key here.
        mNChartView.getChart().setLicenseKey("bWCo+E65fg+Cjg+0M0BAWhfIORkIsDwDBIO5mODAznWdtIQHirZztXtFaRWLwUiALjmPjEv/oyXerwe3dnDCAiTAO/IFiddoYA3ljKOvgx58NfwdUXXNgSmGiAKvetyNlWs6s3vFvFKc/OsdUk7uzc5WpKQcWFNbYGdJJ3cFNHSmeF2KvSDjJL4YaJhvkFoAQ96igwBEbgexORYX5vpVIlibW/F6Kr2oVcCQ3Wb7S9d4XkvkvD8kqIa6bRcnhu4U+Ky/zJ07B/ohuGE0EMGogozgRitI5Am6ZFNb8LwZwJXaekeZLar8+tG+GajUn7+X0CShuTEIZUxfs1IFEGz8aauu5ki/5HY+sDKufs745/jeqYDL4d/lxYEFSkniDSvUUa2rd3x6WBxciXG65Pr8jIDZYPjtrvvc/D7F1eEzp+53os/wBGxSs8FRfWXRqQQjNjeVHTbYRaVkaFTAvXeGWvKJfiYyZQt5OJgq5rIdXZKJh+/JdN8TaYRkZTDnoj8cX8gs4KYDrnvgN+Yp34FdTKBgHA0IGn31KaKN6MFapNypo9rRTlIhPOKeVmuieormClpgzxegrfjHE0uAcNdSpEUhH1O42RU33/XbjkQkYNm0YvTgF94B9eIkLpb4vC7xseYHTN8J/DPudE9ZOMMUgJJP2HCXgskm6UgyyS42Nho=");

        track = new TrackService(true, mNChartView, this, WindowSize);
        track.initView(0);
        track.updateData(0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                float[][] position;
                int i = 1;
                while (!threadDisable) {
                    try {
                        Thread.sleep(WindowSize*sampleInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    position = sensorListener.getPosition();
                    if(position!=null) {
                        track.setPosition(position);
                        track.updateData(i);
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
            sm.registerListener(sensorListener, magnetic, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        threadDisable = true;
        if(track!=null){
            track.stopSelf();
        }
        sensorListener.closeSensorThread();
        sm.unregisterListener(sensorListener);
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
