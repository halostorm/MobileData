package com.ustc.wsn.mydataapp.activity;

/**
 * Created by halo on 2018/1/17.
 */

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import android.view.View.OnClickListener;
import com.ustc.wsn.mydataapp.utils.UploadManagers;

import detector.wsn.ustc.com.mydataapp.R;

public class UploadActivity extends Activity implements OnClickListener {

    private Toast t;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Button StartUpload = (Button) findViewById(R.id.btnStartUpload);
        StartUpload.setOnClickListener(this);

        Button StopUpload = (Button) findViewById(R.id.btnStopUpload);
        StopUpload.setOnClickListener(this);
    }

    @SuppressLint("ShowToast")
    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        //Toast.makeText(this, "Start", Toast.LENGTH_LONG);
        switch (view.getId()) {
            case R.id.btnStartUpload:
                int netType = getNetworkType();
                if (netType == -1) {
                    t = Toast.makeText(this, "请开启网络连接", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
                if (netType == ConnectivityManager.TYPE_WIFI) {
                    t = Toast.makeText(this, "当前是Wifi连接，已开始上传", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                    String psw = "OK";
                    Intent intent = this.getIntent();
                    psw=intent.getStringExtra("userId");
                    //Toast.makeText(UploadActivity.this,psw,Toast.LENGTH_SHORT).show();
                    UploadManagers.initAutoUploadSeriver(UploadActivity.this,
                            Environment.getExternalStorageDirectory().getPath() + "/MobileData",psw);
                } else if (netType == ConnectivityManager.TYPE_MOBILE) {
                    t = Toast.makeText(this, "为避免数据流量消耗，请切换至Wifi再上传", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
                break;
            case R.id.btnStopUpload:
                UploadManagers.stopUpload(UploadActivity.this);
                break;
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.setting, menu);
        //setIconEnable(menu,true);
        return true;
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

    private int getNetworkType() {
        ConnectivityManager connectMgr = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = connectMgr.getActiveNetworkInfo();
        if (info !=null) {
            return info.getType();
        } else {
            return -1;
        }
    }
}
