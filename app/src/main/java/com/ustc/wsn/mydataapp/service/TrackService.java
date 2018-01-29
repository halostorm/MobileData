package com.ustc.wsn.mydataapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
//import org.math.plot.*;
public class TrackService extends Service {
    public TrackService() {

        double[] x = new double[100];
        double[] y = new double[100];

        // create your PlotPanel (you can use it as a JPanel)
        //Plot2DPanel plot = new Plot2DPanel();
        // add a line plot to the PlotPanel
        //plot.addLinePlot("my plot", x, y);

        // put the PlotPanel in a JFrame, as a JPanel

        //JFrame frame = new JFrame("a plot panel");
        //frame.setContentPane(plot);
        //frame.setVisible(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
