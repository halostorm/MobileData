package com.ustc.wsn.mydataapp.service;

/**
 * Created by halo on 2018/1/17.
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
/**
 * Disclaimer: IMPORTANT:  This Nulana software is supplied to you by Nulana
 * LTD ("Nulana") in consideration of your agreement to the following
 * terms, and your use, installation, modification or redistribution of
 * this Nulana software constitutes acceptance of these terms.  If you do
 * not agree with these terms, please do not use, install, modify or
 * redistribute this Nulana software.
 * <p>
 * In consideration of your agreement to abide by the following terms, and
 * subject to these terms, Nulana grants you a personal, non-exclusive
 * license, under Nulana's copyrights in this original Nulana software (the
 * "Nulana Software"), to use, reproduce, modify and redistribute the Nulana
 * Software, with or without modifications, in source and/or binary forms;
 * provided that if you redistribute the Nulana Software in its entirety and
 * without modifications, you must retain this notice and the following
 * text and disclaimers in all such redistributions of the Nulana Software.
 * Except as expressly stated in this notice, no other rights or licenses,
 * express or implied, are granted by Nulana herein, including but not limited
 * to any patent rights that may be infringed by your derivative works or by other
 * works in which the Nulana Software may be incorporated.
 * <p>
 * The Nulana Software is provided by Nulana on an "AS IS" basis.  NULANA
 * MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE, REGARDING THE NULANA SOFTWARE OR ITS USE AND
 * OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS.
 * <p>
 * IN NO EVENT SHALL NULANA BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION,
 * MODIFICATION AND/OR DISTRIBUTION OF THE NULANA SOFTWARE, HOWEVER CAUSED
 * AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE),
 * STRICT LIABILITY OR OTHERWISE, EVEN IF NULANA HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * Copyright (C) 2017 Nulana LTD. All Rights Reserved.
 */

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.nulana.NChart.NChart;
import com.nulana.NChart.NChartAnimationType;
import com.nulana.NChart.NChartBrush;
import com.nulana.NChart.NChartBrushScale;
import com.nulana.NChart.NChartBubbleSeries;
import com.nulana.NChart.NChartDelegate;
import com.nulana.NChart.NChartEventPhase;
import com.nulana.NChart.NChartFont;
import com.nulana.NChart.NChartMargin;
import com.nulana.NChart.NChartMarker;
import com.nulana.NChart.NChartMarkerShape;
import com.nulana.NChart.NChartModel;
import com.nulana.NChart.NChartPoint;
import com.nulana.NChart.NChartPointState;
import com.nulana.NChart.NChartSeries;
import com.nulana.NChart.NChartSeriesDataSource;
import com.nulana.NChart.NChartShadingModel;
import com.nulana.NChart.NChartSolidColorBrush;
import com.nulana.NChart.NChartValueAxesType;
import com.nulana.NChart.NChartView;
import com.ustc.wsn.mydataapp.detectorservice.FCF;

import java.util.Random;

public class TrackService extends Service implements NChartSeriesDataSource, NChartDelegate {
    private final String TAG = TrackService.this.toString();
    private NChartView mNChartView;
    private static int window_size;
    private boolean drawIn3D;
    private NChartBrush brushes;
    private NChartMarker marker;
    private NChartBrush brushesP;
    private NChartMarker markerP;
    private Random random = new Random();
    private Activity c;
    private float[][] position;
    private boolean POSITION_ENABLED = false;

