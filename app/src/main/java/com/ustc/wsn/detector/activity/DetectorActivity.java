package com.ustc.wsn.detector.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.ustc.wsn.detector.service.DetectorService;

import java.util.Timer;
import java.util.TimerTask;

import detector.wsn.ustc.com.detectorservice.R;

public class DetectorActivity extends Activity implements OnClickListener {

    Boolean isExit = false;
	private Intent serviceIntent;
	private LocationManager loc_int;
	//private volatile int stateLabel;
	private DetectorService msgService;
    private boolean serviceStart = false;

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

		Button Static =(Button) findViewById(R.id.btnStatic);
		Button Walk =(Button) findViewById(R.id.btnWalk);
		Button Run =(Button) findViewById(R.id.btnRun);
		Button Elevator =(Button) findViewById(R.id.btnElevator);
		Button Bike =(Button) findViewById(R.id.btnBike);
		Button Car =(Button) findViewById(R.id.btnCar);
		Button Upstairs =(Button) findViewById(R.id.btnUpstairs);
		Button Downstairs =(Button) findViewById(R.id.btnDownstairs);
        Button StopLabel =(Button) findViewById(R.id.btnStopLabel);
		Static.setOnClickListener(this);
		Walk.setOnClickListener(this);
		Run.setOnClickListener(this);
		Elevator.setOnClickListener(this);
		Bike.setOnClickListener(this);
		Car.setOnClickListener(this);
		Upstairs.setOnClickListener(this);
		Downstairs.setOnClickListener(this);
        StopLabel.setOnClickListener(this);

		//stateLabel();
		serviceIntent = new Intent(this, DetectorService.class);
		//bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);

	}

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


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            exitByDoubleClick();
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

//    @Override
//    public void onBackPressed() {
//        Intent home = new Intent(Intent.ACTION_MAIN);
//        home.addCategory(Intent.CATEGORY_HOME);
//        startActivity(home);
//    }


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
				startService(serviceIntent);
                serviceStart = true;
                bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
                Toast.makeText(this, "Service starts successful！", Toast.LENGTH_SHORT).show();
				break;
			case R.id.btnStopService:
                unbindService(conn);
				stopService(serviceIntent);
                Toast.makeText(this, "Service has been closed！", Toast.LENGTH_SHORT).show();
                serviceStart = false;
				break;
			case R.id.btnStatic:
                if(serviceStart == true) {
                    msgService.stateLabel = 1;
                    Toast.makeText(this, "<1> Hold still！", Toast.LENGTH_SHORT).show();
                    break;
                }
                else
                    Toast.makeText(this, "Please Start Service！", Toast.LENGTH_SHORT).show();
				break;
			case R.id.btnWalk:
                if(serviceStart == true) {
                    Toast.makeText(this, "<2> Start walking！", Toast.LENGTH_SHORT).show();
                    msgService.stateLabel = 2;
                    break;
                }
                else
                    Toast.makeText(this, "Please Start Service！", Toast.LENGTH_SHORT).show();
                break;
			case R.id.btnRun:
                if(serviceStart == true) {
                    Toast.makeText(this, "<3> Start running！", Toast.LENGTH_SHORT).show();
                    msgService.stateLabel = 3;
                    break;
                }
                else
                    Toast.makeText(this, "Please Start Service！", Toast.LENGTH_SHORT).show();
                break;
			case R.id.btnElevator:
                if(serviceStart == true) {
                    Toast.makeText(this, "<4> Be in elevator！", Toast.LENGTH_SHORT).show();
                    msgService.stateLabel = 4;
                    break;
                }
                else
                    Toast.makeText(this, "Please Start Service！", Toast.LENGTH_SHORT).show();
                break;
			case R.id.btnBike:
                if(serviceStart == true) {
                    Toast.makeText(this, "<5> Start riding bicycle！", Toast.LENGTH_SHORT).show();
                    msgService.stateLabel = 5;
                    break;
                }
                else
                    Toast.makeText(this, "Please Start Service！", Toast.LENGTH_SHORT).show();
                break;
			case R.id.btnCar:
                if(serviceStart == true) {
                    Toast.makeText(this, "<6> Be in a vehicle！", Toast.LENGTH_SHORT).show();
                    msgService.stateLabel = 6;
                    break;
                }
                else
                    Toast.makeText(this, "Please Start Service！", Toast.LENGTH_SHORT).show();
                break;
			case R.id.btnUpstairs:
                if(serviceStart == true) {
                    Toast.makeText(this, "<7> Go upstairs！", Toast.LENGTH_SHORT).show();
                    msgService.stateLabel = 7;
                    break;
                }
                else
                    Toast.makeText(this, "Please Start Service！", Toast.LENGTH_SHORT).show();
                break;
			case R.id.btnDownstairs:
                if(serviceStart == true) {
                    Toast.makeText(this, "<8> Go downstairs！", Toast.LENGTH_SHORT).show();
                    msgService.stateLabel = 8;
                    break;
                }
                else
                    Toast.makeText(this, "Please Start Service！", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnStopLabel:
                if(serviceStart == true) {
                    Toast.makeText(this, "Closed Label！", Toast.LENGTH_SHORT).show();
                    msgService.stateLabel = 0;
                    break;
                }
                else
                    Toast.makeText(this, "Please Start Service！", Toast.LENGTH_SHORT).show();
                break;
		}
	}
/*
	public void stateLabel()
	{
		Button Static =(Button) findViewById(R.id.btnStatic);
		Button Walk =(Button) findViewById(R.id.btnWalk);
		Button Run =(Button) findViewById(R.id.btnRun);
		Button Elevator =(Button) findViewById(R.id.btnElevator);
		Button Bike =(Button) findViewById(R.id.btnBike);
		Button Car =(Button) findViewById(R.id.btnCar);
		Button Upstairs =(Button) findViewById(R.id.btnUpstairs);
		Button Downstairs =(Button) findViewById(R.id.btnDownstairs);

		Static.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				msgService.stateLabel = 1;
			}
		});
		Walk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				msgService.stateLabel = 1;
			}
		});
		Run.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				msgService.stateLabel = 1;
			}
		});
		Elevator.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				msgService.stateLabel = 1;
			}
		});
		Bike.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				msgService.stateLabel = 1;
			}
		});
		Car.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				msgService.stateLabel = 1;
			}
		});
		Upstairs.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				msgService.stateLabel = 1;
			}
		});
		Downstairs.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				msgService.stateLabel = 1;
			}
		});
	}
	*/
}