package com.ustc.wsn.mobileData.Listenter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import com.ustc.wsn.mobileData.bean.AcceleratorData;
import com.ustc.wsn.mobileData.bean.Filter.EKF;
import com.ustc.wsn.mobileData.bean.Filter.ekfParams;
import com.ustc.wsn.mobileData.bean.Filter.ekfParamsHandle;
import com.ustc.wsn.mobileData.bean.GyroData;
import com.ustc.wsn.mobileData.bean.MagnetData;
import com.ustc.wsn.mobileData.bean.PhoneState;
import com.ustc.wsn.mobileData.bean.RotationData;
import com.ustc.wsn.mobileData.bean.math.myMath;

/**
 * Created by halo on 2018/4/11.
 */

public class LogSensorListener implements SensorEventListener {
    private final String TAG = LogSensorListener.this.toString();
    /////////////////////////////////////////////////////////////////////////store
    // 传感器数据缓冲池
    private final int Data_Size = 10000;// sensor 缓冲池大小为1000

    //加速度校准参数
    private static float[] params;

    private float AccRange;
    private float GyroRange;
    private float MagRange;
    private float RangeK = 0.8f;

    //private String timestamp = "";
    private String[] accData;
    private String[] gyroData;
    private String[] magData;
    private String[] bearData;
    private String[] rotData;

    private String accNow;
    private String gyroNow;
    private String magNow;
    private String rotNow;
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
    private int rot_old;

    private EKF ekf;
    private ekfParams ekfP;
    private ekfParamsHandle ekfPH;

    private volatile float[] acc = new float[3];
    private volatile float[] gyro = new float[3];
    private volatile float[] mag = new float[3];

    long time = 0;
    long timeOld = System.nanoTime();
    float dt = 0.02f;

    public LogSensorListener(float accMaxRange, float gyroMaxRange, float magMaxRange) {
        // TODO Auto-generated constructor stub
        super();
        ///store Task
        getAccCalibrateParams();

        ekfPH = new ekfParamsHandle();
        ekfP = new ekfParams();
        ekf = new EKF();
        //ekf.AttitudeEKF_initialize();

        Log.d(TAG, "Store Task");

        acc_cur = 0;
        gyro_cur = 0;
        mag_cur = 0;
        rot_cur = 0;

        accData = new String[Data_Size];
        gyroData = new String[Data_Size];
        magData = new String[Data_Size];
        bearData = new String[Data_Size];
        rotData = new String[Data_Size];

        //传感器量程
        AccRange = accMaxRange;
        GyroRange = gyroMaxRange;
        MagRange = magMaxRange;
    }

    private void getAccCalibrateParams() {
        PhoneState.initAccCalibrateParams();
        params = PhoneState.getCalibrateParams();
    }

    private float[] AccCalibrate(float[] rData) {
        float[] data = new float[3];
        data[0] = rData[0] - params[9];
        data[1] = rData[1] - params[10];
        data[2] = rData[2] - params[11];
        //减去shift
        float[] aData = new float[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                aData[i] += params[i * 3 + j] * data[j];
            }
        }
        return aData.clone();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                if (event.values != null) {
                    if (myMath.getMoulding(event.values) < RangeK * AccRange) {
                        float[] _rawacc = event.values.clone();
                        float[] rawacc = myMath.V_android2Ned(_rawacc);
                        acc = AccCalibrate(rawacc);
                        accNow = (new AcceleratorData(acc)).toString();
                        setAccData();
                    }
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                if (event.values != null) {
                    if (myMath.getMoulding(event.values) < RangeK * GyroRange) {
                        float[] rawgyro = event.values.clone();
                        gyro = myMath.V_android2Ned(rawgyro);
                        gyroNow = (new GyroData(gyro)).toString();
                        setGyroData();
                    }
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                if (event.values != null) {
                    if (myMath.getMoulding(event.values) < RangeK * MagRange) {
                        float[] rawmag = event.values.clone();
                        //myLog.log(TAG,"mag raw:",_mag);
                        mag = myMath.V_android2Ned(rawmag);
                        magNow = (new MagnetData(mag)).toString();
                        setMagData();
                    }
                }
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                if (event.values != null) {
                    //bear数据优先选择GPS提供，其次选择惯导提供
                    time = System.nanoTime();
                    dt = (time - timeOld) / 1000000000f;
                    timeOld = time;

                    ekfAtt(acc,gyro,mag,dt);

                    rotNow = (new RotationData(ekf.q)).toString();
                    setRotData();

                    String gpsBear = DetectorLocationListener.getCurrentBear();
                    if (gpsBear != null && Math.abs(Float.valueOf(gpsBear)) >= 0.001) {
                        //Log.d(TAG,"GPS__bear："+gpsBear);
                        setBearData(gpsBear);
                    } else{
                        String bear = String.valueOf(ekf.euler[2]/ Math.PI*180 -myMath.DECLINATION);
                        setBearData(bear);
                        //Log.d(TAG,"AM__bear："+ bear);
                    }
                }
        }
    }

