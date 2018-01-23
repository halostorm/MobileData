package com.ustc.wsn.mydataapp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by chong on 2017/9/6.
 */

public class MainActivity extends Activity {

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        int heightPixels = outMetrics.heightPixels;

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams editViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dpiToPx(50));
        editViewParams.leftMargin = dpiToPx(30);
        editViewParams.rightMargin = dpiToPx(30);
        editViewParams.topMargin = (int) (heightPixels * 0.3f);

        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dpiToPx(50));
        textViewParams.topMargin = dpiToPx(15);
        textViewParams.leftMargin = dpiToPx(30);
        textViewParams.rightMargin = dpiToPx(30);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.WHITE);
        gradientDrawable.setCornerRadius(dpiToPx(8));

        EditText editText = new EditText(this);
        editText.setMaxLines(1);
        editText.setInputType(InputType.TYPE_CLASS_PHONE);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        editText.setWidth(dpiToPx(50));
        editText.setHintTextColor(0xff888888);
        editText.setTextSize(14);
        editText.setBackground(gradientDrawable);
        editText.setHint("请输入手机号码");
        editText.setPadding(dpiToPx(20),0,0,0);
        editText.setGravity(Gravity.CENTER_VERTICAL);

        TextView textView = new TextView(this);
        textView.setText("登录");
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(15);
        textView.setBackground(gradientDrawable);
        textView.setTag(editText);
        textView.setOnClickListener(mOnClickListener);

        linearLayout.addView(editText,editViewParams);
        linearLayout.addView(textView,textViewParams);

        setContentView(linearLayout);

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
