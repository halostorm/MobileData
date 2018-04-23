package com.ustc.wsn.mobileData.bean.math;

import android.util.Log;

import com.ustc.wsn.mobileData.bean.PathData;

import java.util.ArrayList;

/**
 * Created by halo on 2018/4/8.
 */

public class PathIntegration {
    private final String TAG = PathIntegration.class.toString();
    private float[] RotMatrix0;
    public float[][] accXQ;
    public float[][] accYQ;
    public float[][] accZQ;
    public float[][] gyroXQ;
    public float[][] gyroYQ;
    public float[][] gyroZQ;

    private float[][] accQueue;
    private float[][] gyroQueue;
    private float[] timeQueue;
    private int PathLength = 0;
    public ArrayList<PathData> PathQ;

    private StringBuffer PathBuffer = new StringBuffer();

    public PathIntegration(ArrayList<PathData> Path,int PathLength){
        accXQ = new float[PathLength*myMath.N][2];
        accYQ= new float[PathLength*myMath.N][2];
        accZQ= new float[PathLength*myMath.N][2];
        gyroXQ= new float[PathLength*myMath.N][2];
        gyroYQ= new float[PathLength*myMath.N][2];
        gyroZQ= new float[PathLength*myMath.N][2];
        PathQ = Path;
        this.PathLength = PathLength;
    }

    public void setRotMatrix0(float[] rot){
        RotMatrix0 = rot.clone();
    }

    public void GenerateDataQueue(){
        PathCubicSpline inter = new PathCubicSpline(PathLength);
        inter.Interpolation(PathQ);

        accXQ = inter.accXQ.clone();
        accYQ= inter.accYQ.clone() ;
        accZQ= inter.accZQ.clone();
        gyroXQ= inter.gyroXQ.clone();
        gyroYQ= inter.gyroYQ.clone();
        gyroZQ= inter.gyroZQ.clone();

        Log.d(TAG,"accXQ length\t"+accXQ.length);
        Log.d(TAG,"accXQ length\t"+inter.accXQ.length);
        /*
        for(int i = 0;i<accXQ.length;i++) {
            Log.d(TAG, "inter accXQ\t" + inter.accXQ[i][0] + "\t" + inter.accXQ[i][1]);
            Log.d(TAG, "accXQ\t" + accXQ[i][0] + "\t" + accXQ[i][1]);
        }
        */
        NormalizeData();
    }

    public void NormalizeData(){
        accQueue = new float[accXQ.length][3];
        gyroQueue = new float[gyroXQ.length][3];
        timeQueue = new float[accXQ.length];
        for(int i = 0;i<accXQ.length;i++){
            accQueue[i][0] = accXQ[i][1];
            accQueue[i][1] = accYQ[i][1];
            accQueue[i][2] = accZQ[i][1];

            gyroQueue[i][0] = gyroXQ[i][1];
            gyroQueue[i][1] = gyroYQ[i][1];
            gyroQueue[i][2] = gyroZQ[i][1];

            timeQueue[i] = accXQ[i][0];
        }
    }

    public void CalPath(float[][] PostionQueue){
        StringBuffer pathOut = new StringBuffer();
        float[] RotMatrix= RotMatrix0.clone();
        float[] RotMatrixLast= RotMatrix.clone();
        float[][] velocityQueue = new float[accQueue.length][3];
        float[][]positionQ = new float[accQueue.length][3];

        float[] accNow = myMath.Rot_coordinatesTransform(RotMatrix, accQueue[0]);

        float[] accPhone = new float[3];

        float deltT = timeQueue[0];

        for(int i =1;i<accQueue.length;i++) {
            //角速度插值
            deltT = timeQueue[i]-timeQueue[i-1];

            float[] W = gyroQueue[i].clone();

            pathOut.append(timeQueue[i] + "\t");

            pathOut.append(W[0] + "\t");
            pathOut.append(W[1] + "\t");
            pathOut.append(W[2] + "\t");

            //旋转矩阵导数
            float[] Matrix_W = new float[]{1f, -W[2] * deltT, W[1] * deltT,//
                    W[2] * deltT, 1f, -W[0] * deltT, //
                    -W[1] * deltT, W[0] * deltT, 1f};

            //新时刻旋转矩阵
            RotMatrix = myMath.matrixMultiply(RotMatrixLast, Matrix_W, 3);
            RotMatrixLast = RotMatrixLast;

            ///新惯性加速度
            accPhone = accQueue[i];

            pathOut.append(accPhone[0]  + "\t");
            pathOut.append(accPhone[1] + "\t");
            pathOut.append(accPhone[2] + "\t");

            accNow = myMath.Rot_coordinatesTransform(RotMatrix, accQueue[i]);

            pathOut.append(accNow[0]  + "\t");
            pathOut.append(accNow[1] + "\t");
            pathOut.append(accNow[2] - myMath.G + "\t");

            //新速度
            velocityQueue[i][0] = velocityQueue[i - 1][0] + accNow[0] * deltT;
            velocityQueue[i][1] = velocityQueue[i - 1][1] + accNow[1]* deltT;
            velocityQueue[i][2] = velocityQueue[i - 1][2] + (accNow[2] - myMath.G) * deltT;


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

           // if(i >accQueue.length-100 || i <100) {
            if(false){
                Log.d(TAG,"deltTime[i]"+ String.valueOf(i) + ":\t" +deltT);
                Log.d(TAG, "accQueue[i][0]:" + String.valueOf(i) + ":\t" + accQueue[i][0]);
                Log.d(TAG, "accQueue[i][1]:" + String.valueOf(i) + ":\t" + accQueue[i][1]);
                Log.d(TAG, "accQueue[i][2]:" + String.valueOf(i) + ":\t" + accQueue[i][2]);
                Log.d(TAG, "accNow[0]:" + String.valueOf(i) + ":\t" + accNow[0]);
                Log.d(TAG, "accNow[1]:" + String.valueOf(i) + ":\t" + accNow[1]);
                Log.d(TAG, "accNow[2]:" + String.valueOf(i) + ":\t" + (accNow[2] - myMath.G));
                Log.d(TAG, "velocityQueue[0]:" + String.valueOf(i) + ":\t" + velocityQueue[i][0]);
                Log.d(TAG, "velocityQueue[1]:" + String.valueOf(i) + ":\t" + velocityQueue[i][1]);
                Log.d(TAG, "velocityQueue[2]:" + String.valueOf(i) + ":\t" + velocityQueue[i][2]);
                Log.d(TAG, "position[0]" + String.valueOf(i) + ":\t" + positionQ[i][0]);
                Log.d(TAG, "position[1]" + String.valueOf(i) + ":\t" + positionQ[i][1]);
                Log.d(TAG, "position[2]" + String.valueOf(i) + ":\t" + positionQ[i][2]);
            }

            //PostionQueue[i] = positionQ[i].clone();

            PathBuffer = pathOut;
        }
    }

    public StringBuffer getPathBuffer(){
        return PathBuffer;
    }
}
