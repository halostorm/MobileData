package com.ustc.wsn.detector.detectorservice;
/**
 * Created by halo on 2017/7/1.
 */

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.ustc.wsn.detector.Application.AppResourceApplication;
import com.ustc.wsn.detector.bean.AcceleratorData;
import com.ustc.wsn.detector.bean.GyroData;
import com.ustc.wsn.detector.bean.MagnetData;
import com.ustc.wsn.detector.utils.TimeUtil;

public class DetectorSensorListener implements SensorEventListener {

    private static final String TAG = null;
    // private AppResource resource;
    // private Data SensorData;
    // private StoreData storeData;
    // 传感器数据缓冲池
    private boolean threadDisable_data_update = false;
    private static int Data_Size = 10000;// sensor 缓冲池大小为1000
    private static float alpha = (float) 0.8;
    private String[] accData;
    private String[] gyroData;
    private String[] magData;
    private String[] bearData;
    // private volatile RotationData[] rotationData;

    private String accNow;
    private String gyroNow;
    private String magNow;
    // private volatile RotationData RotationNow;
    // 当前写入位置
    private int acc_cur;
    private int gyro_cur;
    private int mag_cur;
    private int bear_cur;
    // private int rot_cur;
    // 当前读取位置
    private int acc_old;
    private int gyro_old;
    private int mag_old;
    private int bear_old;
    // private int rot_old;

    private float[] accelOri;
    private float[] gravity;
    private float[] magnetOri;
    private float directionAngle;
    private String gpsBear;
   // private int bear_count=0;

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

        accelOri = new float[3];
        magnetOri = new float[3];
        gravity = new float[3];
        gpsBear = new String();
        // rotationData = new RotationData[Data_Size];

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
                    //bear数据优先选择GPS提供，其次选择惯导提供
                    gpsBear = gps.getCurrentBear();

                    if (gpsBear != null && Math.abs(Float.valueOf(gpsBear)) >= 0.001) {
                        Log.d(TAG,"GPS__bear："+gpsBear);
                        setBearData(gpsBear);
                    } else{
                        calculateOrientation();
                        String tmp = String.valueOf(getAngleData());
                        setBearData(tmp);
                        Log.d(TAG,"AM__bear："+ tmp);
                    }
                    //gps.setCurrentLocationToNull();

