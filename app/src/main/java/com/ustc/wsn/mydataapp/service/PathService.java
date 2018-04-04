package com.ustc.wsn.mydataapp.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.ustc.wsn.mydataapp.Listenter.TrackSensorListener;
import com.ustc.wsn.mydataapp.activity.ChartingDemoActivity;
import com.ustc.wsn.mydataapp.bean.outputFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class PathService extends Service {
    private final String TAG = ChartingDemoActivity.class.toString();
    private TrackSensorListener sensorListener;
    private SensorManager sm;
    private boolean ACCELERATOR_EXIST = false;
    private boolean GYROSCROPE_EXIST = false;
    private boolean MAGNETIC_EXIST = false;
    private Sensor accelerator;
    private Sensor gyroscrope;
    private Sensor magnetic;
    private Toast t;

    private boolean threadDisable = false;

    private float[][] Path;

    //path文件
    private File pathFile;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        pathFile = outputFile.getPathFile();

        initSensor();

        if(ACCELERATOR_EXIST&&GYROSCROPE_EXIST&&MAGNETIC_EXIST) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BufferedWriter writer = null;
                    try {
                        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathFile, true)));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    while (!threadDisable) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(sensorListener.ifNewPath()) {
                            sensorListener.NewPath = false;
                            StringBuffer PathBuffer = sensorListener.getPositionString();
                            try {
                                Log.d(TAG, "Service path write");
                                writer.write(PathBuffer.toString());
                                writer.flush();
                            } catch (IOException e4) {
                                e4.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        }
    }

    @SuppressLint("InlinedApi")
    public void initSensor() {
        Log.d("Sensor", "InitSensor Over");
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerator = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        float accMax = accelerator.getMaximumRange();
        Log.d(TAG,"accMaxRange\t"+accMax);
        if (accelerator != null) {
            ACCELERATOR_EXIST = true;
        } else {
            t = Toast.makeText(this, "您的手机不支持加速度计", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }
        gyroscrope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        float gyroMax = gyroscrope.getMaximumRange();
        Log.d(TAG,"gyroMaxRange\t"+gyroMax);
        if (gyroscrope != null) {
            GYROSCROPE_EXIST = true;
        } else {
            t = Toast.makeText(this, "您的手机不支持陀螺仪", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }
        magnetic = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        float magMax = magnetic.getMaximumRange();
        Log.d(TAG,"magMaxRange\t"+magMax);
        if (magnetic != null) {
            MAGNETIC_EXIST = true;
        } else {
            t = Toast.makeText(this, "您的手机不支持电子罗盘", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }
        sensorListener = new TrackSensorListener(accMax,gyroMax,magMax,true);
        if (ACCELERATOR_EXIST) {
            sm.registerListener(sensorListener, accelerator,SensorManager.SENSOR_DELAY_GAME );
        }
        if (GYROSCROPE_EXIST) {
            sm.registerListener(sensorListener, gyroscrope, SensorManager.SENSOR_DELAY_GAME );
        }
        if (MAGNETIC_EXIST) {
            sm.registerListener(sensorListener, magnetic, SensorManager.SENSOR_DELAY_GAME );
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        threadDisable = true;
        sensorListener.closeSensorThread();
        sm.unregisterListener(sensorListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
