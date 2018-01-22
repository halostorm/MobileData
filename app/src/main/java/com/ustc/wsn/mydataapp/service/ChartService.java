package com.ustc.wsn.mydataapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
/*
public class ChartService extends Service {
    public ChartService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
*/
import java.util.List;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;

public class ChartService {

    private GraphicalView mGraphicalView;
    private XYMultipleSeriesDataset multipleSeriesDataset;// 数据集容器
    private XYMultipleSeriesRenderer multipleSeriesRenderer;// 渲染器容器
    private XYSeries mSeries;// 单条曲线数据集
    private XYSeriesRenderer mRenderer;// 单条曲线渲染器
    private Context context;

    public ChartService(Context context) {
        this.context = context;
    }

    /**
     * 获取图表
     *
     * @return
     */
    public GraphicalView getGraphicalView() {
        mGraphicalView = ChartFactory.getCubeLineChartView(context, multipleSeriesDataset, multipleSeriesRenderer, 0.1f);
        return mGraphicalView;
    }

    /**
     * 获取数据集，及xy坐标的集合
     *
     * @param curveTitle
     */
    public void setXYMultipleSeriesDataset(String curveTitle) {
        multipleSeriesDataset = new XYMultipleSeriesDataset();
        mSeries = new XYSeries(curveTitle);
        multipleSeriesDataset.addSeries(mSeries);
    }

    /**
     * 获取渲染器
     *
     * @param maxX       x轴最大值
     * @param maxY       y轴最大值
     * @param chartTitle 曲线的标题
     * @param xTitle     x轴标题
     * @param yTitle     y轴标题
     * @param axeColor   坐标轴颜色
     * @param labelColor 标题颜色
     * @param curveColor 曲线颜色
     * @param gridColor  网格颜色
     */
    public void setXYMultipleSeriesRenderer(double maxX, double maxY, String chartTitle, String xTitle, String yTitle, int axeColor, int labelColor, int curveColor, int gridColor) {
        multipleSeriesRenderer = new XYMultipleSeriesRenderer();
        if (chartTitle != null) {
            multipleSeriesRenderer.setChartTitle(chartTitle);
        }
        multipleSeriesRenderer.setXTitle(xTitle);
        multipleSeriesRenderer.setYTitle(yTitle);
        multipleSeriesRenderer.setRange(new double[]{0, maxX, 0, maxY});//xy轴的范围
        multipleSeriesRenderer.setLabelsColor(labelColor);
        multipleSeriesRenderer.setXLabels(10);
        multipleSeriesRenderer.setYLabels(10);
        multipleSeriesRenderer.setXLabelsAlign(Align.RIGHT);
        multipleSeriesRenderer.setYLabelsAlign(Align.RIGHT);
        multipleSeriesRenderer.setAxisTitleTextSize(20);
        multipleSeriesRenderer.setChartTitleTextSize(20);
        multipleSeriesRenderer.setLabelsTextSize(20);
        multipleSeriesRenderer.setLegendTextSize(20);
        multipleSeriesRenderer.setPointSize(2f);//曲线描点尺寸
        multipleSeriesRenderer.setFitLegend(true);
        multipleSeriesRenderer.setMargins(new int[]{20, 30, 15, 20});
        multipleSeriesRenderer.setShowGrid(true);
        multipleSeriesRenderer.setZoomEnabled(true, false);
        multipleSeriesRenderer.setAxesColor(axeColor);
        multipleSeriesRenderer.setGridColor(gridColor);
        multipleSeriesRenderer.setBackgroundColor(Color.WHITE);//背景色
        multipleSeriesRenderer.setMarginsColor(Color.WHITE);//边距背景色，默认背景色为黑色，这里修改为白色
        mRenderer = new XYSeriesRenderer();
        mRenderer.setColor(curveColor);
        mRenderer.setPointStyle(PointStyle.CIRCLE);//描点风格，可以为圆点，方形点等等
        multipleSeriesRenderer.addSeriesRenderer(mRenderer);
    }

    /**
     * 根据新加的数据，更新曲线，只能运行在主线程
     *
     * @param x 新加点的x坐标
     * @param y 新加点的y坐标
     */
    public void updateChart(double x, double y) {
        mSeries.add(x, y);
        mGraphicalView.repaint();//此处也可以调用invalidate()
    }

    /**
     * 添加新的数据，多组，更新曲线，只能运行在主线程
     *
     * @param xList
     * @param yList
     */
    public void updateChart(List<Double> xList, List<Double> yList) {
        for (int i = 0; i < xList.size(); i++) {
            mSeries.add(xList.get(i), yList.get(i));
        }
        mGraphicalView.repaint();//此处也可以调用invalidate()
    }
}