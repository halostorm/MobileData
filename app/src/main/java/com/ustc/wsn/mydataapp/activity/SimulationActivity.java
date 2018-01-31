package com.ustc.wsn.mydataapp.activity;

/**
 * Created by halo on 2018/1/17.
 */

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.GraphicalView;

import com.ustc.wsn.mydataapp.Application.AppResourceApplication;
import com.ustc.wsn.mydataapp.detectorservice.DetectorSensorListener;
import com.ustc.wsn.mydataapp.detectorservice.TrackSensorListener;
import com.ustc.wsn.mydataapp.service.ChartService;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import detector.wsn.ustc.com.mydataapp.R;

public class SimulationActivity extends Activity {

    public int FRAME_TYPE = 0;//0 - phone frame/ 1 - inertial frame
    private LinearLayout accCurveLayout;//存放左图表的布局容器
    private LinearLayout linearaccCurveLayout;//存放右图表的布局容器
    private LinearLayout gyroCurveLayout;//存放右图表的布局容器
    private LinearLayout magCurveLayout;//存放右图表的布局容器
    private GraphicalView accView, gyroView, linearaccView, magView;//左右图表
    private ChartService accService, linearService, gyroSeivice, magService;
    private Toast t;
    private Timer timer1;
    private Timer timer2;
    private SensorManager sm;
    private Sensor accelerator;
    private Sensor gyroscrope;
    private Sensor magnetic;
    private DetectorSensorListener sensorListener;
    private TrackSensorListener sensorListener1;
    private float[] LinearAccData;
    private float[] AccData;
    private float[] GyroData;
    private float[] MagData;

    private TextView accxAxis;
    private TextView accyAxis;
    private TextView acczAxis;

    private TextView linearxAxis;
    private TextView linearyAxis;
    private TextView linearzAxis;

    private TextView gyroxAxis;
    private TextView gyroyAxis;
    private TextView gyrozAxis;

    private TextView magxAxis;
    private TextView magyAxis;
    private TextView magzAxis;

