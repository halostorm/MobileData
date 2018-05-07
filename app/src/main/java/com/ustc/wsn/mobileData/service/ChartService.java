package com.ustc.wsn.mobileData.service;

/**
 * Created by halo on 2018/1/17.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.IBinder;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.List;

public class ChartService extends Service {

    private GraphicalView xGraphicalView;
    private XYMultipleSeriesDataset xmultipleSeriesDataset;// 数据集容器
    private XYMultipleSeriesRenderer xmultipleSeriesRenderer;// 渲染器容器
    private XYSeries xSeries;// 单条曲线数据集
    private XYSeriesRenderer xRenderer;// 单条曲线渲染器

    private XYSeries ySeries;// 单条曲线数据集
    private XYSeriesRenderer yRenderer;// 单条曲线渲染器

    private XYSeries zSeries;// 单条曲线数据集
    private XYSeriesRenderer zRenderer;// 单条曲线渲染器

    private Context context;
    private  final int MAXPOINT = 200;

    public ChartService(Context context) {
        this.context = context;
    }

    /**
     * 获取图表
     *
     * @return
     */
    public GraphicalView getGraphicalView() {
        xGraphicalView = ChartFactory.getCubeLineChartView(context, xmultipleSeriesDataset, xmultipleSeriesRenderer, 0.1f);
        xGraphicalView.setBackgroundColor(Color.argb(40,181, 181, 181));
        return xGraphicalView;
    }

    /**
     * 获取数据集，及xy坐标的集合
     *
     * @param xcurveTitle
     * @param ycurveTitle
     * @param zcurveTitle
     */
    public void setXYMultipleSeriesDataset(String xcurveTitle,String ycurveTitle,String zcurveTitle) {
        xmultipleSeriesDataset = new XYMultipleSeriesDataset();

        xSeries = new XYSeries(xcurveTitle);
        xmultipleSeriesDataset.addSeries(xSeries);

        ySeries = new XYSeries(ycurveTitle);
        xmultipleSeriesDataset.addSeries(ySeries);

        zSeries = new XYSeries(zcurveTitle);
        xmultipleSeriesDataset.addSeries(zSeries);
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
     * @param xcurveColor x曲线颜色
     * @param ycurveColor y曲线颜色
     * @param zcurveColor z曲线颜色
     * @param gridColor  网格颜色
     */
    public void setXYMultipleSeriesRenderer(double minX, double maxX, double minY, double maxY, String chartTitle, String xTitle, String yTitle, int axeColor, int labelColor, int xcurveColor,int ycurveColor,int zcurveColor, int gridColor) {
        xmultipleSeriesRenderer = new XYMultipleSeriesRenderer();
        if (chartTitle != null) {
            xmultipleSeriesRenderer.setChartTitle(chartTitle);
        }
        xmultipleSeriesRenderer.setXTitle(xTitle);
        xmultipleSeriesRenderer.setYTitle(yTitle);
        xmultipleSeriesRenderer.setRange(new double[]{minX, maxX, minY, maxY});//xy轴的范围
        xmultipleSeriesRenderer.setLabelsColor(labelColor);
        xmultipleSeriesRenderer.setXLabels(10);
        xmultipleSeriesRenderer.setYLabels(10);
        xmultipleSeriesRenderer.setXLabelsAlign(Align.RIGHT);
        xmultipleSeriesRenderer.setYLabelsAlign(Align.RIGHT);
        xmultipleSeriesRenderer.setAxisTitleTextSize(20);
        xmultipleSeriesRenderer.setChartTitleTextSize(20);
        xmultipleSeriesRenderer.setLabelsTextSize(20);
        xmultipleSeriesRenderer.setLegendTextSize(20);
        xmultipleSeriesRenderer.setPointSize(5f);//曲线描点尺寸
        xmultipleSeriesRenderer.setFitLegend(true);
        xmultipleSeriesRenderer.setMargins(new int[]{20, 30, 15, 20});
        xmultipleSeriesRenderer.setShowGrid(true);
        xmultipleSeriesRenderer.setZoomEnabled(true, false);
        xmultipleSeriesRenderer.setAxesColor(axeColor);
        xmultipleSeriesRenderer.setGridColor(gridColor);
        xmultipleSeriesRenderer.setBackgroundColor(Color.WHITE);//背景色
        xmultipleSeriesRenderer.setMarginsColor(Color.WHITE);//边距背景色，默认背景色为黑色，这里修改为白色
        xmultipleSeriesRenderer.setPanEnabled(false,false);

        xRenderer = new XYSeriesRenderer();
        xRenderer.setColor(xcurveColor);
        xRenderer.setLineWidth(3f);
        xRenderer.setPointStyle(PointStyle.POINT);//描点风格，可以为圆点，方形点等等
        xmultipleSeriesRenderer.addSeriesRenderer(xRenderer);

        yRenderer = new XYSeriesRenderer();
        yRenderer.setColor(ycurveColor);
        yRenderer.setLineWidth(3f);
        yRenderer.setPointStyle(PointStyle.POINT);//描点风格，可以为圆点，方形点等等
        xmultipleSeriesRenderer.addSeriesRenderer(yRenderer);

        zRenderer = new XYSeriesRenderer();
        zRenderer.setColor(zcurveColor);
        zRenderer.setLineWidth(3f);
        zRenderer.setPointStyle(PointStyle.POINT);//描点风格，可以为圆点，方形点等等
        xmultipleSeriesRenderer.addSeriesRenderer(zRenderer);
    }

    /**
     * 根据新加的数据，更新曲线，只能运行在主线程
     *
     * @param x 新加点的x坐标
     * @param y 新加点的y坐标
     */
    public void updateChart(double x, double y) {
        xSeries.add(x, y);
        xGraphicalView.repaint();//此处也可以调用invalidate()
    }

    /**
     * 添加新的数据，多组，更新曲线，只能运行在主线程
     *
     * @param xList
     * @param yList
     */
    public void updateChart(List<Double> xList, List<Double> yList) {
       // xmultipleSeriesDataset.removeSeries(xSeries);
        for (int i = 0; i < xList.size(); i++) {
            xSeries.add(xList.get(i), yList.get(i));
        }
        xGraphicalView.repaint();//此处也可以调用invalidate()
    }

    /**
     * 添加新的数据，多组，更新曲线，只能运行在主线程
     *
     * @param x
     * @param y
     */
    public void updateChart(float[] x, float[] y,float ThresholdX,float ThresholdY) {
        //xmultipleSeriesDataset.removeSeries(xSeries);
        xSeries.clear();
        ySeries.clear();
        zSeries.clear();
        for (int i = 0; i <x.length; i++) {
            xSeries.add(x[i], y[i]);
            ySeries.add(x[i],ThresholdY);
        }
        for(int j = (int)zSeries.getMinY();j<(int)zSeries.getMaxY();j++){
            zSeries.add(ThresholdX,j);
        }

        xGraphicalView.repaint();//此处也可以调用invalidate()
    }
    public void updateChart(){

    }

    public void rightUpdateChart(float addY_X,float addY_Y,float addY_Z) {

        // 设置好下一个需要增加的节点
        float addX = 0.f;
        float[] Xxv = new float[MAXPOINT];
        float[] Xyv = new float[MAXPOINT];

        float[] Yxv = new float[MAXPOINT];
        float[] Yyv = new float[MAXPOINT];

        float[] Zxv = new float[MAXPOINT];
        float[] Zyv = new float[MAXPOINT];
        // 移除数据集中旧的点集
        xmultipleSeriesDataset.removeSeries(xSeries);
        xmultipleSeriesDataset.removeSeries(ySeries);
        xmultipleSeriesDataset.removeSeries(zSeries);
        // 判断当前点集中到底有多少点，因为屏幕总共只能容纳MAX_POINT个，所以当点数超过MAX_POINT时，长度永远是MAX_POINT
        int length = xSeries.getItemCount();
        if (length > MAXPOINT) {
            length = MAXPOINT;
        }
        // 将旧的点集中x和y的数值取出来放入backup中，并且将x的值加1，造成曲线向右平移的效果
        for (int i = 0; i < length; i++) {
            Xxv[i] = (float) ((xSeries.getX(i) + 1.f/20.f));
            Xyv[i] = (float) (xSeries.getY(i));

            Yxv[i] = (float) ((ySeries.getX(i) + 1.f/20.f));
            Yyv[i] = (float) (ySeries.getY(i));

            Zxv[i] = (float) ((zSeries.getX(i) + 1.f/20.f));
            Zyv[i] = (float) (zSeries.getY(i));
        }
        // 点集先清空，为了做成新的点集而准备
        xSeries.clear();
        ySeries.clear();
        zSeries.clear();
        // 将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
        // 这里可以试验一下把顺序颠倒过来是什么效果，即先运行循环体，再添加新产生的点
        xSeries.add(addX, addY_X);
        ySeries.add(addX, addY_Y);
        zSeries.add(addX, addY_Z);
        for (int k = 0; k < length; k++) {
            xSeries.add(Xxv[k], Xyv[k]);
            ySeries.add(Yxv[k], Yyv[k]);
            zSeries.add(Zxv[k], Zyv[k]);
        }

        // 在数据集中添加新的点集
        xmultipleSeriesDataset.addSeries(xSeries);
        xmultipleSeriesDataset.addSeries(ySeries);
        xmultipleSeriesDataset.addSeries(zSeries);

        // 视图更新，没有这一步，曲线不会呈现动态
        // 如果在非UI主线程中，需要调用postInvalidate()，具体参考api
        xGraphicalView.repaint();
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




