package com.ustc.wsn.detector.Application;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import com.ustc.wsn.detector.bean.AcceleratorData;
import com.ustc.wsn.detector.bean.CellInfo;
import com.ustc.wsn.detector.bean.Data;
import com.ustc.wsn.detector.bean.GyroData;
import com.ustc.wsn.detector.bean.LocationData;
import com.ustc.wsn.detector.bean.MagnetData;
import com.ustc.wsn.detector.detectorservice.DetectorLocationListener;

import org.xutils.x;

import java.util.LinkedList;
import java.util.Queue;

@SuppressLint("NewApi")
public class AppResourceApplication extends Application {

	private static final int SensorDatasSize = 200;
	private static final float zlimit = 9.0f;
	private static final float xlimit = 9.0f;
	private static final float ylimit = 9.0f;
	private static final float zthreshold = 1.0f;
	private static final float xthreshold = 1.0f;
	private static final float ythreshold = 1.0f;

	// private static final int EXCEPTION_ROAD=1;
	// private static final int EXCEPTION_STOP=2;
	// private static final int EXCEPTION_SHIFT=3;

	private Queue<AcceleratorData> accDatas;
	private Queue<GyroData> gyroDatas;
	private Queue<MagnetData> magDatas;

	private int msgCounter = 0;

	private float accXSum = 0;
	private float accYSum = 0;
	private float accZSum = 0;

	private float accXSqureSum = 0;
	private float accYSqureSum = 0;
	private float accZSqureSum = 0;

	private float gyroXSum = 0;
	private float gyroYSum = 0;
	private float gyroZSum = 0;

	private float magXSum = 0;
	private float magYSum = 0;
	private float magZSum = 0;

	private LocationManager locationManager;
	private DetectorLocationListener locationListener;
	private Criteria criteria;
	private Location currentBestLocation;

	public boolean exception_road = false;
	public boolean exception_stop = false;
	public boolean exception_shift = false;
	// private boolean hasReportException=true;

	private int locationChangedCounter = 0;

	public ExceptionUtil exceptionUtil;

	private boolean sendflag = false;

	private Data data;

	@Override

	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();



		x.Ext.init(this);

//		UploadManagers.initAutoUploadSeriver(this,Environment.getExternalStorageDirectory().getPath() + "/DetectorService/" + "Data","13439070000");

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationListener = new DetectorLocationListener((AppResourceApplication) getApplicationContext());
		criteria = new Criteria();
		accDatas = new LinkedList<AcceleratorData>();
		gyroDatas = new LinkedList<GyroData>();
		magDatas = new LinkedList<MagnetData>();
		data = new Data();
		exceptionUtil = new ExceptionUtil();