                    // setDirectAng();
                    // setRotationData();
                }
            }
        }).start();

    }
    // storeData = new StoreData();
    // this.resource = resource;

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
                // Log.d("Detector", "onSensorChanged: " + "Accelerator" + ", x: "
                // + event.values[0] + ", y: " + event.values[1] + ", z: "
                // + event.values[2]);
                if (event.values != null) {
                    this.accNow = (new AcceleratorData(event.values)).toString();

                    accelOri = new float[3];
                    accelOri[0] = event.values[0];
                    accelOri[1] = event.values[1];
                    accelOri[2] = event.values[2];

                    gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                    gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                    gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
                    // AcceleratorData accData = new AcceleratorData(event.values,
                    // System.currentTimeMillis());
                    // boolean road = resource.roadIsOK(accData, null);
                    // boolean stop = resource.suddenStop(accData, null);
                    // boolean shift = resource.suddenShift(accData, null);
                    // resource.updataAccDatas(accData);
                    // StoreData storeData = new StoreData();
                    // try {
                    // storeData.storeDataAccelerator(accData);
                    // } catch (IOException e) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                    // }
                /*
				 * if (road == false || shift == false || stop == false) {
				 * String headString = "[";
				 *
				 * if (shift == false) headString += "1 "; else headString +=
				 * "0 ";
				 *
				 * if (stop == false) headString += " 2"; else headString +=
				 * " 0";
				 *
				 * if (road == false) headString += " 3"; else headString +=
				 * " 0";
				 *
				 * headString += "]"; //resource.AddMsgToQueue(headString +
				 * "\t$\t" + resource.getData().toStringExcptLoc(), 0); }
				 */

                    // storeData.storeDataAccelerator(accData);
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                // Log.d("Detector", "onSensorChanged: " + "Gyro" + ", x: "
                // + event.values[0] + ", y: " + event.values[1] + ", z: "
                // + event.values[2]);
                if (event.values != null) {
                    this.gyroNow = (new GyroData(event.values)).toString();
                    // gyroData = new GyroData(event.values,
                    // System.currentTimeMillis());
                    // resource.updateGyroDatas(gyroData);
                    // try {
                    // storeData.storeDataGyroscope(gyroData);
                    // } catch (IOException e) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                    // }
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                // Log.d("Detector", "onSensorChanged: " + "Gyro" + ", x: "
                // + event.values[0] + ", y: " + event.values[1] + ", z: "
                // + event.values[2]);
                if (event.values != null) {
                    //Log.d(TAG, "mag:" + event.values[0] + " " + event.values[1] + " " + event.values[2]);
                    this.magNow = (new MagnetData(event.values)).toString();

                    magnetOri = new float[3];
                    magnetOri[0] = event.values[0];
                    magnetOri[1] = event.values[1];
                    magnetOri[2] = event.values[2];
                    // magData = new MagnetData(event.values,
                    // System.currentTimeMillis());
                    // resource.updateMagDatas(magData);
                    // try {
                    // storeData.storeDataMagnetic(magData);
                    // } catch (IOException e) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                    // }
                }
                break;
            // case Sensor.TYPE_ROTATION_VECTOR:
            // Log.d("Detector", "onSensorChanged: " + "Gyro" + ", x: "
            // + event.values[0] + ", y: " + event.values[1] + ", z: "
            // + event.values[2]);
            // if (event.values != null) {
            // this.RotationNow = (new RotationData(event.values,
            // System.currentTimeMillis()));
            // gyroData = new GyroData(event.values,
            // System.currentTimeMillis());
            // resource.updateGyroDatas(gyroData);
            // try {
            // storeData.storeDataGyroscope(gyroData);
            // } catch (IOException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            // }
            // break;
        }

    }

    public void closeSensorThread() {
        threadDisable_data_update = true;
    }

    public void setAccData() {
        if (((acc_cur + 1) % Data_Size != acc_old) && accNow != null) {// 不满
            if (accNow != null) {
                this.accData[acc_cur] = TimeUtil.getTime(System.currentTimeMillis()) + "\t" + System.currentTimeMillis()
                        + "\t" + accNow;
            } else {
                this.accData[acc_cur] = null;
            }
            acc_cur = (acc_cur + 1) % Data_Size;
        }
    }

    public void setGyroData() {
        if (((gyro_cur + 1) % Data_Size != gyro_old) && gyroNow != null) {// 不满
            if (gyroNow != null) {
                this.gyroData[gyro_cur] = TimeUtil.getTime(System.currentTimeMillis()) + "\t"
                        + String.valueOf(System.currentTimeMillis()) + "\t" + gyroNow;
            } else {
                this.gyroData[gyro_cur] = null;
            }
            gyro_cur = (gyro_cur + 1) % Data_Size;
        }
    }

    public void setMagData() {
        if (((mag_cur + 1) % Data_Size != mag_old) && magNow != null) {// 不满
            if (magNow != null) {
                this.magData[mag_cur] = TimeUtil.getTime(System.currentTimeMillis()) + "\t"
                        + String.valueOf(System.currentTimeMillis()) + "\t" + magNow;
            } else {
                this.magData[mag_cur] = null;
            }
            mag_cur = (mag_cur + 1) % Data_Size;
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
        } else
            return null;
    }

    /*
     * public void setRotationData() { if ((rot_cur + 1) % Data_Size != rot_old)
     * {// 不满 this.rotationData[rot_cur] = RotationNow; rot_cur = (rot_cur + 1)
     * % Data_Size; } }
     */
    public String getAccData() {
        if (acc_cur != acc_old) { // 不空
            int i = acc_old;
            acc_old = (acc_old + 1) % Data_Size;
            return accData[i];
        } else
            return null;
    }

    public String getGyroData() {
        if (gyro_cur != gyro_old) {// 不空
            int i = gyro_old;
            gyro_old = (gyro_old + 1) % Data_Size;
            return gyroData[i];
        } else
            return null;
    }

    public String getMagData() {
        if (mag_cur != mag_old) {// 不空
            int i = mag_old;
            mag_old = (mag_old + 1) % Data_Size;
            return magData[i];
        } else
            return null;
    }

    public float getAngleData() {
        return directionAngle;
    }

    /*
     * public RotationData getRotationData() { if (rot_cur != rot_old) {// 不空
     * int i = rot_old; rot_old = (rot_old + 1) % Data_Size; return
     * rotationData[i]; } else return null; }
     */
    public void calculateOrientation() {
        float[] values = new float[3];
        // float[] q = new float[4];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, gravity, magnetOri);
        SensorManager.getOrientation(R, values);
        // test
        // Log.d(TAG, "origin"+R[0]+" "+R[1]+" "+R[2]+" "+R[3]+" "+R[4]+"
        // "+R[5]+" "+R[6]+" "+R[7]+" "+R[8]);
        // eulerAnglesToQuaternion(values,q);
        // quaternionToRotationMatrix(q,R);
        // Log.d(TAG, "after"+R[0]+" "+R[1]+" "+R[2]+" "+R[3]+" "+R[4]+"
        // "+R[5]+" "+R[6]+" "+R[7]+" "+R[8]);
        // test
        // 要经过一次数据格式的转换，转换为度

        values[0] = (float) Math.toDegrees(values[0]);
        if(values[0] < 0)
        {
            values[0] += 360;
        }
        directionAngle = values[0];
        // Log.d(TAG, values[0]+"");
        // values[1] = (float) Math.toDegrees(values[1]);
        // values[2] = (float) Math.toDegrees(values[2]);
		/*
		 * if(values[0] >= -5 && values[0] < 5){ Log.d(TAG, "正北"); } else
		 * if(values[0] >= 5 && values[0] < 85){ Log.d(TAG, "东北"); } else
		 * if(values[0] >= 85 && values[0] <=95){ Log.d(TAG, "正东"); } else
		 * if(values[0] >= 95 && values[0] <175){ Log.d(TAG, "东南"); } else
		 * if((values[0] >= 175 && values[0] <= 180) || (values[0]) >= -180 &&
		 * values[0] < -175){ Log.d(TAG, "正南"); } else if(values[0] >= -175 &&
		 * values[0] <-95){ Log.d(TAG, "西南"); } else if(values[0] >= -95 &&
		 * values[0] < -85){ Log.d(TAG, "正西"); } else if(values[0] >= -85 &&
		 * values[0] <-5){ Log.d(TAG, "西北"); }
		 */
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
        rMat[1] = 2.0f * (q1q2 + -q0q3);
        rMat[2] = 2.0f * (q1q3 - -q0q2);

        rMat[3] = 2.0f * (q1q2 - -q0q3);
        rMat[4] = 1.0f - 2.0f * q1q1 - 2.0f * q3q3;
        rMat[5] = 2.0f * (q2q3 + -q0q1);

        rMat[6] = 2.0f * (q1q3 + -q0q2);
        rMat[7] = 2.0f * (q2q3 - -q0q1);
        rMat[8] = 1.0f - 2.0f * q1q1 - 2.0f * q2q2;
    }

}
