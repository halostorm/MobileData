package com.ustc.wsn.mobileData.activity;

/**
 * Created by halo on 2018/1/17.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.shapes.Shape;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.ustc.wsn.mobileData.R;
import com.ustc.wsn.mobileData.bean.PhoneState;
import com.ustc.wsn.mobileData.bean.outputFile;
import com.ustc.wsn.mobileData.service.DetectorService;
import com.ustc.wsn.mobileData.service.GpsService;
import com.ustc.wsn.mobileData.service.PathService;

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

    private final int N_ID = 9032;
    private final int resId = R.mipmap.ic_launcher;

    protected final String TAG = DetectorActivity.this.toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        File accParams = outputFile.getAccParamsFile();
        if (!accParams.exists()) {
            Toast.makeText(DetectorActivity.this, "请先校准加速度计！", Toast.LENGTH_SHORT).show();
            Intent intent1 = new Intent(DetectorActivity.this, EllipsoidFitActivity.class);
            startActivity(intent1);
        }

        setContentView(R.layout.activity_detector);
        loc_int = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        btnStartService = (Button) findViewById(R.id.btnStartService);
        btnStartService.setOnClickListener(this);

        btnStopService = (Button) findViewById(R.id.btnStopService);
        btnStopService.setOnClickListener(this);

        btnGps = (CheckBox) (findViewById(R.id.checkboxEnableGPS));
        btnGps.setOnCheckedChangeListener(new GpsCheckBoxListener());

        btnPath = (CheckBox) (findViewById(R.id.checkboxEnablePath));
        btnPath.setOnCheckedChangeListener(new PathCheckBoxListener());

        btnPath.setChecked(false);

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

        Intent intent = this.getIntent();
        psw = intent.getStringExtra("userId");
        Log.d(TAG, "userID:" + psw);

        PhoneState.initStateParams();

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
                    loc_int = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    // 判断GPS是否正常启动
                    if(!loc_int.isProviderEnabled(LocationManager.GPS_PROVIDER) && gpsEnabled) {
                        t = Toast.makeText(DetectorActivity.this, "请开启高精度GPS！", Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.show();
                        // 返回开启GPS导航设置界面
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, 0);
                    }
                    Log.d(TAG, "Gps manual Start--------------------------");
                    startService(GpsserviceIntent);
                    gpsStart = true;
                    String notificationS;
                    if (pathStart) {
                        notificationS = "当前记录数据：(1)Sensors\t(2)GPS\t(3)轨迹";
                    } else {
                        notificationS = "当前记录数据：(1)Sensors\t(2)GPS";
                    }
                    addIconToStatusbar(notificationS);
                }
            } else {
                gpsEnabled = false;
                if (gpsStart && detectorStart) {
                    stopService(GpsserviceIntent);
                    gpsStart = false;
                    String notificationS;
                    if (pathStart) {
                        notificationS = "当前记录数据：(1)Sensors\t(2)轨迹";
                    } else {
                        notificationS = "当前记录数据：(1)Sensors\t";
                    }
                    addIconToStatusbar(notificationS);
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
                    pathStart = true;
                    Log.d(TAG, "Path manual Start--------------------------");
                    String notificationS;
                    if (gpsStart) {
                        notificationS = "当前记录数据：(1)Sensors\t(2)GPS\t(3)轨迹";
                    } else {
                        notificationS = "当前记录数据：(1)Sensors\t(2)轨迹";
                    }
                    addIconToStatusbar(notificationS);
                }
            } else {
                pathEnabled = false;
                if (pathStart && detectorStart) {
                    stopService(PathserviceIntent);
                    pathStart = false;
                    String notificationS;
                    if (gpsStart) {
                        notificationS = "当前记录数据：(1)Sensors\t(2)GPS";
                    } else {
                        notificationS = "当前记录数据：(1)Sensors\t";
                    }
                    addIconToStatusbar(notificationS);
                }
            }
        }
    }

    public void openSystemFile() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //系统调用Action属性
        intent.setDataAndType(Uri.fromFile(outputFile.getUserDir()), "*/*");
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
            Class<?> clazz = Class.forName("com.ustc.wsn.mobileData.activity.DetectorActivity");
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            m.setAccessible(true);

            //MenuBuilder实现Menu接口，创建菜单时，传进来的menu其实就是MenuBuilder对象(java的多态特征)
            m.invoke(menu, enable);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calibrate_state() {
        Intent intent = new Intent(this, CalibrateStateActivity.class);
        startActivity(intent);
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
            case R.id.calibrate_state:
                calibrate_state();
                return true;
            case R.id.calibrate_accel:
                stopAllService();
                deleteIconToStatusbar();
                startActivity(new Intent(this, EllipsoidFitActivity.class));
                return true;
            case R.id.ReLogin:
                deleteIconToStatusbar();
                stopAllService();
                stopService(DetectorserviceIntent);
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            case R.id.exitSystem:
                deleteIconToStatusbar();
                stopAllService();
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(startMain);
                finish();
                System.exit(0);
                return true;
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
        deleteIconToStatusbar();
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
                    outputFile.updateDir();
                    startService(DetectorserviceIntent);
                    btnStartService.setText("采集中");
                    btnStartService.setTextColor(Color.BLUE);
                    detectorStart = true;
                    String notificationS = "当前记录数据：(1)Sensors\t";
                    if (gpsEnabled) {
                        loc_int = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        // 判断GPS是否正常启动
                        if(!loc_int.isProviderEnabled(LocationManager.GPS_PROVIDER) && gpsEnabled) {
                            t = Toast.makeText(this, "请开启高精度GPS！", Toast.LENGTH_SHORT);
                            t.setGravity(Gravity.CENTER, 0, 0);
                            t.show();
                            // 返回开启GPS导航设置界面
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, 0);
                        }
                        startService(GpsserviceIntent);
                        Log.d(TAG, "Gps Together Start--------------------------");
                        notificationS += "(2)GPS\t";
                        gpsStart = true;
                    }
                    if (pathEnabled) {
                        startService(PathserviceIntent);
                        Log.d(TAG, "Path Together Start--------------------------");
                        notificationS += "(3)轨迹\t";
                        pathStart = true;
                    }
                    addIconToStatusbar(notificationS);
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
                    deleteIconToStatusbar();
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

    private void addIconToStatusbar(String s) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Notification n = builder.setContentTitle("Mobile Data正在后台运行").setContentText(s).setWhen(System.currentTimeMillis()).setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher)).build();
        //常驻状态栏的图标
        n.icon = resId;
        // 将此通知放到通知栏的"Ongoing"即"正在运行"组中
        n.flags |= Notification.FLAG_ONGOING_EVENT;
        // 表明在点击了通知栏中的"清除通知"后，此通知不清除， 经常与FLAG_ONGOING_EVENT一起使用
        n.flags |= Notification.FLAG_NO_CLEAR;
        PendingIntent pi = PendingIntent.getActivity(this, 0, getIntent(), 0);
        n.contentIntent = pi;
        nm.notify(N_ID, n);
    }

    private void deleteIconToStatusbar() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(N_ID);
    }

    private void stopAllService(){
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
        }
    }
}