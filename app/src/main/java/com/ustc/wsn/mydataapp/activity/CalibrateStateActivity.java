package com.ustc.wsn.mydataapp.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ustc.wsn.mydataapp.Application.AppResourceApplication;
import com.ustc.wsn.mydataapp.bean.PhoneState;
import com.ustc.wsn.mydataapp.detectorservice.TrackSensorListener;
import com.ustc.wsn.mydataapp.detectorservice.outputFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Timer;
import java.util.TimerTask;

import detector.wsn.ustc.com.mydataapp.R;

public class CalibrateStateActivity extends Activity {
    protected final String TAG = CalibrateStateActivity.this.toString();
    private boolean ACCELERATOR_EXIST = false;
    private boolean GYROSCROPE_EXIST = false;
    private boolean MAGNETIC_EXIST = false;
    private SensorManager sm;
    private Sensor accelerator;
    private Sensor gyroscrope;
    private Sensor magnetic;
    private Toast t;
    private String psw;
    private TextView stateValue;
    private TrackSensorListener sensorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate_state);
        initSensor();
        stateValue = (TextView) findViewById(R.id.state_value);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendMessage(handler.obtainMessage());
            }
        }, 0, 500);

        EditText editTextAcc_Ab = (EditText) findViewById(R.id.acc_Ab);

        EditText editTextGyro_Ab = (EditText) findViewById(R.id.gyro_Ab);

        EditText editTextAcc_User = (EditText) findViewById(R.id.acc_User);

        EditText editTextGyro_User = (EditText) findViewById(R.id.gyro_User);

        editTextAcc_Ab.setHint("请输入：绝对静止-加速度阈值，"+PhoneState.ACC_ABSOLUTE_STATIC_THRESHOLD+"（当前）");
        editTextGyro_Ab.setHint("请输入：绝对静止-角速度阈值，"+PhoneState.GYRO_ABSOLUTE_STATIC_THRESHOLD+"（当前）");
        editTextAcc_User.setHint("请输入：相对静止-加速度阈值，"+PhoneState.ACC_STATIC_THRESHOLD+"（当前）");
        editTextGyro_User.setHint("请输入：相对静止-角速度阈值，"+PhoneState.GYRO_STATIC_THRESHOLD+"（当前）");

        TextView confirmText = (TextView) findViewById(R.id.btnconfirmParams);

        confirmText.setTag(R.id.key1, editTextAcc_Ab);
        confirmText.setTag(R.id.key2, editTextGyro_Ab);
        confirmText.setTag(R.id.key3, editTextAcc_User);
        confirmText.setTag(R.id.key4, editTextGyro_User);

        confirmText.setOnClickListener(cOnClickListener);

        TextView initParams = (TextView) findViewById(R.id.btninitParams);

        initParams.setTag(R.id.key1, editTextAcc_Ab);
        initParams.setTag(R.id.key2, editTextGyro_Ab);
        initParams.setTag(R.id.key3, editTextAcc_User);
        initParams.setTag(R.id.key4, editTextGyro_User);

        initParams.setOnClickListener(mOnClickListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String accAbParams = String.valueOf(0.01);
            String gyroAbParams = String.valueOf(0.01);
            String accUserParams = String.valueOf(0.1);
            String gyroUserParams = String.valueOf(0.1);

            ((EditText) view.getTag(R.id.key1)).setText(accAbParams);
            ((EditText) view.getTag(R.id.key2)).setText(gyroAbParams);
            ((EditText) view.getTag(R.id.key3)).setText(accUserParams);
            ((EditText) view.getTag(R.id.key4)).setText(gyroUserParams);
        }
    };

    private View.OnClickListener cOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            EditText t1 = ((EditText) view.getTag(R.id.key1));
            EditText t2 = ((EditText) view.getTag(R.id.key2));
            EditText t3 = ((EditText) view.getTag(R.id.key3));
            EditText t4 = ((EditText) view.getTag(R.id.key4));

            String accAbParams = t1.getText().toString().trim();
            String gyroAbParams = t2.getText().toString().trim();
            String accUserParams = t3.getText().toString().trim();
            String gyroUserParams = t4.getText().toString().trim();

            Log.d(TAG, "params1:" + accAbParams);
            Log.d(TAG, "params2:" + gyroAbParams);
            Log.d(TAG, "params3:" + accUserParams);
            Log.d(TAG, "params4:" + gyroUserParams);

            String out = new String();
            if (accAbParams.length() == 0) {
                out += PhoneState.ACC_ABSOLUTE_STATIC_THRESHOLD + "\t";
            } else {
                PhoneState.ACC_ABSOLUTE_STATIC_THRESHOLD = Float.parseFloat(accAbParams);
                out += accAbParams + "\t";
            }
            if (gyroAbParams.length() == 0) {
                out += PhoneState.GYRO_ABSOLUTE_STATIC_THRESHOLD + "\t";
            } else {
                PhoneState.GYRO_ABSOLUTE_STATIC_THRESHOLD = Float.parseFloat(gyroAbParams);
                out += gyroAbParams + "\t";
            }
            if (accUserParams.length() == 0) {
                out += PhoneState.ACC_STATIC_THRESHOLD + "\t";
            } else {
                PhoneState.ACC_STATIC_THRESHOLD = Float.parseFloat(accUserParams);
                out += accUserParams + "\t";
            }
            if (gyroUserParams.length() == 0) {
                out += PhoneState.GYRO_STATIC_THRESHOLD + "\n";
            } else {
                PhoneState.GYRO_STATIC_THRESHOLD = Float.parseFloat(gyroUserParams);
                out += gyroUserParams + "\n";
            }

            File params = outputFile.getParamsFile();
            try {
                FileWriter writer = new FileWriter(params);
                Log.d(TAG, "come in");
                writer.write(out);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            t1.setText("");
            t2.setText("");
            t3.setText("");
            t4.setText("");

            t1.setHint("请输入：绝对静止-加速度阈值，"+PhoneState.ACC_ABSOLUTE_STATIC_THRESHOLD+"（当前）");
            t2.setHint("请输入：绝对静止-角速度阈值，"+PhoneState.GYRO_ABSOLUTE_STATIC_THRESHOLD+"（当前）");
            t3.setHint("请输入：相对静止-加速度阈值，"+PhoneState.ACC_STATIC_THRESHOLD+"（当前）");
            t4.setHint("请输入：相对静止-角速度阈值，"+PhoneState.GYRO_STATIC_THRESHOLD+"（当前）");

        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int state = sensorListener.getNowState();
            switch (state) {
                case PhoneState.ABSOLUTE_STATIC_STATE:
                    stateValue.setText("绝对静止");
                    break;
                case PhoneState.USER_STATIC_STATE:
                    stateValue.setText("相对静止");
                    break;
                case PhoneState.UNKONW_STATE:
                    stateValue.setText("用户运动");
                    break;
            }
        }
    };

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
        super.onDestroy();
        sensorListener.closeSensorThread();
        sm.unregisterListener(sensorListener);
    }

}
