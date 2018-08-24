package xyz.spiralhalo.sherlock.report;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import xyz.spiralhalo.sherlock.report.persist.ChartMeta;

import java.time.ZonedDateTime;

public class Charts {
    public static ChartPanel createDayBarChart(DefaultCategoryDataset dataset, ChartMeta colors, ZonedDateTime chartDate){
        JFreeChart chart = ChartFactory.createStackedBarChart(
                "",//FormatUtil.DTF_LONG_DATE.format(chartDate),  // chart title
                "Hour of day",
                "Time spent (minute)",
                dataset,                     // data
                PlotOrientation.VERTICAL,    // the plot orientation
                true,
                true,
                false
        );
        BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        for (Object x:dataset.getRowKeys()) {
            int y = dataset.getRowIndex((Comparable)x);
            renderer.setSeriesPaint(y, colors.get(x));
        }
        ChartPanel panel = new ChartPanel(chart);
        panel.setMaximumDrawHeight(270);
        panel.setMaximumDrawWidth(1920);
        return panel;
    }
}
