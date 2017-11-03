package com.ustc.wsn.detector.detectorservice;

import android.content.Context;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import java.lang.Float;

import com.ustc.wsn.detector.bean.LocationData;

import java.util.Iterator;

public class gps {
	private LocationManager lm;
	private static int DataSize = 10000;// GPS缓冲池大小为10000
	private static final String TAG = "DetectorService";
	private String[] slocation;
	private static String bear;
	private int loc_cur;
	private int loc_old;

	// private StoreData sd;

	/**
	 * 初始化
	 *
	 * @param ctx
	 */
	public gps(Context ctx) {
		// 判断GPS是否正常启动
		slocation = new String[DataSize];
		lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		// sd = new StoreData();

		// 为获取地理位置信息时设置查询条件
		String bestProvider = lm.getBestProvider(getCriteria(), true);
		// 获取位置信息
		// 如果不设置查询要求，getLastKnownLocation方法传人的参数为LocationManager.GPS_PROVIDER
		Location location = lm.getLastKnownLocation(bestProvider);
		// updateView(location);
		// store(location);
		// if(location != null)
		loc_cur = 0;
		loc_old = 0;
		if (location != null) {
			slocation[loc_cur] = (new LocationData(location.getLongitude(), location.getLatitude(),
					System.currentTimeMillis(), location.getSpeed(), location.getBearing())).toString();
			bear = String.valueOf(location.getBearing());
			loc_cur = (loc_cur + 1) % DataSize;
		}
		// store(location);
		// 监听状态
		lm.addGpsStatusListener(listener);
		// 绑定监听，有4个参数
		// 参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种
		// 参数2，位置信息更新周期，单位毫秒
		// 参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
		// 参数4，监听
		// 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新

		// 1秒更新一次，或最小位移变化超过1米更新一次；
		// 注意：此处更新准确度非常低，推荐在service里面启动一个Thread，在run中sleep(10000);然后执行handler.sendMessage(),更新位置
		lm.requestLocationUpdates(bestProvider, 1000, 1, locationListener);
	}

	public String getLocation() {
		if (loc_old != loc_cur)// 非空
		{
			int i = loc_old;
			loc_old = (loc_old + 1) % DataSize;
			return slocation[i];
		} else
			return null;
	}

	public static void setCurrentBearToNull() {
		bear = null;
	}

	public static String getCurrentBear() {
		if (bear != null)// 非空
		{
			return bear;
		}
		return null;
	}

	public void closeLocation() {
		if (lm != null) {
			if (locationListener != null) {
				lm.removeUpdates(locationListener);
				locationListener = null;
			}
			lm = null;
		}
	}

	// 位置监听
	private LocationListener locationListener = new LocationListener() {

		/**
		 * 位置信息变化时触发
		 */
		public void onLocationChanged(Location location) {
			// store(location);
			if (location != null && (loc_cur + 1) % DataSize != loc_old)// 未满
			{
				slocation[loc_cur] = (new LocationData(location.getLongitude(), location.getLatitude(),
						System.currentTimeMillis(), location.getSpeed(), location.getBearing())).toString();
				bear = String.valueOf(location.getBearing());
				loc_cur = (loc_cur + 1) % DataSize;
			}
			/*
			 * slocation = location; if (thread == null) { thread = new
			 * Thread(new Runnable() {
			 *
			 * @Override public void run() { while (true) { //sLocation =
			 * getBestLocation(); if (slocation != null) { store(slocation); }
			 * synchronized (this) { try { wait(1000); } catch
			 * (InterruptedException e) { e.printStackTrace(); } } } } });
			 * thread.start(); }
			 */
			// updateView(location);
			// Log.i(TAG, "时间：" + location.getTime());
			// Log.i(TAG, "经度：" + location.getLongitude());
			// Log.i(TAG, "纬度：" + location.getLatitude());
			// Log.i(TAG, "海拔：" + location.getAltitude());
		}

		/**
		 * GPS状态变化时触发
		 */
		public void onStatusChanged(String provider, int status, Bundle extras) {
			switch (status) {
				// GPS状态为可见时
				case LocationProvider.AVAILABLE:
					// Log.i(TAG, "当前GPS状态为可见状态");
					break;
				// GPS状态为服务区外时
				case LocationProvider.OUT_OF_SERVICE:
					// Log.i(TAG, "当前GPS状态为服务区外状态");
					break;
				// GPS状态为暂停服务时
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
					// Log.i(TAG, "当前GPS状态为暂停服务状态");
					break;
			}
		}

		/**
		 * GPS开启时触发
		 */
		public void onProviderEnabled(String provider) {
			Location location = lm.getLastKnownLocation(provider);
			// updateView(location);
			// store(location);
			if (location != null) {
				slocation[loc_cur] = (new LocationData(location.getLongitude(), location.getLatitude(),
						System.currentTimeMillis(), location.getSpeed(), location.getBearing())).toString();
				bear = String.valueOf(location.getBearing());
				loc_cur = (loc_cur + 1) % DataSize;
			}
		}

		/**
		 * GPS禁用时触发
		 */
		public void onProviderDisabled(String provider) {
			// updateView(null);
			// store(null);
			slocation = null;
		}

	};

	// 状态监听
	GpsStatus.Listener listener = new GpsStatus.Listener() {
		public void onGpsStatusChanged(int event) {
			switch (event) {
				// 第一次定位
				case GpsStatus.GPS_EVENT_FIRST_FIX:
					// Log.i(TAG, "第一次定位");
					break;
				// 卫星状态改变
				case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
					// Log.i(TAG, "卫星状态改变");
					// 获取当前状态
					GpsStatus gpsStatus = lm.getGpsStatus(null);
					// 获取卫星颗数的默认最大值
					int maxSatellites = gpsStatus.getMaxSatellites();
					// 创建一个迭代器保存所有卫星
					Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
					int count = 0;
					while (iters.hasNext() && count <= maxSatellites) {
						GpsSatellite s = iters.next();
						count++;
					}
					// System.out.println("搜索到：" + count + "颗卫星");
					break;
				// 定位启动
				case GpsStatus.GPS_EVENT_STARTED:
					// Log.i(TAG, "定位启动");
					break;
				// 定位结束
				case GpsStatus.GPS_EVENT_STOPPED:
					// Log.i(TAG, "定位结束");
					break;
			}
		};
	};

	/**
	 * 返回查询条件
	 *
	 * @return
	 */
	private Criteria getCriteria() {
		Criteria criteria = new Criteria();
		// 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		// 设置是否要求速度
		criteria.setSpeedRequired(true);
		// 设置是否允许运营商收费
		criteria.setCostAllowed(false);
		// 设置是否需要方位信息
		criteria.setBearingRequired(true);
		// 设置是否需要海拔信息
		criteria.setAltitudeRequired(false);
		// 设置对电源的需求
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		return criteria;
	}
}
