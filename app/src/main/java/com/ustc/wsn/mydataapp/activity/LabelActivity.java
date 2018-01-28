package com.ustc.wsn.mydataapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Gravity;
import android.widget.Button;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.ustc.wsn.mydataapp.service.DetectorService;

import detector.wsn.ustc.com.mydataapp.R;

public class LabelActivity extends Activity implements OnClickListener {

    protected final String TAG = LabelActivity.this.toString();
    private DetectorService.MyBinder service;
    private MyConnection conn;
    private Intent DetectorserviceIntent;
    private Toast t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_label);
        /*
        Button Static = (Button) findViewById(R.id.btnStatic);
        Button Walk = (Button) findViewById(R.id.btnWalk);
        Button Run = (Button) findViewById(R.id.btnRun);
        Button Elevator = (Button) findViewById(R.id.btnElevator);
        Button Bike = (Button) findViewById(R.id.btnBike);
        Button Car = (Button) findViewById(R.id.btnCar);
        Button Upstairs = (Button) findViewById(R.id.btnUpstairs);
        Button Downstairs = (Button) findViewById(R.id.btnDownstairs);
        Button StopLabel = (Button) findViewById(R.id.btnStopLabel);

        Static.setOnClickListener(this);
        Walk.setOnClickListener(this);
        Run.setOnClickListener(this);
        Elevator.setOnClickListener(this);
        Bike.setOnClickListener(this);
        Car.setOnClickListener(this);
        Upstairs.setOnClickListener(this);
        Downstairs.setOnClickListener(this);
        StopLabel.setOnClickListener(this);
        */
        conn = new MyConnection();
        DetectorserviceIntent = new Intent(this, DetectorService.class);
        bindService(DetectorserviceIntent, conn, Context.BIND_AUTO_CREATE);

        Button cofirmLabel = (Button) findViewById(R.id.btncofirmLabel);
        cofirmLabel.setOnClickListener(this);

        RadioGroup signlabel = (RadioGroup) findViewById(R.id.signLabel);
        signlabel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup signlabel, int checkedId) {
                RadioButton label = (RadioButton) findViewById(checkedId);
                switch (checkedId){
                    case R.id.btnStatic:
                        service.setLabel(1);
                        break;
                    case R.id.btnWalk:
                        service.setLabel(2);
                        break;
                    case R.id.btnRun:
                        service.setLabel(3);
                        break;
                    case R.id.btnElevator:
                        service.setLabel(4);
                        break;
                    case R.id.btnBike:
                        service.setLabel(5);
                        break;
                    case R.id.btnCar:
                        service.setLabel(6);
                        break;
                    case R.id.btnUpstairs:
                        service.setLabel(7);
                        break;
                    case R.id.btnDownstairs:
                        service.setLabel(8);
                        break;
                    case R.id.btnStopLabel:
                        service.setLabel(0);
                        break;
                }
                t = Toast.makeText(getApplicationContext(), "当前标签是:" + label.getText(), Toast.LENGTH_LONG);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
            }
        });
    }

    public void binderClick(View v) {
        bindService(DetectorserviceIntent, conn, Context.BIND_AUTO_CREATE);
    }

    private class MyConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = (DetectorService.MyBinder) binder;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    @SuppressLint("ShowToast")
    public void onClick(View view) {
        // TODO Auto-generated method stub
        if(view.getId() == R.id.btncofirmLabel){
            finish();
        }
    }
/*
    @SuppressLint("ShowToast")
    public void onClick(View view) {
        // TODO Auto-generated method stub
        //t = Toast.makeText(this, "Start", Toast.LENGTH_LONG);
        switch (view.getId()) {
            case R.id.btnStatic:
                service.setLabel(1);
                t = Toast.makeText(this, "<1>当前状态：静止！", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                break;
            case R.id.btnWalk:
                t = Toast.makeText(this, "<2> 当前状态：步行", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                ;
                service.setLabel(2);
                break;
            case R.id.btnRun:
                t = Toast.makeText(this, "<3> 当前状态：跑步", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                service.setLabel(3);
                break;
            case R.id.btnElevator:
                t = Toast.makeText(this, "<4> 当前状态：乘坐电梯", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                service.setLabel(4);
                break;
            case R.id.btnBike:
                t = Toast.makeText(this, "<5> 当前状态：骑自行车", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                service.setLabel(5);
                break;
            case R.id.btnCar:
                t = Toast.makeText(this, "<6> 当前状态：坐车", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                service.setLabel(6);
                break;
            case R.id.btnUpstairs:
                t = Toast.makeText(this, "<7>当前状态：上楼梯", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                service.setLabel(7);
                break;
            case R.id.btnDownstairs:
                t = Toast.makeText(this, "<8> 当前状态：下楼梯", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                service.setLabel(8);
                break;
            case R.id.btnStopLabel:
                t = Toast.makeText(this, "已停止标记", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                service.setLabel(0);
                break;
        }
    }
    */

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if(conn!=null)
            unbindService(conn);
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

}
