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

    private final String TAG = DetectorSensorListener.this.toString();
    private static final int Attitude_ANDROID = 1;
    private static final int Attitude_EKF = 2;
    private static final int Attitude_FCF = 3;
    private static int AttitudeMode = Attitude_ANDROID;
    // private AppResource resource;
    // private Data SensorData;
    // private StoreData storeData;
    // 传感器数据缓冲池
    private DetectorSensorListener mContext = DetectorSensorListener.this;
    private boolean threadDisable_data_update = false;
    private  boolean initDataPass = false;
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
    private String LinearaccNow;
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

    private volatile float[] gravityOri;
    private volatile float[] magnetOri;
    private volatile float[] linear_acceleration;
    private float bearAngle;
    private String gpsBear;
    private volatile float[] DCM;
    private volatile float[] euler;
    private boolean gravityOriOriNew = false;
    private boolean magOriNew = false;

    private LPF_I accLPF;
    private LPF_I gyroLPF;
    private LPF_I magLPF;

    private EKF ekf;
    private ekfParams ekfP;
    private ekfParamsHandle ekfPH;
    private float mag_decl = -0.091f; //合肥磁偏角
    private float[] gyroOri;
    private boolean gyroOriNew = false;
    private long time;
    private long timeOld;
    private float dt;

    private FCF fcf;

    public DetectorSensorListener(AppResourceApplication resource) {
        // TODO Auto-generated constructor stub
        super();
        acc_cur = 0;
        gyro_cur = 0;
        mag_cur = 0;
        rot_cur = 0;

        accData = new String[Data_Size];
        gyroData = new String[Data_Size];
        magData = new String[Data_Size];
        bearData = new String[Data_Size];
        rotData = new String[Data_Size];

        magnetOri = new float[3];
        gravityOri = new float[3];
        gyroOri = new float[3];

        accLPF= new LPF_I();
        gyroLPF = new LPF_I();
        magLPF = new LPF_I();

        linear_acceleration = new float[3];
        gpsBear = new String();
        DCM = new float[]{1, 0, 0, 0, 1, 0, 0, 0, 1};
        euler = new float[]{0.0f,0.0f,0.0f};

        setAttitudeMode(Attitude_ANDROID);

        if (AttitudeMode == Attitude_EKF) {
            ekfPH = new ekfParamsHandle();
            ekfP = new ekfParams();
            ekf = new EKF();
        }
        if (AttitudeMode == Attitude_FCF) {
            fcf = new FCF();
        }

        time = System.currentTimeMillis();
        timeOld = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!threadDisable_data_update) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (gravityOriOriNew && magOriNew && gyroOriNew) {
                        if (AttitudeMode == Attitude_EKF) {
                            ekf.update_vect[0] = 1;
                            ekf.update_vect[1] = 1;
                            ekf.update_vect[2] = 1;

                            ekf.z_k[0] = gyroOri[1];
                            ekf.z_k[1] = gyroOri[0];
                            ekf.z_k[2] = -gyroOri[2];

                            ekf.z_k[3] = gravityOri[1];
                            ekf.z_k[4] = gravityOri[0];
                            ekf.z_k[5] = -gravityOri[2];

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
                            ekf.AttitudeEKF(0, // approx_prediction
                                    ekfP.use_moment_inertia, ekf.update_vect, dt, ekf.z_k, ekfP.q[0], // q_rotSpeed,
                                    ekfP.q[1], // q_rotAcc
                                    ekfP.q[2], // q_acc
                                    ekfP.q[3], // q_mag
                                    ekfP.r[0], // r_gyro
                                    ekfP.r[1], // r_accel
                                    ekfP.r[2], // r_mag
                                    ekfP.moment_inertia_J, ekf.x_aposteriori_k, ekf.P_aposteriori_k, ekf.Rot_matrix, ekf.euler, ekf.debugOutput, ekf.euler_pre);
                            rotNow = ekf.euler[0] + "\t" + ekf.euler[1] + "\t" + ekf.euler[2];
                        }
                        if (AttitudeMode == Attitude_ANDROID) {
                            calculateOrientation();
                            rotNow = euler[2]+ "\t" +euler[1]+ "\t" +euler[0];
                        }
                        if (AttitudeMode == Attitude_FCF) {
                            //FCF
                            fcf.acc[0] = gravityOri[1];
                            fcf.acc[1] = gravityOri[0];
                            fcf.acc[2] = -gravityOri[2];

                            fcf.gyro[0] = gyroOri[1];
                            fcf.gyro[1] = gyroOri[0];
                            fcf.gyro[2] = -gyroOri[2];

                            fcf.mag[0] = magnetOri[1];
                            fcf.mag[1] = magnetOri[0];
                            fcf.mag[2] = -magnetOri[2];
                            fcf.dt = dt;
                            fcf.Filter();
                        }
                    }
                }
            }
        }).start();


        new Thread(new Runnable() {
            @Override
            public void run() {
                int sensorCount=0;
                while (!threadDisable_data_update) {
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // do
                    if(sensorCount<100)
                    {
                        sensorCount++;
                    }
                    else{
                        initDataPass=true;
                    }
                    if(initDataPass) {
                        setAccData();
                        setGyroData();
                        setMagData();
                        setRotData();

                        gpsBear = gps.getCurrentBear();
                        if (gpsBear != null && Math.abs(Float.valueOf(gpsBear)) >= 0.001) {
                            //Log.d(TAG, "GPS__bear：" + gpsBear);
                            setBearData(gpsBear);
                        } else {
                            String tmp = String.valueOf(getBear());
                            setBearData(tmp);
                            //Log.d(TAG, "AM__bear：" + tmp);
                        }
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        // storeData = new StoreData();
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                if (event.values != null) {
                    //Log.d(TAG,"acc:"+accNow);
                    /*
                    gravityOri[0] = alpha * gravityOri[0] + (1 - alpha) * event.values[0];
                    gravityOri[1] = alpha * gravityOri[1] + (1 - alpha) * event.values[1];
                    gravityOri[2] = alpha * gravityOri[2] + (1 - alpha) * event.values[2];
                    */
                    gravityOri = accLPF.filter(event.values);
                    linear_acceleration[0] = event.values[0] - gravityOri[0];
                    linear_acceleration[1] = event.values[1] - gravityOri[1];
                    linear_acceleration[2] = event.values[2] - gravityOri[2];
                    gravityOriOriNew = true;

                    float[] worldData = new float[3];
                    float[] temp = new float[3];
                    float[] tempL = new float[3];
                    if (AttitudeMode == Attitude_EKF) {
                        worldData = phoneToEarth(ekf.Rot_matrix, event.values);
                        temp[0] = worldData[1];
                        temp[1] = worldData[0];
                        temp[2] = -worldData[2];
                    }
                    if (AttitudeMode == Attitude_ANDROID) {
                        worldData = phoneToEarth(DCM, linear_acceleration);
                        tempL = worldData.clone();
                        worldData = phoneToEarth(DCM, event.values);
                        temp = worldData.clone();
                    }
                    if (AttitudeMode == Attitude_FCF) {
                        //worldData = phoneToEarth(DCM, event.values);
                        worldData = fcf.translate_to_BODY(fcf.q_est, event.values);
                        temp[0] = worldData[1];
                        temp[1] = worldData[0];
                        temp[2] = -worldData[2];
                    }
                    //Log.d(TAG,"accRaw:"+String.valueOf(event.values[2]));
                    this.LinearaccNow = (new AcceleratorData(tempL)).toString();
                    this.accNow =(new AcceleratorData(temp)).toString();
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                if (event.values != null) {
                    /*
                    gyroOri[0] = event.values[0];
                    gyroOri[1] = event.values[1];
                    gyroOri[2] = event.values[2];
                    */
                    gyroOri = gyroLPF.filter(event.values);
                    time = System.currentTimeMillis();
                    dt = (time - timeOld) / 1000.0f;
                    timeOld = time;
                    gyroOriNew = true;
                    float[] worldData = new float[3];
                    float[] temp = new float[3];

                    if (AttitudeMode == Attitude_EKF) {
                        worldData = phoneToEarth(ekf.Rot_matrix, event.values);
                        temp = new float[3];
                        temp[0] = worldData[1];
                        temp[1] = worldData[0];
                        temp[2] = -worldData[2];
                    }
                    if (AttitudeMode == Attitude_ANDROID) {
                        worldData = phoneToEarth(DCM, event.values);
                        temp = worldData.clone();
                    }
                    if (AttitudeMode == Attitude_FCF) {
                        //worldData = phoneToEarth(DCM, event.values);
                        worldData = fcf.translate_to_BODY(fcf.q_est, event.values);
                        temp = new float[3];
                        temp[0] = worldData[1];
                        temp[1] = worldData[0];
                        temp[2] = -worldData[2];
                    }

                    this.gyroNow = (new GyroData(temp)).toString();
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                if (event.values != null) {
                    /*
                    magnetOri[0] = event.values[0];
                    magnetOri[1] = event.values[1];
                    magnetOri[2] = event.values[2];
                    */
                    magnetOri = magLPF.filter(event.values);
                    magOriNew = true;
                    float[] worldData = new float[3];
                    float[] temp = new float[3];
                    if (AttitudeMode == Attitude_EKF) {
                        worldData = phoneToEarth(ekf.Rot_matrix, event.values);
                        temp = new float[3];
                        temp[0] = worldData[1];
                        temp[1] = worldData[0];
                        temp[2] = -worldData[2];
                    }
                    if (AttitudeMode == Attitude_ANDROID) {
                        worldData = phoneToEarth(DCM, event.values);
                        temp = worldData.clone();
                    }
                    if (AttitudeMode == Attitude_FCF) {
                        //worldData = phoneToEarth(DCM, event.values);
                        worldData = fcf.translate_to_BODY(fcf.q_est, event.values);
                        temp = new float[3];
                        temp[0] = worldData[1];
                        temp[1] = worldData[0];
                        temp[2] = -worldData[2];
                    }

                    this.magNow = (new MagnetData(temp)).toString();
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


    public void setRotData() {
        if (((rot_cur + 1) % Data_Size != rot_old) && rotNow != null) {// 不满
            if (rotNow != null) {
                this.rotData[rot_cur] = rotNow;
            } else {
                this.rotData[rot_cur] = null;
            }
            rot_cur = (rot_cur + 1) % Data_Size;
        }
    }

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

    public float[] phoneToEarth(float[] DCM, float[] values) {
        float[] valuesEarth = new float[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                valuesEarth[i] += values[j] * DCM[3 * i + j];
            }
        }
        return valuesEarth;
    }

    public float[] readLinearAccData(){
        float values[] = new float[3];
        if(LinearaccNow!=null) {
            String valuesNow = LinearaccNow;
            String[] accArray = new String[5];
            accArray = valuesNow.split("\t");
            values[0] = Float.parseFloat(accArray[0]);
            values[1] = Float.parseFloat(accArray[1]);
            values[2] = Float.parseFloat(accArray[2]);
        }
        return values;
    }

    public float[] readAccData(){
        float values[] = new float[3];
        if(accNow!=null) {
            String valuesNow = accNow;
            String[] accArray = new String[5];
            accArray = valuesNow.split("\t");
            values[0] = Float.parseFloat(accArray[0]);
            values[1] = Float.parseFloat(accArray[1]);
            values[2] = Float.parseFloat(accArray[2]);
        }
        return values;
    }

    public float[] readGyroData(){
        float values[] = new float[3];
        if(gyroNow!=null) {
            String valuesNow = gyroNow;
            String[] accArray = new String[5];
            accArray = valuesNow.split("\t");
            values[0] = Float.parseFloat(accArray[0]);
            values[1] = Float.parseFloat(accArray[1]);
            values[2] = Float.parseFloat(accArray[2]);
        }
        return values;
    }

    public float[] readMagData(){
        float values[] = new float[3];
        if(magNow!=null) {
            String valuesNow = magNow;
            String[] accArray = new String[5];
            accArray = valuesNow.split("\t");
            values[0] = Float.parseFloat(accArray[0]);
            values[1] = Float.parseFloat(accArray[1]);
            values[2] = Float.parseFloat(accArray[2]);
        }
        return values;
    }


    public void calculateOrientation() {
        float[] values = new float[3];
        //float[] R = new float[9];
        SensorManager.getRotationMatrix(DCM, null, gravityOri, magnetOri);
        SensorManager.getOrientation(DCM, euler);
        values[0] = (float) Math.toDegrees(euler[0]);
        values[1] = (float) Math.toDegrees(euler[1]);
        values[2] = (float) Math.toDegrees(euler[2]);
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
