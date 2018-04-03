package com.ustc.wsn.mydataapp.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ustc.wsn.mydataapp.Listenter.TrackSensorListener;
import com.ustc.wsn.mydataapp.R;
import com.ustc.wsn.mydataapp.bean.PhoneState;
import com.ustc.wsn.mydataapp.bean.cubeView.MyRender;

public class AttitudeViewActivity extends Activity implements View.OnClickListener {

    private final String TAG = AttitudeViewActivity.class.toString();

    private LinearLayout attLayout;

    private boolean threadDisable_data_update = false;
    private boolean ACCELERATOR_EXIST = false;
    private boolean GYROSCROPE_EXIST = false;
    private boolean MAGNETIC_EXIST = false;
    private Toast t;
    private SensorManager sm;
    private Sensor accelerator;
    private Sensor gyroscrope;
    private Sensor magnetic;
    private TrackSensorListener sensorListener;

    MyRender myRender;

    private Button EKF;
    private Button FCF;
    private Button GDF;
    private Button ANDROID;
    private Button GYRO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attitude_view);
        attLayout = (LinearLayout) findViewById(R.id.attView);
        initSensor();
        GLSurfaceView glView = new GLSurfaceView(this);
        myRender = new MyRender();
        glView.setRenderer(myRender);
        //glView.setRenderMode(RENDERMODE_WHEN_DIRTY);
        attLayout.addView(glView);

        EKF = (Button) findViewById(R.id.btnEKF);
        EKF.setOnClickListener(this);

        FCF = (Button) findViewById(R.id.btnFCF);
        FCF.setOnClickListener(this);

        GDF = (Button) findViewById(R.id.btnGDF);
        GDF.setOnClickListener(this);

        ANDROID = (Button) findViewById(R.id.btnAndroid);
        ANDROID.setOnClickListener(this);

        GYRO = (Button) findViewById(R.id.btnGyro);
        GYRO.setOnClickListener(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!threadDisable_data_update) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // do
                    myRender.updateEuler(sensorListener.readEuler());
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
        sensorListener = new TrackSensorListener(false);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEKF:
                sensorListener.setAttitudeMode(PhoneState.Attitude_EKF);
                EKF.setTextColor(Color.BLUE);
                //EKF.setTextColor(Color.GRAY);
                FCF.setTextColor(Color.GRAY);
                GDF.setTextColor(Color.GRAY);
                ANDROID.setTextColor(Color.GRAY);
                GYRO.setTextColor(Color.GRAY);
                break;
            case R.id.btnFCF:
                sensorListener.setAttitudeMode(PhoneState.Attitude_FCF);
                FCF.setTextColor(Color.BLUE);
                EKF.setTextColor(Color.GRAY);
                GDF.setTextColor(Color.GRAY);
                ANDROID.setTextColor(Color.GRAY);
                GYRO.setTextColor(Color.GRAY);
                break;
            case R.id.btnGDF:
                sensorListener.setAttitudeMode(PhoneState.Attitude_GDF);
                GDF.setTextColor(Color.BLUE);
                EKF.setTextColor(Color.GRAY);
                FCF.setTextColor(Color.GRAY);
                ANDROID.setTextColor(Color.GRAY);
                GYRO.setTextColor(Color.GRAY);
                break;
            case R.id.btnAndroid:
                sensorListener.setAttitudeMode(PhoneState.Attitude_ANDROID);
                ANDROID.setTextColor(Color.BLUE);
                EKF.setTextColor(Color.GRAY);
                FCF.setTextColor(Color.GRAY);
                GDF.setTextColor(Color.GRAY);
                GYRO.setTextColor(Color.GRAY);
                break;
            case R.id.btnGyro:
                sensorListener.setAttitudeMode(PhoneState.Attitude_GYRO);
                GYRO.setTextColor(Color.BLUE);
                EKF.setTextColor(Color.GRAY);
                FCF.setTextColor(Color.GRAY);
                GDF.setTextColor(Color.GRAY);
                ANDROID.setTextColor(Color.GRAY);
                break;
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        threadDisable_data_update = true;
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
