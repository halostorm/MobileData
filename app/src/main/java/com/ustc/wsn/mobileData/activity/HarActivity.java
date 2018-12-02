package com.ustc.wsn.mobileData.activity;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.Toast;
import com.ustc.wsn.mobileData.Listenter.DetectorLocationListener;
import com.ustc.wsn.mobileData.Listenter.LogSensorListener;
import com.ustc.wsn.mobileData.Listenter.TrackSensorListener;
import com.ustc.wsn.mobileData.R;
import com.ustc.wsn.mobileData.bean.PhoneState;
import com.ustc.wsn.mobileData.bean.math.myMath;
import com.ustc.wsn.mobileData.utils.TimeUtil;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.BatchUpdateException;
import java.util.ArrayList;
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
            Har.setText(PhoneState.HAR);
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
                    StringBuffer dataBuffer = new StringBuffer();
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
                        StringBuffer dataBuffer = featureExtract(message);
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
                                    PhoneState.HAR = "静止";
                                case 2:
                                    PhoneState.HAR = "步行";
                                case 3:
                                    PhoneState.HAR = "跑步";
                                case 5:
                                    PhoneState.HAR = "骑行";
                                case 6:
                                    PhoneState.HAR = "乘车";

                            }

                        } else PhoneState.HAR = "Unknow";

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

    private StringBuffer featureExtract(float[] accNorm) {
        StringBuffer features = new StringBuffer();
        //时域特征
        DescriptiveStatistics stats = new DescriptiveStatistics();
        double[] data = new double[accNorm.length];
        for (int i = 0; i < accNorm.length; i++) {
            stats.addValue(accNorm[i]);
            data[i] = accNorm[i];
        }
        double mean = stats.getMean();
        double std = stats.getStandardDeviation();
        double skew = stats.getSkewness();
        double kurtosis = stats.getKurtosis();
        double maxTopKMean = new myStat().getMaxTopKMean(data, 20);
        double minTopKMean = new myStat().getMinTopKMean(data, 20);

        features.append(mean);
        features.append("\t" + std);
        features.append("\t" + skew);
        features.append("\t" + kurtosis);
        features.append("\t" + maxTopKMean);
        features.append("\t" + minTopKMean);

        // 频谱特征
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] freSpectrum = fft.transform(data, TransformType.FORWARD);
        ArrayList<Double> Spectrum = new ArrayList<>();
        for (int i = 0; i <= freSpectrum.length / 2; i++) {
            Spectrum.add(freSpectrum[i].abs());
        }
        ArrayList<Double>[] SpectrumPoints = new ArrayList[11];
        double[] SpectrumFeatures = new double[11];
        for (int i = 0; i < 11; i++)
            SpectrumPoints[i] = new ArrayList<>();
        final double SampleRate = 50.0;
        final int SpectrumLength = Spectrum.size() - 1;

        final double density = (SampleRate * 0.5) / SpectrumLength;

        for (int i = 1; i <= SpectrumLength; i++) {
            double frequency = density * i;
            int k = (int) Math.floor(frequency);
            if (k < 10) SpectrumPoints[k].add(Spectrum.get(i));
            else SpectrumPoints[10].add(Spectrum.get(i));
        }

        for (int k = 0; k < SpectrumPoints.length; k++) {
            SpectrumFeatures[k] = new myStat().getMean(SpectrumPoints[k]);
            features.append("\t" + SpectrumFeatures[k]);
        }
        return features;
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


class myStat {
    int partionMin(double a[], int first, int end) {
        int i = first;
        double main = a[end];
        for (int j = first; j < end; j++) {
            if (a[j] < main) {
                double temp = a[j];
                a[j] = a[i];
                a[i] = temp;
                i++;
            }
        }
        a[end] = a[i];
        a[i] = main;
        return i;
    }

    int partionMax(double a[], int first, int end) {
        int i = first;
        double main = a[end];
        for (int j = first; j < end; j++) {
            if (a[j] > main) {
                double temp = a[j];
                a[j] = a[i];
                a[i] = temp;
                i++;
            }
        }
        a[end] = a[i];
        a[i] = main;
        return i;
    }

    void getTopKMinBySort(double a[], int first, int end, int k) {
        if (first < end) {
            int partionIndex = partionMin(a, first, end);
            if (partionIndex == k - 1) return;
            else if (partionIndex > k - 1) getTopKMinBySort(a, first, partionIndex - 1, k);
            else getTopKMinBySort(a, partionIndex + 1, end, k);
        }
    }

    void getTopKMaxBySort(double a[], int first, int end, int k) {
        if (first < end) {
            int partionIndex = partionMax(a, first, end);
            if (partionIndex == k - 1) return;
            else if (partionIndex > k - 1) getTopKMaxBySort(a, first, partionIndex - 1, k);
            else getTopKMaxBySort(a, partionIndex + 1, end, k);
        }
    }

    double getMinTopKMean(double[] a, int k) {
        double mean = 0;
        getTopKMinBySort(a, 0, a.length - 1, k);
        for (int i = 0; i < k; i++) {
            mean += a[i] / k;
        }
        return mean;
    }

    double getMaxTopKMean(double[] a, int k) {
        double mean = 0;
        getTopKMaxBySort(a, 0, a.length - 1, k);
        for (int i = 0; i < k; i++) {
            mean += a[i] / k;
        }
        return mean;
    }

    double getMean(ArrayList<Double> a) {
        double mean = 0;
        for (int i = 0; i < a.size(); i++) {
            mean += a.get(i) / a.size();
        }
        return mean;
    }
}