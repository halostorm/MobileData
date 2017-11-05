package com.ustc.wsn.detector.bean;
/**
 * Created by halo on 2017/7/1.
 */

public class Data {

	private AcceleratorData accData;
	private GyroData gyroData;
	private MagnetData magData;
	public LocationData location;
	private CellInfo cellInfo;

	@Override
	public String toString() {
		// Log.d("accData", String.valueOf(accData==null));
		// Log.d("gyroData", String.valueOf(gyroData==null));
		// Log.d("locData", String.valueOf(location==null));
		// Log.d("cellInfo", String.valueOf(cellInfo==null));
		// return
		// accData.toString()+"$"+gyroData.toString()+"$"+location.toString()+"$"+cellInfo.toString();
		return accData.toString() + "\t$\t" + location.toString() + "\t$\t" + cellInfo.toString();
	}

	public AcceleratorData getAccData() {
		return accData;
	}

	public void setAccData(AcceleratorData accData) {
		this.accData = accData;
	}

	public GyroData getGyroData() {
		return gyroData;
	}

	public void setGyroData(GyroData gyroData) {
		this.gyroData = gyroData;
	}

	public MagnetData getMagData() {
		return magData;
	}

	public void setMagData(MagnetData magData) {
		this.magData = magData;
	}

	public LocationData getLocation() {
		return location;
	}

	public void setLocation(LocationData location) {
		this.location = location;
	}

	public CellInfo getCellInfo() {
		return cellInfo;
	}

	public void setCellInfo(CellInfo cellInfo) {
		this.cellInfo = cellInfo;
	}

	public String toStringExcptLoc() {
		return accData.toString() + "\t$\t" + cellInfo.toString();
	}

}
