package com.ustc.wsn.mobileData.bean.math;

/**
 * Created by halo on 2018/4/5.
 */
//Inspiration drawn from: http://facstaff.cbu.edu/wschrein/media/M329%20Notes/M329L67.pdf

import com.ustc.wsn.mobileData.bean.PathData;

import java.util.ArrayList;

/*
Test Cases:

#1)
Input:
	(1, 2), (2, 3), (3, 5)
Result:
	S(x) =
	    S0(x) = 2.0 + 0.75(x - 1.0) + 0.0(x - 1.0)^2 + 0.25(x - 1.0)^3    (1.0 <= x <= 2.0)
	    S1(x) = 3.0 + 1.5(x - 2.0) + 0.75(x - 2.0)^2 + -0.25(x - 2.0)^3    (2.0 <= x <= 3.0)

#2) (Textbook Problem 9.2.32)
Input:
	(1, 0), (2, 1), (3, 0), (4, 1), (5, 0)
Result:
	S(x) =
	    S0(x) = 0.0 + 1.7142857142857144(x - 1.0) + 0.0(x - 1.0)^2 + -0.7142857142857143(x - 1.0)^3    (1.0 <= x <= 2.0)
	    S1(x) = 1.0 + -0.4285714285714285(x - 2.0) + -2.142857142857143(x - 2.0)^2 + 1.5714285714285712(x - 2.0)^3    (2.0 <= x <= 3.0)
	    S2(x) = 0.0 + 1.1102230246251565E-16(x - 3.0) + 2.571428571428571(x - 3.0)^2 + -1.5714285714285712(x - 3.0)^3    (3.0 <= x <= 4.0)
	    S3(x) = 1.0 + 0.4285714285714286(x - 4.0) + -2.142857142857143(x - 4.0)^2 + 0.7142857142857143(x - 4.0)^3    (4.0 <= x <= 5.0)

#3) (Textbook Problem 9.2.41)
Input:
	(0, 1), (1, 2), (2, 3), (3, 4), (4, 5)
Result:
	S(x) =
        S0(x) = 1.0 + 1.0(x - 0.0) + 0.0(x - 0.0)^2 + 0.0(x - 0.0)^3    (0.0 <= x <= 1.0)
    	S1(x) = 2.0 + 1.0(x - 1.0) + 0.0(x - 1.0)^2 + 0.0(x - 1.0)^3    (1.0 <= x <= 2.0)
    	S2(x) = 3.0 + 1.0(x - 2.0) + 0.0(x - 2.0)^2 + 0.0(x - 2.0)^3    (2.0 <= x <= 3.0)
    	S3(x) = 4.0 + 1.0(x - 3.0) + 0.0(x - 3.0)^2 + 0.0(x - 3.0)^3    (3.0 <= x <= 4.0)

*/

public class PathCubicSpline {
    private  final String TAG = PathCubicSpline.class.toString();

    public float[][] accXQ;
    public float[][] accYQ;
    public float[][] accZQ;
    public float[][] gyroXQ;
    public float[][] gyroYQ;
    public float[][] gyroZQ;

    public PathCubicSpline(int PathLength){
        accXQ = new float[PathLength*myMath.N][2];
        accYQ= new float[PathLength*myMath.N][2];
        accZQ= new float[PathLength*myMath.N][2];
        gyroXQ= new float[PathLength*myMath.N][2];
        gyroYQ= new float[PathLength*myMath.N][2];
        gyroZQ= new float[PathLength*myMath.N][2];
    }

