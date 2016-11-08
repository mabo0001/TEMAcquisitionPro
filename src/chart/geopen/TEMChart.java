/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chart.geopen;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import tem.geopen.TEMAcquisitionProMain;

/**
 *
 * @author Administrator
 */
public class TEMChart implements ChartMouseListener {

    TEMAcquisitionProMain frame;

    public TEMChart(TEMAcquisitionProMain frame) {
        this.frame = frame;
    }

    public JFreeChart createChart(String titleStr, String xLabel, String yLabel, String chartVersion, XYDataset dataset) {
        JFreeChart chart = ChartFactory.createScatterPlot(
                //        JFreeChart chart = ChartFactory.createScatterPlot(
                titleStr,
                xLabel,
                yLabel,
                dataset,
                PlotOrientation.VERTICAL,
                false,//lengend
                true,
                false);
        TextTitle title = chart.getTitle();
        title.setHorizontalAlignment(HorizontalAlignment.LEFT);
        title.setFont(new Font("楷体_GB2312", Font.BOLD, 12));
        title.setPaint(Color.BLUE);
        if (chartVersion.equalsIgnoreCase("TEM")) {//前五道
            java.awt.geom.Ellipse2D.Double double1 = new java.awt.geom.Ellipse2D.Double(-2D, -2D, 4D, 4D);
            XYPlot xyplot = (XYPlot) chart.getPlot();
            //设定可见不可见
            XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer) xyplot.getRenderer();
            xylineandshaperenderer.setBaseItemLabelsVisible(false);
            xylineandshaperenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());//弹出数值
            xylineandshaperenderer.setBaseShapesVisible(true);
            xylineandshaperenderer.setSeriesShape(0, double1);
            xylineandshaperenderer.setSeriesLinesVisible(0, true);
            xylineandshaperenderer.setSeriesPaint(0, Color.RED);
            ItemLabelPosition itemLabelPosition = new ItemLabelPosition(ItemLabelAnchor.INSIDE12, TextAnchor.CENTER_RIGHT, TextAnchor.CENTER_RIGHT, -1.57D);
            xylineandshaperenderer.setSeriesNegativeItemLabelPosition(0, itemLabelPosition);
//            xylineandshaperenderer.setBaseFillPaint(Color.BLUE);//填充
//            xylineandshaperenderer.setUseFillPaint(true);//填充
            //显示样式
            StandardXYToolTipGenerator ttg = new StandardXYToolTipGenerator(
                    StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, new java.text.DecimalFormat("0.000"),
                    new java.text.DecimalFormat("0.0"));
            xylineandshaperenderer.setBaseToolTipGenerator(ttg);

            //坐标轴
            NumberAxis xAxis = new NumberAxis(xLabel);
            NumberAxis yAxis = new NumberAxis(yLabel);
            xyplot.setDomainAxis(xAxis);
            xyplot.setRangeAxis(yAxis);
//            xAxis.setFixedAutoRange(30000D);//设定最小显示
            //坐标轴只显示数字
            NumberAxis numberaxisX = (NumberAxis) xyplot.getDomainAxis();
            NumberAxis numberaxisY = (NumberAxis) xyplot.getRangeAxis();
            //设定x轴和y轴的坐标轴显示样式
            DecimalFormat format = new DecimalFormat("#");
            numberaxisX.setNumberFormatOverride(format);//去掉逗号
            format.setMaximumFractionDigits(10);//消除逗号并且设定最大精度
            numberaxisY.setNumberFormatOverride(new NumberFormat() {
                @Override
                public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
                    StringBuffer formattedValue = new StringBuffer();
                    int tempvalue = (int) number;
                    String strValue = String.valueOf(tempvalue);
                    if (strValue.length() < 8) {
                        for (int i = 0; i <= 8 - strValue.length(); i++) {
                            formattedValue = formattedValue.append("  ");
                        }
                    }
                    formattedValue = formattedValue.append(tempvalue);
                    return formattedValue;
                }

                @Override
                public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
                    return null;
                }

                @Override
                public Number parse(String source, ParsePosition parsePosition) {
                    return null;
                }
            });
            numberaxisX.setAxisLineVisible(true);
            numberaxisX.setTickMarksVisible(true);
            numberaxisX.setLabelPaint(Color.BLUE);
            numberaxisY.setLabelPaint(Color.BLUE);
            numberaxisX.setTickLabelFont(new Font("", Font.PLAIN, 10));
            numberaxisY.setTickLabelFont(new Font("", Font.PLAIN, 10));
            numberaxisX.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
            numberaxisY.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
            numberaxisY.setAxisLineVisible(false);
            numberaxisY.setTickMarksVisible(true);
//            numberaxisY.setTickLabelInsets(new RectangleInsets(0, 0, 0, 0));
            //设定背景
            xyplot.setBackgroundPaint(Color.WHITE);
            xyplot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));//坐标标签设定显示位置
//            xyplot.setDomainGridlinesVisible(false);//纵向线
//            xyplot.setRangeGridlinesVisible(false);
//            xyplot.setDomainGridlineStroke(new BasicStroke(1f));
//            xyplot.setRangeGridlineStroke(new BasicStroke(1f));
            xyplot.setDomainGridlinePaint(Color.lightGray);//纵向线
            xyplot.setRangeGridlinePaint(Color.lightGray);
            xyplot.setDomainCrosshairVisible(true);
            xyplot.setDomainCrosshairPaint(Color.BLUE);
            xyplot.setDomainCrosshairLockedOnData(true);
            xyplot.setRangeCrosshairVisible(true);
            xyplot.setRangeCrosshairPaint(Color.BLUE);//设定颜色
            xyplot.setRangeCrosshairLockedOnData(true);
            xyplot.setDomainZeroBaselineVisible(false);
            xyplot.setRangeZeroBaselineVisible(false);
            xyplot.setDomainPannable(true);//不可ctrl+拖动
            xyplot.setRangePannable(true);
        }
        return chart;
    }

    /**
     * 创建面板
     *
     * @param title
     * @param xLabel
     * @param yLabel
     * @param chartVersion
     * @param dataset //
     * @param flagLineOrSingle//标记是单个点显示还是测线显示
     * @return \
     */
    public ChartPanel createDemoPanel(String title, String xLabel, String yLabel, String chartVersion, XYDataset dataset) {
        JFreeChart chart = createChart(title, xLabel, yLabel, chartVersion, dataset);
        ChartPanel chartPanel = new ChartPanel(chart);
        //增加属性
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
        chartPanel.setPopupMenu(null);
        //设定显示时间
        chartPanel.setDismissDelay(100000000);
        //设定图标最小值
        chartPanel.setPreferredSize(new Dimension(250, 200));
        //防止图形变形
        chartPanel.setMaximumDrawWidth(10000);
        chartPanel.setMaximumDrawHeight(2000);
        chartPanel.setMouseWheelEnabled(false);
        return chartPanel;
    }

    @Override
    public void chartMouseClicked(ChartMouseEvent chartmouseevent) {
        frame.requestFocus();
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent cme) {
    }
}
