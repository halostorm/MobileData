package com.ustc.wsn.mydataapp.detectorservice;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.ustc.wsn.mydataapp.Application.AppResourceApplication;
import com.ustc.wsn.mydataapp.bean.AcceleratorData;
import com.ustc.wsn.mydataapp.bean.GyroData;
import com.ustc.wsn.mydataapp.bean.MagnetData;
import com.ustc.wsn.mydataapp.bean.PhoneState;

/**
 * Created by halo on 2018/1/28.
 */

public class TrackSensorListener implements SensorEventListener {
    private static int PHONE_STATE = PhoneState.UNKONW_STATE;
    private final float ACC_STATIC_THRESHOLD = 0.1f;
    private final float GYRO_STATIC_THRESHOLD = 0.1f;
    private final String TAG = TrackSensorListener.this.toString();
    private static int windowSize = 256;// 256
    private float[] gravity = new float[windowSize];
    // 传感器数据缓冲池
    private TrackSensorListener mContext = TrackSensorListener.this;
    private boolean threadDisable_data_update = false;
    private boolean initDataPass = false;
    private final int Data_Size = 10000;// sensor 缓冲池大小

    private String[] LinearaccData;
    private String[] accData;
    private String[] gyroData;
    private String[] magData;
    private String[] rotData;

    private String accNow;
    private String gyroNow;
    private String magNow;
    private String LinearaccNow;
    // 当前写入位置
    private int LinearAcc_cur;
    private int gyro_cur;
    private int mag_cur;
    private int acc_cur;
    // 当前读取位置
    private int LinearAcc_old;
    private int gyro_old;
    private int mag_old;
    private int acc_old;

    private volatile float[] gravityOri;
    private volatile float[] accOri;
    private volatile float[] magnetOri;
    private volatile float[] gyroOri;
    private volatile float[] linear_acceleration;

    private volatile float[] dcmAndroid;
    private volatile float[] dcmGyro;
    private volatile float[] eulerAndroid;
    private volatile float[] eulerGyro;

    private boolean gravityOriOriNew = false;
    private boolean magOriNew = false;
    private boolean accOriNew = false;
    private boolean gyroOriNew = false;

    private LPF_I accLPF;
    private LPF_I gyroLPF;
    private LPF_I magLPF;

    private MeanFilter accMF;
    private MeanFilter gyroMF;
    private MeanFilter magMF;

    private long time;
    private long timeOld;
    private float dt;

