package com.ustc.wsn.mobileData.bean.Filter;

/**
 * Created by halo on 2018/1/21.
 */

public class LPF_I {
    // 截至频率: Ft = [(1-alpha) / (2*PI) * Fc]
    private float alpha = 0.372f;// 5HZ
    //private float[] in;
    private float[] out;

    private float out1 = 0;

    public LPF_I(){
        //in = new float[3];
        out = new float[3];
    }
    public float[] filter(float[] in){
        out[0] = alpha * out[0] + (1 - alpha) * in[0];
        out[1] = alpha * out[1] + (1 - alpha) * in[1];
        out[2] = alpha * out[2] + (1 - alpha) * in[2];
        return out;
    }

    public float filter(float in){
        out1 = alpha * out1 + (1 - alpha) * in;
        return out1;
    }

    public void setAlpha(float alpha)
    {
        this.alpha = alpha;
    }
}
