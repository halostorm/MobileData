package com.ustc.wsn.mydataapp.activity;

/**
 * Created by halo on 2018/1/17.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.ustc.wsn.mydataapp.bean.PhoneState;
import com.ustc.wsn.mydataapp.bean.outputFile;
import com.ustc.wsn.mydataapp.service.DetectorService;
import com.ustc.wsn.mydataapp.service.GpsService;
import com.ustc.wsn.mydataapp.utils.UploadManagers;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import detector.wsn.ustc.com.mydataapp.R;

public class DetectorActivity extends Activity implements OnClickListener {

    protected Intent DetectorserviceIntent;
    protected Intent GpsserviceIntent;
    protected Intent SimpleActivityIntent;
    protected Intent LabelActivityIntent;
    protected Intent UploadActivityIntent;
    protected Intent trackActivityIntent;
    private outputFile store;
    private Toast t;
    private LocationManager loc_int;
    private Button btnStartService;
    private Button btnStopService;
    private String psw;

    private boolean gpsEnabled = true;
    private boolean serviceStart = false;
    protected final String TAG = DetectorActivity.this.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detector);

        loc_int = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        btnStartService = (Button) findViewById(R.id.btnStartService);
        btnStopService = (Button) findViewById(R.id.btnStopService);
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

        //ifCollecting.setOnClickListener(this);

        Intent intent = this.getIntent();
        psw = intent.getStringExtra("userId");
        Log.d(TAG, "userID1:" + psw);
        store = new outputFile(psw);//create data path

        PhoneState.initStateParams();
        //Log.d(TAG,"path:"+store.getDir().getPath()+"\t"+store.getDir().getName());
        DetectorserviceIntent = new Intent(this, DetectorService.class);
        GpsserviceIntent = new Intent(this, GpsService.class);
        SimpleActivityIntent = new Intent(this, SimulationActivity.class);
        LabelActivityIntent = new Intent(this, LabelActivity.class);
        UploadActivityIntent = new Intent(this, UploadActivity.class);
        UploadActivityIntent.putExtra("userId", psw);
        trackActivityIntent = new Intent(this, ChartingDemoActivity.class);
    }

    public void openSystemFile() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //系统调用Action属性
        intent.setDataAndType(Uri.fromFile(store.getDir()), "*/*");
        //设置文件类型
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        // 添加Category属性
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "无可用文件管理器", Toast.LENGTH_SHORT).show();
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detector_setting, menu);
        //setIconEnable(menu,true);
        return true;
    }

    private void setIconEnable(Menu menu, boolean enable) {
        try {
            Class<?> clazz = Class.forName("com.ustc.wsn.mydataapp.activity.DetectorActivity");
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            m.setAccessible(true);

            //MenuBuilder实现Menu接口，创建菜单时，传进来的menu其实就是MenuBuilder对象(java的多态特征)
            m.invoke(menu, enable);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings_help:
                showHelpDialog();
                return true;
            case R.id.open_path:
                openSystemFile();
                return true;
            case R.id.exitSystem:
                finish();
                System.exit(0);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        PackageManager pm = getPackageManager();
        ResolveInfo homeInfo = pm.resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ActivityInfo ai = homeInfo.activityInfo;
            Intent startIntent = new Intent(Intent.ACTION_MAIN);
            startIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            startIntent.setComponent(new ComponentName(ai.packageName, ai.name));
            startActivitySafely(startIntent);
            return true;
        } else return super.onKeyDown(keyCode, event);
    }

    private void startActivitySafely(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
        } catch (SecurityException e) {
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
                if (serviceStart == false) {
                    /*
                    t = Toast.makeText(this, "开始采集", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                    */
                    loc_int = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    // 判断GPS是否正常启动
                    if (!loc_int.isProviderEnabled(LocationManager.GPS_PROVIDER)&& gpsEnabled) {
                        t = Toast.makeText(this, "请开启高精度GPS！", Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.show();
                        // 返回开启GPS导航设置界面
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, 0);
                        if(!loc_int.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                            gpsEnabled = false;
                        }
                    } else {
                        startService(DetectorserviceIntent);
                        if(gpsEnabled) {
                            startService(GpsserviceIntent);
                        }
                        serviceStart = true;
                        btnStartService.setText("采集中");
                        btnStartService.setTextColor(Color.BLUE);
                    }
                }
                break;
            case R.id.btnViewData:
                startActivity(SimpleActivityIntent);
                break;
            case R.id.btnStopService:
                if (serviceStart == true) {
                    // unbindService(conn);
                    stopService(DetectorserviceIntent);
                    stopService(GpsserviceIntent);
                    /*
                    t = Toast.makeText(this, "停止采集", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                    */
                    serviceStart = false;
                    btnStartService.setText("开始采集");
                    btnStartService.setTextColor(Color.BLACK);
                    break;
                }
                break;
            case R.id.btnStartLabel:
                if (serviceStart == true) {
                    startActivity(LabelActivityIntent);
                    break;
                } else {
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