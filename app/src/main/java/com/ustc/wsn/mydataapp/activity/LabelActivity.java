package com.ustc.wsn.mydataapp.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
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

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import detector.wsn.ustc.com.mydataapp.R;

public class LabelActivity extends Activity implements OnClickListener {

    protected final String TAG = LabelActivity.this.toString();
    private DetectorService.MyBinder service;
    private MyConnection conn;
    private Intent DetectorserviceIntent;
    private Toast t;
    private Timer timer;
    private int Label = 0;
    private int LabelOld = 0;

    private RadioButton Static;
    private RadioButton Walk;
    private RadioButton Run;
    private RadioButton Elevator;
    private RadioButton Bike;
    private RadioButton Car;
    private RadioButton Upstairs;
    private RadioButton Downstairs;
    private RadioButton StopLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_label);
        conn = new MyConnection();
        DetectorserviceIntent = new Intent(this, DetectorService.class);
        bindService(DetectorserviceIntent, conn, Context.BIND_AUTO_CREATE);

        Button cofirmLabel = (Button) findViewById(R.id.btncofirmLabel);
        cofirmLabel.setOnClickListener(this);

        RadioGroup signlabel = (RadioGroup) findViewById(R.id.signLabel);

        Static = (RadioButton) findViewById(R.id.btnStatic);
        Walk = (RadioButton) findViewById(R.id.btnWalk);
        Run = (RadioButton) findViewById(R.id.btnRun);
        Elevator = (RadioButton) findViewById(R.id.btnElevator);
        Bike = (RadioButton) findViewById(R.id.btnBike);
        Car = (RadioButton) findViewById(R.id.btnCar);
        Upstairs = (RadioButton) findViewById(R.id.btnUpstairs);
        Downstairs = (RadioButton) findViewById(R.id.btnDownstairs);
        StopLabel = (RadioButton) findViewById(R.id.btnStopLabel);

        signlabel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup signlabel, int checkedId) {
                RadioButton label = (RadioButton) findViewById(checkedId);
                switch (checkedId) {
                    case R.id.btnStatic:
                        Label = 1;
                        break;
                    case R.id.btnWalk:
                        Label = 2;
                        break;
                    case R.id.btnRun:
                        Label = 3;
                        break;
                    case R.id.btnElevator:
                        Label = 4;
                        break;
                    case R.id.btnBike:
                        Label = 5;
                        break;
                    case R.id.btnCar:
                        Label = 6;
                        break;
                    case R.id.btnUpstairs:
                        Label = 7;
                        break;
                    case R.id.btnDownstairs:
                        Label = 8;
                        break;
                    case R.id.btnStopLabel:
                        Label = 0;
                        break;
                }
                /*
                if(Label!=LabelOld) {
                    t = Toast.makeText(getApplicationContext(), "当前标签是:" + label.getText(), Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();
                }
                */
            }
        });
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

    private class MyConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = (DetectorService.MyBinder) binder;
            Label = service.getLabel();
            LabelOld = Label;
            switch (service.getLabel()) {
                case 0:
                    StopLabel.setChecked(true);
                    break;
                case 1:
                    Static.setChecked(true);
                    break;
                case 2:
                    Walk.setChecked(true);
                    break;
                case 3:
                    Run.setChecked(true);
                    break;
                case 4:
                    Elevator.setChecked(true);
                    break;
                case 5:
                    Bike.setChecked(true);
                    break;
                case 6:
                    Car.setChecked(true);
                    break;
                case 7:
                    Upstairs.setChecked(true);
                    break;
                case 8:
                    Downstairs.setChecked(true);
                    break;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    @SuppressLint("ShowToast")
    public void onClick(View view) {
        // TODO Auto-generated method stub
        service.setLabel(Label);
        if (view.getId() == R.id.btncofirmLabel) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (conn != null) unbindService(conn);
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