    public void Interpolation(ArrayList<PathData> Path) {
        float[] accX = new float[Path.size()];
        float[] accY = new float[Path.size()];
        float[] accZ = new float[Path.size()];

        float[] gyroX = new float[Path.size()];
        float[] gyroY = new float[Path.size()];
        float[] gyroZ = new float[Path.size()];

        float[] time = new float[Path.size()];

        for(int i = 0;i<Path.size();i++) {
            accX[i]= Path.get(i).acc[0];
            accY[i]= Path.get(i).acc[1];
            accZ[i]= Path.get(i).acc[2];

            gyroX[i]= Path.get(i).gyro[0];
            gyroY[i]= Path.get(i).gyro[1];
            gyroZ[i]= Path.get(i).gyro[2];

            time[i] = Path.get(i).timestamp;
            //Log.d(TAG,"timestamp\t"+i+"\t"+time[i]);
            //Log.d(TAG,"accX\t"+i+"\t"+accX[i]);
        }

        accXQ = GenerateNewData(GenerateFunction(accX,time));
        for(int i = 0;i<accXQ.length;i++){
            //Log.d(TAG,"accXQ IN\t"+accXQ[i][0]+"\t"+accXQ[i][1]);
        }
        accYQ = GenerateNewData(GenerateFunction(accY,time));
        accZQ = GenerateNewData(GenerateFunction(accZ,time));

        gyroXQ = GenerateNewData(GenerateFunction(gyroX,time));
        gyroYQ = GenerateNewData(GenerateFunction(gyroY,time));
        gyroZQ = GenerateNewData(GenerateFunction(gyroZ,time));
    }

    public  float[][] GenerateNewData(ArrayList<float[]> value) {//基于插值函数生成新的点集
        //Log.d(TAG,"Gener value Size:\t"+value.size());
        final int N= myMath.N;
        final int Length = (value.size()-1)*N;
        float [][] FinalData = new float[Length][2];

        for(int i = 0;i<value.size()-1;i++){
            float deltX = ((value.get(i+1)[0]-value.get(i)[0]))/N;
            float xi = value.get(i)[0];
            float yi = value.get(i)[1];
            float bs = value.get(i)[2];
            float cs = value.get(i)[3];
            float ds = value.get(i)[4];
            FinalData[i*N][0] = xi;
            FinalData[i*N][1] = yi;
            for(int j = 1;j<N;j++) {
                int k = i * N + j;
                FinalData[i * N + j][0] = xi + j * deltX;
                FinalData[i * N + j][1] = Function_I(xi + j * deltX, xi, yi, bs, cs, ds);
                //Log.d(TAG, "i*N+j: x[i]\t y[i]\t" + k + "\t" + FinalData[i * N + j][0] + "\t" + FinalData[i * N + j][1]);
            }
        }
        FinalData[Length-1][0] = value.get(value.size()-1)[0];
        FinalData[Length-1][1] = value.get(value.size()-1)[1];

        return FinalData;
    }

    public  float Function_I(float x,float xi,float yi,float bs,float cs,float ds){
        float y = yi+bs*(x-xi)+cs*(x-xi)*(x-xi)+ds*(x-xi)*(x-xi)*(x-xi);
        return y;
    }

    public  ArrayList<float[]> GenerateFunction(float[] Y, float[] X) {//生成插值函数

        ArrayList<float[]> xy = new ArrayList<float[]>();
        ArrayList<Float> hi = new ArrayList<>();
        ArrayList<Float> ss = new ArrayList<>();


        for(int i = 0;i<X.length;i++){

            float[] inPoint = new float[2];
            inPoint[0] = X[i];
            inPoint[1] = Y[i];
            xy.add(inPoint);
        }

        // make sure no dup x's
        for (int i = 0; i < xy.size(); i++) {
            for (int j = i + 1; j < xy.size(); j++) {
                if ((xy.get(i)[0] == xy.get(j)[0])) {
                    return null;
                }
            }
        }

        // create the h list
        for (int i = 0; i < xy.size() - 1; i++) {
            hi.add(xy.get(i + 1)[0] - xy.get(i)[0]);
        }

        // set up the right hand side of the tridiangonal system
        for (int i = 1; i < xy.size() - 1; i++) {
            ss.add(((3 / hi.get(i)) * (xy.get(i + 1)[1] - xy.get(i)[1])) - ((3 / hi.get(i - 1)) * (xy.get(i)[1] - xy.get(i - 1)[1])));
        }

        // setup lists for tridiagonal subroutine to use
        ArrayList<Float> ls = new ArrayList<>();
        ArrayList<Float> us = new ArrayList<>();
        ArrayList<Float> zs = new ArrayList<>();
        ArrayList<Float> bs = new ArrayList<>();
        ArrayList<Float> cs = new ArrayList<>();
        ArrayList<Float> ds = new ArrayList<>();

        // Set up ArrayLists
        for (int i = 0; i < xy.size(); i++) {
            bs.add(0.0f);
            cs.add(0.0f);
            ds.add(0.0f);
        }

        // do tridiagonal subroutine
        tridAlgo(xy, hi, ss, ls, us, zs, bs, cs, ds);
        // print the natural cubic interpolating splitLine

        return ncisAlgo(xy, bs, cs, ds);
    }