    public TrackSensorListener(AppResourceApplication resource) {
        // TODO Auto-generated constructor stub
        super();
        LinearAcc_cur = 0;
        gyro_cur = 0;
        mag_cur = 0;
        acc_cur = 0;

        LinearaccData = new String[Data_Size];
        gyroData = new String[Data_Size];
        magData = new String[Data_Size];
        accData = new String[Data_Size];

        magnetOri = new float[3];
        gravityOri = new float[3];
        gyroOri = new float[3];
        accOri = new float[3];

        accLPF = new LPF_I();
        gyroLPF = new LPF_I();
        magLPF = new LPF_I();

        accMF = new MeanFilter();
        gyroMF = new MeanFilter();
        magMF = new MeanFilter();

        linear_acceleration = new float[3];

        dcmAndroid = new float[]{1, 0, 0, 0, 1, 0, 0, 0, 1};
        eulerAndroid = new float[]{0.0f, 0.0f, 0.0f};
        dcmGyro = new float[]{1, 0, 0, 0, 1, 0, 0, 0, 1};
        eulerGyro = new float[]{0.0f, 0.0f, 0.0f};

        time = System.currentTimeMillis();
        timeOld = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!threadDisable_data_update) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (gravityOriOriNew && magOriNew && gyroOriNew) {
                            A_Android();
                    }
                }
            }
        }).start();


        new Thread(new Runnable() {
            @Override
            public void run() {
                int sensorCount = 0;
                while (!threadDisable_data_update) {
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // do
                    if (sensorCount < 100) {
                        sensorCount++;
                    } else {
                        initDataPass = true;
                    }
                    if (initDataPass) {
                        setLinearAccData();
                        setGyroData();
                        setMagData();
                    }
                }
            }
        }).start();

    }

    public void setPhoneState(int state) {
        this.PHONE_STATE = state;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        // storeData = new StoreData();
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                if (event.values != null) {
                    gravityOri = accLPF.filter(event.values);
                    linear_acceleration[0] = event.values[0] - gravityOri[0];
                    linear_acceleration[1] = event.values[1] - gravityOri[1];
                    linear_acceleration[2] = event.values[2] - gravityOri[2];
                    gravityOriOriNew = true;
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                if (event.values != null) {
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                if (event.values != null) {
                }
                break;
        }
    }

    public void setLinearAccData() {
        if (((LinearAcc_cur + 1) % Data_Size != LinearAcc_old) && LinearaccNow != null) {// 不满
            if (LinearaccNow != null) {
                this.LinearaccData[LinearAcc_cur] = System.currentTimeMillis() + "\t" + LinearaccNow;
            } else {
                this.LinearaccData[LinearAcc_cur] = null;
            }
            LinearAcc_cur = (LinearAcc_cur + 1) % Data_Size;
        }
    }

    public void setAccData() {
        if (((acc_cur + 1) % Data_Size != acc_old) && accNow != null) {// 不满
            if (accNow != null) {
                this.accData[acc_cur] = System.currentTimeMillis() + "\t" + accNow;
            } else {
                this.accData[acc_cur] = null;
            }
            acc_cur = (acc_cur + 1) % Data_Size;
        }
    }

    public void setGyroData() {
        if (((gyro_cur + 1) % Data_Size != gyro_old) && gyroNow != null) {// 不满
            if (gyroNow != null) {
                this.gyroData[gyro_cur] = gyroNow;
            } else {
                this.gyroData[gyro_cur] = null;
            }
            gyro_cur = (gyro_cur + 1) % Data_Size;
        }
    }

    public void setMagData() {
        if (((mag_cur + 1) % Data_Size != mag_old) && magNow != null) {// 不满
            if (magNow != null) {
                this.magData[mag_cur] = magNow;
            } else {
                this.magData[mag_cur] = null;
            }
            mag_cur = (mag_cur + 1) % Data_Size;
        }
    }


    public String getLinearAccData() {
        if (LinearAcc_cur != LinearAcc_old) { // 不空
            int i = LinearAcc_old;
            LinearAcc_old = (LinearAcc_old + 1) % Data_Size;
            return LinearaccData[i];
        } else return null;
    }

    public String getGyroData() {
        if (gyro_cur != gyro_old) {// 不空
            int i = gyro_old;
            gyro_old = (gyro_old + 1) % Data_Size;
            return gyroData[i];
        } else return null;
    }

    public String getMagData() {
        if (mag_cur != mag_old) {// 不空
            int i = mag_old;
            mag_old = (mag_old + 1) % Data_Size;
            return magData[i];
        } else return null;
    }

    public float[] phoneToEarth(float[] DCM, float[] values) {
        float[] valuesEarth = new float[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                valuesEarth[i] += values[j] * DCM[3 * i + j];
            }
        }
        return valuesEarth;
    }

    public void ifABSOLUTE_STATE() throws InterruptedException {
        float[] accSample = new float[windowSize];
        float[] gyroSample = new float[windowSize];
        for (int i = 0; i < windowSize; i++) {
            float[] accS = new float[3];
            float[] gyroS = new float[3];
            String valuesNow = accNow;
            String[] Array = new String[5];
            Array = valuesNow.split("\t");
            accS[0] = Float.parseFloat(Array[0]);
            accS[1] = Float.parseFloat(Array[1]);
            accS[2] = Float.parseFloat(Array[2]);
            accSample[i] = (float) Math.sqrt(accS[0] * accS[0] + accS[1] * accS[1] + accS[2] * accS[2]);
            gravity[i] = accS[2];

            valuesNow = gyroNow;
            Array = valuesNow.split("\t");
            gyroS[0] = Float.parseFloat(Array[0]);
            gyroS[1] = Float.parseFloat(Array[1]);
            gyroS[2] = Float.parseFloat(Array[2]);

            gyroSample[i] = (float) Math.sqrt(gyroS[0] * gyroS[0] + gyroS[1] * gyroS[1] + gyroS[2] * gyroS[2]);
            i++;
        }
        float accSTD = getStdVar(accSample);
        float gyroSTD = getStdVar(gyroSample);
        float accMean = getMean(accSample);
        float gyroMean = getMean(gyroSample);

        if (accSTD < ACC_STATIC_THRESHOLD && gyroSTD < GYRO_STATIC_THRESHOLD && accMean < ACC_STATIC_THRESHOLD && gyroMean < GYRO_STATIC_THRESHOLD) {
            this.setPhoneState(PhoneState.ABSOLUTE_STATIC_STATE);
        } else {
            this.setPhoneState(PhoneState.UNKONW_STATE);
        }
    }

    public void A_Android() {
        //float[] R = new float[9];
        SensorManager.getRotationMatrix(dcmAndroid, null, gravityOri, magnetOri);
        SensorManager.getOrientation(dcmAndroid, eulerAndroid);
    }
    public void A_Gyro() {
        float[] values = new float[3];

    }

    public float getStdVar(float[] x) {
        int m = x.length;
        float sum = 0;
        for (int i = 0; i < m; i++) {// 求和
            sum += x[i];
        }
        float dAve = sum / m;// 求平均值
        float dVar = 0;
        for (int i = 0; i < m; i++) {// 求方差
            dVar += (x[i] - dAve) * (x[i] - dAve);
        }
        return (float) Math.sqrt(dVar / m);
    }

    public float getMean(float[] x) {
        int m = x.length;
        float sum = 0;
        for (int i = 0; i < m; i++) {// 求和
            sum += x[i];
        }
        float dAve = sum / m;// 求平均值
        return dAve;
    }

    public void eulerAnglesToQuaternion(float angle[], float q[]) {
        float cosRoll = (float) Math.cos(angle[2] * 0.5f);
        float sinRoll = (float) Math.sin(angle[2] * 0.5f);

        float cosPitch = (float) Math.cos(angle[1] * 0.5f);
        float sinPitch = (float) Math.sin(angle[1] * 0.5f);

        float cosHeading = (float) Math.cos(angle[0] * 0.5f);
        float sinHeading = (float) Math.sin(angle[0] * 0.5f);

        q[0] = cosRoll * cosPitch * cosHeading + sinRoll * sinPitch * sinHeading;
        q[1] = sinRoll * cosPitch * cosHeading - cosRoll * sinPitch * sinHeading;
        q[2] = cosRoll * sinPitch * cosHeading + sinRoll * cosPitch * sinHeading;
        q[3] = cosRoll * cosPitch * sinHeading - sinRoll * sinPitch * cosHeading;
    }

    public void quaternionToRotationMatrix(float q[], float rMat[]) {
        float q1q1 = (float) Math.sqrt(q[1]);
        float q2q2 = (float) Math.sqrt(q[2]);
        float q3q3 = (float) Math.sqrt(q[3]);

        float q0q1 = q[0] * q[1];
        float q0q2 = q[0] * q[2];
        float q0q3 = q[0] * q[3];
        float q1q2 = q[1] * q[2];
        float q1q3 = q[1] * q[3];
        float q2q3 = q[2] * q[3];

        rMat[0] = 1.0f - 2.0f * q2q2 - 2.0f * q3q3;
        rMat[1] = 2.0f * (q1q2 + q0q3);
        rMat[2] = 2.0f * (q1q3 - q0q2);

        rMat[3] = 2.0f * (q1q2 - q0q3);
        rMat[4] = 1.0f - 2.0f * q1q1 - 2.0f * q3q3;
        rMat[5] = 2.0f * (q2q3 + q0q1);

        rMat[6] = 2.0f * (q1q3 + q0q2);
        rMat[7] = 2.0f * (q2q3 - q0q1);
        rMat[8] = 1.0f - 2.0f * q1q1 - 2.0f * q2q2;
    }
}
