package com.ustc.wsn.detector.bean;
/**
 * Created by halo on 2017/7/1.
 */

import com.ustc.wsn.detector.utils.TimeUtil;

public class RotationData {

	private float x;
	private float y;
	private float z;
	private float w;
	private long time;

	public RotationData(float[] values, long time) {
		x = values[0];
		y = values[1];
		z = values[2];
		w = values[3];
		this.time = time;
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

	public float getW() {
		return w;
	}

	public void setW(float w) {
		this.w = w;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
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
		return TimeUtil.getTime(time) + "\t" + time + "\t" + x + "\t" + y + "\t" + z + "\t" + w;
	}

}
