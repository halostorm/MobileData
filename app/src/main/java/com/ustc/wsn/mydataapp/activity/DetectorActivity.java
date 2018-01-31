package com.ustc.wsn.mydataapp.activity;

/**
 * Created by halo on 2018/1/17.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

import com.nulana.nchart3d.example.DifferentCharts.ChartingDemoActivity;
import com.ustc.wsn.mydataapp.detectorservice.outputFile;
import com.ustc.wsn.mydataapp.service.DetectorService;
import com.ustc.wsn.mydataapp.service.GpsService;
import com.ustc.wsn.mydataapp.utils.UploadManagers;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import detector.wsn.ustc.com.mydataapp.R;

public class DetectorActivity extends Activity implements OnClickListener {

    Boolean isExit = false;
    protected Intent DetectorserviceIntent;
    protected Intent GpsserviceIntent;
    protected Intent SimpleActivityIntent;
    protected Intent LabelActivityIntent;
    protected Intent UploadActivityIntent;
    protected  Intent trackActivityIntent;
    private Toast t;
    private LocationManager loc_int;
    //private volatile int stateLabel;
    //private DetectorService msgService;
    private boolean serviceStart = false;
    protected static final String TAG = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detector);

        loc_int = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // 判断GPS是否正常启动
        if (!loc_int.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            t = Toast.makeText(this, "请开启高精度GPS导航！", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            // 返回开启GPS导航设置界面
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            // return;
        }
          //  就是这一句使图标能显示
        Button btnStartService = (Button) findViewById(R.id.btnStartService);
        Button btnStopService = (Button) findViewById(R.id.btnStopService);
        btnStartService.setOnClickListener(this);
        btnStopService.setOnClickListener(this);

        Button ViewData = (Button) findViewById(R.id.btnViewData);
        ViewData.setOnClickListener(this);

        Button btnstartLabel = (Button) findViewById(R.id.btnStartLabel);
        btnstartLabel.setOnClickListener(this);

        Button btnBeginUploadActivity = (Button) findViewById(R.id.btnBeginUploadActivity);
        btnBeginUploadActivity.setOnClickListener(this);

        Button btnTrack = (Button) findViewById(R.id.btnTrack);
        btnTrack.setOnClickListener(this);

        new outputFile();//create data path
        DetectorserviceIntent = new Intent(this, DetectorService.class);
        GpsserviceIntent = new Intent(this, GpsService.class);
        SimpleActivityIntent = new Intent(this, SimulationActivity.class);
        LabelActivityIntent = new Intent(this, LabelActivity.class);
        UploadActivityIntent = new Intent(this, UploadActivity.class);
        trackActivityIntent = new Intent(this, ChartingDemoActivity.class);
    }

    private void showHelpDialog() {
        Dialog helpDialog = new Dialog(this);
        helpDialog.setCancelable(true);
        helpDialog.setCanceledOnTouchOutside(true);

        helpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        helpDialog.setContentView(getLayoutInflater().inflate(R.layout.help, null));

        helpDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.setting, menu);
        //setIconEnable(menu,true);
        return true;
    }

    private void setIconEnable(Menu menu, boolean enable)
    {
        try
        {
            Class<?> clazz = Class.forName("com.ustc.wsn.mydataapp.activity.DetectorActivity");
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            m.setAccessible(true);

            //MenuBuilder实现Menu接口，创建菜单时，传进来的menu其实就是MenuBuilder对象(java的多态特征)
            m.invoke(menu, enable);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings_help:
                showHelpDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitByDoubleClick();
        }
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            //exitByDoubleClick();
        }
        return false;
    }

    private void exitByDoubleClick() {
        Timer tExit = null;
        if (!isExit) {
            isExit = true;
            Toast.makeText(this, "再按一次可以退出Mobile Data!", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;//取消退出
                }
            }, 1000);// 如果1秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        } else {
            finish();
            System.exit(0);
        }
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        //unbindService(conn);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @SuppressLint("ShowToast")
    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        Toast.makeText(this, "Start", Toast.LENGTH_LONG);
        switch (view.getId()) {
            case R.id.btnStartService:
                startService(DetectorserviceIntent);
                startService(GpsserviceIntent);
                serviceStart = true;
                // bindService(DetectorserviceIntent, conn, Context.BIND_AUTO_CREATE);
                t = Toast.makeText(this, "开始采集", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                break;
            case R.id.btnViewData:
                    startActivity(SimpleActivityIntent);

                break;
            case R.id.btnStopService:
                if (serviceStart == true) {
                    // unbindService(conn);
                    stopService(DetectorserviceIntent);
                    stopService(GpsserviceIntent);
                    t = Toast.makeText(this, "停止采集", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                    serviceStart = false;
                    break;
                } else {
                    t = Toast.makeText(this, "未开始采集", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
                break;
            case R.id.btnStartLabel:
                if (serviceStart == true) {
                    startActivity(LabelActivityIntent);
                    break;
                } else{
                    t = Toast.makeText(this, "请先开始采集数据", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
                break;
            case R.id.btnBeginUploadActivity:
                startActivity(UploadActivityIntent);
                break;
            case R.id.btnTrack:
                t = Toast.makeText(this, "3D启动中！", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                startActivity(trackActivityIntent);
                break;
        }
    }
}