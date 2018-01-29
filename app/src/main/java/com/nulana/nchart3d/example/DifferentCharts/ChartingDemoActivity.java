package com.nulana.nchart3d.example.DifferentCharts;

import android.app.Activity;
import android.os.Bundle;
import com.nulana.NChart.NChartView;
import java.util.Timer;
import java.util.TimerTask;

import detector.wsn.ustc.com.mydataapp.R;

public class ChartingDemoActivity extends Activity {
    NChartView mNChartView;
    BubbleChartController ctr;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.nchart);
        mNChartView = (NChartView) findViewById(R.id.surface);
        loadView();
    }
    private void loadView() {
        // Paste your license key here.
        mNChartView.getChart().setLicenseKey("bWCo+E65fg+Cjg+0M0BAWhfIORkIsDwDBIO5mODAznWdtIQHirZztXtFaRWLwUiALjmPjEv/oyXerwe3dnDCAiTAO/IFiddoYA3ljKOvgx58NfwdUXXNgSmGiAKvetyNlWs6s3vFvFKc/OsdUk7uzc5WpKQcWFNbYGdJJ3cFNHSmeF2KvSDjJL4YaJhvkFoAQ96igwBEbgexORYX5vpVIlibW/F6Kr2oVcCQ3Wb7S9d4XkvkvD8kqIa6bRcnhu4U+Ky/zJ07B/ohuGE0EMGogozgRitI5Am6ZFNb8LwZwJXaekeZLar8+tG+GajUn7+X0CShuTEIZUxfs1IFEGz8aauu5ki/5HY+sDKufs745/jeqYDL4d/lxYEFSkniDSvUUa2rd3x6WBxciXG65Pr8jIDZYPjtrvvc/D7F1eEzp+53os/wBGxSs8FRfWXRqQQjNjeVHTbYRaVkaFTAvXeGWvKJfiYyZQt5OJgq5rIdXZKJh+/JdN8TaYRkZTDnoj8cX8gs4KYDrnvgN+Yp34FdTKBgHA0IGn31KaKN6MFapNypo9rRTlIhPOKeVmuieormClpgzxegrfjHE0uAcNdSpEUhH1O42RU33/XbjkQkYNm0YvTgF94B9eIkLpb4vC7xseYHTN8J/DPudE9ZOMMUgJJP2HCXgskm6UgyyS42Nho=");

        ctr = new BubbleChartController(true, mNChartView, this);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ctr.updateData();
            }
        }, 0, 500);
    }
}
