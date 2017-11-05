package com.ustc.wsn.detector.bean;
/**
 * Created by halo on 2017/7/1.
 */

public class AcceleratorData {

	private float x;
	private float y;
	private float z;
	//private long time;

	public AcceleratorData(float[] values) {
		x = values[0];
		y = values[1];
		z = values[2];
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}

	@Override
	public String toString() {
		// SimpleDateFormat formatter = new SimpleDateFormat(
		// "yyyy/MM/dd HH:mm:ss:sss");
		// Date curDate = new Date(time);
		// return formatter.format(curDate)+"\t"+time + "\t" + x + "\t" + y +
		// "\t" + z;
		// return "AcceleratorData [x=" + x + ", y=" + y + ", z=" + z +
		// ", time="
		// + time + "]";
		return x + "\t" + y + "\t" + z;
	}

}
