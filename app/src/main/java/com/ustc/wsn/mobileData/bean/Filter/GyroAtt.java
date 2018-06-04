package com.ustc.wsn.mobileData.bean.Filter;

import com.ustc.wsn.mobileData.bean.math.myMath;

/**
 * Created by halo on 2018/4/9.
 */

public class GyroAtt {
    /**
     * Created by halo on 2018/3/29.
     */
    private final String TAG = GyroAtt.class.toString();
    public volatile float[] q = {1, 0, 0, 0};
    public volatile float q0 = 1.0f, q1 = 0.0f, q2 = 0.0f, q3 = 0.0f;    // quaternion of sensor frame relative to auxiliary frame
    public volatile float[] Rot_Matrix = {1, 0, 0, 0, 1, 0, 0, 0, 1};
    public volatile float[] Euler = {0, 0, 0};

    public GyroAtt(float[] Q_begin){
        q = Q_begin.clone();
    }

    public void Filter(float[] gyro, float dt) {
        q0 = q[0];
        q1 = q[1];
        q2 = q[2];
        q3 = q[3];
        float gx = gyro[0];
        float gy = gyro[1];
        float gz = gyro[2];
        float recipNorm;
        float qDot1, qDot2, qDot3, qDot4;
        // Rate of change of quaternion from gyroscope
        qDot1 = 0.5f * (-q1 * gx - q2 * gy - q3 * gz);
        qDot2 = 0.5f * (q0 * gx + q2 * gz - q3 * gy);
        qDot3 = 0.5f * (q0 * gy - q1 * gz + q3 * gx);
        qDot4 = 0.5f * (q0 * gz + q1 * gy - q2 * gx);

        // Integrate rate of change of quaternion to yield quaternion
        q0 += qDot1 * dt;
        q1 += qDot2 * dt;
        q2 += qDot3 * dt;
        q3 += qDot4 * dt;

        // Normalise quaternion
        recipNorm = (float) (1 / Math.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3));
        q0 *= recipNorm;
        q1 *= recipNorm;
        q2 *= recipNorm;
        q3 *= recipNorm;

        calRotMatrix_Euler();
    }

    public void calRotMatrix_Euler() {
        q[0] = q0;
        q[1] = q1;
        q[2] = q2;
        q[3] = q3;
        Rot_Matrix = myMath.Q2Rot(q);
        Euler = myMath.Q2Euler(q);
    }
}

