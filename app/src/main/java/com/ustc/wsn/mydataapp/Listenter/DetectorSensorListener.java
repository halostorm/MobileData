package com.ustc.wsn.mydataapp.Listenter;
/**
 * Created by halo on 2017/7/1.
 */

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.ustc.wsn.mydataapp.Application.AppResourceApplication;
import com.ustc.wsn.mydataapp.bean.AcceleratorData;
import com.ustc.wsn.mydataapp.bean.Filter.EKF;
import com.ustc.wsn.mydataapp.bean.Filter.FCF;
import com.ustc.wsn.mydataapp.bean.Filter.GDF;
import com.ustc.wsn.mydataapp.bean.Filter.LPF_II;
import com.ustc.wsn.mydataapp.bean.Filter.MeanFilter;
import com.ustc.wsn.mydataapp.bean.Filter.ekfParams;
import com.ustc.wsn.mydataapp.bean.Filter.ekfParamsHandle;
import com.ustc.wsn.mydataapp.bean.GyroData;
import com.ustc.wsn.mydataapp.bean.MagnetData;
import com.ustc.wsn.mydataapp.bean.PhoneState;
import com.ustc.wsn.mydataapp.bean.math.myMath;

public class DetectorSensorListener implements SensorEventListener {

    private final String TAG = DetectorSensorListener.this.toString();
    private final float GRAVITY = 9.807f;
    private static int windowSize = 256;// 256
    public final int sampleInterval = 20;//ms
    private static int AttitudeMode = PhoneState.Attitude_GDF;

    // 传感器数据缓冲池
    private DetectorSensorListener mContext = DetectorSensorListener.this;
    private boolean threadDisable_data_update = false;
    private boolean initDataPass = false;
    private final int Data_Size = 10000;// sensor 缓冲池大小为1000

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

    private volatile float[] accOri;
    private volatile float[] magnetOri;
    private volatile float[] gyroOri;

    private float bearAngle;
    private String gpsBear;
    private volatile float[] DCM;
    private volatile float[] euler;

    private boolean accOriOriNew = false;
    private boolean magOriNew = false;
    private boolean gyroOriNew = false;

    private LPF_II accLPF;
    private LPF_II gyroLPF;
    private LPF_II magLPF;

    private MeanFilter accMF;
    private MeanFilter gyroMF;
    private MeanFilter magMF;

    private EKF ekf;
    private ekfParams ekfP;
    private ekfParamsHandle ekfPH;
    private float mag_decl = -0.091f; //合肥磁偏角

    private long time;
    private long timeOld;
    private float dt;
    private FCF fcf;

    private GDF gdf;

