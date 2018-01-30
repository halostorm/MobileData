package com.ustc.wsn.mydataapp.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nulana.nchart3d.example.DifferentCharts.ChartingDemoActivity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import detector.wsn.ustc.com.mydataapp.R;

/**
 * Created by chong on 2017/9/6.
 */

public class MainActivity extends Activity {

    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        EditText editText = (EditText) findViewById(R.id.IdNumber);
        TextView textView = (TextView) findViewById(R.id.btnlogin);
        textView.setTag(editText);
        textView.setOnClickListener(mOnClickListener);
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.setting, menu);
        //setIconEnable(menu,true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings_help:
                showHelpDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String psw = ((EditText) view.getTag()).getText().toString().trim();
            if (psw.length() == 0){
                Toast.makeText(MainActivity.this,"请输入手机号",Toast.LENGTH_SHORT).show();
                return;
            }
            if (psw.length() != 11){
                Toast.makeText(MainActivity.this,"手机号码只能为11位",Toast.LENGTH_SHORT).show();
                return;
            }
            //Intent intent=new Intent(MainActivity.this,DetectorActivity.class);
            Intent intent=new Intent(MainActivity.this,DetectorActivity.class);
            intent.putExtra("userId",psw);
            startActivity(intent);
            finish();
        }
    };
}