    //Store Task

    private void setAccData() {
        if (((acc_cur + 1) % Data_Size != acc_old) && accNow != null) {// 不满
            if (accNow != null) {
                this.accData[acc_cur] = System.nanoTime() + "\t" + accNow;
            } else {
                this.accData[acc_cur] = null;
            }
            acc_cur = (acc_cur + 1) % Data_Size;
        }
    }

    private void setGyroData() {
        if (((gyro_cur + 1) % Data_Size != gyro_old) && gyroNow != null) {// 不满
            if (gyroNow != null) {
                this.gyroData[gyro_cur] = gyroNow;
            } else {
                this.gyroData[gyro_cur] = null;
            }
            gyro_cur = (gyro_cur + 1) % Data_Size;
        }
    }

    private void setMagData() {
        if (((mag_cur + 1) % Data_Size != mag_old) && magNow != null) {// 不满
            if (magNow != null) {
                this.magData[mag_cur] = magNow;
            } else {
                this.magData[mag_cur] = null;
            }
            mag_cur = (mag_cur + 1) % Data_Size;
        }
    }


    private void setRotData() {
        if (((rot_cur + 1) % Data_Size != rot_old) && rotNow != null) {// 不满
            if (rotNow != null) {
                this.rotData[rot_cur] = rotNow;
            } else {
                this.rotData[rot_cur] = null;
            }
            rot_cur = (rot_cur + 1) % Data_Size;
        }
    }

    private void setBearData(String bear) {
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

    public String getRotData() {
        if (rot_cur != rot_old) {// 不空
            int i = rot_old;
            rot_old = (rot_old + 1) % Data_Size;
            return rotData[i];
        } else return null;
    }

    private void ekfAtt(float[] accOri, float[] gyroOri, float[] magOri, float dt) {
        ekf.update_vect[0] = 1;
        ekf.update_vect[1] = 1;
        ekf.update_vect[2] = 1;

        ekf.z_k[0] = gyroOri[0];
        ekf.z_k[1] = gyroOri[1];
        ekf.z_k[2] = gyroOri[2];

        ekf.z_k[3] = accOri[0];
        ekf.z_k[4] = accOri[1];
        ekf.z_k[5] = accOri[2];

        ekf.z_k[6] = magOri[0] / 100.f;
        ekf.z_k[7] = magOri[1] / 100.f;
        ekf.z_k[8] = magOri[2] / 100.f;

        ekf.x_aposteriori_k[0] = ekf.z_k[0];
        ekf.x_aposteriori_k[1] = ekf.z_k[1];
        ekf.x_aposteriori_k[2] = ekf.z_k[2];
        ekf.x_aposteriori_k[3] = 0.0f;
        ekf.x_aposteriori_k[4] = 0.0f;
        ekf.x_aposteriori_k[5] = 0.0f;
        ekf.x_aposteriori_k[6] = ekf.z_k[3];
        ekf.x_aposteriori_k[7] = ekf.z_k[4];
        ekf.x_aposteriori_k[8] = ekf.z_k[5];
        ekf.x_aposteriori_k[9] = ekf.z_k[6];
        ekf.x_aposteriori_k[10] = ekf.z_k[7];
        ekf.x_aposteriori_k[11] = ekf.z_k[8];
        //Log.d(TAG, "calculateOrientation");
        ekfP.parameters_update(ekfPH);
        ekf.dt = dt;
        ekf.AttitudeEKF(false, // approx_prediction
                ekfP.use_moment_inertia, ekf.update_vect, ekf.dt, ekf.z_k, ekfP.q0, // q_rotSpeed,
                ekfP.q1, // q_rotAcc
                ekfP.q2, // q_acc
                ekfP.q3, // q_mag
                ekfP.r0, // r_gyro
                ekfP.r1, // r_accel
                ekfP.r2, // r_mag
                ekfP.moment_inertia_J, ekf.x_aposteriori, ekf.P_aposteriori, ekf.Rot_matrix, ekf.euler, ekf.debugOutput, ekf.euler_pre);
        //ekf.time = System.nanoTime();
        ekf.q = myMath.Rot2Q(ekf.Rot_matrix);
    }
}
