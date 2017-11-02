package com.ustc.wsn.detector.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.ustc.wsn.detector.service.DetectorService;

import detector.wsn.ustc.com.detectorservice.R;

public class DetectorActivity extends Activity implements OnClickListener {

	private Intent serviceIntent;
	private LocationManager loc_int;

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
		serviceIntent = new Intent(this, DetectorService.class);

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
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
				break;
			case R.id.btnStopService:
				stopService(serviceIntent);
				break;
		}
	}
}