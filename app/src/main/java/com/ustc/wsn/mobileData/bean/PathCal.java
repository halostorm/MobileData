package com.ustc.wsn.mobileData.bean;

import android.util.Log;

import com.ustc.wsn.mobileData.bean.math.myMath;

import java.util.ArrayList;

/**
 * Created by halo on 2018/4/20.
 */

public class PathCal {
    private final String TAG = this.getClass().toString();
    private float[][] accQueue = null;
    private float[][] PathQueue = null;
    private float[] timeQueue = null;
    private int RawLength = 0;
    private StringBuffer PathBuffer = new StringBuffer();

    public PathCal(ArrayList<PathBasicData> path, int length){
        Log.d(TAG,"Path Final length\t"+String.valueOf(length*myMath.N));

        CubicSpline inter = new CubicSpline(length);
        inter.Interpolation(path);
        RawLength = inter.ValueX.length;
        accQueue = new float[RawLength][3];
        timeQueue = new float[RawLength];

        for (int i = 1; i < RawLength; i++) {
            accQueue[i][0] = inter.ValueX[i][1];
            accQueue[i][1] = inter.ValueY[i][1];
            accQueue[i][2] = inter.ValueZ[i][1]-myMath.G;

            timeQueue[i] = inter.ValueX[i][0];
        }
    }

    public void CalPath() {
        StringBuffer pathOut = new StringBuffer();
        float[][] velocityQueue = new float[RawLength][3];
        float[][] positionQ = new float[RawLength][3];

        float[] accNow = accQueue[0];

        float deltT = timeQueue[0];

        for (int i = 1; i < RawLength; i++) {

            deltT = timeQueue[i] - timeQueue[i - 1];

            pathOut.append(timeQueue[i] + "\t");

            ///新惯性加速度
            accNow = accQueue[i];

            pathOut.append(accNow[0] + "\t");
            pathOut.append(accNow[1] + "\t");
            pathOut.append(accNow[2] + "\t");

            //新速度
            velocityQueue[i][0] = velocityQueue[i - 1][0] + accNow[0] * deltT;
            velocityQueue[i][1] = velocityQueue[i - 1][1] + accNow[1] * deltT;
            velocityQueue[i][2] = velocityQueue[i - 1][2] + accNow[2] * deltT;


            pathOut.append(velocityQueue[i][0] + "\t");
            pathOut.append(velocityQueue[i][1] + "\t");
            pathOut.append(velocityQueue[i][2] + "\t");

            //新位置
            positionQ[i][0] = positionQ[i - 1][0] + 0.5f * (velocityQueue[i][0] + velocityQueue[i - 1][0]) * deltT;
            positionQ[i][1] = positionQ[i - 1][1] + 0.5f * (velocityQueue[i][1] + velocityQueue[i - 1][1]) * deltT;
            positionQ[i][2] = positionQ[i - 1][2] + 0.5f * (velocityQueue[i][2] + velocityQueue[i - 1][2]) * deltT; //- freeFallPosition;

            pathOut.append(positionQ[i][0] + "\t");
            pathOut.append(positionQ[i][1] + "\t");
            pathOut.append(positionQ[i][2] + "\n");

             //if(i >accQueue.length-100 || i <100) {
           if (false) {
                Log.d(TAG, "deltTime[i]" + String.valueOf(i) + ":\t" + deltT);
                Log.d(TAG, "accNow[0]:" + String.valueOf(i) + ":\t" + accNow[0]);
                Log.d(TAG, "accNow[1]:" + String.valueOf(i) + ":\t" + accNow[1]);
                Log.d(TAG, "accNow[2]:" + String.valueOf(i) + ":\t" + accNow[2]);
                Log.d(TAG, "velocityQueue[0]:" + String.valueOf(i) + ":\t" + velocityQueue[i][0]);
                Log.d(TAG, "velocityQueue[1]:" + String.valueOf(i) + ":\t" + velocityQueue[i][1]);
                Log.d(TAG, "velocityQueue[2]:" + String.valueOf(i) + ":\t" + velocityQueue[i][2]);
                Log.d(TAG, "position[0]" + String.valueOf(i) + ":\t" + positionQ[i][0]);
                Log.d(TAG, "position[1]" + String.valueOf(i) + ":\t" + positionQ[i][1]);
                Log.d(TAG, "position[2]" + String.valueOf(i) + ":\t" + positionQ[i][2]);
            }
        }
        PathBuffer = pathOut;
        PathQueue = positionQ.clone();
    }

    public StringBuffer getPathBuffer(){
        return PathBuffer;
    }

    public float[][] getPathQueue(){
        if(PathQueue!=null) {
            return PathQueue;
        }
        return null;
    }

}
