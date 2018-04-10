package com.ustc.wsn.mydataapp.bean.Log;

import android.util.Log;

/**
 * Created by halo on 2018/3/26.
 */

public class myLog {
    public static void log(String TAG, String label, String s) {

        Log.d(TAG, label+"\t--"+s);
    }

    public static void log(String TAG, float s) {
        Log.d(TAG, String.valueOf(s));
    }

    public static void log(String TAG, String label, float[] s) {
        String l = new String();
        l += label+"\t--\n";
        for (int i = 0; i < s.length; i++) {
            l += s[i] + "\t";
        }
        Log.d(TAG, l);
    }

    public static void log(String TAG, String label, double[][] s) {
        String l = new String();
        l += label+"\t--\n";
        for (int i = 0; i < s.length; i++) {
            for(int j = 0;j<s[0].length;j++) {
                l += s[i][j] + "\t";
            }
            l += "\n";
        }
        Log.d(TAG, l);
    }

    public static void log(String TAG, float[] s, String label, int N) {
        String l = new String();
        l += label+"\t--\n";
        for (int i = 0; i < s.length; i++) {
            if ((i + 1) % N == 0) {
                l += s[i] + "\n";
            } else l += s[i] + "\t";
        }
        Log.d(TAG, l);
    }
}