    public DetectorSensorListener(AppResourceApplication resource) {
        // TODO Auto-generated constructor stub
        super();
        acc_cur=0;
        gyro_cur = 0;
        mag_cur = 0;
        rot_cur = 0;

        accData = new String[Data_Size];
        gyroData = new String[Data_Size];
        magData = new String[Data_Size];
        bearData = new String[Data_Size];
        rotData = new String[Data_Size];

        magnetOri = new float[3];
        accOri = new float[3];
        gyroOri = new float[3];

        accLPF = new LPF_II();
        gyroLPF = new LPF_II();
        magLPF = new LPF_II();

        accMF = new MeanFilter(5);
        gyroMF = new MeanFilter(5);
        magMF = new MeanFilter(5);

        gpsBear = new String();
        DCM = new float[]{1, 0, 0, 0, 1, 0, 0, 0, 1};
        euler = new float[]{0.0f, 0.0f, 0.0f};

        //setAttitudeMode(PhoneState.Attitude_FCF);////////////////////////////////////: attitude estimator is PhoneState.Attitude_ANDROID

        if (AttitudeMode == PhoneState.Attitude_EKF) {
            ekfPH = new ekfParamsHandle();
            ekfP = new ekfParams();
            ekf = new EKF();
            //ekf.AttitudeEKF_initialize();
        }
        if (AttitudeMode == PhoneState.Attitude_FCF) {
            fcf = new FCF();
        }

        if (AttitudeMode == PhoneState.Attitude_GDF) {
            gdf = new GDF();
        }

        time = System.nanoTime();
        dt = (time - timeOld)/1000000000f;
        timeOld = time;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!threadDisable_data_update) {
                    try {
                        Thread.sleep(sampleInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    gpsBear = DetectorLocationListener.getCurrentBear();
                    if (gpsBear != null && Math.abs(Float.valueOf(gpsBear)) >= 0.001) {
                        //Log.d(TAG, "GPS__bear：" + gpsBear);
                        setBearData(gpsBear);
                    } else {
                        String tmp = String.valueOf(getBear());
                        setBearData(tmp);
                        //Log.d(TAG, "AM__bear：" + tmp);
                    }

                    if (accOriOriNew && magOriNew && gyroOriNew) {
                        if (AttitudeMode == PhoneState.Attitude_EKF) {
                            ekf.update_vect[0] = 1;
                            ekf.update_vect[1] = 1;
                            ekf.update_vect[2] = 1;

                            ekf.z_k[0] = gyroOri[1];
                            ekf.z_k[1] = gyroOri[0];
                            ekf.z_k[2] = -gyroOri[2];

                            ekf.z_k[3] = accOri[1];
                            ekf.z_k[4] = accOri[0];
                            ekf.z_k[5] = -accOri[2];

                            ekf.z_k[6] = magnetOri[1] / 100.f;
                            ekf.z_k[7] = magnetOri[0] / 100.f;
                            ekf.z_k[8] = -magnetOri[2] / 100.f;

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
                            ekf.dt = (System.nanoTime()-ekf.time)/1000000000.f;
                            ekf.AttitudeEKF(false, // approx_prediction
                                    ekfP.use_moment_inertia, ekf.update_vect, ekf.dt, ekf.z_k, ekfP.q0, // q_rotSpeed,
                                    ekfP.q1, // q_rotAcc
                                    ekfP.q2, // q_acc
                                    ekfP.q3, // q_mag
                                    ekfP.r0, // r_gyro
                                    ekfP.r1, // r_accel
                                    ekfP.r2, // r_mag
                                    ekfP.moment_inertia_J, ekf.x_aposteriori_k, ekf.P_aposteriori_k, ekf.Rot_matrix, ekf.euler, ekf.debugOutput, ekf.euler_pre);
                            ekf.time = System.nanoTime();
                            //ekf.Ned2Android();
                            rotNow = ekf.euler[0] + "\t" + ekf.euler[1] + "\t" + ekf.euler[2];
                        }
                        if (AttitudeMode == PhoneState.Attitude_ANDROID) {
                            calBear();
                            rotNow = euler[2] + "\t" + euler[1] + "\t" + euler[0];
                        }
                        if (AttitudeMode == PhoneState.Attitude_FCF) {
                            fcf.acc[0] = accOri[1];
                            fcf.acc[1] = accOri[0];
                            fcf.acc[2] = -accOri[2];

                            fcf.gyro[0] = gyroOri[1];
                            fcf.gyro[1] = gyroOri[0];
                            fcf.gyro[2] = -gyroOri[2];

                            fcf.mag[0] = magnetOri[1];
                            fcf.mag[1] = magnetOri[0];
                            fcf.mag[2] = -magnetOri[2];
                            fcf.dt = dt;
                            fcf.attitude(dt);
                            rotNow = fcf.euler[0] + "\t" + fcf.euler[1] + "\t" + fcf.euler[2];
                        }
                        if(AttitudeMode ==PhoneState.Attitude_GDF){
                            float[] _accOri = accOri;
                            float[] _gyroOri = gyroOri;
                            float[] _magOri = magnetOri;

                            gdf.Filter(_gyroOri[1],-_gyroOri[0],_gyroOri[2],
                                    _accOri[1],-_accOri[0],_accOri[2],
                                    _magOri[1],-_magOri[0],_magOri[2], dt,false);

                            rotNow = gdf.Euler[0] + "\t" + gdf.Euler[1] + "\t" + gdf.Euler[2];
                        }
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
                        Thread.sleep(sampleInterval);
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
                        setAccData();
                        setGyroData();
                        setMagData();
                        setRotData();
                    }
                }
            }
        }).start();

    }

    public void setAttitudeMode(final int Mode) {
        AttitudeMode = Mode;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    public void closeSensorThread() {
        threadDisable_data_update = true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        // storeData = new StoreData();
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                if (event.values != null) {
                    accOri = accLPF.filter(event.values);
                    accOriOriNew = true;

                    float[] worldData = new float[3];
                    if (AttitudeMode == PhoneState.Attitude_EKF) {
                        worldData = myMath.Rot_coordinatesTransform(ekf.Rot_matrix, event.values);
                    }
                    if (AttitudeMode == PhoneState.Attitude_GDF) {
                        worldData = myMath.Rot_coordinatesTransform(gdf.Rot_Matrix, event.values);
                    }
                    if (AttitudeMode == PhoneState.Attitude_ANDROID) {
                        worldData = myMath.Rot_coordinatesTransform(DCM, event.values);
                    }
                    if (AttitudeMode == PhoneState.Attitude_FCF) {
                        //worldData = myMath.Rot_coordinatesTransform(DCM, event.values);
                        worldData = myMath.Rot_coordinatesTransform(fcf.Rot_matrix, event.values);
                    }
                    this.accNow = (new AcceleratorData(event.values)).toString();
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                if (event.values != null) {
                    gyroOri = gyroLPF.filter(event.values);
                    gyroOriNew = true;
                    float[] worldData = new float[3];

                    if (AttitudeMode == PhoneState.Attitude_EKF) {
                        worldData = myMath.Rot_coordinatesTransform(ekf.Rot_matrix, event.values);
                    }
                    if (AttitudeMode == PhoneState.Attitude_GDF) {
                        worldData = myMath.Rot_coordinatesTransform(gdf.Rot_Matrix, event.values);
                    }
                    if (AttitudeMode == PhoneState.Attitude_ANDROID) {
                        worldData = myMath.Rot_coordinatesTransform(DCM, event.values);
                    }
                    if (AttitudeMode == PhoneState.Attitude_FCF) {
                        worldData =  myMath.Rot_coordinatesTransform(fcf.Rot_matrix, event.values);
                    }

                    this.gyroNow = (new GyroData(event.values)).toString();
                    timeOld = time;
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                if (event.values != null) {
                    magnetOri = magLPF.filter(event.values);
                    magOriNew = true;
                    float[] worldData = new float[3];
                    if (AttitudeMode == PhoneState.Attitude_EKF) {
                        worldData = myMath.Rot_coordinatesTransform(ekf.Rot_matrix, event.values);
                    }
                    if (AttitudeMode == PhoneState.Attitude_GDF) {
                        worldData = myMath.Rot_coordinatesTransform(gdf.Rot_Matrix, event.values);
                    }
                    if (AttitudeMode == PhoneState.Attitude_ANDROID) {
                        worldData = myMath.Rot_coordinatesTransform(DCM, event.values);
                    }
                    if (AttitudeMode == PhoneState.Attitude_FCF) {
                        //worldData = myMath.Rot_coordinatesTransform(DCM, event.values);
                        worldData =  myMath.Rot_coordinatesTransform(fcf.Rot_matrix, event.values);
                    }
                    //this.magNow = (new MagnetData(temp)).toString();
                    this.magNow = (new MagnetData(event.values)).toString();
                }
                break;
        }

    }


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

    public float getBear() {
        return bearAngle;
    }

    private void calBear() {
        float[] values = new float[3];
        //float[] R = new float[9];
        SensorManager.getRotationMatrix(DCM, null, accOri, magnetOri);
        SensorManager.getOrientation(DCM, euler);
        values[0] = (float) Math.toDegrees(euler[0]);
        values[1] = (float) Math.toDegrees(euler[1]);
        values[2] = (float) Math.toDegrees(euler[2]);
        if (values[0] < 0) {
            values[0] += 360;
        }
        bearAngle = values[0];
    }
}
