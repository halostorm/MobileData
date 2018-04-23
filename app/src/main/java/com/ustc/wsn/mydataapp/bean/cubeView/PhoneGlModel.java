package com.ustc.wsn.mydataapp.bean.cubeView;

import java.nio.IntBuffer;

/**
 * Created by halo on 2018/3/27.
 */

public class PhoneGlModel {

    public int one = 0x10000*2;
    public IntBuffer colorBuffer;
    public int[] colors = new int[]{

            one / 4, one, 0, one, one / 4, one, 0, one, one / 4, one, 0, one, one / 4, one, 0, one,

            one, one / 4, 0, one, one, one / 4, 0, one, one, one / 4, 0, one, one, one / 4, 0, one, one, one, 0, one, one, one, 0, one, one, one, 0, one, one, one, 0, one, one, 0, 0, one, one, 0, 0, one, one, 0, 0, one, one, 0, 0, one,

            0, 0, one, one, 0, 0, one, one, 0, 0, one, one, 0, 0, one, one,

            one, 0, one, one, one, 0, one, one, one, 0, one, one, one, 0, one, one,};

    public IntBuffer quaterBuffer;
    public int[] quaterVertices = new int[]{one/100, one, -one/10, -one/100, one, -one/10, one/100, one, one/10, -one/100, one, one/10,//前

            one/2, -one, one/10, -one/2, -one, one/10, one/2, -one, -one/10, -one/2, -one, -one/10,//后

            one/100, one, one/10, -one/100, one, one/10, one/2, -one, one/10, -one/2, -one, one/10,//上

            one/2, -one, -one/10, -one/2, -one, -one/10, one/100, one, -one/10, -one/100, one, -one/10,//下

            -one/100, one, one/10, -one/100, one, -one/10, -one/2, -one, one/10, -one/2, -one, -one/10,//左

            one/100, one, -one/10, one/100, one, one/10, one/2, -one, -one/10, one/2, -one, one/10};//右
}
