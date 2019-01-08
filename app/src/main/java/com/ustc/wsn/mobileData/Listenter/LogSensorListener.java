package com.ustc.wsn.mobileData.Listenter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.ustc.wsn.mobileData.bean.AcceleratorData;
import com.ustc.wsn.mobileData.bean.EulerData;
import com.ustc.wsn.mobileData.bean.Filter.EKF;
import com.ustc.wsn.mobileData.bean.Filter.FCF;
import com.ustc.wsn.mobileData.bean.Filter.GDF;
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
    private String[] rotData;
    private String[] rotData1;
    private String[] rotData2;
    private String[] rotData3;

    private String accNow;
    private String gyroNow;
    private String magNow;
    private String rotNow;
    private String rotNow1;
    private String rotNow2;
    private String rotNow3;
    // 当前写入位置
    private int acc_cur;
    private int gyro_cur;
    private int mag_cur;
    private int rot_cur;
    private int rot_cur1;
    private int rot_cur2;
    private int rot_cur3;
    // 当前读取位置

    private int acc_old;
    private int gyro_old;
    private int mag_old;
    private int rot_old;
    private int rot_old1;
    private int rot_old2;
    private int rot_old3;

    private EKF ekf;
    private ekfParams ekfP;
    private ekfParamsHandle ekfPH;

    private volatile float[] acc = new float[3];
    private volatile float[] gyro = new float[3];
    private volatile float[] mag = new float[3];

    long time = 0;
    long timeOld = System.nanoTime();
    float dt = 0.02f;

    int gdf_init = 0;

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
        rot_cur1 = 0;
        rot_cur2 = 0;
        rot_cur3 = 0;

        accData = new String[Data_Size];
        gyroData = new String[Data_Size];
        magData = new String[Data_Size];
        rotData = new String[Data_Size];
        rotData1 = new String[Data_Size];
        rotData2 = new String[Data_Size];
        rotData3 = new String[Data_Size];

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
                    time = event.timestamp;
                    dt = (time - timeOld) / 1000000000f;
                    timeOld = time;

                    ekfAtt(acc,gyro,mag,dt);
                    if(myMath.isLegalArray(ekf.q)) {
//                        float[] euler = myMath.Q2Euler(ekf.q);
//                        rotNow = (new EulerData(euler)).toString();
                        rotNow = (new RotationData(ekf.q)).toString();
                        setRotData();
                    }
                    /*
                    if(gdf_init++<100)
                        gdf.q = ekf.q.clone();
                    gdfAtt(acc,gyro,mag,dt);
                    if(myMath.isLegalArray(gdf.q)) {
                        float[] euler = myMath.Q2Euler(gdf.q);
                        rotNow1 = (new EulerData(euler)).toString();
                        setRotData1();
                    }

                    fcfAtt(acc,gyro,mag,dt);
                    if(myMath.isLegalArray(fcf.q)) {
                        float[] euler = myMath.Q2Euler(fcf.q);
                        rotNow2 = (new EulerData(euler)).toString();
                        setRotData2();
                    }

                    androidAtt(acc,mag);
                    if(myMath.isLegalArray(androidQ)) {
                        float[] euler = myMath.Q2Euler(androidQ);
                        rotNow3 = (new EulerData(euler)).toString();
                        setRotData3();
                    }
                    */
                }
        }
    }

    //Store Task

    private void setAccData() {
        if (((acc_cur + 1) % Data_Size != acc_old) && accNow != null) {// 不满
            if (accNow != null) {
                this.accData[acc_cur] = System.currentTimeMillis() + "\t" + accNow;
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

    private void setRotData1() {
        if (((rot_cur1 + 1) % Data_Size != rot_old1) && rotNow1 != null) {// 不满
            if (rotNow1 != null) {
                this.rotData1[rot_cur1] = rotNow1;
            } else {
                this.rotData1[rot_cur1] = null;
            }
            rot_cur1 = (rot_cur1 + 1) % Data_Size;
        }
    }

    private void setRotData2() {
        if (((rot_cur2 + 1) % Data_Size != rot_old2) && rotNow2 != null) {// 不满
            if (rotNow2 != null) {
                this.rotData2[rot_cur2] = rotNow2;
            } else {
                this.rotData2[rot_cur2] = null;
            }
            rot_cur2 = (rot_cur2 + 1) % Data_Size;
        }
    }

    private void setRotData3() {
        if (((rot_cur3 + 1) % Data_Size != rot_old3) && rotNow3 != null) {// 不满
            if (rotNow3 != null) {
                this.rotData3[rot_cur3] = rotNow3;
            } else {
                this.rotData3[rot_cur3] = null;
            }
            rot_cur3 = (rot_cur3 + 1) % Data_Size;
        }
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

    public String getRotData1() {
        if (rot_cur1 != rot_old1) {// 不空
            int i = rot_old1;
            rot_old1 = (rot_old1 + 1) % Data_Size;
            return rotData1[i];
        } else return null;
    }

    public String getRotData2() {
        if (rot_cur2 != rot_old2) {// 不空
            int i = rot_old2;
            rot_old2 = (rot_old2 + 1) % Data_Size;
            return rotData2[i];
        } else return null;
    }

    public String getRotData3() {
        if (rot_cur3 != rot_old3) {// 不空
            int i = rot_old3;
            rot_old3 = (rot_old3 + 1) % Data_Size;
            return rotData3[i];
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

    private GDF gdf = new GDF();
    private void gdfAtt(float[] accOri, float[] gyroOri, float[] magOri, float dt) {
        gdf.Filter(gyroOri[0], -gyroOri[1], -gyroOri[2], accOri[0], -accOri[1], -accOri[2], magOri[0], -magOri[1], -magOri[2], dt, false);
    }

    private volatile float[] androidDCM = new float[]{1.f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f};
    private volatile float[] androidQ = new float[]{1.f, 0f, 0f, 0f};

    private float[] androidAtt(float[] accOri, float[] magOri) {
        float[] _accOri = myMath.V_android2Ned(accOri);
        float[] _magOri = myMath.V_android2Ned(magOri);

        float[] aDCM = new float[9];
        SensorManager.getRotationMatrix(aDCM, null, _accOri, _magOri);
        androidDCM = myMath.R_android2Ned(aDCM);
        androidQ = myMath.Rot2Q(androidDCM);
        return myMath.R_android2Ned(aDCM).clone();
    }

    private FCF fcf = new FCF();
    private void fcfAtt(float[] accOri, float[] gyroOri, float[] magOri, float dt) {

        accOri[0] *= -1;
        accOri[2] *= -1;
        gyroOri[0] *= -1;
        gyroOri[2] *= -1;
        magOri[0] *= -1;
        magOri[2] *= -1;
        fcf.acc = accOri.clone();
        fcf.gyro = gyroOri.clone();
        fcf.mag = magOri.clone();
        fcf.dt = dt;
        fcf.attitude(dt);
    }

}
