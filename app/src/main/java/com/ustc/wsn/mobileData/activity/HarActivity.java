package com.ustc.wsn.mobileData.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.Toast;

import com.ustc.wsn.mobileData.Listenter.DetectorLocationListener;
import com.ustc.wsn.mobileData.Listenter.LogSensorListener;
import com.ustc.wsn.mobileData.Listenter.TrackSensorListener;
import com.ustc.wsn.mobileData.R;
import com.ustc.wsn.mobileData.bean.harTools.HAR;
import com.ustc.wsn.mobileData.bean.math.myMath;
import com.ustc.wsn.mobileData.utils.TimeUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class HarActivity extends Activity {
    private static int windowSize = 256;// 256
    protected final String TAG = this.toString();
    public volatile int stateLabel = 0;
    private Toast t;
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
    private boolean threadDisable = false;

    private String[] ACC = new String[windowSize];
    private String[] GYRO = new String[windowSize];
    private String[] MAG = new String[windowSize];
    private String[] ROT = new String[windowSize];

    private String recMsg = "No Reply";
    
    private String harState = "unknow";

    private Button Har;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_har);
        Log.d(TAG, "Sensor Service Start");
        initSensor();
        mainThread();

        Har = (Button)findViewById(R.id.btnHar);

        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendMessage(handler.obtainMessage());
            }
        }, 0, 2500);
    }

    private Handler handler = new Handler() {
        @Override
        //定时更新图表
        public void handleMessage(Message msg) {
            Har.setText(harState);
        }
    };

    public void mainThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!threadDisable) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int i = 0;
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
                    float[] accNorm = new float[windowSize];
                    for (int j = 0; j < windowSize; j++) {
                        String[] acc_j = ACC[j].split("\t");
                        accNorm[j] = (float) Math.sqrt(Math.pow(Float.parseFloat(acc_j[1]), 2) + Math.pow(Float.parseFloat(acc_j[2]), 2) + Math.pow(Float.parseFloat(acc_j[3]), 2)) + myMath.G;
                    }

                    //Send Sensor Data
                    Message(accNorm);

                    //Globel State File //usualTime, timeStamp, Bear, velocity, onVehicleProbability, pathVector
                    StringBuffer GlobelStateBuffer = new StringBuffer();

                    long timeStamp = System.currentTimeMillis();
                    GlobelStateBuffer.append(TimeUtil.getTime(timeStamp) + "\t" + timeStamp + "\t");

                    String gpsBear = DetectorLocationListener.getCurrentBear();
                    if (gpsBear != null && Math.abs(Float.valueOf(gpsBear)) >= 0.001) {
                        GlobelStateBuffer.append(gpsBear + "\t");
                    } else {
                        GlobelStateBuffer.append("*" + "\t");
                    }

                    String gpsVelocity = DetectorLocationListener.getCurrentVelocity();
                    if (gpsVelocity != null && Math.abs(Float.valueOf(gpsBear)) >= 0.001) {
                        GlobelStateBuffer.append(gpsVelocity + "\t");
                    } else {
                        GlobelStateBuffer.append("*" + "\t");

                    }

                    String onVehicleProbability = String.valueOf(trackSensorListener.getIfOnVehicleProbability());

                    GlobelStateBuffer.append(onVehicleProbability + "\t");

                    String PathVector = "*\t*\t*";
                    if (trackSensorListener.ifNewPath()) {
                        float[] pathVector = trackSensorListener.getPathVector();
                        if (pathVector[0] < -50f) {
                            PathVector = "*\t*\t*";
                        } else {
                            PathVector = pathVector[0] + "\t" + pathVector[1] + "\t" + pathVector[2];
                        }

                        trackSensorListener.ifNewPath = false;
                    }
                    GlobelStateBuffer.append(PathVector + "\n");
                    //Send State Data
                }
            }
        }).start();
    }

    protected void Message(final float[] message) {
        // TODO Auto-generated method stub
        new Thread() {
            @Override
            public void run() {
                do {
                    try {
                        Socket s = new Socket(Data.getHost(), Data.getPORT());
                        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                        StringBuffer dataBuffer = new HAR().featureExtract(message);
                        String str = dataBuffer.toString();
//                        Log.d(TAG, "myData: "+str);
                        dos.write(str.getBytes("utf-8"));
                        dos.flush();
                        Log.d(TAG, "HarServer send: ");
                        Thread.sleep(1000);

                        DataInputStream dis = new DataInputStream(s.getInputStream());
                        byte[] recvStr = new byte[512];
                        int rn = dis.read(recvStr);
                        recMsg = "No Reply";
                        if (rn > 0) {
                            recMsg = new String(recvStr, 0, rn);
                            int har = Integer.parseInt(recMsg);
                            switch (har) {
                                case 1:
                                   harState = "静止";
                                   break;
                                case 2:
                                   harState = "步行";
                                   break;
                                case 3:
                                   harState = "跑步";
                                   break;
                                case 5:
                                   harState = "骑行";
                                   break;
                                case 6:
                                   harState = "乘车";
                                   break;

                            }

                        } else harState = "Unknow";

                        Log.d(TAG, "HarServer reply: " + recMsg);

                        dis.close();
                        dos.close();
                        s.close();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } while (false);

            }
        }.start();
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
        trackSensorListener = new TrackSensorListener(accMax, gyroMax, magMax, true, true, true, true);
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
        threadDisable = true;
    }
}

class Data {
    private static String localHOST = "localhost";
    private static String HOST = "192.168.1.106";
    private static int PORT = 10087;

    public static int getPORT() {
        return PORT;
    }

    public static String getHost() {
        return HOST;
    }

    public static void setHost(String s) {
        Data.HOST = s;
    }

    public static void setPort(int s) {
        Data.PORT = s;
    }
}