    // tridiagonal linear system solver
    public  void tridAlgo(ArrayList<float[]> xy, ArrayList<Float> hi, ArrayList<Float> ss, ArrayList<Float> ls, ArrayList<Float> us, ArrayList<Float> zs, ArrayList<Float> bs, ArrayList<Float> cs, ArrayList<Float> ds) {

        // set up
        ls.add(1.0f);
        us.add(0.0f);
        zs.add(0.0f);

        // add input points to lists, setting up the z's, lower, and upper diagonals
        for (int i = 1; i < xy.size() - 1; i++) {
            ls.add(2 * (xy.get(i + 1)[0] - xy.get(i - 1)[0]) - hi.get(i - 1) * us.get(i - 1));
            us.add(hi.get(i) / ls.get(i));
            zs.add((ss.get(i - 1) - hi.get(i - 1) * zs.get(i - 1)) / ls.get(i));
        }

        // continue set up
        ls.add(1.0f);
        zs.add(0.0f);
        cs.add(0.0f);

        // change the values of each diagional (apply Gaussian Elimination) and store in the to-be-printed functions
        for (int j = xy.size() - 2; j >= 0; j--) {
            cs.set(j, zs.get(j) - us.get(j) * cs.get(j + 1));
            bs.set(j, (xy.get(j + 1)[1] - xy.get(j)[1]) / hi.get(j) - hi.get(j) * (cs.get(j + 1) + 2 * cs.get(j)) / 3);
            ds.set(j, (cs.get(j + 1) - cs.get(j)) / (3 * hi.get(j)));
        }

    }

    // creates and prints the S(x) function and its parts
    public  ArrayList<float[]> ncisAlgo(ArrayList<float[]> xy, ArrayList<Float> bs, ArrayList<Float> cs, ArrayList<Float> ds) {
        // System.out.println("S(x) = ");
        ArrayList<float[]> InterQueue = new ArrayList<>();
        float[][] value = new float[xy.size()][5];
        for (int j = 0; j < xy.size() - 1; j++) {

            String si = "S" + Integer.toString(j) + "(x) = ";
            String aj = Float.toString(xy.get(j)[1]);
            String bj = bs.get(j).toString();
            String cj = cs.get(j).toString();
            String dj = ds.get(j).toString();
            String xj = Float.toString(xy.get(j)[0]);
            //Log.d(TAG,"    " + si + aj + " + " + bj + "(x - " + xj + ") + " + cj + "(x - " + xj + ")^2 + " + dj + "(x - " + xj + ")^3" + "    (" + Float.toString(xy.get(j)[0]) + " <= x <= " + Float.toString(xy.get(j + 1)[0]) + ")");

            value[j][0] = xy.get(j)[0];
            value[j][1] = xy.get(j)[1];
            value[j][2] = bs.get(j);
            value[j][3] = cs.get(j);
            value[j][4] = ds.get(j);

            InterQueue.add(value[j]);
        }
        value[xy.size()-1][0] = xy.get( xy.size() - 1)[0];
        value[xy.size()-1][1] = xy.get( xy.size() - 1)[1];
        value[xy.size()-1][2] = 0;
        value[xy.size()-1][3] = 0;
        value[xy.size()-1][4] = 0;
        InterQueue.add(value[xy.size()-1]);
        //Log.d(TAG,"Inter value size\t"+InterQueue.size());
        return InterQueue;
    }
}
