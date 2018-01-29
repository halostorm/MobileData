/**
 * Disclaimer: IMPORTANT:  This Nulana software is supplied to you by Nulana
 * LTD ("Nulana") in consideration of your agreement to the following
 * terms, and your use, installation, modification or redistribution of
 * this Nulana software constitutes acceptance of these terms.  If you do
 * not agree with these terms, please do not use, install, modify or
 * redistribute this Nulana software.
 *
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
 *
 * The Nulana Software is provided by Nulana on an "AS IS" basis.  NULANA
 * MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE, REGARDING THE NULANA SOFTWARE OR ITS USE AND
 * OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS.
 *
 * IN NO EVENT SHALL NULANA BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION,
 * MODIFICATION AND/OR DISTRIBUTION OF THE NULANA SOFTWARE, HOWEVER CAUSED
 * AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE),
 * STRICT LIABILITY OR OTHERWISE, EVEN IF NULANA HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright (C) 2017 Nulana LTD. All Rights Reserved.
 */
 
package com.nulana.nchart3d.example.DifferentCharts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.util.LruCache;

import com.nulana.NChart.NChart;
import com.nulana.NChart.NChartAnimationType;
import com.nulana.NChart.NChartBrush;
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
import com.nulana.NChart.NChartTimeAxis;
import com.nulana.NChart.NChartTimeAxisDataSource;
import com.nulana.NChart.NChartView;
import java.util.Random;

public class BubbleChartController implements NChartSeriesDataSource ,NChartTimeAxisDataSource,NChartDelegate {
    NChartView mNChartView;
    boolean drawIn3D;
    NChartBrush[][] brushes;
    Random random = new Random();
    Activity c;

    public BubbleChartController(boolean is3D, NChartView view, Activity c) {
        mNChartView = view;
        drawIn3D = is3D;
        this.c = c;
        brushes = new NChartBrush[1][40];

        // Switch on antialiasing.
        mNChartView.getChart().setShouldAntialias(true);
        if (drawIn3D) {
            // Switch 3D on.
            mNChartView.getChart().setDrawIn3D(true);
            mNChartView.getChart().getCartesianSystem().setMargin(new NChartMargin(30.0f, 30.0f, 10.0f, 20.0f));
            mNChartView.getChart().getPolarSystem().setMargin(new NChartMargin(30.0f, 30.0f, 10.0f, 20.0f));
        } else {
            mNChartView.getChart().getCartesianSystem().setMargin(new NChartMargin(10.0f, 10.0f, 10.0f, 20.0f));
            mNChartView.getChart().getPolarSystem().setMargin(new NChartMargin(10.0f, 10.0f, 10.0f, 20.0f));
        }
    }

    public void updateData() {
        // Create series that will be displayed on the chart.
        createSeries();
        // Update data in the chart.
        mNChartView.getChart().updateData();

        mNChartView.getChart().getCartesianSystem().getXAxis().setColor(Color.BLUE);
        mNChartView.getChart().getCartesianSystem().getYAxis().setColor(Color.CYAN);
        mNChartView.getChart().getCartesianSystem().getXAxis().setColor(Color.RED);
    }

    public void createSeries() {
            NChartBubbleSeries series = new NChartBubbleSeries();
            series.setDataSource(this);
            series.tag = 0;
            mNChartView.getChart().addSeries(series);
    }


    @Override
    public NChartPoint[] points(NChartSeries series) {
        // Create points with some data for the series.
        NChartPoint[] result = new NChartPoint[1];

        for (int i = 0; i < 1; ++i) {
            NChartPointState[] states = new NChartPointState[40];
            for (int j = 0; j < 40; ++j) {
                brushes[i][j] = new NChartSolidColorBrush(Color.argb(255, 0, 0, 205));
                states[j] = NChartPointState.PointStateWithXYZ(random.nextInt(10) + i * j*0.2, random.nextInt(10) + 7, random.nextInt(10) + 11);
                mNChartView.getChart().setPointSelectionEnabled(true);
                states[j].setMarker(new NChartMarker());
                states[j].getMarker().setSize(5);
                states[j].getMarker().setBrush(brushes[i][j]);
                states[j].getMarker().setShape(NChartMarkerShape.Sphere);
                states[j].getMarker().getBrush().setShadingModel(NChartShadingModel.Phong);
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
    public String[] timestamps(NChartTimeAxis nChartTimeAxis) {
        return new String[0];
    }
}
