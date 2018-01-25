package com.ustc.wsn.mydataapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.ustc.wsn.mydataapp.bean.StoreData;
import com.ustc.wsn.mydataapp.detectorservice.DetectorLocationListener;

import java.io.IOException;

public class GpsService extends Service {
    protected static final String TAG = null;
    private GpsService mContext = GpsService.this;
    private StoreData sd;
    private DetectorLocationListener sgps;
    private String location;
    private boolean threadDisable_gps = false;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        sd = new StoreData();//create data store class
        location = new String();
        sgps = new DetectorLocationListener(mContext);// start gps
        // GPS thread
        //Log.d(TAG, "GpsService Begin!");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!threadDisable_gps) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "GpsService Begin!");
                    if (sgps != null) {
                        location = sgps.getLocation();
                        if (location != null) { //
                            try {
                                sd.storeLocation(location);
                            } catch (IOException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                        }
                    }

                }
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new GpsService.MsgBinder();
    }

    // TODO: Return the communication channel to the service.
    // throw new UnsupportedOperationException("Not yet implemented");
    public class MsgBinder extends Binder {
        /**
         * 获取当前Service的实例
         *
         * @return
         */
        public GpsService getService() {
            return GpsService.this;
        }
    }


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (sgps != null) {
            sgps.closeLocation();
            sgps = null;
        }
        threadDisable_gps = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }
}
