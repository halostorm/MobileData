package com.ustc.wsn.mydataapp.bean;
/**
 * Created by halo on 2017/7/1.
 */

import android.content.Context;
import android.os.Environment;

import com.ustc.wsn.mydataapp.utils.TimeUtil;

import java.io.File;

public class outputFile {

    private Context context;
    private File sdCardDir;
    private static File stateParamsFile;
    private static File accelParamsFile;
    private static File accFile;
    private static File magFile;
    private static File gyroFile;

    private static File accFileTrans;
    private static File magFileTrans;
    private static File gyroFileTrans;

    private static File combineFile;
    private static File rawFile;
    private static File pathFile;
    private static File attitudeFile;
    private static File locFile;
    private static File z7RawFile;
    private static File z7CombineFile;
    static File dir;
    static File paramsDir;
    static File appDir;
    private static long current_time;

    public outputFile(String psw) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            sdCardDir = Environment.getExternalStorageDirectory();// 获取SDCard目录
            current_time = System.currentTimeMillis();
            String dirPath = sdCardDir.getPath() + "/MobileData/" + "/" + psw + "/" + "Data" + TimeUtil.getTime_name(current_time);
            dir = new File(dirPath);
            String paramsDirPath = sdCardDir.getPath() + "/MobileData/"+ "/" + psw + "/";
            String app =  sdCardDir.getPath() + "/MobileData/";
            appDir = new File(app);
            paramsDir = new File(paramsDirPath);
            if (!dir.exists()) dir.mkdirs();
            // Log.i("创建存储目录", "--------------------");
        } else {
            File temp = Environment.getDataDirectory();
            dir = new File(temp + "/MobileData/" + "/" + psw + "/" + "Data" + TimeUtil.getTime_name(current_time));
            paramsDir = new File(temp + "/MobileData/"+ "/" + psw + "/");
            appDir = new File(temp + "/MobileData/");
        }

    }
    public outputFile() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            sdCardDir = Environment.getExternalStorageDirectory();// 获取SDCard目录
            String app =  sdCardDir.getPath() + "/MobileData/";
            appDir = new File(app);
        }else {
            File temp = Environment.getDataDirectory();
            appDir = new File(temp + "/MobileData/");
        }
    }

    public static File getDir() {
        return dir;
    }

    public static File getAppDir(){
        return appDir;
    }

    public static File getParamsFile() {
        stateParamsFile = new File(paramsDir, "stateParams.txt");
        return stateParamsFile;
    }

    public static File getPathFile() {
        pathFile = new File(dir, "path"+ "_" + TimeUtil.getTime_name(current_time) + ".txt");
        return pathFile;
    }

    public static File getAccParamsFile() {
        accelParamsFile = new File(appDir, "accParams.txt");
        return accelParamsFile;
    }

    public static File getaccFile() {
        current_time = System.currentTimeMillis();
        accFile = new File(dir, "Accel" + "_" + TimeUtil.getTime_name(current_time) + ".txt");
        return accFile;
    }

    public static File getmagFile() {
        current_time = System.currentTimeMillis();
        magFile = new File(dir, "Magnet" + "_" + TimeUtil.getTime_name(current_time) + ".txt");
        return magFile;
    }

    public static File getgyroFile() {
        current_time = System.currentTimeMillis();
        gyroFile = new File(dir, "Gyro" + "_" + TimeUtil.getTime_name(current_time) + ".txt");
        return gyroFile;
    }

    public static File getaccTransFile() {
        current_time = System.currentTimeMillis();
        accFileTrans = new File(dir, "AccelTrans" + "_" + TimeUtil.getTime_name(current_time) + ".txt");
        return accFileTrans;
    }

    public static File getmagTransFile() {
        current_time = System.currentTimeMillis();
        magFileTrans = new File(dir, "MagnetTrans" + "_" + TimeUtil.getTime_name(current_time) + ".txt");
        return magFileTrans;
    }

    public static File getgyroTransFile() {
        current_time = System.currentTimeMillis();
        gyroFileTrans = new File(dir, "GyroTrans" + "_" + TimeUtil.getTime_name(current_time) + ".txt");
        return gyroFileTrans;
    }

    public static File getCombineFile() {
        current_time = System.currentTimeMillis();
        combineFile = new File(dir, "DataCombine" + "_" + TimeUtil.getTime_name(current_time) + ".txt");
        return combineFile;
    }

    public static File getlocFile() {
        current_time = System.currentTimeMillis();
        locFile = new File(dir, "Location" + "_" + TimeUtil.getTime_name(current_time) + ".txt");

        return locFile;
    }

    public static File getattitudeFile() {
        current_time = System.currentTimeMillis();
        attitudeFile = new File(dir, "Attitude" + "_" + TimeUtil.getTime_name(current_time) + ".txt");
        return attitudeFile;
    }

    public static File getrawFile() {
        current_time = System.currentTimeMillis();
        rawFile = new File(dir, "raw" + "_" + TimeUtil.getTime_name(current_time) + ".txt");
        return rawFile;
    }

    public static File getz7RawFile() {
        current_time = System.currentTimeMillis();
        z7RawFile = new File(dir, "z7Raw" + "_" + TimeUtil.getTime_name(current_time) + ".7z");
        return z7RawFile;
    }

    public static File getz7CombineFile() {
        current_time = System.currentTimeMillis();
        z7CombineFile = new File(dir, "z7Combine" + "_" + TimeUtil.getTime_name(current_time) + ".7z");
        return z7CombineFile;
    }
}
