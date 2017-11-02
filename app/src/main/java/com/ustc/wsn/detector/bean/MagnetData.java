package com.ustc.wsn.detector.bean;

public class MagnetData {

	private float x;
	private float y;
	private float z;
	//private long time;

	public MagnetData(float[] values) {
		x = values[0];
		y = values[1];
		z = values[2];
		//this.time = time;
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
/*
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
*/
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
