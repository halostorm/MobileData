package com.ustc.wsn.mobileData.bean;
/**
 * Created by halo on 2017/7/1.
 */

import android.content.Context;
import android.os.Environment;

import com.ustc.wsn.mobileData.utils.TimeUtil;

import java.io.File;

public class outputFile {

    private Context context;
    private static File sdCardDir;
    private static File GeographicalParamsFile;
    private static File stateParamsFile;
    private static File accelParamsFile;
    private static File rawFile;
    private static File featureFile;
    private static File pathFile;
    private static File GlobalStateFile;
    private static File InterpathFile;
    private static File locFile;
    private static File z7RawFile;
    private static File z7CombineFile;
    private static File userInfoFile;
    static File dir;
    static File userDir;
    static File appDir;
    static File INIDir;
    private static long current_time;

    private static String PSW;

    public outputFile(String psw) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            PSW = psw;
            sdCardDir = Environment.getExternalStorageDirectory();// 获取SDCard目录
            String app = sdCardDir.getPath() + "/mobileData/";
            appDir = new File(app);
            String userDirPath = sdCardDir.getPath() + "/mobileData/" + "/" + psw + "/";
            userDir = new File(userDirPath);
        } else {
            File temp = Environment.getDataDirectory();
            userDir = new File(temp + "/mobileData/" + "/" + psw + "/");
            appDir = new File(temp + "/mobileData/");
        }

        if(!userDir.exists()){
            userDir.mkdirs();
        }

    }

    public outputFile() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            sdCardDir = Environment.getExternalStorageDirectory();// 获取SDCard目录
            String app = sdCardDir.getPath() + "/mobileData/";
            appDir = new File(app);
            INIDir = new File(app+"/INI/");
        } else {
            File temp = Environment.getDataDirectory();
            appDir = new File(temp + "/mobileData/");
            INIDir = new File(temp + "/mobileData/"+"/INI/");
        }

        if(!INIDir.exists()){
            INIDir.mkdirs();
        }
    }

    public static void updateDir() {
        current_time = System.currentTimeMillis();
        dir = new File(userDir, "Data" + TimeUtil.getTime_name(current_time));
        if (!dir.exists()) dir.mkdirs();
    }

    public static File getUserDir() {
        return userDir;
    }

    public static File getUserInfoFile(){
        userInfoFile = new File(INIDir,"Info.bat");
        return userInfoFile;
    }

    public static File getAppDir() {
        return appDir;
    }

    public static File getParamsFile() {
        stateParamsFile = new File(INIDir, "stateParams.bat");
        return stateParamsFile;
    }

    public static File getPathFile() {
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.getName().contains("path") && f.getName().contains(".txt")) {
                return f;
            }
        }
        pathFile = new File(dir, "path" + "_" + TimeUtil.getTime_name(current_time) + ".txt");
        return pathFile;
    }

    public static File getInterPathFile() {
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.getName().contains("Inter_path") && f.getName().contains(".txt")) {
                return f;
            }
        }
        InterpathFile = new File(dir, "Inter_path" + "_" + TimeUtil.getTime_name(current_time) + ".txt");
        return InterpathFile;
    }

    public static File getAccParamsFile() {
        accelParamsFile = new File(INIDir, "accCalibrateParams.bat");
        return accelParamsFile;
    }

    public static File getGeographicalParamsFile(){
        GeographicalParamsFile = new File(INIDir,"GeographicalParams.bat");
        return GeographicalParamsFile;
    }

    public static File getlocFile() {
        File[] files = dir.listFiles();
        for (File f : files) {
            if (f.getName().contains("Location") && f.getName().contains(".txt")) {
                return f;
            }
        }
        locFile = new File(dir, "Location" + "_" + TimeUtil.getTime_name(current_time) + ".txt");

        return locFile;
    }

    public static File getrawFile() {
        current_time = System.currentTimeMillis();
        rawFile = new File(dir, "raw" + "_" + TimeUtil.getTime_name(current_time) + ".txt");
        return rawFile;
    }

    public static File getFeatureFile() {
        current_time = System.currentTimeMillis();
        featureFile = new File(dir, "feature" + "_" + TimeUtil.getTime_name(current_time) + ".txt");
        return featureFile;
    }

    public static File getStateFile() {
        current_time = System.currentTimeMillis();
        GlobalStateFile = new File(dir, "state" + "_" + TimeUtil.getTime_name(current_time) + ".txt");
        return GlobalStateFile;
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
