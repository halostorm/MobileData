package com.ustc.wsn.mydataapp.detectorservice;
/**
 * Created by halo on 2017/7/1.
 */

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.ustc.wsn.mydataapp.Application.AppResourceApplication;
import com.ustc.wsn.mydataapp.bean.AcceleratorData;
import com.ustc.wsn.mydataapp.bean.GyroData;
import com.ustc.wsn.mydataapp.bean.MagnetData;
import com.ustc.wsn.mydataapp.utils.TimeUtil;

public class DetectorSensorListener implements SensorEventListener {

    private static final String TAG = null;
    // private AppResource resource;
    // private Data SensorData;
    // private StoreData storeData;
    // 传感器数据缓冲池
    private DetectorSensorListener mContext = DetectorSensorListener.this;
    private boolean threadDisable_data_update = false;
    private final int Data_Size = 10000;// sensor 缓冲池大小为1000
    private final float alpha = 0.8f;
    private String[] accData;
    private String[] gyroData;
    private String[] magData;
    private String[] bearData;

    private String accNow;
    private String gyroNow;
    private String magNow;
    // 当前写入位置
    private int acc_cur;
    private int gyro_cur;
    private int mag_cur;
    private int bear_cur;
    private int rot_cur;
    // 当前读取位置
    private int acc_old;
    private int gyro_old;
    private int mag_old;
    private int bear_old;

    private volatile float[] gravity;
    private volatile float[] magnetOri;
    private volatile float[] linear_acceleration;
    private float bearAngle;
    private String gpsBear;
    private volatile float[] DCM;
    private float mag_decl = -0.091f; //合肥磁偏角
    private boolean gravityOriNew = false;
    private boolean magOriNew = false;

    public DetectorSensorListener(AppResourceApplication resource) {
        // TODO Auto-generated constructor stub
        super();
        acc_cur = 0;
        gyro_cur = 0;
        mag_cur = 0;
        // rot_cur = 0;
        accData = new String[Data_Size];
        gyroData = new String[Data_Size];
        magData = new String[Data_Size];
        bearData = new String[Data_Size];

        magnetOri = new float[3];
        gravity = new float[3];
        linear_acceleration = new float[3];
        gpsBear = new String();
        DCM = new float[]{1, 0, 0, 0, 1, 0, 0, 0, 1};

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!threadDisable_data_update) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (gravityOriNew && magOriNew) {
                        //Log.d(TAG, "calculateOrientation");
                        calculateOrientation();
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!threadDisable_data_update) {
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // do
                    setAccData();
                    setGyroData();
                    setMagData();

                    gpsBear = gps.getCurrentBear();
                    if (gpsBear != null && Math.abs(Float.valueOf(gpsBear)) >= 0.001) {
                        Log.d(TAG, "GPS__bear：" + gpsBear);
                        setBearData(gpsBear);
                    } else {
                        String tmp = String.valueOf(getBear());
                        setBearData(tmp);
                        Log.d(TAG, "AM__bear：" + tmp);
                    }
                }
            }
        }).start();

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
                    //Log.d(TAG,"acc:"+accNow);
                    gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                    gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                    gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
                    linear_acceleration[0] = event.values[0] - gravity[0];
                    linear_acceleration[1] = event.values[1] - gravity[1];
                    linear_acceleration[2] = event.values[2] - gravity[2];
                    gravityOriNew = true;
                    float[] worldData = phoneToEarth(linear_acceleration);
                    //Log.d(TAG,"accRaw:"+String.valueOf(event.values[2]));
                    this.accNow = (new AcceleratorData(worldData)).toString();
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                if (event.values != null) {
                    float[] worldData = phoneToEarth(event.values);
                    this.gyroNow = (new GyroData(worldData)).toString();
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                if (event.values != null) {
                    float[] worldData = phoneToEarth(event.values);
                    this.magNow = (new MagnetData(worldData)).toString();
                    magnetOri[0] = event.values[0];
                    magnetOri[1] = event.values[1];
                    magnetOri[2] = event.values[2];
                    magOriNew = true;
                }
                break;
        }

    }

    public void closeSensorThread() {
        threadDisable_data_update = true;
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

    /*
    public void setRotData() {
        if (((rot_cur + 1) % Data_Size != rot_old) && RotationNow != null) {// 不满
            if (RotationNow != null) {
                this.rotationData[rot_cur] = TimeUtil.getTime(System.currentTimeMillis()) + "\t" + System.currentTimeMillis()
                        + "\t" + RotationNow;
            } else {
                this.rotationData[rot_cur] = null;
            }
            rot_cur = (rot_cur + 1) % Data_Size;
        }
    }
   */
    public void setBearData(String bear) {
        if (((bear_cur + 1) % Data_Size != bear_old)) {// 不满
            //Log.d(TAG,"bear"+bear);
            this.bearData[bear_cur] = bear;
            bear_cur = (bear_cur + 1) % Data_Size;
        }
    }

    public String getBearData() {
        if (bear_cur != bear_old) { // 不空
            int i = bear_old;
            bear_old = (bear_old + 1) % Data_Size;
            return bearData[i];
        } else return null;
    }

    public String getAccData() {
        if (acc_cur != acc_old) { // 不空
            int i = acc_old;
            acc_old = (acc_old + 1) % Data_Size;
            return accData[i];
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

    /*
    public String getRotData() {
        if (rot_cur != rot_old) {// 不空
            int i = rot_old;
            rot_old = (rot_old + 1) % Data_Size;
            return rotationData[i];
        } else
            return null;
    }
    */

    public float getBear() {
        return bearAngle;
    }

    public float[] phoneToEarth(float[] values) {
        float[] valuesEarth = new float[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                valuesEarth[i] += values[j] * DCM[3 * i + j];
            }
        }
        return valuesEarth;
    }

    public void calculateOrientation() {
        float[] values = new float[3];
        //float[] R = new float[9];
        SensorManager.getRotationMatrix(DCM, null, gravity, magnetOri);
        SensorManager.getOrientation(DCM, values);
        values[0] = (float) Math.toDegrees(values[0]);
        values[1] = (float) Math.toDegrees(values[1]);
        values[2] = (float) Math.toDegrees(values[2]);
        if (values[0] < 0) {
            values[0] += 360;
        }
        bearAngle = values[0];
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
