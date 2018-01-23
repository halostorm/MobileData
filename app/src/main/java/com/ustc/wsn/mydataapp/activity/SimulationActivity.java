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

    private LinearLayout accCurveLayout;//存放左图表的布局容器
    private LinearLayout linearaccCurveLayout;//存放右图表的布局容器
    private LinearLayout gyroCurveLayout;//存放右图表的布局容器
    private LinearLayout magCurveLayout;//存放右图表的布局容器
    private GraphicalView accView, gyroView, linearaccView, magView;//左右图表
    private ChartService accService, linearService, gyroSeivice, magService;
    private Timer timer;
    private SensorManager sm;
    private Sensor accelerator;
    private Sensor gyroscrope;
    private Sensor magnetic;
    private DetectorSensorListener sensorListener;
    private float[] LinearAccData;
    private float[] AccData;
    private float[] GyroData;
    private float[] MagData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation);

        linearaccCurveLayout = (LinearLayout) findViewById(R.id.linear_acc_curve);
        accCurveLayout = (LinearLayout) findViewById(R.id.acc_curve);
        gyroCurveLayout = (LinearLayout) findViewById(R.id.gyro_curve);
        magCurveLayout = (LinearLayout) findViewById(R.id.mag_curve);

        accService = new ChartService(this);
        accService.setXYMultipleSeriesDataset("AccX","AccY","AccZ");
        accService.setXYMultipleSeriesRenderer(8, 30, "加速度", "时间 /s", "m2/s", Color.BLACK, Color.BLACK, Color.BLUE,Color.GREEN,Color.RED, Color.BLACK);
        accView = accService.getGraphicalView();

        linearService = new ChartService(this);
        linearService.setXYMultipleSeriesDataset("LinearAccX","LinearAccY","LinearAccZ");
        linearService.setXYMultipleSeriesRenderer(8, 20, "线性加速度", "时间 /s", "m2/", Color.BLACK, Color.BLACK, Color.BLUE,Color.GREEN,Color.RED, Color.BLACK);
        linearaccView = linearService.getGraphicalView();

        gyroSeivice = new ChartService(this);
        gyroSeivice.setXYMultipleSeriesDataset("GyroX","GyroY","GyroZ");
        gyroSeivice.setXYMultipleSeriesRenderer(8, 20, "陀螺仪", "时间 /s", "rad/s", Color.BLACK, Color.BLACK, Color.BLUE,Color.GREEN,Color.RED, Color.BLACK);
        gyroView = gyroSeivice.getGraphicalView();

        magService = new ChartService(this);
        magService.setXYMultipleSeriesDataset("MagX","MagY","MagZ");
        magService.setXYMultipleSeriesRenderer(8, 50, "磁力计", "时间 /s", "uT", Color.BLACK, Color.BLACK, Color.BLUE,Color.GREEN,Color.RED, Color.BLACK);
        magView = magService.getGraphicalView();
        //将左右图表添加到布局容器中
        accCurveLayout.addView(accView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        linearaccCurveLayout.addView(linearaccView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        gyroCurveLayout.addView(gyroView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        magCurveLayout.addView(magView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        initSensor();
        LinearAccData = new float[3];
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendMessage(handler.obtainMessage());
            }
        }, 5, 40);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_detector, menu);
        return true;
    }

    private Handler handler = new Handler() {
        @Override
        //定时更新图表
        public void handleMessage(Message msg) {
            LinearAccData = sensorListener.readLinearAccData();
            AccData = sensorListener.readAccData();
            GyroData = sensorListener.readGyroData();
            MagData = sensorListener.readMagData();

            accService.rightUpdateChart(AccData[0],AccData[1],AccData[2]);
            gyroSeivice.rightUpdateChart(GyroData[0],GyroData[1],GyroData[2]);
            magService.rightUpdateChart(MagData[0],MagData[1],MagData[2]);
            linearService.rightUpdateChart(LinearAccData[0],LinearAccData[1],LinearAccData[2]);
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
        sensorListener.closeSensorThread();
        sm.unregisterListener(sensorListener);
    }

}