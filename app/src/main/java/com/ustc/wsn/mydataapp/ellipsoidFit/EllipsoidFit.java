package com.ustc.wsn.mydataapp.ellipsoidFit;

import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Creates an example of fitting a sphere with center = [0,0,0] and radii =
 * [1,1,1] and an ellipsoid with with center = [2,2,2] and radii = [1.4,1.3,1.2]
 * 
 * Apache License Version 2.0, January 2004.
 * 
 * @author Kaleb
 * @version 1.0
 * 
 */
public class EllipsoidFit
{
	final String TAG = EllipsoidFit.class.toString();
	ArrayList<ThreeSpacePoint> p;
	float[] params = new float[6];
	final float G = 9.806f;
	/*
	static ArrayList<ThreeSpacePoint> CONTROL_SPHERE_POINTS;
	static ArrayList<ThreeSpacePoint> CONTROL_ELLIPSOID_POINTS;
	static ArrayList<ThreeSpacePoint> CONTROL_GRAVITY_POINTS;

	// Draw a control sphere with center = [0,0,0] and radii = [1,1,1]
	static float A_CONTROL_GRAVITY = 1;
	static float B_CONTROL_GRAVITY = 1;
	static float C_CONTROL_GRAVITY = 1;
	static float SHIFT_X_CONTROL_GRAVITY = 0;
	static float SHIFT_Y_CONTROL_GRAVITY = 0;
	static float SHIFT_Z_CONTROL_GRAVITY = 0;
		
	// Draw a control sphere with center = [0,0,0] and radii = [1,1,1]
	static float A_CONTROL_SPHERE = 1;
	static float B_CONTROL_SPHERE = 1;
	static float C_CONTROL_SPHERE = 1;
	static float SHIFT_X_CONTROL_SPHERE = 0;
	static float SHIFT_Y_CONTROL_SPHERE = 0;
	static float SHIFT_Z_CONTROL_SPHERE = 0;

	// Draw a control ellipsoid != control sphere.
	// This ellipsoid will be scaled back to the sphere
	static float A_CONTROL_ELLIPSE = 1.4f;
	static float B_CONTROL_ELLIPSE = 1.3f;
	static float C_CONTROL_ELLIPSE = 1.2f;
	static float SHIFT_X_CONTROL_ELLIPSE = 2;
	static float SHIFT_Y_CONTROL_ELLIPSE = 2;
	static float SHIFT_Z_CONTROL_ELLIPSE = 2;

	// The jzy3D plotter isn't good about creating square
	// charts and you can't set the bounds manually, so
	// the dirty workaround is to just create two dummy
	// points at the max and min bounds and plot them.
	static float BOUNDS_MAX = 4;
	static float BOUNDS_MIN = -4;

	static float NOISE_INTENSITY = 0.01f;

	// Create a chart and add scatter.

	// Generates points for plots.
	GeneratePoints pointGenerator = new GeneratePoints();

	// Scale the ellipsoid into a sphere.
	*/
	public EllipsoidFit(ArrayList<ThreeSpacePoint> sample) throws IOException
	{
		/*
		File f= new File("E:/data.txt");
		BufferedReader buf = new BufferedReader(new FileReader(f));
		String a = buf.readLine();
		ArrayList<ThreeSpacePoint> p = new ArrayList<ThreeSpacePoint>();
		while(a!=null){
			Log.d(TAG,a);
			String[] s = a.split("\t");
			for(int i =2;i<s.length;i++){
				System.out.print(Float.parseFloat(s[i])+"\t");
			}
			ThreeSpacePoint itsp = new ThreeSpacePoint(Float.parseFloat(s[2]), Float.parseFloat(s[3]), Float.parseFloat(s[4]));
			p.add(itsp);
			a = buf.readLine();
		}
		*/

		/*
		// Generate the random points for the control ellipsoid.
		CONTROL_ELLIPSOID_POINTS = pointGenerator.generatePoints(
				A_CONTROL_ELLIPSE, B_CONTROL_ELLIPSE, C_CONTROL_ELLIPSE,
				SHIFT_X_CONTROL_ELLIPSE, SHIFT_Y_CONTROL_ELLIPSE,
				SHIFT_Z_CONTROL_ELLIPSE, NOISE_INTENSITY);

		// Generate the random points for the control sphere.
		CONTROL_SPHERE_POINTS = pointGenerator
				.generatePoints(A_CONTROL_SPHERE, B_CONTROL_SPHERE,
						C_CONTROL_SPHERE, SHIFT_X_CONTROL_SPHERE,
						SHIFT_Y_CONTROL_SPHERE, SHIFT_Z_CONTROL_SPHERE,
						NOISE_INTENSITY);
		
		 */
		/*
		// Fit the ellipsoid points to a polynomial
		FitPoints ellipsoidFit = new FitPoints();
		ellipsoidFit.fitEllipsoid(CONTROL_ELLIPSOID_POINTS);
		
		// Fit the ellipsoid points to a polynomial
		FitPoints sphereFit = new FitPoints();
		sphereFit.fitEllipsoid(CONTROL_SPHERE_POINTS);
		
		*/
		p = sample;
		FitPoints gravityFit = new FitPoints();
		gravityFit.fitEllipsoid(p);
		log(gravityFit, "Gravity");
	}

	public float[] getParams(){
		return params;
	}

	private void log(FitPoints points, String label)
	{
		Log.d(TAG,label);
		Log.d(TAG,points.center.toString());
		Log.d(TAG,points.radii.toString());
		Log.d(TAG,Arrays.toString(points.evals));
		Log.d(TAG,points.evecs.toString());
		Log.d(TAG,points.evecs1.toString());
		Log.d(TAG,points.evecs2.toString());

		double[] scale = points.radii.toArray().clone();
		params[0] = (float)(G/scale[0]);
		params[1] = (float)(G/scale[1]);
		params[2] = (float)(G/scale[2]);
		double[] shilft = points.center.toArray().clone();
		params[3] = (float) shilft[0];
		params[4] = (float) shilft[1];
		params[5] = (float) shilft[2];
	}
}
