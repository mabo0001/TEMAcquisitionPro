/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chart.geopen;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

public class ResistanceChart implements ChartMouseListener {

    public MyBarRenderer mybarrenderer;

    private static CategoryDataset createDataset() {
        String s = "电阻";
        String s1 = "通道 1";
        String s2 = "通道 2";
        String s3 = "通道 3";
        String s4 = "通道 4";
        String s5 = "通道 5";
        String s6 = "通道 6";
        String s7 = "通道 7";
        String s8 = "通道 8";
        String s9 = "通道 9";
        String s10 = "通道 10";
        DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();
        return defaultcategorydataset;
    }

    private JFreeChart createChart(CategoryDataset categorydataset) {
        JFreeChart jfreechart = ChartFactory.createBarChart("", null, null, categorydataset, PlotOrientation.VERTICAL, false, true, false);
        jfreechart.setBackgroundPaint(Color.white);
        jfreechart.getCategoryPlot().getRenderer().setBaseOutlinePaint(Color.WHITE);
        CategoryPlot categoryplot = (CategoryPlot) jfreechart.getPlot();
        //自定义Barrender
        mybarrenderer = new MyBarRenderer();
        mybarrenderer.setBaseToolTipGenerator(new CategoryToolTipGenerator() {//自定义返回的数值
            @Override
            public String generateToolTip(CategoryDataset cd, int i, int i1) {
                return Integer.toString((int) (cd.getValue(i, i1).doubleValue())) + " (ohm)";
            }
        });
        mybarrenderer.setDrawBarOutline(true);
        final DecimalFormat format = new DecimalFormat("0");//不带逗号的计数方法
        mybarrenderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator(StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING, format));
        mybarrenderer.setSeriesItemLabelsVisible(0, Boolean.TRUE);
        mybarrenderer.setShadowVisible(true);
        mybarrenderer.setBaseItemLabelFont(new Font("", Font.BOLD, 12));
        mybarrenderer.setMaximumBarWidth(0.025);//设定通道宽度
        categoryplot.setRenderer(mybarrenderer);

//        MyBarRenderer categoryitemrenderer = (MyBarRenderer) categoryplot.getRenderer();
        categoryplot.setBackgroundPaint(Color.white);
        categoryplot.setRangeGridlinePaint(Color.GRAY);
        categoryplot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));//坐标标签设定显示位置
        categoryplot.setOutlineVisible(false);//外边框不可见
        NumberAxis numberaxisY = (NumberAxis) categoryplot.getRangeAxis();
        numberaxisY.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        numberaxisY.setUpperMargin(0.14999999999999999D);
        numberaxisY.setTickLabelFont(new Font("", Font.BOLD, 12));
        numberaxisY.setLabelPaint(Color.BLUE);
        numberaxisY.setLabelFont(new Font("SansSerif", Font.BOLD, 12));
        numberaxisY.setVisible(false);//设定坐标轴和数值不可见
        CategoryAxis numberaxisX = categoryplot.getDomainAxis();
        numberaxisX.setAxisLineVisible(false);//设定坐标轴不可见
        numberaxisX.setTickLabelPaint(Color.RED);
        numberaxisX.setTickLabelFont(new Font("楷体_GB2312", Font.BOLD, 12));
        numberaxisX.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
        return jfreechart;
    }

    public JPanel createDemoPanel() {
        JFreeChart jfreechart = createChart(createDataset());
        ChartPanel chartPanel = new ChartPanel(jfreechart);
        chartPanel.setMouseZoomable(false);
        chartPanel.addChartMouseListener(this);
        //防止图形变形
        chartPanel.setMaximumDrawWidth(10000);
        chartPanel.setMaximumDrawHeight(2000);
        return chartPanel;
    }

    @Override
    public void chartMouseClicked(ChartMouseEvent cme) {
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent chartmouseevent) {
        org.jfree.chart.entity.ChartEntity chartentity = chartmouseevent.getEntity();
        if (!(chartentity instanceof CategoryItemEntity)) {
            mybarrenderer.setHighlightedItem(-1, -1);
            return;
        } else {
            CategoryItemEntity categoryitementity = (CategoryItemEntity) chartentity;
            CategoryDataset categorydataset = categoryitementity.getDataset();
            mybarrenderer.setHighlightedItem(categorydataset.getRowIndex(categoryitementity.getRowKey()), categorydataset.getColumnIndex(categoryitementity.getColumnKey()));
            return;
        }
    }

    class MyBarRenderer extends BarRenderer {

        private int highlightRow;
        private int highlightColumn;

        public void setHighlightedItem(int i, int j) {
            if (highlightRow == i && highlightColumn == j) {
                return;
            } else {
                highlightRow = i;
                highlightColumn = j;
                notifyListeners(new RendererChangeEvent(this));
                return;
            }
        }

        public Paint getItemOutlinePaint(int i, int j) {
            if (i == highlightRow && j == highlightColumn) {
                return Color.yellow;
            } else {
                return super.getItemOutlinePaint(i, j);
            }
        }

        MyBarRenderer() {
            highlightRow = -1;
            highlightColumn = -1;
        }
    }
}
