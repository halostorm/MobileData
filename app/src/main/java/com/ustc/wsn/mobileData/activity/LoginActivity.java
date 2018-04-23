

package com.ustc.wsn.mobileData.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ustc.wsn.mobileData.R;
import com.ustc.wsn.mobileData.bean.outputFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by halo on 2017/9/6.
 */

public class LoginActivity extends Activity {
    private String gender = "";
    protected Boolean isExit = false;

    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);

        EditText userID = (EditText) findViewById(R.id.userID);
        EditText age = (EditText) findViewById(R.id.age);
        TextView textView = (TextView) findViewById(R.id.btnlogin);

        RadioGroup genders = (RadioGroup) findViewById(R.id.gender);

        textView.setTag(R.id.userID, userID);
        textView.setTag(R.id.age, age);

        textView.setOnClickListener(mOnClickListener);

        genders.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup genders, int checkedId) {
                RadioButton _genders = (RadioButton) findViewById(checkedId);
                switch (checkedId) {
                    case R.id.btnMale:
                        gender = "male";
                        break;
                    case R.id.btnFemale:
                        gender = "female";
                        break;
                }

            }
        });

        //Calibrate accel if app is firstly used
        File accParams = outputFile.getAccParamsFile();
        if (!accParams.exists()) {
            Toast.makeText(LoginActivity.this, "首次使用，请查看使用说明！", Toast.LENGTH_LONG).show();
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
        getMenuInflater().inflate(R.menu.login_setting, menu);
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
                startActivity(new Intent(this, EllipsoidFitActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String psw = ((EditText) view.getTag(R.id.userID)).getText().toString().trim();
            String age = ((EditText) view.getTag(R.id.age)).getText().toString().trim();

            if (psw.length() == 0) {
                Toast.makeText(LoginActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            if (psw.length() != 11) {
                Toast.makeText(LoginActivity.this, "手机号码只能为11位", Toast.LENGTH_SHORT).show();
                return;
            }
            if(age.length()==0){
                Toast.makeText(LoginActivity.this, "请输入年龄", Toast.LENGTH_SHORT).show();
                return;
            }
            if(gender.length() ==0){
                Toast.makeText(LoginActivity.this, "请选择性别", Toast.LENGTH_SHORT).show();
                return;
            }

            new outputFile(psw);

            File Info = outputFile.getUserInfoFile();
            String out = psw + "\t" + age + "\t" + gender;
            try {
                FileWriter writer = new FileWriter(Info);
                writer.write(out);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(LoginActivity.this, DetectorActivity.class);
            intent.putExtra("userId", psw);
            startActivity(intent);
            finish();
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitByDoubleClick();
        }
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            //exitByDoubleClick();
        }
        return false;
    }

    private void exitByDoubleClick() {
        Timer tExit;
        if (!isExit) {
            isExit = true;
            Toast.makeText(this, "再按一次退出 Mobile Data !", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;//取消退出
                }
            }, 1000);// 如果1秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        } else {
            finish();
            System.exit(0);
        }
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        onDestroy();
    }
}
