package com.ustc.wsn.mobileData.activity;

/**
 * Created by halo on 2018/1/17.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ustc.wsn.mobileData.Listenter.TrackSensorListener;
import com.ustc.wsn.mobileData.R;
import com.ustc.wsn.mobileData.bean.PhoneState;
import com.ustc.wsn.mobileData.service.ChartService;

import org.achartengine.GraphicalView;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class SimulationActivity extends Activity {
    private final String TAG = SimulationActivity.class.toString();
    public int FRAME_TYPE = 0;//0 - phone frame/ 1 - inertial frame
    public int AttChoose = 0;
    private boolean ACCELERATOR_EXIST = false;
    private boolean GYROSCROPE_EXIST = false;
    private boolean MAGNETIC_EXIST = false;

    private LinearLayout accCurveLayout;//存放左图表的布局容器
    private LinearLayout rawaccCurveLayout;//存放右图表的布局容器
    private LinearLayout gyroCurveLayout;//存放右图表的布局容器
    private LinearLayout magCurveLayout;//存放右图表的布局容器
    private GraphicalView accView, gyroView, rawaccView, magView;//左右图表
    private ChartService accService, rawService, gyroSeivice, magService;
    private Toast t;
    private Timer timer1;
    private Timer timer2;
    private SensorManager sm;
    private Sensor accelerator;
    private Sensor gyroscrope;
    private Sensor magnetic;

    private TrackSensorListener sensorListener;
    private float[] RawAccData;
    private float[] AccData;
    private float[] GyroData;
    private float[] MagData;

    private TextView accxAxis;
    private TextView accyAxis;
    private TextView acczAxis;

    private TextView rawxAxis;
    private TextView rawyAxis;
    private TextView rawzAxis;

    private TextView gyroxAxis;
    private TextView gyroyAxis;
    private TextView gyrozAxis;

    private TextView magxAxis;
    private TextView magyAxis;
    private TextView magzAxis;

    private TextView aframevalue;
    private TextView rframevalue;
    private TextView gframevalue;
    private TextView mframevalue;

    private DecimalFormat df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation);

        rawaccCurveLayout = (LinearLayout) findViewById(R.id.linear_acc_curve);
        accCurveLayout = (LinearLayout) findViewById(R.id.acc_curve);
        gyroCurveLayout = (LinearLayout) findViewById(R.id.gyro_curve);
        magCurveLayout = (LinearLayout) findViewById(R.id.mag_curve);

        accService = new ChartService(this);
        accService.setXYMultipleSeriesDataset("AccX", "AccY", "AccZ");
        accService.setXYMultipleSeriesRenderer(0, 10, -20, 20, "校准加速度", "时间 /s", "m2/s", Color.BLACK, Color.BLACK, Color.BLUE, Color.CYAN, Color.RED, Color.BLACK);
        accView = accService.getGraphicalView();

        rawService = new ChartService(this);
        rawService.setXYMultipleSeriesDataset("RawAccX", "RawAccY", "RawAccZ");
        rawService.setXYMultipleSeriesRenderer(0, 10, -20, 20, "未校准加速度", "时间 /s", "m2/", Color.BLACK, Color.BLACK, Color.BLUE, Color.CYAN, Color.RED, Color.BLACK);
        rawaccView = rawService.getGraphicalView();

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
        rawaccCurveLayout.addView(rawaccView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        gyroCurveLayout.addView(gyroView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        magCurveLayout.addView(magView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        accxAxis = (TextView) findViewById(R.id.value_accx_axis);
        accyAxis = (TextView) findViewById(R.id.value_accy_axis);
        acczAxis = (TextView) findViewById(R.id.value_accz_axis);

        rawxAxis = (TextView) findViewById(R.id.value_linearx_axis);
        rawyAxis = (TextView) findViewById(R.id.value_lineary_axis);
        rawzAxis = (TextView) findViewById(R.id.value_linearz_axis);

        gyroxAxis = (TextView) findViewById(R.id.value_gyrox_axis);
        gyroyAxis = (TextView) findViewById(R.id.value_gyroy_axis);
        gyrozAxis = (TextView) findViewById(R.id.value_gyroz_axis);

        magxAxis = (TextView) findViewById(R.id.value_magx_axis);
        magyAxis = (TextView) findViewById(R.id.value_magy_axis);
        magzAxis = (TextView) findViewById(R.id.value_magz_axis);

        aframevalue = (TextView) findViewById(R.id.AframeType);
        rframevalue = (TextView) findViewById(R.id.LframeType);
        gframevalue = (TextView) findViewById(R.id.GframeType);
        mframevalue = (TextView) findViewById(R.id.MframeType);

        df = new DecimalFormat("0.00");

        initSensor();
        RawAccData = new float[3];
        timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                handler1.sendMessage(handler1.obtainMessage());
            }
        }, 0, 20);

        timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                handler2.sendMessage(handler2.obtainMessage());
            }
        }, 5, 100);

    }

    private String[] items = {"手机坐标系", "惯性坐标系"};

    class DialogSingleClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0:
                    FRAME_TYPE = 0;
                    aframevalue.setText(items[0]);
                    rframevalue.setText(items[0]);
                    gframevalue.setText(items[0]);
                    mframevalue.setText(items[0]);
                    break;
                case 1:
                    FRAME_TYPE = 1;
                    aframevalue.setText(items[1] + AttItems[0]);
                    rframevalue.setText(items[1] + AttItems[0]);
                    mframevalue.setText(items[1] + AttItems[0]);
                    gframevalue.setText(items[1] + AttItems[0]);
                    break;
            }
            dialog.dismiss();
        }

    }

    private void ShowFrameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择传感器坐标系");
        builder.setIcon(R.drawable.ic_launcher);
        if (FRAME_TYPE == 0) {
            builder.setSingleChoiceItems(items, 0, new DialogSingleClickListener());
        } else if (FRAME_TYPE == 1) {
            builder.setSingleChoiceItems(items, 1, new DialogSingleClickListener());
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String[] AttItems = {"EKF算法", "互补滤波算法", "梯度下降滤波算法", "仅陀螺仪,周期校正", "Android自带算法"};

    class AttDialogSingleClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            AttChoose = which;
            aframevalue.setText(AttItems[AttChoose]);
            rframevalue.setText(AttItems[AttChoose]);
            mframevalue.setText(AttItems[AttChoose]);
            gframevalue.setText(AttItems[AttChoose]);
            switch (which) {
                case 0:
                    sensorListener.setAttitudeMode(PhoneState.Attitude_EKF);
                    break;
                case 1:
                    sensorListener.setAttitudeMode(PhoneState.Attitude_FCF);
                    break;
                case 2:
                    sensorListener.setAttitudeMode(PhoneState.Attitude_GDF);
                    break;
                case 4:
                    sensorListener.setAttitudeMode(PhoneState.Attitude_ANDROID);
                    break;
                case 3:
                    sensorListener.setAttitudeMode(PhoneState.Attitude_GYRO);
                    break;
            }
            dialog.dismiss();
        }

    }

    private void ShowAttChooseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择姿态解算算法");
        builder.setIcon(R.drawable.ic_launcher);

        builder.setSingleChoiceItems(AttItems, AttChoose, new AttDialogSingleClickListener());

        AlertDialog dialog = builder.create();
        dialog.show();
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
                ShowFrameDialog();
                return true;
            case R.id.AttChoose:
                if (FRAME_TYPE == 0) {
                    t = Toast.makeText(this, "请先选择坐标系为惯性坐标系", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                } else {
                    ShowAttChooseDialog();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Handler handler1 = new Handler() {
        @Override
        //定时更新图表
        public void handleMessage(Message msg) {
            RawAccData = sensorListener.readRawAccData(FRAME_TYPE);
            AccData = sensorListener.readAccData(FRAME_TYPE);
            GyroData = sensorListener.readGyroData(FRAME_TYPE);
            MagData = sensorListener.readMagData(FRAME_TYPE);

            accService.rightUpdateChart(AccData[0], AccData[1], AccData[2]);
            gyroSeivice.rightUpdateChart(GyroData[0], GyroData[1], GyroData[2]);
            magService.rightUpdateChart(MagData[0], MagData[1], MagData[2]);
            rawService.rightUpdateChart(RawAccData[0], RawAccData[1], RawAccData[2]);
        }
    };

    private Handler handler2 = new Handler() {
        @Override
        //定时更新图表
        public void handleMessage(Message msg) {
            accxAxis.setText(df.format(AccData[0]));
            accyAxis.setText(df.format(AccData[1]));
            acczAxis.setText(df.format(AccData[2]));

            rawxAxis.setText(df.format(RawAccData[0]));
            rawyAxis.setText(df.format(RawAccData[1]));
            rawzAxis.setText(df.format(RawAccData[2]));

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
        sensorListener = new TrackSensorListener(accMax, gyroMax, magMax, true,false,false,false);
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
        super.onDestroy();
        if (timer1 != null) {
            timer1.cancel();
        }
        if (timer2 != null) {
            timer2.cancel();
        }
        if (rawService != null) {
            rawService.stopSelf();
        }
        if (accService != null) {
            accService.stopSelf();
        }
        if (gyroSeivice != null) {
            gyroSeivice.stopSelf();
        }
        if (magService != null) {
            magService.stopSelf();
        }
        sensorListener.closeSensorThread();
        sm.unregisterListener(sensorListener);
    }
}
