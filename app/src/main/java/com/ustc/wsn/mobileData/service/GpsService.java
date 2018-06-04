package com.ustc.wsn.mobileData.service;
/**
 * Created by halo on 2018/1/28.
 */
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.ustc.wsn.mobileData.bean.StoreData;
import com.ustc.wsn.mobileData.Listenter.DetectorLocationListener;

import java.io.IOException;

public class GpsService extends Service {
    protected static final String TAG = GpsService.class.toString();
    private GpsService mContext = GpsService.this;
    private StoreData sd;
    private DetectorLocationListener sgps;
    private String location;
    private boolean threadDisable_gps = false;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Log.d(TAG,"GPS Service Start");
        Toast t = Toast.makeText(this, "采集GPS辅助信息！", Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
        sd = new StoreData(true,false);//create data store class
        location = new String();
        sgps = new DetectorLocationListener(mContext);// start gps
        // GPS thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!threadDisable_gps) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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
