package com.ustc.wsn.mydataapp.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ustc.wsn.mydataapp.Listenter.DetectorLocationListener;
import com.ustc.wsn.mydataapp.R;
import com.ustc.wsn.mydataapp.bean.math.myMath;
import com.ustc.wsn.mydataapp.bean.outputFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    private final String TAG = this.getClass().toString();
    protected Boolean isExit = false;
    private String userID = "";
    private DetectorLocationListener GPS = null;

    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        new outputFile();

        GPS = new DetectorLocationListener(this);// start GPS to update geographical params: gravity, declination;

        myMath.getGeographicalParams();

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);

        TextView Pass = (TextView) findViewById(R.id.btnPass);
        Pass.setOnClickListener(mOnClickListener);

        //Login if app is firstly used

        File userInfo = outputFile.getUserInfoFile();
        if (!userInfo.exists()) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
        else{
            try {
                BufferedReader bf = new BufferedReader(new FileReader(userInfo));
                String values = new String();
                values = bf.readLine();
                if (values.length() != 0) {
                    String[] v = new String[10];
                    v = values.split("\t");
                    userID = v[0];
                    new outputFile(userID);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        getMenuInflater().inflate(R.menu.main_setting, menu);
        //setIconEnable(menu,true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings_help:
                showHelpDialog();
                return true;
            case R.id.calibrate_accel:
                startActivity(new Intent(this, EllipsoidFitActivity.class));
                return true;
            case R.id.ReLogin:
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent intent = new Intent(MainActivity.this, DetectorActivity.class);
            intent.putExtra("userId", userID);
            startActivity(intent);
            finish();
        }
    };

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
        Timer tExit;
        if (!isExit) {
            isExit = true;
            Toast.makeText(this, "再按一次退出 Mobile Data !", Toast.LENGTH_SHORT).show();
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
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        onDestroy();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if(GPS!=null){
            GPS.closeLocation();
        }
    }
}