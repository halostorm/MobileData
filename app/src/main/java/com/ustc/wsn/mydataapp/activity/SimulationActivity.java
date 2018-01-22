package com.ustc.wsn.mydataapp.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;

import detector.wsn.ustc.com.mydataapp.R;
/*
public class SimulationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation2);
    }

}
*/
import java.util.Timer;
import java.util.TimerTask;
import org.achartengine.GraphicalView;

import com.ustc.wsn.mydataapp.Application.AppResourceApplication;
import com.ustc.wsn.mydataapp.detectorservice.DetectorSensorListener;
import com.ustc.wsn.mydataapp.service.ChartService;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class SimulationActivity extends Activity {

    private LinearLayout mLeftCurveLayout;//存放左图表的布局容器
    private LinearLayout mRightCurveLayout;//存放右图表的布局容器
    private GraphicalView mView, mView2;//左右图表
    private ChartService mService, mService2;
    private Timer timer;
    private SensorManager sm;
    private Sensor accelerator;
    private Sensor gyroscrope;
    private Sensor magnetic;
    private DetectorSensorListener sensorListener;
    private float[] LinearAccData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation2);

        mLeftCurveLayout = (LinearLayout) findViewById(R.id.left_temperature_curve);
        mRightCurveLayout = (LinearLayout) findViewById(R.id.right_temperature_curve);

        mService = new ChartService(this);
        mService.setXYMultipleSeriesDataset("加速度曲线");
        mService.setXYMultipleSeriesRenderer(100, 100, "加速度", "时间", "m2/s", Color.BLACK, Color.BLACK, Color.BLUE, Color.BLACK);
        mView = mService.getGraphicalView();

        mService2 = new ChartService(this);
        mService2.setXYMultipleSeriesDataset("线性加速度曲线");
        mService2.setXYMultipleSeriesRenderer(100, 100, "线性加速度", "时间", "m2/", Color.BLACK, Color.BLACK, Color.BLUE, Color.BLACK);
        mView2 = mService2.getGraphicalView();

        //将左右图表添加到布局容器中
        mLeftCurveLayout.addView(mView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mRightCurveLayout.addView(mView2, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        initSensor();
        LinearAccData = new float[3];
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendMessage(handler.obtainMessage());
            }
        }, 1, 50);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detector, menu);
        return true;
    }

    private int t = 0;
    private Handler handler = new Handler() {
        @Override
        //定时更新图表
        public void handleMessage(Message msg) {
            LinearAccData = sensorListener.readAccData();
            mService.updateChart(t, Math.random() * 100);
            mService2.updateChart(t, LinearAccData[0] * 100);
            t += 5;
        }
    };

    @SuppressLint("InlinedApi")
    public void initSensor() {
        Log.d("Sensor", "InitSensor Over");
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerator = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscrope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetic = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //rotation = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorListener = new DetectorSensorListener((AppResourceApplication) getApplicationContext());
        sm.registerListener(sensorListener, accelerator, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(sensorListener, gyroscrope, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(sensorListener, magnetic, SensorManager.SENSOR_DELAY_GAME);
        //sm.registerListener(sensorListener,rotation,SensorManager.SENSOR_DELAY_GAME);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

}