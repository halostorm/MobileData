package com.ustc.wsn.mydataapp.ellipsoidFit;

import android.util.Log;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

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
	float[] params = new float[12];//系数矩阵-9 + 偏移向量-3 = 12
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
		calParams(gravityFit);
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
	}

	private void calParams(FitPoints points){
		/*
		double[] scale = points.radii.toArray().clone();
		params[0] = (float)(G/scale[0]);
		params[1] = (float)(G/scale[1]);
		params[2] = (float)(G/scale[2]);
		double[] shilft = points.center.toArray().clone();
		params[3] = (float) shilft[0];
		params[4] = (float) shilft[1];
		params[5] = (float) shilft[2];
		*/
		double[][] K = new double[3][3];
		double [] m = points.radii.toArray();
		double[] l = new double[]{G/m[0],G/m[1],G/m[2]};

		K[0] = points.evecs.toArray();
		K[1] = points.evecs1.toArray();
		K[2] = points.evecs2.toArray();
		//特征矩阵
		RealMatrix A = new Array2DRowRealMatrix(3, 3);
		A.setEntry(0, 0, K[0][0]);
		A.setEntry(0, 1, K[0][1]);
		A.setEntry(0, 2, K[0][2]);

		A.setEntry(1, 0, K[1][0]);
		A.setEntry(1, 1, K[1][1]);
		A.setEntry(1, 2, K[1][2]);

		A.setEntry(2, 0, K[2][0]);
		A.setEntry(2, 1, K[2][1]);
		A.setEntry(2, 2, K[2][2]);

		RealMatrix _A;

		_A = A.transpose();

		RealMatrix L = new Array2DRowRealMatrix(3, 3);

		L.setEntry(0, 0, l[0]);
		L.setEntry(1, 1, l[1]);
		L.setEntry(2, 2, l[2]);

		RealMatrix Cal;
		//计算校准系数矩阵
		Cal = (_A.multiply(L)).multiply(A);
		double[][] P = Cal.getData();
		//计算偏移向量
		double[] C = points.center.toArray();

		for(int i =0;i<3;i++){
			for(int j =0;j<3;j++) {
				params[3*i+j] = (float)P[i][j];
			}
		}
		params[9] = (float) C[0];
		params[10] = (float) C[1];
		params[11] = (float) C[2];
	}
}
