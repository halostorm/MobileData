package com.ustc.wsn.mobileData.activity;
/**
 * Created by halo on 2018/1/17.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nulana.NChart.NChartView;
import com.ustc.wsn.mobileData.Listenter.TrackSensorListener;
import com.ustc.wsn.mobileData.R;
import com.ustc.wsn.mobileData.bean.cubeView.MyRender;
import com.ustc.wsn.mobileData.service.TrackService;

import java.util.Timer;
import java.util.TimerTask;

public class ChartingDemoActivity extends Activity {
    private final String TAG = ChartingDemoActivity.class.toString();
    NChartView mNChartView;
    private RelativeLayout attLayout;
    MyRender myRender;

    TextView EulerxAxis;
    TextView EuleryAxis;
    TextView EulerzAxis;

    private float[] Euler = {0,0,0};

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
        mNChartView.getChart().setShouldAntialias(true);

        attLayout = (RelativeLayout) findViewById(R.id.ncattView);
        initSensor();
        GLSurfaceView glView = new GLSurfaceView(this);
        myRender = new MyRender();
        glView.setBackgroundColor(Color.argb(40,255,255, 255));
        glView.setRenderer(myRender);
        attLayout.addView(glView);

        EulerxAxis = (TextView) findViewById(R.id.value_x);
        EuleryAxis = (TextView) findViewById(R.id.value_y);
        EulerzAxis = (TextView) findViewById(R.id.value_z);

        if (ACCELERATOR_EXIST && GYROSCROPE_EXIST && MAGNETIC_EXIST) {
            WindowSize = sensorListener.windowSize * sensorListener.DurationWindow;
            sampleInterval = sensorListener.sampleInterval;
            loadView();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!threadDisable) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // do
                        Euler = sensorListener.readEuler();
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

        RadioGroup IfInter = (RadioGroup) findViewById(R.id.IfInterpolation);

        IfInter.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup IfInter, int checkedId) {
                RadioButton _IfInter = (RadioButton) findViewById(checkedId);
                switch (checkedId) {
                    case R.id.InterYes:
                        sensorListener.ifInterpolation = true;
                        track.markerUse = track.markerInter;
                        break;
                    case R.id.InterNo:
                        sensorListener.ifInterpolation = false;
                        track.markerUse = track.marker;
                        break;
                }

            }
        });

    }

    private Handler handler = new Handler() {
        @Override
        //定时更新图表
        public void handleMessage(Message msg) {
            EulerxAxis.setText(String.valueOf((int)(Euler[0]/Math.PI*180)));
            EuleryAxis.setText(String.valueOf((int)(Euler[1]/Math.PI*180)));
            EulerzAxis.setText(String.valueOf((int)(Euler[2]/Math.PI*180)));
        }
    };

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings_help:
                showHelpDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadView() {
        // Paste your license key here.
        mNChartView.getChart().setLicenseKey("Xw2f+e+5GVoEAKky3PR2zsMNI4nQ3KE6dIy1YOEqofD7OXlMc6w0OMtOrbmHFfZunPxd7uYp5TY0j9hB7mJMN/feIngo7DK1l7q9ya0zbkBoxEVFoIhrwrl5ua+azYv787LvNYG7DsjHDA67Aa8lSyzXgNXTt5rdPNTHHNzGe5gt/vmNnOI6miQY6fTBOE0XEitnst9X+SVaBfFQXuUODLZVAlKKl+wdlr2nuT+gqFw+0e9SJS6MV/ZlStMjSR/Cr+UnKZzdUm+RPggC2vLbnKTLewkEB70dUFIx1Dr4zuqLaZqmzHdfuujTiFterq3u7aJ30FgrFxG6rE1QCTBWMSNCkhRJH91vhmIADeu1UrDUs60R/yVauoG0k242c+gCmeaMrSyOrfvgUb3Dhk8Rt0cPahZDfOpA96BlEb8yL+Ns0/+hiAQaV66e0mOgPjzOTV1ta3JdqY6xHYg5XzyIHecMUomwPeApTh0QaAyc0EfpTwK0XIbk/VsUD3hcoTOasLVMiir6FixgBaYWp3cNtnQcuqh9hd2cACO4vxN+JAoBBL5OOmzxOci1H28r9LWZqXZympN/Huz+oxTSmztB+epFXbo6dzDlDLcopjmP9OVYnyN2tkrpLBTJfBKwXfDm1AorurVD4KEzxDkYErpxkKUFnWNwiLyi9Hqanjr2RTs=");
        track = new TrackService(true, mNChartView, this, WindowSize);
        track.initView(0);
        track.updateData(0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                float[][] position;
                float[][] interPosition;
                int i = 1;
                while (!threadDisable) {
                    try {
                        Thread.sleep(WindowSize * sampleInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (sensorListener.ifNewPath()) {
                        sensorListener.ifNewPath = false;
                        position = sensorListener.getPosition();
                        interPosition =  sensorListener.getInterPosition();
                        if (position != null && interPosition!=null) {
                            track.setPosition(position,interPosition);
                            track.updateData(i);
                        }
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
        sensorListener = new TrackSensorListener(accMax, gyroMax, magMax, true,false,true,false);
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
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        threadDisable = true;
        if (track != null) {
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "bengin touch time \t" + System.currentTimeMillis());
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;

            default:
                break;
        }
        return super.onTouchEvent(event);
    }
}
