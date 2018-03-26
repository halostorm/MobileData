package com.ustc.wsn.mydataapp.bean.Filter;

/**
 * Created by halo on 2018/1/16.
 */

public class ekfParams {
    public float[] r;
    public float[] q;
    public float[] moment_inertia_J;
    public int use_moment_inertia;
    public float roll_off;
    public float pitch_off;
    public float yaw_off;
    public float mag_decl;
    public int acc_comp;

    public  ekfParams()
    {
         r= new float[3];
         q= new float[4];
         moment_inertia_J= new float[9];
         use_moment_inertia=0;
    }
    public void parameters_update(ekfParamsHandle h)
    {
        q[0] = h.q0;
        q[1] = h.q1;
        q[2] = h.q2;
        q[3] = h.q3;

        r[0] = h.r0;
        r[1] = h.r1;
        r[2] = h.r2;

        for (int i = 0; i < 3; i++) {
            moment_inertia_J[3 * i + i] = h.moment_inertia_J[i];
        }
        use_moment_inertia = h.use_moment_inertia;

    }
}
