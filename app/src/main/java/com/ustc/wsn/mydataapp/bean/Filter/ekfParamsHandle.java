package com.ustc.wsn.mydataapp.bean.Filter;

/**
 * Created by halo on 2018/1/16.
 */

public class ekfParamsHandle {
    public float r0, r1, r2;
    public float q0, q1, q2, q3;
    public float[] moment_inertia_J=new float[3]; /**< diagonal entries of the matrix */
    public int use_moment_inertia;
    public float mag_decl;
    public int acc_comp;

    public  ekfParamsHandle() {
	/* PID parameters */
	/*
        q0 = 1e-4f;
        q1 = 0.08f;
        q2 = 0.009f;
        q3 = 0.005f;

        r0 = 0.0008f;
        r1 = 10000.0f;
        r2 = 100.0f;

        moment_inertia_J[0] = 0.0018f;
        moment_inertia_J[1] = 0.0018f;
        moment_inertia_J[2] = 0.1f;
        moment_inertia_J[2] = 0.0037f;
        use_moment_inertia = 0;
    */
        q0 = 1e-2f;
        q1 = 1;
        q2 = 1;
        q3 = 1;

        r0 = 0.08f;
        r1 = 10000.0f;
        r2 = 100.0f;

        moment_inertia_J[0] = 0.0018f;
        moment_inertia_J[1] = 0.0018f;
        moment_inertia_J[2] = 0.1f;
        moment_inertia_J[2] = 0.0037f;
        use_moment_inertia = 0;
    }
}