		// ShowMsg();
	}

	public AppResourceApplication() {
		accDatas = new LinkedList<AcceleratorData>();
		gyroDatas = new LinkedList<GyroData>();
		magDatas = new LinkedList<MagnetData>();
		data = new Data();
		exceptionUtil = new ExceptionUtil();
	}

	public Queue<AcceleratorData> getAccDatas() {
		return accDatas;
	}

	public void setAccDatas(Queue<AcceleratorData> accDatas) {
		this.accDatas = accDatas;
	}

	public Queue<GyroData> getGyroDatas() {
		return gyroDatas;
	}

	public void setGyroDatas(Queue<GyroData> gyroDatas) {
		this.gyroDatas = gyroDatas;
	}

	public Queue<MagnetData> getMagnetDatas() {
		return magDatas;
	}

	public void setMagDatas(Queue<MagnetData> magDatas) {
		this.magDatas = magDatas;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public Criteria getCriteria() {
		return criteria;
	}

	public void setCriteria(Criteria criteria) {
		this.criteria = criteria;
	}

	public boolean isException_road() {
		return exception_road;
	}

	public void setException_road(boolean exception_road) {
		this.exception_road = exception_road;
	}

	public boolean isException_stop() {
		return exception_stop;
	}

	public void setException_stop(boolean exception_stop) {
		this.exception_stop = exception_stop;
	}

	public boolean isException_shift() {
		return exception_shift;
	}

	public void setException_shift(boolean exception_shift) {
		this.exception_shift = exception_shift;
	}

	public LocationManager getLocationManager() {
		return locationManager;
	}

	public void setLocationManager(LocationManager locationManager) {
		this.locationManager = locationManager;
	}

	public DetectorLocationListener getLocationListener() {
		return locationListener;
	}

	public void setLocationListener(DetectorLocationListener locationListener) {
		this.locationListener = locationListener;
	}

	public void updateAccData(AcceleratorData accData) {
		this.data.setAccData(accData);
	}

	public void updataAccDatas(AcceleratorData accData) {
		if (accDatas.size() < SensorDatasSize) {
			accDatas.offer(accData);
			accXSum += accData.getX();
			accYSum += accData.getY();
			accZSum += accData.getZ();

			accXSqureSum += accData.getX() * accData.getX();
			accYSqureSum += accData.getY() * accData.getY();
			accZSqureSum += accData.getZ() * accData.getZ();
			// data.setAccData(accData);
		} else {
			AcceleratorData oldData = accDatas.poll();
			accXSum -= oldData.getX();
			accYSum -= oldData.getY();
			accZSum -= oldData.getZ();
			accXSqureSum -= oldData.getX() * oldData.getX();
			accYSqureSum -= oldData.getY() * oldData.getY();
			accZSqureSum -= oldData.getZ() * oldData.getZ();

			accDatas.offer(accData);
			accXSum += accData.getX();
			accYSum += accData.getY();
			accZSum += accData.getZ();
			accXSqureSum += accData.getX() * accData.getX();
			accYSqureSum += accData.getY() * accData.getY();
			accZSqureSum += accData.getZ() * accData.getZ();
			// data.setAccData(accData);
		}
	}

	public void updateGyroData(GyroData gyroData) {
		this.data.setGyroData(gyroData);
	}

	public void updateGyroDatas(GyroData gyroData) {
		if (gyroDatas.size() < SensorDatasSize) {
			gyroDatas.offer(gyroData);
			gyroXSum += gyroData.getX();
			gyroYSum += gyroData.getY();
			gyroZSum += gyroData.getZ();
			// data.setGyroData(gyroData);
		} else {
			GyroData oldData = gyroDatas.poll();
			gyroXSum -= oldData.getX();
			gyroYSum -= oldData.getY();
			gyroZSum -= oldData.getZ();

			gyroDatas.offer(gyroData);
			gyroXSum += gyroData.getX();
			gyroYSum += gyroData.getY();
			gyroZSum += gyroData.getZ();
			// data.setGyroData(gyroData);
		}
	}

	public void updateMagData(MagnetData magData) {
		this.data.setMagData(magData);
	}

	public void updateMagDatas(MagnetData magData) {
		if (magDatas.size() < SensorDatasSize) {
			magDatas.offer(magData);
			magXSum += magData.getX();
			magYSum += magData.getY();
			magZSum += magData.getZ();
			// data.setGyroData(gyroData);
		} else {
			MagnetData oldData = magDatas.poll();
			magXSum -= oldData.getX();
			magYSum -= oldData.getY();
			magZSum -= oldData.getZ();

			magDatas.offer(magData);
			magXSum += magData.getX();
			magYSum += magData.getY();
			magZSum += magData.getZ();
			// data.setGyroData(gyroData);
		}
	}

	public void updateCellInfo(CellInfo cellInfo) {
		this.data.setCellInfo(cellInfo);
	}

	public void updateLocationData(LocationData locData) {
		this.data.setLocation(locData);
	}
/*
	public boolean roadIsOK(AcceleratorData accData, GyroData gyroData) {
		// if(Math.abs(accZSum-accData.getZ()*accDatas.size())>zlimit*accDatas.size()){
		double threshold = zlimit * (accZSqureSum / accDatas.size() - Math.pow((accZSum / accDatas.size()), 2));
		double newdata = Math.pow((accZSum / accDatas.size() - accData.getZ()), 2);
		if (newdata >= Math.pow(zthreshold, 2) && newdata >= threshold) {
			exception_road = true;
			this.updateAccData(accData);
			// Log.e("Road", "Excetion");
			/*
			 * ExceptionUtil eu=new
			 * ExceptionUtil(ExceptionUtil.EXCEPTION_ROAD+"\t$\t"+data.
			 * toStringExcptLoc());
			 * eu.reportException(ExceptionUtil.EXCEPTION_ROAD);
			 * 
			 * StoreData sd=new StoreData(); sd.storeExceptionExptLoc(data,
			 * ExceptionUtil.EXCEPTION_ROAD);
			 */
	/*
			accData.setTime(System.currentTimeMillis());
			data.setAccData(accData);
			// AddMsgToQueue(ExceptionUtil.EXCEPTION_ROAD
			// +"\t$\t"+data.toStringExcptLoc(), ExceptionUtil.EXCEPTION_ROAD);

			// LocationData loc = new LocationData(0.55555, 0.66666,
			// System.currentTimeMillis());
			// exceptionUtil.reportException(1, loc, loc.getTime());

			locationManager.requestSingleUpdate(criteria, locationListener, getMainLooper());

			return false;
		}
		return true;
	}
*/
	public boolean suddenStop(AcceleratorData accData, GyroData gyroData) {
		// if(Math.abs(accYSum-accData.getY()*accDatas.size())>ylimit*accDatas.size()){
		double newdata = Math.pow((accYSum / accDatas.size() - accData.getY()), 2);
		double threshold = ylimit * (accYSqureSum / accDatas.size() - Math.pow((accYSum / accDatas.size()), 2));
		if (newdata >= Math.pow(ythreshold, 2) && newdata >= threshold) {
			exception_stop = true;
			this.updateAccData(accData);
			// Log.e("Driving", "Suddenly Stop or Start");
			/*
			 * ExceptionUtil eu=new
			 * ExceptionUtil(ExceptionUtil.EXCEPTION_STOP+"\t$\t"+data.
			 * toStringExcptLoc());
			 * eu.reportException(ExceptionUtil.EXCEPTION_STOP);
			 * 
			 * StoreData sd=new StoreData(); sd.storeExceptionExptLoc(data,
			 * ExceptionUtil.EXCEPTION_STOP);
			 */
			//accData.setTime(System.currentTimeMillis());
			data.setAccData(accData);
			// AddMsgToQueue(ExceptionUtil.EXCEPTION_STOP+"\t$\t"+data.toStringExcptLoc(),ExceptionUtil.EXCEPTION_STOP);

			locationManager.requestSingleUpdate(criteria, locationListener, getMainLooper());
			return false;
		}
		return true;
	}

	public boolean suddenShift(AcceleratorData accData, GyroData gyroData) {
		// if(Math.abs(accXSum-accData.getX()*accDatas.size())>xlimit*accDatas.size()){
		double newdata = Math.pow((accXSum / accDatas.size() - accData.getX()), 2);
		double threshold = xlimit * (accXSqureSum / accDatas.size() - Math.pow((accXSum / accDatas.size()), 2));
		if (newdata >= Math.pow(xthreshold, 2) && newdata >= threshold) {
			exception_shift = true;
			this.updateAccData(accData);
			// Log.e("Driving", "Suddenly Shift");

			/*
			 * ExceptionUtil eu=new
			 * ExceptionUtil(ExceptionUtil.EXCEPTION_SHIFT+"\t$\t"+data.
			 * toStringExcptLoc());
			 * eu.reportException(ExceptionUtil.EXCEPTION_SHIFT);
			 * 
			 * StoreData sd=new StoreData(); sd.storeExceptionExptLoc(data,
			 * ExceptionUtil.EXCEPTION_SHIFT);
			 */
			//accData.setTime(System.currentTimeMillis());
			data.setAccData(accData);
			// AddMsgToQueue(ExceptionUtil.EXCEPTION_SHIFT+"\t$\t"+data.toStringExcptLoc(),
			// ExceptionUtil.EXCEPTION_SHIFT);

			locationManager.requestSingleUpdate(criteria, locationListener, getMainLooper());
			return false;
		}
		return true;
	}

	@SuppressLint("ShowToast")
	public void AddMsgToQueue(String accmsg, int flag) {

		if (data.location != null) {
			accmsg += "$" + data.location.toString();
		}

		if (msgCounter % 50 == 0)
			// ShowMsgCounter();

			if (exceptionUtil.messageQueue.size() % exceptionUtil.msgSendThreshold == 0
					&& exceptionUtil.messageQueue.size() > 0) {
				// if(exceptionUtil.isBusy == true)
				// {
				// return;
				// }

				// if(exceptionUtil.socket.isConnected())
				// return;
				// Timer timer = new Timer(true);
				/*
				 * timer.schedule( new java.util.TimerTask() { public void run()
				 * { ShowMsgSendNum(); } }, 500, 2000);
				 */
				// ShowMsg();
				// LocationData currLoc = new
				// LocationData(currentBestLocation.getLongitude(),
				// currentBestLocation.getLatitude(),
				// System.currentTimeMillis(),currentBestLocation.getSpeed());
				exceptionUtil.reportException(1);
				// return;
			}

		msgCounter++;
		// msgCounter = msgCounter%exceptionUtil.messageQueueSize;
		if (exceptionUtil.messageQueue.size() < exceptionUtil.messageQueueSize) {
			exceptionUtil.messageQueue.add(accmsg);
			// StoreData sd=new StoreData();
			// sd.storeExceptionExptLocation(accmsg, flag);
		} else {
			return;
		}
	}
	/*
	 * @SuppressLint("ShowToast") public void ShowMsgSendNum() {
	 * Toast.makeText(this, "Received Num: " +
	 * exceptionUtil.receiveThread.sendNumber, Toast.LENGTH_SHORT).show(); }
	 * 
	 * public void ShowMsg() { Toast.makeText(this, "Send Data to Server!" +
	 * "Message Number: " + exceptionUtil.messageQueue.size(),
	 * Toast.LENGTH_SHORT).show(); }
	 * 
	 * public void ShowMsgCounter() { Toast.makeText(this, "Message Number: " +
	 * msgCounter + "sendflag" + exceptionUtil.isBusy,
	 * Toast.LENGTH_SHORT).show(); }
	 */
}
