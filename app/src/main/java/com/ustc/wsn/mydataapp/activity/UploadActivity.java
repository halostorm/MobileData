package com.ustc.wsn.mydataapp.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.view.View.OnClickListener;
import com.ustc.wsn.mydataapp.utils.UploadManagers;

import detector.wsn.ustc.com.mydataapp.R;

public class UploadActivity extends Activity implements OnClickListener {

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
        Toast.makeText(this, "Start", Toast.LENGTH_LONG);
        switch (view.getId()) {
            case R.id.btnStartUpload:
                int netType = getNetworkType();
                if (netType == -1) {
                    Toast.makeText(UploadActivity.this,"请开启网络连接",Toast.LENGTH_SHORT).show();
                }
                if (netType == ConnectivityManager.TYPE_WIFI) {
                    Toast.makeText(UploadActivity.this,"当前是Wifi连接，请放心使用",Toast.LENGTH_SHORT).show();
                    String psw = "OK";
                    Intent intent = this.getIntent();
                    psw=intent.getStringExtra("userId");
                    //Toast.makeText(UploadActivity.this,psw,Toast.LENGTH_SHORT).show();
                    UploadManagers.initAutoUploadSeriver(UploadActivity.this,
                            Environment.getExternalStorageDirectory().getPath() + "/MobileData",psw);
                } else if (netType == ConnectivityManager.TYPE_MOBILE) {
                    Toast.makeText(UploadActivity.this,"为避免数据流量消耗，请切换至Wifi再上传",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnStopUpload:
                UploadManagers.stopUpload(UploadActivity.this);
                break;
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
