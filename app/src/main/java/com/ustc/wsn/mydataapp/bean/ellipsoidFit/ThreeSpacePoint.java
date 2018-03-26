package com.ustc.wsn.mydataapp.bean.ellipsoidFit;
/**
 * A representation of a three space point with float precision.
 * 
 * Apache License Version 2.0, January 2004.
 * 
 * @author Kaleb
 * @version 1.0
 * 
 */
public class ThreeSpacePoint
{
	public float x;
	public float y;
	public float z;

	/**
	 * Instantiate a new object.
	 * @param x the point on the x-axis
	 * @param y the point on the y-axis
	 * @param z the point on the z-axis
	 */
	public ThreeSpacePoint(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
