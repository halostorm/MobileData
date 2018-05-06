package com.ustc.wsn.mobileData.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ustc.wsn.mobileData.Listenter.TrackSensorListener;
import com.ustc.wsn.mobileData.R;
import com.ustc.wsn.mobileData.bean.PhoneState;
import com.ustc.wsn.mobileData.bean.cubeView.MyRender;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class AttitudeViewActivity extends Activity implements View.OnClickListener {

    private final String TAG = AttitudeViewActivity.class.toString();

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

    private RelativeLayout attLayout;
    MyRender myRender;

    private Button EKF;
    private Button FCF;
    private Button GDF;
    private Button ANDROID;
    private Button GYRO;

    TextView EulerxAxis;
    TextView EuleryAxis;
    TextView EulerzAxis;

    TextView Q0Axis;
    TextView Q1yAxis;
    TextView Q2Axis;
    TextView Q3Axis;

    private float[] Euler = {0, 0, 0};

    private float[] Q = {0, 0, 0,0};

    private DecimalFormat df = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attitude_view);
        attLayout = (RelativeLayout) findViewById(R.id.attView);
        initSensor();
        GLSurfaceView glView = new GLSurfaceView(this);

        glView.setBackgroundColor(Color.argb(40,255,255, 255));
        myRender = new MyRender();
        glView.setRenderer(myRender);
        attLayout.addView(glView);

        EKF = (Button) findViewById(R.id.btnEKF);
        EKF.setOnClickListener(this);
        EKF.setTextColor(Color.BLUE);

        FCF = (Button) findViewById(R.id.btnFCF);
        FCF.setOnClickListener(this);

        GDF = (Button) findViewById(R.id.btnGDF);
        GDF.setOnClickListener(this);

        ANDROID = (Button) findViewById(R.id.btnAndroid);
        ANDROID.setOnClickListener(this);

        GYRO = (Button) findViewById(R.id.btnGyro);
        GYRO.setOnClickListener(this);

        EulerxAxis = (TextView) findViewById(R.id.value_x);
        EuleryAxis = (TextView) findViewById(R.id.value_y);
        EulerzAxis = (TextView) findViewById(R.id.value_z);

        Q0Axis = (TextView) findViewById(R.id.Q0Value);
        Q1yAxis = (TextView) findViewById(R.id.Q1Value);
        Q2Axis = (TextView) findViewById(R.id.Q2Value);
        Q3Axis = (TextView) findViewById(R.id.Q3Value);

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
                    Euler = sensorListener.readEuler();
                    Q = sensorListener.readQ();
                    myRender.updateEuler(Euler);
                }
            }
        }).start();

        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendMessage(handler.obtainMessage());
            }
        }, 0, 50);

    }

    private Handler handler = new Handler() {
        @Override
        //定时更新图表
        public void handleMessage(Message msg) {
            EulerxAxis.setText(String.valueOf((int) (Euler[0] / Math.PI * 180)));
            EuleryAxis.setText(String.valueOf((int) (Euler[1] / Math.PI * 180)));
            EulerzAxis.setText(String.valueOf((int) (Euler[2] / Math.PI * 180)));

            Q0Axis.setText(df.format(Q[0]));
            Q1yAxis.setText(df.format(Q[1]));
            Q2Axis.setText(df.format(Q[2]));
            Q3Axis.setText(df.format(Q[3]));
        }
    };

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
        sensorListener = new TrackSensorListener(accMax, gyroMax, magMax, false,false);
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