    public TrackService(boolean is3D, NChartView view, Activity c, int window_size) {
        mNChartView = view;
        drawIn3D = is3D;
        this.c = c;
        this.window_size = window_size;

        brushes = new NChartSolidColorBrush(Color.argb(255, 0, 0, 205));
        brushes.setShadingModel(NChartShadingModel.Phong);
        marker = new NChartMarker();
        marker.setBrush(brushes);
        marker.setShape(NChartMarkerShape.Circle);
        marker.setSize(5);

        brushesP = new NChartSolidColorBrush(Color.argb(255, 205, 0, 0));
        brushesP.setShadingModel(NChartShadingModel.Phong);
        markerP = new NChartMarker();
        markerP.setBrush(brushesP);
        markerP.setShape(NChartMarkerShape.Circle);
        markerP.setSize(1);

        position = new float[this.window_size][3];

        //Switch on antialiasing.
        mNChartView.getChart().setShouldAntialias(true);
        if (drawIn3D) {
            // Switch 3D on.
            //NChartValueAxesType type = new NChartValueAxesType(NChartValueAxesType.Absolute);
            mNChartView.getChart().setDrawIn3D(true);
            mNChartView.getChart().getCartesianSystem().setMargin(new NChartMargin(30.0f, 30.0f, 10.0f, 20.0f));
            mNChartView.getChart().getPolarSystem().setMargin(new NChartMargin(30.0f, 30.0f, 10.0f, 20.0f));
        } else {
            mNChartView.getChart().getCartesianSystem().setMargin(new NChartMargin(10.0f, 10.0f, 10.0f, 20.0f));
            mNChartView.getChart().getPolarSystem().setMargin(new NChartMargin(10.0f, 10.0f, 10.0f, 20.0f));
        }
    }

    public void setPosition(float[][] p) {
        position = p.clone();
    }

    public void initView(int i ) {
        POSITION_ENABLED = true;
        createSeries(i);
    }

    public void updateData(int i) {
        mNChartView.getChart().updateData();
    }

    public void createSeries(int i) {
        NChartBubbleSeries series = new NChartBubbleSeries();
        series.setDataSource(this);
        series.tag = i;
        mNChartView.getChart().addSeries(series);
    }

    @Override
    public NChartPoint[] points(NChartSeries series) {
        // Create points with some data for the series.
        NChartPoint[] result = new NChartPoint[window_size + 1];
        for (int i = 0; i <= window_size; i++) {
            NChartPointState[] states = new NChartPointState[1];
            if (i != window_size) {
                if (POSITION_ENABLED) {
                    //brushes[i][j] = new NChartSolidColorBrush(Color.argb(255, 0, 0, 205));
                    states[0] = NChartPointState.PointStateWithXYZ(position[i][0] * 100, 20, position[i][1] * 100);//position[j][2]
                    //Log.d(TAG,"pathpoint:"+position[i][0]+"\t"+position[i][1]);
                } else {
                    //brushes[i][j] = new NChartSolidColorBrush(Color.argb(255, 205, 0, 0));
                    states[0] = NChartPointState.PointStateWithXYZ(random.nextInt(10), random.nextInt(10), random.nextInt(10));
                }
                mNChartView.getChart().setPointSelectionEnabled(true);
                states[0].setMarker(marker);
            } else {
                if (POSITION_ENABLED) {
                    //brushes[i][j] = new NChartSolidColorBrush(Color.argb(255, 0, 0, 205));
                    states[0] = NChartPointState.PointStateWithXYZ(20, 20, 20);//position[j][2]
                } else {
                    //brushes[i][j] = new NChartSolidColorBrush(Color.argb(255, 205, 0, 0));
                    states[0] = NChartPointState.PointStateWithXYZ(random.nextInt(10), random.nextInt(10), random.nextInt(10));
                }
                mNChartView.getChart().setPointSelectionEnabled(true);
                states[0].setMarker(markerP);
            }

            result[i] = new NChartPoint(states, series);
        }
        return result;
    }

    @Override
    public String name(NChartSeries nChartSeries) {
        return null;
    }

    @Override
    public Bitmap image(NChartSeries nChartSeries) {
        return null;
    }

    @Override
    public NChartPoint[] extraPoints(NChartSeries nChartSeries) {
        return new NChartPoint[0];
    }


    @Override
    public void timeIndexChanged(NChart nChart, double v) {

    }

    @Override
    public void pointSelected(NChart nChart, NChartPoint nChartPoint) {

    }

    @Override
    public void didEndAnimating(NChart nChart, Object o, NChartAnimationType nChartAnimationType) {

    }

    @Override
    public void didChangeZoomPhase(NChart nChart, NChartEventPhase nChartEventPhase) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
    }
}
