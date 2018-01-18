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
import com.ustc.wsn.mydataapp.utils.UploadManagers;

import java.util.Timer;
import java.util.TimerTask;

import detector.wsn.ustc.com.mydataapp.R;

public class DetectorActivity extends Activity implements OnClickListener {

    Boolean isExit = false;
    protected Intent DetectorserviceIntent;
	private LocationManager loc_int;
	//private volatile int stateLabel;
	private DetectorService msgService;
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

		Button Static =(Button) findViewById(R.id.btnStatic);
		Button Walk =(Button) findViewById(R.id.btnWalk);
		Button Run =(Button) findViewById(R.id.btnRun);
		Button Elevator =(Button) findViewById(R.id.btnElevator);
		Button Bike =(Button) findViewById(R.id.btnBike);
		Button Car =(Button) findViewById(R.id.btnCar);
		Button Upstairs =(Button) findViewById(R.id.btnUpstairs);
		Button Downstairs =(Button) findViewById(R.id.btnDownstairs);
        Button StopLabel =(Button) findViewById(R.id.btnStopLabel);
        Button StartUpload =(Button) findViewById(R.id.btnStartUpload);
		Static.setOnClickListener(this);
		Walk.setOnClickListener(this);
		Run.setOnClickListener(this);
		Elevator.setOnClickListener(this);
		Bike.setOnClickListener(this);
		Car.setOnClickListener(this);
		Upstairs.setOnClickListener(this);
		Downstairs.setOnClickListener(this);
        StopLabel.setOnClickListener(this);
        StartUpload.setOnClickListener(this);

		//stateLabel();
        new outputFile();//create data path
        DetectorserviceIntent = new Intent(this, DetectorService.class);
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
				startService(DetectorserviceIntent);
                serviceStart = true;
                bindService(DetectorserviceIntent, conn, Context.BIND_AUTO_CREATE);
                Toast.makeText(this, "服务已启动", Toast.LENGTH_SHORT).show();
				break;
			case R.id.btnStopService:
                if(serviceStart == true) {
                    unbindService(conn);
                    stopService(DetectorserviceIntent);
                    Toast.makeText(this, "服务已关闭！", Toast.LENGTH_SHORT).show();
                    serviceStart = false;
                    break;
                }
                else
                    Toast.makeText(this, "请先启动服务", Toast.LENGTH_SHORT).show();
                break;
			case R.id.btnStatic:
                if(serviceStart == true) {
                    msgService.stateLabel = 1;
                    Toast.makeText(this, "<1>当前状态：静止！", Toast.LENGTH_SHORT).show();
                    break;
                }
                else
                    Toast.makeText(this, "请先启动服务", Toast.LENGTH_SHORT).show();
				break;
			case R.id.btnWalk:
                if(serviceStart == true) {
                    Toast.makeText(this, "<2> 当前状态：步行", Toast.LENGTH_SHORT).show();
                    msgService.stateLabel = 2;
                    break;
                }
                else
                    Toast.makeText(this, "请先启动服务", Toast.LENGTH_SHORT).show();
                break;
			case R.id.btnRun:
                if(serviceStart == true) {
                    Toast.makeText(this, "<3> 当前状态：跑步", Toast.LENGTH_SHORT).show();
                    msgService.stateLabel = 3;
                    break;
                }
                else
                    Toast.makeText(this, "请先启动服务", Toast.LENGTH_SHORT).show();
                break;
			case R.id.btnElevator:
                if(serviceStart == true) {
                    Toast.makeText(this, "<4> 当前状态：乘坐电梯", Toast.LENGTH_SHORT).show();
                    msgService.stateLabel = 4;
                    break;
                }
                else
                    Toast.makeText(this, "请先启动服务", Toast.LENGTH_SHORT).show();
                break;
			case R.id.btnBike:
                if(serviceStart == true) {
                    Toast.makeText(this, "<5> 当前状态：骑自行车", Toast.LENGTH_SHORT).show();
                    msgService.stateLabel = 5;
                    break;
                }
                else
                    Toast.makeText(this, "请先启动服务", Toast.LENGTH_SHORT).show();
                break;
			case R.id.btnCar:
                if(serviceStart == true) {
                    Toast.makeText(this, "<6> 当前状态：坐车", Toast.LENGTH_SHORT).show();
                    msgService.stateLabel = 6;
                    break;
                }
                else
                    Toast.makeText(this, "请先启动服务", Toast.LENGTH_SHORT).show();
                break;
			case R.id.btnUpstairs:
                if(serviceStart == true) {
                    Toast.makeText(this, "<7>当前状态：上楼梯", Toast.LENGTH_SHORT).show();
                    msgService.stateLabel = 7;
                    break;
                }
                else
                    Toast.makeText(this, "请先启动服务", Toast.LENGTH_SHORT).show();
                break;
			case R.id.btnDownstairs:
                if(serviceStart == true) {
                    Toast.makeText(this, "<8> 当前状态：下楼梯", Toast.LENGTH_SHORT).show();
                    msgService.stateLabel = 8;
                    break;
                }
                else
                    Toast.makeText(this, "请先启动服务", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnStopLabel:
                if(serviceStart == true) {
                    Toast.makeText(this, "已关闭标记", Toast.LENGTH_SHORT).show();
                    msgService.stateLabel = 0;
                    break;
                }
                else
                    Toast.makeText(this, "请先启动服务", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnStartUpload:
                int netType = getNetworkType();
                if (netType == -1) {
                    Toast.makeText(DetectorActivity.this,"请开启网络连接",Toast.LENGTH_SHORT).show();
                }
                if (netType == ConnectivityManager.TYPE_WIFI) {
                    Toast.makeText(DetectorActivity.this,"当前是Wifi连接，请放心使用",Toast.LENGTH_SHORT).show();
                    String psw = "OK";
                    Intent intent = this.getIntent();
                    psw=intent.getStringExtra("userId");
                    //Toast.makeText(DetectorActivity.this,psw,Toast.LENGTH_SHORT).show();
                    UploadManagers.initAutoUploadSeriver(DetectorActivity.this,
                            Environment.getExternalStorageDirectory().getPath() + "/MyDataApp",psw);
                } else if (netType == ConnectivityManager.TYPE_MOBILE) {
                    Toast.makeText(DetectorActivity.this,"为避免数据流量消耗，请切换至Wifi再上传",Toast.LENGTH_SHORT).show();
                }
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