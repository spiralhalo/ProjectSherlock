package xyz.spiralhalo.sherlock.report;

import org.jfree.chart.*;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarPainter;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.GradientPaintTransformer;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import xyz.spiralhalo.sherlock.Main;
import xyz.spiralhalo.sherlock.report.persist.ChartMeta;

import java.awt.*;
import java.awt.geom.RectangularShape;
import java.time.ZonedDateTime;

import static xyz.spiralhalo.sherlock.util.ColorUtil.interpolateNicely;

public class Charts {
    public static ChartPanel createDayBarChart(DefaultCategoryDataset dataset, ChartMeta colors, ZonedDateTime chartDate){
        JFreeChart chart = createStackedBarChart("", "Hour of day", "Time spent (minute)",
                dataset, colors, PlotOrientation.VERTICAL, true, true, false);
        ChartPanel panel = new ChartPanel(chart);
        panel.setMaximumDrawHeight(270);
        panel.setMaximumDrawWidth(1920);
        return panel;
    }

    private static JFreeChart createStackedBarChart(String title, String domainAxisLabel, String rangeAxisLabel,
                                                   CategoryDataset dataset, ChartMeta colors, PlotOrientation orientation,
                                                   boolean showLegend, boolean tooltips, boolean urls) {
        ParamChecks.nullNotPermitted(orientation, "orientation");
        CategoryAxis categoryAxis = new CategoryAxis(domainAxisLabel);
        ValueAxis valueAxis = new NumberAxis(rangeAxisLabel);
        StackedBarRenderer renderer = new StackedBarRenderer();
        if (tooltips) renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        if (urls) renderer.setBaseItemURLGenerator(new StandardCategoryURLGenerator());
        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis, renderer);
        plot.setOrientation(orientation);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, showLegend);

        renderer.setBarPainter(new SherlockBarPainter());
        renderer.setShadowVisible(true);
        for (Object x:dataset.getRowKeys()) {
            int y = dataset.getRowIndex((Comparable)x);
            renderer.setSeriesPaint(y, colors.get(x));
        }

        Color fg = new Color(Main.currentTheme.foreground);
        Color bg = new Color(Main.currentTheme.background);
        Color leanToBg = null;
        Color leanToFg = null;
        if(Main.currentTheme.dark) {
            //interpolate less harshly on (darker) background
            leanToBg = interpolateNicely(0.8f, fg, bg);
            leanToFg = interpolateNicely(0.2f, fg, bg);
        } else {
            //interpolate less harshly on (darker) foreground
            leanToBg = interpolateNicely(0.95f, fg, bg);
            leanToFg = interpolateNicely(0.4f, fg, bg);
        }

        Font regularFont = new Font("Tahoma", 0, 12);

        categoryAxis.setTickLabelPaint(fg);
        categoryAxis.setLabelPaint(fg);
        categoryAxis.setLabelFont(regularFont);
        categoryAxis.setTickLabelFont(regularFont);

        valueAxis.setTickLabelPaint(fg);
        valueAxis.setLabelPaint(fg);
        valueAxis.setTickLabelFont(regularFont);

        LegendTitle legend = chart.getLegend();
        legend.setBackgroundPaint(leanToBg);
        legend.setItemPaint(fg);
        legend.setFrame(new LineBorder(fg, new BasicStroke(), new RectangleInsets()));
        legend.setItemFont(regularFont);

        plot.setRangeGridlinePaint(leanToFg);
        plot.setBackgroundPaint(leanToBg);
        plot.setOutlinePaint(fg);

        chart.setBackgroundPaint(bg);

        return chart;
    }

    public static class SherlockBarPainter implements BarPainter {
        private final Color fg;
        private final Stroke stroke = new BasicStroke(2f);

        SherlockBarPainter() {
            this(Color.darkGray);
        }

        SherlockBarPainter(Color fg) {
            this.fg = fg;
        }

        public void paintBar(Graphics2D g2, BarRenderer renderer, int row, int column, RectangularShape bar, RectangleEdge base) {
            Paint itemPaint = renderer.getItemPaint(row, column);
            GradientPaintTransformer t = renderer.getGradientPaintTransformer();
            if (t != null && itemPaint instanceof GradientPaint) {
                itemPaint = t.transform((GradientPaint)itemPaint, bar);
            }

            g2.setPaint(itemPaint);
            g2.fill(bar);

        }

        public void paintBarShadow(Graphics2D g2, BarRenderer renderer, int row, int column, RectangularShape bar, RectangleEdge base, boolean pegShadow) {
            if(bar.getHeight()==0)return;
            g2.setStroke(stroke);
            g2.setPaint(fg);
            g2.draw(bar);
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else {
                return obj instanceof SherlockBarPainter && obj.hashCode() == this.hashCode();
            }
        }

        public int hashCode() {
            return fg.getRGB();
        }
    }
}