    private DecimalFormat df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation);

        linearaccCurveLayout = (LinearLayout) findViewById(R.id.linear_acc_curve);
        accCurveLayout = (LinearLayout) findViewById(R.id.acc_curve);
        gyroCurveLayout = (LinearLayout) findViewById(R.id.gyro_curve);
        magCurveLayout = (LinearLayout) findViewById(R.id.mag_curve);

        accService = new ChartService(this);
        accService.setXYMultipleSeriesDataset("AccX", "AccY", "AccZ");
        accService.setXYMultipleSeriesRenderer(0, 10, -20, 30, "加速度", "时间 /s", "m2/s", Color.BLACK, Color.BLACK, Color.BLUE, Color.CYAN, Color.RED, Color.BLACK);
        accView = accService.getGraphicalView();

        linearService = new ChartService(this);
        linearService.setXYMultipleSeriesDataset("LinearAccX", "LinearAccY", "LinearAccZ");
        linearService.setXYMultipleSeriesRenderer(0, 10, -10, 10, "线性加速度", "时间 /s", "m2/", Color.BLACK, Color.BLACK, Color.BLUE, Color.CYAN, Color.RED, Color.BLACK);
        linearaccView = linearService.getGraphicalView();

        gyroSeivice = new ChartService(this);
        gyroSeivice.setXYMultipleSeriesDataset("GyroX", "GyroY", "GyroZ");
        gyroSeivice.setXYMultipleSeriesRenderer(0, 10, -10, 10, "陀螺仪", "时间 /s", "rad/s", Color.BLACK, Color.BLACK, Color.BLUE, Color.CYAN, Color.RED, Color.BLACK);
        gyroView = gyroSeivice.getGraphicalView();

        magService = new ChartService(this);
        magService.setXYMultipleSeriesDataset("MagX", "MagY", "MagZ");
        magService.setXYMultipleSeriesRenderer(0, 10, -70, 70, "磁力计", "时间 /s", "uT", Color.BLACK, Color.BLACK, Color.BLUE, Color.CYAN, Color.RED, Color.BLACK);
        magView = magService.getGraphicalView();
        //将左右图表添加到布局容器中
        accCurveLayout.addView(accView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        linearaccCurveLayout.addView(linearaccView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        gyroCurveLayout.addView(gyroView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        magCurveLayout.addView(magView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        accxAxis = (TextView) findViewById(R.id.value_accx_axis);
        accyAxis = (TextView) findViewById(R.id.value_accy_axis);
        acczAxis = (TextView) findViewById(R.id.value_accz_axis);

        linearxAxis = (TextView) findViewById(R.id.value_linearx_axis);
        linearyAxis = (TextView) findViewById(R.id.value_lineary_axis);
        linearzAxis = (TextView) findViewById(R.id.value_linearz_axis);

        gyroxAxis = (TextView) findViewById(R.id.value_gyrox_axis);
        gyroyAxis = (TextView) findViewById(R.id.value_gyroy_axis);
        gyrozAxis = (TextView) findViewById(R.id.value_gyroz_axis);

        magxAxis = (TextView) findViewById(R.id.value_magx_axis);
        magyAxis = (TextView) findViewById(R.id.value_magy_axis);
        magzAxis = (TextView) findViewById(R.id.value_magz_axis);

        df = new DecimalFormat("0.00");

        initSensor();
        LinearAccData = new float[3];
        timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                handler1.sendMessage(handler1.obtainMessage());
            }
        }, 0, 25);

        timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                handler2.sendMessage(handler2.obtainMessage());
            }
        }, 5, 100);

    }

    private void showHelpDialog() {
        Dialog helpDialog = new Dialog(this);
        helpDialog.setCancelable(true);
        helpDialog.setCanceledOnTouchOutside(true);

        helpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        helpDialog.setContentView(getLayoutInflater().inflate(R.layout.help, null));

        helpDialog.show();
    }

    private void chooseFrameType() {
        Dialog FrameChooseDialog = new Dialog(this);
        FrameChooseDialog.setCancelable(true);
        FrameChooseDialog.setCanceledOnTouchOutside(true);

        FrameChooseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        FrameChooseDialog.setContentView(getLayoutInflater().inflate(R.layout.frame_choose, null));

        FrameChooseDialog.show();
        /*
        setContentView(R.layout.frame_choose);
        RadioGroup chooseFrame = (RadioGroup) findViewById(R.id.chooseFrame);

        chooseFrame.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup chooseFrame, int checkedId) {
                RadioButton label = (RadioButton) findViewById(checkedId);
                switch (checkedId) {
                    case R.id.btnPhoneFrame:
                        FRAME_TYPE = 0;
                        t = Toast.makeText(getApplicationContext(), "已切换至:" + label.getText()+"视图", Toast.LENGTH_LONG);
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.show();
                        break;
                    case R.id.btnInertialFrame:
                        FRAME_TYPE = 1;
                        t = Toast.makeText(getApplicationContext(), "已切换至:" + label.getText()+"视图", Toast.LENGTH_LONG);
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.show();
                        break;
                }
            }
        });
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.simulation_setting, menu);
        //setIconEnable(menu,true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings_help:
                showHelpDialog();
                return true;
            case R.id.frameType:
                chooseFrameType();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Handler handler1 = new Handler() {
        @Override
        //定时更新图表
        public void handleMessage(Message msg) {
            LinearAccData = sensorListener1.readLinearAccData(FRAME_TYPE);
            AccData = sensorListener1.readAccData(FRAME_TYPE);
            GyroData = sensorListener1.readGyroData(FRAME_TYPE);
            MagData = sensorListener1.readMagData(FRAME_TYPE);

            accService.rightUpdateChart(AccData[0], AccData[1], AccData[2]);
            gyroSeivice.rightUpdateChart(GyroData[0], GyroData[1], GyroData[2]);
            magService.rightUpdateChart(MagData[0], MagData[1], MagData[2]);
            linearService.rightUpdateChart(LinearAccData[0], LinearAccData[1], LinearAccData[2]);
        }
    };

    private Handler handler2 = new Handler() {
        @Override
        //定时更新图表
        public void handleMessage(Message msg) {
            accxAxis.setText(df.format(AccData[0]));
            accyAxis.setText(df.format(AccData[1]));
            acczAxis.setText(df.format(AccData[2]));

            linearxAxis.setText(df.format(LinearAccData[0]));
            linearyAxis.setText(df.format(LinearAccData[1]));
            linearzAxis.setText(df.format(LinearAccData[2]));

            gyroxAxis.setText(df.format(GyroData[0]));
            gyroyAxis.setText(df.format(GyroData[1]));
            gyrozAxis.setText(df.format(GyroData[2]));

            magxAxis.setText(df.format(MagData[0]));
            magyAxis.setText(df.format(MagData[1]));
            magzAxis.setText(df.format(MagData[2]));
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
        sensorListener1 = new TrackSensorListener((AppResourceApplication) getApplicationContext());
        sm.registerListener(sensorListener1, accelerator, SensorManager.SENSOR_DELAY_FASTEST);
        sm.registerListener(sensorListener1, gyroscrope, SensorManager.SENSOR_DELAY_FASTEST);
        sm.registerListener(sensorListener1, magnetic, SensorManager.SENSOR_DELAY_FASTEST);
        //sm.registerListener(sensorListener,rotation,SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer1 != null) {
            timer1.cancel();
        }
        if (timer2 != null) {
            timer2.cancel();
        }
        sensorListener.closeSensorThread();
        sm.unregisterListener(sensorListener);
    }

}