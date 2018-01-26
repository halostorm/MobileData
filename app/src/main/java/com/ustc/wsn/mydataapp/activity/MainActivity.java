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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
        int heightPixels = outMetrics.heightPixels;
        EditText editText = (EditText) findViewById(R.id.IdNumber);
        TextView textView = (TextView) findViewById(R.id.btnlogin);
        textView.setTag(editText);
        textView.setOnClickListener(mOnClickListener);
    }

    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
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


    public int dpiToPx(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
