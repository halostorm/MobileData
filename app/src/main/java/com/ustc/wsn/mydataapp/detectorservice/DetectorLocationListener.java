package com.ustc.wsn.mydataapp.detectorservice;
/**
 * Created by halo on 2017/7/1.
 */

import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.ustc.wsn.mydataapp.Application.AppResourceApplication;

@SuppressLint("ShowToast")
public class DetectorLocationListener implements LocationListener {

	private Location sLocation;
	// private Thread thread = null;
	private AppResourceApplication resource;

	public DetectorLocationListener(AppResourceApplication resource) {
		super();
		// TODO Auto-generated constructor stub
		this.resource = resource;
	}

	// private Handler mHandler = new Handler(){
	// public void handleMessage(Message msg){
	// super.handleMessage(msg);
	// //handle the UI
	// }
	// };
	//
	// Runnable runnable = new Runnable(){
	// public void run(){
	// Socket socket = null;
	// int port = 8821;
	// try {
	// socket = new Socket("219.219.216.181", port);// input the Server, Address
	// OutputStream socketOut = socket.getOutputStream();
	// socketOut.write(resource.getData().toString().getBytes());
	// socketOut.flush();
	// } catch (UnknownHostException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } //
	// // Log.d("Report Exception", "Report Over");
	// mHandler.sendEmptyMessage(0);
	// }
	// };

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		// Log.i("Location", location.toString());
		/*
		 * LocationData loc = new LocationData(location.getLongitude(),
		 * location.getLatitude(),
		 * System.currentTimeMillis(),location.getSpeed());
		 * resource.updateLocationData(loc); StoreData sd = new StoreData();
		 * sd.storeLocation(loc);
		 */
		/*
		 * StoreData sd = new StoreData(); if (resource.isException_road()) {
		 * ExceptionUtil eu = new ExceptionUtil(ExceptionUtil.EXCEPTION_ROAD +
		 * "\t$\t" + resource.getData().toString());
		 * eu.reportException(ExceptionUtil.EXCEPTION_ROAD);
		 * resource.setException_road(false);
		 * sd.storeException(resource.getData(), ExceptionUtil.EXCEPTION_ROAD);
		 * } if (resource.isException_stop()) { ExceptionUtil eu = new
		 * ExceptionUtil(ExceptionUtil.EXCEPTION_STOP + "\t$\t" +
		 * resource.getData().toString());
		 * eu.reportException(ExceptionUtil.EXCEPTION_STOP);
		 * resource.setException_stop(false);
		 * sd.storeException(resource.getData(), ExceptionUtil.EXCEPTION_STOP);
		 * } if (resource.isException_shift()) { ExceptionUtil eu = new
		 * ExceptionUtil(ExceptionUtil.EXCEPTION_SHIFT + "\t$\t" +
		 * resource.getData().toString());
		 * eu.reportException(ExceptionUtil.EXCEPTION_SHIFT);
		 * resource.setException_shift(false);
		 * sd.storeException(resource.getData(), ExceptionUtil.EXCEPTION_SHIFT);
		 * } sd.storeLocation(loc);
		 */

		/*
		 * if (resource.isException_road() || resource.isException_shift() ||
		 * resource.isException_stop()) {
		 * 
		 * resource.ShowMsg(); if(resource.exceptionUtil.messageQCounter >= 200)
		 * resource.exceptionUtil.reportException(1, loc, loc.getTime()); }
		 */

		// if (resource.exceptionUtil.totalQCounter < 200)
		// resource.ShowMsgForLoc();
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

}
