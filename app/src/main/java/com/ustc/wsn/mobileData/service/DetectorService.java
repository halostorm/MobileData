package com.ustc.wsn.mobileData.service;
/**
 * Created by halo on 2017/7/1.
 */

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.ustc.wsn.mobileData.Listenter.DetectorLocationListener;
import com.ustc.wsn.mobileData.Listenter.LogSensorListener;
import com.ustc.wsn.mobileData.Listenter.TrackSensorListener;
import com.ustc.wsn.mobileData.bean.CellInfo;
import com.ustc.wsn.mobileData.bean.StoreData;
import com.ustc.wsn.mobileData.bean.outputFile;
import com.ustc.wsn.mobileData.utils.TimeUtil;
import com.ustc.wsn.mobileData.utils.z7Compression;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class DetectorService extends Service {
    protected final String TAG = DetectorService.this.toString();
    public volatile int stateLabel = 0;
    private Toast t;
    ArrayList<CellInfo> cellIds = null;
    private SensorManager sm;
    private boolean ACCELERATOR_EXIST = false;
    private boolean GYROSCROPE_EXIST = false;
    private boolean MAGNETIC_EXIST = false;

    private Sensor accelerator;
    private Sensor gyroscrope;
    private Sensor magnetic;
    private Sensor rotation;
    private LogSensorListener sensorListener;
    private TrackSensorListener trackSensorListener;
    private static int windowSize = 256;// 256
    private boolean threadDisable_sensor = false;

    private boolean threadDisable_sensorPackage;
    private volatile boolean rawFileReadFlag = false;

    private StoreData sd;
    private File  globalFile;
    private String[] ACC = new String[windowSize];
    private String[] GYRO = new String[windowSize];
    private String[] MAG = new String[windowSize];
    private String[] ROT = new String[windowSize];
    /**
     * 返回一个Binder对象
     */
    @Override
    public IBinder onBind(Intent intent) {
        //return new MsgBinder();
        return new MyBinder();
    }

    public class MsgBinder extends Binder {
        /**
         * 获取当前Service的实例
         *
         * @return
         */
        public DetectorService getService() {
            return DetectorService.this;
        }
    }

    public class MyBinder extends Binder {
        private DetectorService service;

        public MyBinder() {
            service = DetectorService.this;
        }

        public void setLabel(int label) {
            stateLabel = label;
        }

        public int getLabel() {
            return stateLabel;
        }

    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Log.d(TAG, "Sensor Service Start");
        sd = new StoreData(false, true);//create data store class
        initSensor();// init sensor
        sensorDataStore();//begin reading sensor data
        globalFile = outputFile.getStateFile();
        try {
            sensorDataPackage();
        } catch (InterruptedException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
    }

    public void sensorDataStore() {
        // Sensor更新线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!threadDisable_sensor) {
                    try {
                        Thread.sleep(5000);// 6400
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //SensorFile
                    int i = 0;
                    String[] outStoreRaw = new String[windowSize];
                    String accData;
                    String gyroData;
                    String magData;
                    String rotData;

                    int cLabel = stateLabel;
                    while (i < windowSize) {
                        accData = sensorListener.getAccData();
                        gyroData = sensorListener.getGyroData();
                        magData = sensorListener.getMagData();
                        rotData = sensorListener.getRotData();

                        // 转化数据
                        if (accData != null && gyroData != null && magData != null) {
                            ACC[i] = accData;
                            GYRO[i] = gyroData;
                            MAG[i] = magData;
                            ROT[i] = rotData;
                            i++;
                        }
                        // 如果出現緩衝池空，則停止讀取，等待5s
                        else {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    for (int i_2 = 0; i_2 < windowSize; i_2++) {
                        outStoreRaw[i_2] = cLabel + "\t" + ACC[i_2] + "\t" + GYRO[i_2] + "\t" + MAG[i_2] + "\t" + ROT[i_2];
                    }

                    while (rawFileReadFlag == true) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        sd.storeDataRaw(outStoreRaw);
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    //Globel State File //usualTime, timeStamp, Bear, velocity, onVehicleProbability, pathVector
                    StringBuffer GlobelStateBuffer = new StringBuffer();
                    long timeStamp = System.currentTimeMillis();
                    GlobelStateBuffer.append(TimeUtil.getTime(timeStamp)+"\t"+timeStamp+"\t");
                    String gpsBear = DetectorLocationListener.getCurrentBear();
                    if (gpsBear != null && Math.abs(Float.valueOf(gpsBear)) >= 0.001) {
                        //Log.d(TAG,"GPS__bear："+gpsBear);
                        GlobelStateBuffer.append(gpsBear+"\t");
                    } else{
                        //String bear = String.valueOf(ekf.euler[2]/ Math.PI*180 -myMath.DECLINATION);

                        GlobelStateBuffer.append("*"+"\t");
                        //Log.d(TAG,"AM__bear："+ bear);
                    }

                    String gpsVelocity = DetectorLocationListener.getCurrentVelocity();
                    if (gpsVelocity != null && Math.abs(Float.valueOf(gpsBear)) >= 0.001) {
                        GlobelStateBuffer.append(gpsVelocity+"\t");
                    } else{
                        GlobelStateBuffer.append("*"+"\t");

                    }

                    String onVehicleProbability = String.valueOf(trackSensorListener.getIfOnVehicleProbability());

                    GlobelStateBuffer.append(onVehicleProbability+"\t");

                    String PathVector = "*";
                    if(trackSensorListener.ifNewPath()){
                        float[] pathVector = trackSensorListener.getPathVector();
                        PathVector = pathVector[0]+"\t"+pathVector[1]+"\t"+pathVector[2];
                        trackSensorListener.ifNewPath = false;
                    }
                    GlobelStateBuffer.append(PathVector+"\n");

                    try {
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(globalFile, true)));
                        writer.write(GlobelStateBuffer.toString());
                        writer.flush();
                        writer.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void sensorDataPackage() throws InterruptedException {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!threadDisable_sensorPackage) {
                    try {
                        Thread.sleep(5 * 60 * 1000);//
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    File inputFileRaw = sd.getRawDataFile(); //当前使用的txt文件
                    // File inputFileCombine = sd.getCombineDataFile();

                    if (inputFileRaw.length() > 5 * 1024 * 1024) {
                        ///Log.d(TAG, "begin");
                        File outputFile = sd.getz7RawDataFile();//当前7z文件
                        sd.getNewz7RawDataFile();//创建新的7z文件备下次使用
                        //Log.d(TAG, "package");
                        String inputPath = inputFileRaw.getPath();
                        String outputPath = outputFile.getPath();
                        rawFileReadFlag = true;
                        sd.newRawDataFile();//创建新的txt文件供写入
                        rawFileReadFlag = false;
                        try {
                            z7Compression.z7(inputPath, outputPath);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        inputFileRaw.delete();//删除压缩完的txt文件
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
            this.onDestroy();
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
            this.onDestroy();
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
            this.onDestroy();
        }
        sensorListener = new LogSensorListener(accMax, gyroMax, magMax);
        trackSensorListener = new TrackSensorListener(accMax, gyroMax, magMax, true,true,true,true);
        if (ACCELERATOR_EXIST) {
            sm.registerListener(sensorListener, accelerator, SensorManager.SENSOR_DELAY_GAME);
            sm.registerListener(trackSensorListener, accelerator, SensorManager.SENSOR_DELAY_GAME);
        }
        if (GYROSCROPE_EXIST) {
            sm.registerListener(sensorListener, gyroscrope, SensorManager.SENSOR_DELAY_GAME);
            sm.registerListener(trackSensorListener, gyroscrope, SensorManager.SENSOR_DELAY_GAME);
        }
        if (MAGNETIC_EXIST) {
            sm.registerListener(sensorListener, magnetic, SensorManager.SENSOR_DELAY_GAME);
            sm.registerListener(trackSensorListener, magnetic, SensorManager.SENSOR_DELAY_GAME);
        }
        if (ACCELERATOR_EXIST && GYROSCROPE_EXIST && MAGNETIC_EXIST) {
            rotation = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            sm.registerListener(sensorListener, rotation, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        sm.unregisterListener(sensorListener);
        sm.unregisterListener(trackSensorListener);
        trackSensorListener.closeSensorThread();
        threadDisable_sensor = true;
        threadDisable_sensorPackage = true;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }

}
