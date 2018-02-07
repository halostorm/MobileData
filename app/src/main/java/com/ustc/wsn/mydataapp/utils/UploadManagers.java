package com.ustc.wsn.mydataapp.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.ustc.wsn.mydataapp.activity.UploadActivity;
import com.ustc.wsn.mydataapp.service.AutoUploadSeriver;

import java.util.List;

/**
 * Created by chong on 2017/8/25.
 */

public class UploadManagers {

    private static  Intent intent;
    private UploadManagers() {

    }
    public static void initAutoUploadSeriver(Context context, String foldersPath, String userId) {
        if (!isServiceRunning(AutoUploadSeriver.class.getName(),context)) {
            Toast.makeText(context,foldersPath,Toast.LENGTH_SHORT).show();
            intent = new Intent(context, AutoUploadSeriver.class);
            intent.putExtra("foldersPath",foldersPath);
            intent.putExtra("userId",userId);
            context.startService(intent);
        }
    }

    public static void stopUpload(Context context) {
        if (isServiceRunning(AutoUploadSeriver.class.getName(),context)) {
            context.stopService(intent);
            Toast.makeText(context, "上传停止", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isServiceRunning(String serviceClassName , Context context){

        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
                return true;
            }
        }
        return false;
    }

}
