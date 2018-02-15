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

        EditText editTextAccMean_Ab = (EditText) findViewById(R.id.accMean_Ab);

        EditText editTextAccVar_Ab = (EditText) findViewById(R.id.accVar_Ab);

        EditText editTextAccMean_User = (EditText) findViewById(R.id.accMean_User);

        EditText editTextAccVar_User = (EditText) findViewById(R.id.accVar_User);

        editTextAccMean_Ab.setHint("请输入绝对静止-加速度均值阈值（当前值：" + PhoneState.ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD + "）");
        editTextAccVar_Ab.setHint("请输入绝对静止-加速度方差阈值（当前值：" + PhoneState.ACC_VAR_ABSOLUTE_STATIC_THRESHOLD + ")");
        editTextAccMean_User.setHint("请输入相对静止-加速度均值阈值（当前值：" + PhoneState.ACC_MEAN_STATIC_THRESHOLD + "）");
        editTextAccVar_User.setHint("请输入相对静止-加速度方差阈值(当前值：" + PhoneState.ACC_VAR_STATIC_THRESHOLD + "）");

        TextView confirmText = (TextView) findViewById(R.id.btnconfirmParams);

        confirmText.setTag(R.id.key1, editTextAccMean_Ab);
        confirmText.setTag(R.id.key2, editTextAccVar_Ab);
        confirmText.setTag(R.id.key3, editTextAccMean_User);
        confirmText.setTag(R.id.key4, editTextAccVar_User);

        confirmText.setOnClickListener(cOnClickListener);

        TextView initParams = (TextView) findViewById(R.id.btninitParams);

        initParams.setTag(R.id.key1, editTextAccMean_Ab);
        initParams.setTag(R.id.key2, editTextAccVar_Ab);
        initParams.setTag(R.id.key3, editTextAccMean_User);
        initParams.setTag(R.id.key4, editTextAccVar_User);

        initParams.setOnClickListener(mOnClickListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String accMeanAbParams = String.valueOf(PhoneState.ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD_DEFAULT);
            String accVarAbParams = String.valueOf(PhoneState.ACC_VAR_ABSOLUTE_STATIC_THRESHOLD_DEFAULT);
            String accMeanUserParams = String.valueOf(PhoneState.ACC_MEAN_STATIC_THRESHOLD_DEFAULT);
            String accVarUserParams = String.valueOf(PhoneState.ACC_VAR_STATIC_THRESHOLD_DEFAULT);

            ((EditText) view.getTag(R.id.key1)).setText(accMeanAbParams);
            ((EditText) view.getTag(R.id.key2)).setText(accVarAbParams);
            ((EditText) view.getTag(R.id.key3)).setText(accMeanUserParams);
            ((EditText) view.getTag(R.id.key4)).setText(accVarUserParams);
        }
    };

    private View.OnClickListener cOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            EditText t1 = ((EditText) view.getTag(R.id.key1));
            EditText t2 = ((EditText) view.getTag(R.id.key2));
            EditText t3 = ((EditText) view.getTag(R.id.key3));
            EditText t4 = ((EditText) view.getTag(R.id.key4));

            String accMeanAbParams = t1.getText().toString().trim();
            String accVarAbParams = t2.getText().toString().trim();
            String accMeanUserParams = t3.getText().toString().trim();
            String accVarUserParams = t4.getText().toString().trim();

            //Log.d(TAG, "params1:" + accMeanAbParams);
            //Log.d(TAG, "params2:" + accVarAbParams);
            //Log.d(TAG, "params3:" + accMeanUserParams);
            //Log.d(TAG, "params4:" + accVarUserParams);

            String out = new String();
            if (accMeanAbParams.length() == 0) {
                out += PhoneState.ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD + "\t";
            } else {
                PhoneState.ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD = Float.parseFloat(accMeanAbParams);
                out += accMeanAbParams + "\t";
            }
            if (accVarAbParams.length() == 0) {
                out += PhoneState.ACC_VAR_ABSOLUTE_STATIC_THRESHOLD + "\t";
            } else {
                PhoneState.ACC_VAR_ABSOLUTE_STATIC_THRESHOLD = Float.parseFloat(accVarAbParams);
                out += accVarAbParams + "\t";
            }
            if (accMeanUserParams.length() == 0) {
                out += PhoneState.ACC_MEAN_STATIC_THRESHOLD + "\t";
            } else {
                PhoneState.ACC_MEAN_STATIC_THRESHOLD = Float.parseFloat(accMeanUserParams);
                out += accMeanUserParams + "\t";
            }
            if (accVarUserParams.length() == 0) {
                out += PhoneState.ACC_VAR_STATIC_THRESHOLD + "\n";
            } else {
                PhoneState.ACC_VAR_STATIC_THRESHOLD = Float.parseFloat(accVarUserParams);
                out += accVarUserParams + "\n";
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

            t1.setHint("请输入绝对静止-加速度均值阈值（当前值：" + PhoneState.ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD + "）");
            t2.setHint("请输入绝对静止-加速度方差阈值（当前值：" + PhoneState.ACC_VAR_ABSOLUTE_STATIC_THRESHOLD + ")");
            t3.setHint("请输入相对静止-加速度均值阈值（当前值：" + PhoneState.ACC_MEAN_STATIC_THRESHOLD + "）");
            t4.setHint("请输入相对静止-加速度方差阈值(当前值：" + PhoneState.ACC_VAR_STATIC_THRESHOLD + "）");

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
