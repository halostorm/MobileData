package com.ustc.wsn.mobileData.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ustc.wsn.mobileData.Listenter.TrackSensorListener;
import com.ustc.wsn.mobileData.R;
import com.ustc.wsn.mobileData.bean.PhoneState;
import com.ustc.wsn.mobileData.bean.outputFile;
import com.ustc.wsn.mobileData.service.ChartService;

import org.achartengine.GraphicalView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

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
    private TextView stateParams;

    private TextView ifVehicle;
    private TextView  maxFreText;
    private TrackSensorListener sensorListener;

    private LinearLayout valueCurveLayout;//存放左图表的布局容器
    private GraphicalView valueView;//左右图表
    private ChartService valueService;

    private LinearLayout freCurveLayout;//存放左图表的布局容器
    private GraphicalView freView;//左右图表
    private ChartService freService;

    private float[] stateValue = {0,0};

    private TextView meanAxis;
    private TextView varAxis;

    private TextView freAxis;

    private DecimalFormat df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate_state);
        initSensor();
        stateParams = (TextView) findViewById(R.id.state_param);

        ifVehicle = (TextView)findViewById(R.id.ifVehicle);

        EditText editTextAccMean_Ab = (EditText) findViewById(R.id.accMean_Ab);

        EditText editTextAccVar_Ab = (EditText) findViewById(R.id.accVar_Ab);

        EditText editTextAccMean_User = (EditText) findViewById(R.id.accMean_User);

        EditText editTextAccVar_User = (EditText) findViewById(R.id.accVar_User);

        EditText editTextAMPDB = (EditText) findViewById(R.id.AmpDBThreshold);

        EditText editTextPEAKFRE = (EditText) findViewById(R.id.PeakFreThreshold);

        EditText editTextPROBABILITY = (EditText) findViewById(R.id.ifVehicleProbability);

        editTextAccMean_Ab.setHint("输入绝对静止-acc均值阈值（当前值：" + PhoneState.ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD + "）");
        editTextAccVar_Ab.setHint("输入绝对静止-acc方差阈值（当前值：" + PhoneState.ACC_VAR_ABSOLUTE_STATIC_THRESHOLD + ")");
        editTextAccMean_User.setHint("输入相对静止-acc均值阈值（当前值：" + PhoneState.ACC_MEAN_STATIC_THRESHOLD + "）");
        editTextAccVar_User.setHint("输入相对静止-acc方差阈值(当前值：" + PhoneState.ACC_VAR_STATIC_THRESHOLD + "）");

        editTextAMPDB.setHint("输入幅值显著性阈值（当前值：" + PhoneState.AMPDB_THRESHOLD + "）");
        editTextPEAKFRE.setHint("输入峰值频率阈值（当前值：" + PhoneState.PEAK_FRE_THRESHOLD + ")");
        editTextPROBABILITY.setHint("输入车内识别概率阈值（当前值：" + PhoneState.VEHICLE_PROBABILITY_THRESHOLD + "）");

        TextView confirmText = (TextView) findViewById(R.id.btnconfirmParams);

        confirmText.setTag(R.id.accMean_Ab, editTextAccMean_Ab);
        confirmText.setTag(R.id.accVar_Ab, editTextAccVar_Ab);
        confirmText.setTag(R.id.accMean_User, editTextAccMean_User);
        confirmText.setTag(R.id.accVar_User, editTextAccVar_User);

        confirmText.setTag(R.id.AmpDBThreshold, editTextAMPDB);
        confirmText.setTag(R.id.PeakFreThreshold, editTextPEAKFRE);
        confirmText.setTag(R.id.ifVehicleProbability, editTextPROBABILITY);

        confirmText.setOnClickListener(cOnClickListener);

        TextView initParams = (TextView) findViewById(R.id.btninitParams);

        maxFreText = (TextView)findViewById(R.id.value_frequency_axis);

        initParams.setTag(R.id.accMean_Ab, editTextAccMean_Ab);
        initParams.setTag(R.id.accVar_Ab, editTextAccVar_Ab);
        initParams.setTag(R.id.accMean_User, editTextAccMean_User);
        initParams.setTag(R.id.accVar_User, editTextAccVar_User);

        initParams.setTag(R.id.AmpDBThreshold, editTextAMPDB);
        initParams.setTag(R.id.PeakFreThreshold, editTextPEAKFRE);
        initParams.setTag(R.id.ifVehicleProbability, editTextPROBABILITY);

        initParams.setOnClickListener(mOnClickListener);
        //图表
        valueCurveLayout = (LinearLayout) findViewById(R.id.state_value_curve);

        valueService = new ChartService(this);
        valueService.setXYMultipleSeriesDataset("Mean ", " Variance ", "Threshold (Var)");
        valueService.setXYMultipleSeriesRenderer(0, 10, 0, 8, "状态值", "时间 /s", "0.0",
                Color.BLACK, Color.BLACK,Color.RED, Color.BLUE, Color.argb(255,238, 154, 0),  Color.BLACK);
        valueView = valueService.getGraphicalView();

        //将左右图表添加到布局容器中
        valueCurveLayout.addView(valueView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        meanAxis = (TextView) findViewById(R.id.value_accMean_axis);
        varAxis = (TextView) findViewById(R.id.value_accVar_axis);


        freCurveLayout = (LinearLayout) findViewById(R.id.frequency_value_curve);

        freService = new ChartService(this);
        freService.setXYMultipleSeriesDataset("对数频谱系数 ", "幅值显著性阈值", "Vehicle峰值频率阈值");
        freService.setXYMultipleSeriesRenderer(0, 25, -20, -5, "频谱系数", "频率", "10log(A)",
                Color.BLACK, Color.BLACK,Color.RED, Color.argb(255,238, 154, 0), Color.BLUE,Color.BLACK);
        freView = freService.getGraphicalView();

        //将左右图表添加到布局容器中
        freCurveLayout.addView(freView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        freAxis = (TextView) findViewById(R.id.value_frequency_axis);

        df = new DecimalFormat("0.000");

        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                handler2.sendMessage(handler2.obtainMessage());
            }
        }, 0, 20);

        Timer timer2 = new Timer();
        timer2.schedule(new TimerTask() {
            @Override
            public void run() {
                handler1.sendMessage(handler1.obtainMessage());
                handler3.sendMessage(handler3.obtainMessage());
            }
        }, 0, 100);

        Timer timer3 = new Timer();
        timer3.schedule(new TimerTask() {
            @Override
            public void run() {
                handler4.sendMessage(handler1.obtainMessage());
            }
        }, 10000, sensorListener.FFT_SampleInterval);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String accMeanAbParams = String.valueOf(PhoneState.ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD_DEFAULT);
            String accVarAbParams = String.valueOf(PhoneState.ACC_VAR_ABSOLUTE_STATIC_THRESHOLD_DEFAULT);
            String accMeanUserParams = String.valueOf(PhoneState.ACC_MEAN_STATIC_THRESHOLD_DEFAULT);
            String accVarUserParams = String.valueOf(PhoneState.ACC_VAR_STATIC_THRESHOLD_DEFAULT);

            String AMPDBParams = String.valueOf(PhoneState.AMPDB_THRESHOLD_DEFAULT);
            String PeakFreParams = String.valueOf(PhoneState.PEAK_FRE_THRESHOLD_DEFAULT);
            String VehicleProParams = String.valueOf(PhoneState.VEHICLE_PROBABILITY_THRESHOLD_DEFAULT);

            ((EditText) view.getTag(R.id.accMean_Ab)).setText(accMeanAbParams);
            ((EditText) view.getTag(R.id.accVar_Ab)).setText(accVarAbParams);
            ((EditText) view.getTag(R.id.accMean_User)).setText(accMeanUserParams);
            ((EditText) view.getTag(R.id.accVar_User)).setText(accVarUserParams);

            ((EditText) view.getTag(R.id.AmpDBThreshold)).setText(AMPDBParams);
            ((EditText) view.getTag(R.id.PeakFreThreshold)).setText(PeakFreParams);
            ((EditText) view.getTag(R.id.ifVehicleProbability)).setText(VehicleProParams);
        }
    };

    private View.OnClickListener cOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            EditText t1 = ((EditText) view.getTag(R.id.accMean_Ab));
            EditText t2 = ((EditText) view.getTag(R.id.accVar_Ab));
            EditText t3 = ((EditText) view.getTag(R.id.accMean_User));
            EditText t4 = ((EditText) view.getTag(R.id.accVar_User));
            EditText t5 = ((EditText) view.getTag(R.id.AmpDBThreshold));
            EditText t6 = ((EditText) view.getTag(R.id.PeakFreThreshold));
            EditText t7 = ((EditText) view.getTag(R.id.ifVehicleProbability));

            String accMeanAbParams = t1.getText().toString().trim();
            String accVarAbParams = t2.getText().toString().trim();
            String accMeanUserParams = t3.getText().toString().trim();
            String accVarUserParams = t4.getText().toString().trim();

            String AMPDBParams = t5.getText().toString().trim();
            String PeakFreParams = t6.getText().toString().trim();
            String VehicleProParams = t7.getText().toString().trim();

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
                out += accVarUserParams + "\t";
            }

            if (AMPDBParams.length() == 0) {
                out += PhoneState.AMPDB_THRESHOLD + "\t";
            } else {
                PhoneState.AMPDB_THRESHOLD = Float.parseFloat(AMPDBParams);
                out += AMPDBParams + "\t";
            }
            if (PeakFreParams.length() == 0) {
                out += PhoneState.PEAK_FRE_THRESHOLD + "\t";
            } else {
                PhoneState.PEAK_FRE_THRESHOLD = Float.parseFloat(PeakFreParams);
                out += PeakFreParams + "\t";
            }
            if (VehicleProParams.length() == 0) {
                out += PhoneState.VEHICLE_PROBABILITY_THRESHOLD + "\t";
            } else {
                PhoneState.PEAK_FRE_THRESHOLD = Float.parseFloat(VehicleProParams);
                out += VehicleProParams;
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
            t5.setText("");
            t6.setText("");
            t7.setText("");

            t1.setHint("输入绝对静止-acc均值阈值（当前值：" + PhoneState.ACC_MEAN_ABSOLUTE_STATIC_THRESHOLD + "）");
            t2.setHint("输入绝对静止-acc方差阈值（当前值：" + PhoneState.ACC_VAR_ABSOLUTE_STATIC_THRESHOLD + ")");
            t3.setHint("输入相对静止-acc均值阈值（当前值：" + PhoneState.ACC_MEAN_STATIC_THRESHOLD + "）");
            t4.setHint("输入相对静止-acc方差阈值(当前值：" + PhoneState.ACC_VAR_STATIC_THRESHOLD + "）");

            t5.setHint("输入幅值显著性阈值（当前值：" + PhoneState.AMPDB_THRESHOLD + "）");
            t6.setHint("输入峰值频率阈值（当前值：" + PhoneState.PEAK_FRE_THRESHOLD + ")");
            t7.setHint("输入车内识别概率阈值（当前值：" + PhoneState.VEHICLE_PROBABILITY_THRESHOLD + "）");

        }
    };

    private Handler handler1 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int state = sensorListener.getNowState();
            switch (state) {
                case PhoneState.ABSOLUTE_STATIC_STATE:
                    stateParams.setText("绝对静止");
                    break;
                case PhoneState.USER_STATIC_STATE:
                    stateParams.setText("相对静止");
                    break;
                case PhoneState.UNKONW_STATE:
                    stateParams.setText("用户运动");
                    break;
            }
        }
    };

    private Handler handler2 = new Handler() {
        @Override
        //定时更新图表
        public void handleMessage(Message msg) {
            stateValue = sensorListener.getNowStateValues();
            valueService.rightUpdateChart(stateValue[0], stateValue[1], PhoneState.ACC_VAR_STATIC_THRESHOLD);
        }
    };

    private Handler handler3 = new Handler() {
        @Override
        //定时更新图表
        public void handleMessage(Message msg) {
            meanAxis.setText(df.format(stateValue[0]));
            varAxis.setText(df.format(stateValue[1]));
        }
    };

    private Handler handler4 = new Handler() {
        @Override
        //定时更新图表
        public void handleMessage(Message msg) {
            float[] Spectrum = sensorListener.getSpectrum();
            float[] SpectrumID = sensorListener.getSpectrumID();

            float maxFre = sensorListener.getMaxFrequency();
            if(Spectrum!=null && SpectrumID !=null) {
                freService.updateChart(SpectrumID, Spectrum,PhoneState.PEAK_FRE_THRESHOLD,PhoneState.AMPDB_THRESHOLD);
                if(maxFre>0){
                    maxFreText.setText(df.format(maxFre));
                }
                else{
                    maxFreText.setText("不显著");
                }
            }
            boolean ifOnvehicle = sensorListener.getIfOnVehicle();
            if(ifOnvehicle){
                ifVehicle.setText("在车上");
            }else {
                ifVehicle.setText("不在车上");
            }
        }
    };

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
        sensorListener = new TrackSensorListener(accMax,gyroMax,magMax,true,false);
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
    protected void onDestroy() {
        super.onDestroy();
        sensorListener.closeSensorThread();
        sm.unregisterListener(sensorListener);
    }

}
