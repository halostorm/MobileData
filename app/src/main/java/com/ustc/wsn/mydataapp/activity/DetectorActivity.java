package com.ustc.wsn.mydataapp.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.ustc.wsn.mydataapp.detectorservice.outputFile;
import com.ustc.wsn.mydataapp.service.DetectorService;
import com.ustc.wsn.mydataapp.service.GpsService;
import com.ustc.wsn.mydataapp.utils.UploadManagers;

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
			Toast.makeText(this, "请开启高精度GPS导航！", Toast.LENGTH_SHORT).show();
			// 返回开启GPS导航设置界面
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivityForResult(intent, 0);
			// return;
		}

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

        new outputFile();//create data path
        DetectorserviceIntent = new Intent(this, DetectorService.class);
        GpsserviceIntent = new Intent(this, GpsService.class);
        SimpleActivityIntent = new Intent(this,SimulationActivity.class);
        LabelActivityIntent = new Intent(this,LabelActivity.class);
        UploadActivityIntent = new Intent(this,UploadActivity.class);
		//bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);

	}
/*
	ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			//返回一个MsgService对象
			msgService = ((DetectorService.MsgBinder)service).getService();
		}
	};
*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            exitByDoubleClick();
        }
        if(keyCode==KeyEvent.KEYCODE_HOME){
            //exitByDoubleClick();
        }
        return false;
    }

    private void exitByDoubleClick() {
        Timer tExit=null;
        if(!isExit){
            isExit=true;
            Toast.makeText(this,"再按一次退出程序!",Toast.LENGTH_SHORT).show();
            tExit=new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit=false;//取消退出
                }
            },1000);// 如果1秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        }else{
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
                Toast.makeText(this, "服务已启动", Toast.LENGTH_SHORT).show();
				break;
            case R.id.btnViewData:
                if(serviceStart == true) {
                    startActivity(SimpleActivityIntent);
                }
                else
                    Toast.makeText(this, "请先启动服务", Toast.LENGTH_SHORT).show();
                break;
			case R.id.btnStopService:
                if(serviceStart == true) {
                   // unbindService(conn);
                    stopService(DetectorserviceIntent);
                    stopService(GpsserviceIntent);
                    Toast.makeText(this, "服务已关闭！", Toast.LENGTH_SHORT).show();
                    serviceStart = false;
                    break;
                }
                else
                    Toast.makeText(this, "请先启动服务", Toast.LENGTH_SHORT).show();
                break;
			case R.id.btnStartLabel:
                if(serviceStart == true) {
                    startActivity(LabelActivityIntent);
                    break;
                }
                else
                    Toast.makeText(this, "请先启动服务", Toast.LENGTH_SHORT).show();
				break;
            case R.id.btnBeginUploadActivity:
                startActivity(UploadActivityIntent);
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