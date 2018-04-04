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
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.ustc.wsn.mydataapp.R;
import com.ustc.wsn.mydataapp.bean.PhoneState;
import com.ustc.wsn.mydataapp.bean.outputFile;
import com.ustc.wsn.mydataapp.service.DetectorService;
import com.ustc.wsn.mydataapp.service.GpsService;
import com.ustc.wsn.mydataapp.service.PathService;

import java.io.File;
import java.lang.reflect.Method;

public class DetectorActivity extends Activity implements OnClickListener {

    protected Intent DetectorserviceIntent;
    protected Intent GpsserviceIntent;
    protected Intent PathserviceIntent;
    protected Intent SimpleActivityIntent;
    protected Intent LabelActivityIntent;
    protected Intent UploadActivityIntent;
    protected Intent AttitudeViewActivityIntent;
    protected Intent trackActivityIntent;
    private outputFile store;
    private Toast t;
    private LocationManager loc_int;
    private Button btnStartService;
    private Button btnStopService;
    private CheckBox btnGps;
    private CheckBox btnPath;
    private String psw;

    private boolean gpsEnabled = true;
    private boolean pathEnabled = true;

    private boolean detectorStart = false;
    private boolean gpsStart = false;
    private boolean pathStart = false;

    protected final String TAG = DetectorActivity.this.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detector);

        File accParams = outputFile.getAccParamsFile();
        if(!accParams.exists()) {
            Toast.makeText(DetectorActivity.this, "首次使用，请先校准加速度计！", Toast.LENGTH_LONG).show();
            Intent intent1 = new Intent(DetectorActivity.this, EllipsoidFitActivity.class);
            startActivity(intent1);
        }

        loc_int = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        btnStartService = (Button) findViewById(R.id.btnStartService);
        btnStartService.setOnClickListener(this);

        btnStopService = (Button) findViewById(R.id.btnStopService);
        btnStopService.setOnClickListener(this);

        btnGps = (CheckBox) (findViewById(R.id.checkboxEnableGPS));
        btnGps.setOnCheckedChangeListener(new GpsCheckBoxListener());

        btnPath = (CheckBox) (findViewById(R.id.checkboxEnablePath));
        btnPath.setOnCheckedChangeListener(new PathCheckBoxListener());

        Button ViewData = (Button) findViewById(R.id.btnViewData);
        ViewData.setOnClickListener(this);

        Button btnstartLabel = (Button) findViewById(R.id.btnStartLabel);
        btnstartLabel.setOnClickListener(this);

        Button btnBeginUploadActivity = (Button) findViewById(R.id.btnBeginUploadActivity);
        btnBeginUploadActivity.setOnClickListener(this);

        Button btnAttView = (Button) findViewById(R.id.btnAttView);
        btnAttView.setOnClickListener(this);

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
        AttitudeViewActivityIntent = new Intent(this, AttitudeViewActivity.class);
        trackActivityIntent = new Intent(this, ChartingDemoActivity.class);
        PathserviceIntent = new Intent(this, PathService.class);
    }

    class GpsCheckBoxListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    gpsEnabled = true;
                    if (detectorStart) {
                        Log.d(TAG,"Gps manual Start--------------------------");
                        startService(GpsserviceIntent);
                    }
                } else {
                    gpsEnabled = false;
                    if (gpsStart && detectorStart) {
                        stopService(GpsserviceIntent);
                    }
                }
        }
    }

    class PathCheckBoxListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if (isChecked) {
                pathEnabled = true;
                if (detectorStart) {
                    startService(PathserviceIntent);
                    Log.d(TAG,"Path manual Start--------------------------");
                }
            } else {
                pathEnabled = false;
                if (pathStart && detectorStart) {
                    stopService(PathserviceIntent);
                }
            }
        }
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
                if (detectorStart == false) {

                    startService(DetectorserviceIntent);
                    btnStartService.setText("采集中");
                    btnStartService.setTextColor(Color.BLUE);
                    detectorStart = true;

                    if (gpsEnabled) {
                        loc_int = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        // 判断GPS是否正常启动
                        while (!loc_int.isProviderEnabled(LocationManager.GPS_PROVIDER) && gpsEnabled) {
                            t = Toast.makeText(this, "请开启高精度GPS！", Toast.LENGTH_SHORT);
                            t.setGravity(Gravity.CENTER, 0, 0);
                            t.show();
                            // 返回开启GPS导航设置界面
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, 0);
                        }
                        startService(GpsserviceIntent);
                        Log.d(TAG,"Gps Together Start--------------------------");
                        gpsStart = true;
                    }
                    if (pathEnabled) {
                        startService(PathserviceIntent);
                        Log.d(TAG,"Path Together Start--------------------------");
                        pathStart = true;
                    }
                }
                break;
            case R.id.btnViewData:
                startActivity(SimpleActivityIntent);
                break;
            case R.id.btnStopService:
                if (detectorStart == true) {
                    // unbindService(conn);
                    stopService(DetectorserviceIntent);
                    detectorStart = false;
                    btnStartService.setText("开始采集");
                    btnStartService.setTextColor(Color.BLACK);
                    if (gpsStart == true) {
                        stopService(GpsserviceIntent);
                        gpsStart = false;
                    }
                    if (pathStart == true) {
                        stopService(PathserviceIntent);
                        pathStart = false;
                    }
                    /*
                    t = Toast.makeText(this, "停止采集", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                    */

                    break;
                }
                break;
            case R.id.btnStartLabel:
                if (detectorStart == true) {
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
            case R.id.btnAttView:
                startActivity(AttitudeViewActivityIntent);
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