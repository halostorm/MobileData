package com.ustc.wsn.mydataapp.bean.ellipsoidFit;

import java.util.ArrayList;
import java.util.Random;

/**
 * Generate the points of an ellipse with the specified parameters.
 * 
 * Apache License Version 2.0, January 2004.
 * 
 * @author Kaleb
 * @version 1.0
 * 
 */
public class GeneratePoints
{

	/**
	 * Generate the points of an ellipse with the specified parameters.
	 * 
	 * @param a
	 *            the radii of the x-axis.
	 * @param b
	 *            the radii of the y-axis.
	 * @param c
	 *            the radii of the z-axis.
	 * @param shiftx
	 *            the shift from center on the x-axis.
	 * @param shifty
	 *            the shift from center on the y-axis.
	 * @param shiftz
	 *            the shift from center on the z-axis.
	 * @param noiseIntensity
	 *            a base value for the intensity of the noise, 0 = no noise.
	 */
	public ArrayList<ThreeSpacePoint> generatePoints(float a, float b,
			float c, float shiftx, float shifty, float shiftz,
			float noiseIntensity)
	{
		ArrayList<ThreeSpacePoint> points = new ArrayList<ThreeSpacePoint>();
		float[] x;
		float[] y;
		float[] z;

		int numPoints = 1000;

		x = new float[numPoints];
		y = new float[numPoints];
		z = new float[numPoints];
		Random r = new Random();

		for (int i = 0; i < numPoints; i++)
		{
			float s = (float)Math.toRadians(r.nextInt(360));
			float t = (float)Math.toRadians(r.nextInt(360));

			x[i] = a * (float)Math.cos(s) * (float)Math.cos(t);
			y[i] = b * (float)Math.cos(s) * (float)Math.sin(t);
			z[i] = c * (float)Math.sin(s);
		}

		float angle = (float)Math.toRadians(((float)Math.PI / 6));

		float[] xt = new float[numPoints];
		float[] yt = new float[numPoints];

		for (int i = 0; i < numPoints; i++)
		{
			xt[i] = x[i] * (float)Math.cos(angle) - y[i] * (float)Math.sin(angle);
			yt[i] = x[i] * (float)Math.sin(angle) + y[i] * (float)Math.cos(angle);
		}

		for (int i = 0; i < numPoints; i++)
		{
			x[i] = xt[i] + shiftx;
			y[i] = yt[i] + shifty;
			z[i] = z[i] + shiftz;
		}

		for (int i = 0; i < numPoints; i++)
		{
			x[i] = x[i] + r.nextFloat() * noiseIntensity;
			y[i] = y[i] + r.nextFloat() * noiseIntensity;
			z[i] = z[i] + r.nextFloat() * noiseIntensity;
		}

		ThreeSpacePoint tsp;

		for (int i = 0; i < numPoints; i++)
		{
			tsp = new ThreeSpacePoint(x[i], y[i], z[i]);
			points.add(tsp);
		}

		return points;
	}
}