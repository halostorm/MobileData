package com.ustc.wsn.mydataapp.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ustc.wsn.mydataapp.R;
import com.ustc.wsn.mydataapp.bean.outputFile;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by halo on 2017/9/6.
 */

public class MainActivity extends Activity {

    /*
    static {
        System.loadLibrary("native-lib");
    }
*/
    protected Boolean isExit = false;
    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        new outputFile();
        /*
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        */
        EditText editText = (EditText) findViewById(R.id.IdNumber);
        TextView textView = (TextView) findViewById(R.id.btnlogin);
        textView.setTag(editText);
        textView.setOnClickListener(mOnClickListener);
        //Calibrate accel if app is firstly used
        File accParams = outputFile.getAccParamsFile();
        if(!accParams.exists()){
            Toast.makeText(MainActivity.this, "首次使用，请查看使用说明！", Toast.LENGTH_LONG).show();
            showHelpDialog();
        }
    }

    private void showHelpDialog() {
        Dialog helpDialog = new Dialog(this);
        helpDialog.setCancelable(true);
        helpDialog.setCanceledOnTouchOutside(true);

        helpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        helpDialog.setContentView(getLayoutInflater().inflate(R.layout.help, null));

        helpDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_setting, menu);
        //setIconEnable(menu,true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings_help:
                showHelpDialog();
                return true;
            case R.id.calibrate_accel:
                //Intent intent = new Intent(MainActivity.this, AccCalibrateActivity.class);
                Intent intent = new Intent(MainActivity.this, EllipsoidFitActivity.class);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String psw = ((EditText) view.getTag()).getText().toString().trim();
            if (psw.length() == 0) {
                Toast.makeText(MainActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            if (psw.length() != 11) {
                Toast.makeText(MainActivity.this, "手机号码只能为11位", Toast.LENGTH_SHORT).show();
                return;
            }
            //Intent intent=new Intent(MainActivity.this,DetectorActivity.class);
            Intent intent = new Intent(MainActivity.this, DetectorActivity.class);
            intent.putExtra("userId", psw);
            startActivity(intent);
            finish();
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            exitByDoubleClick();
        }
        if(keyCode==KeyEvent.KEYCODE_HOME){
            //exitByDoubleClick();
        }
        return false;
    }

    private void exitByDoubleClick() {
        Timer tExit;
        if(!isExit){
            isExit=true;
            Toast.makeText(this,"再按一次退出 Mobile Data !",Toast.LENGTH_SHORT).show();
            tExit=new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit=false;//取消退出
                }
            },1000);// 如果1秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        }else{
            finish();
            System.exit(0);
        }
    }
    /*
    public native String stringFromJNI();
    public native String getName();
    */
    @Override
    public void onStop(){
        // TODO Auto-generated method stub
        super.onStop();
        onDestroy();
    }
}
