package xyz.spiralhalo.sherlock.report;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.AbstractAnnotation;
import org.jfree.chart.annotations.CategoryAnnotation;
import org.jfree.chart.axis.*;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.category.BarPainter;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.*;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import xyz.spiralhalo.sherlock.Main;
import xyz.spiralhalo.sherlock.notes.YearNotes;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig;
import xyz.spiralhalo.sherlock.report.factory.charts.ChartData;
import xyz.spiralhalo.sherlock.report.factory.charts.ChartMeta;
import xyz.spiralhalo.sherlock.report.factory.charts.ChartType;
import xyz.spiralhalo.sherlock.report.factory.charts.FlexibleLocale;
import xyz.spiralhalo.sherlock.report.factory.summary.MonthSummary;
import xyz.spiralhalo.sherlock.report.factory.summary.YearSummary;
import xyz.spiralhalo.sherlock.util.ImgUtil;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.HashMap;

import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserInt.DAILY_TARGET_SECOND;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserNode.TRACKING;
import static xyz.spiralhalo.sherlock.util.ColorUtil.*;

public class Charts {
    private static Image noteImg = null;
    static {
        try {
            noteImg = ImgUtil.loadImage("sm_note.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CategoryImageAnnotation monthAnnotation(YearMonth ym, int dayOfMonth, double max){
        return new CategoryImageAnnotation(ChartType.DAY_IN_MONTH.unitLabel(ym, dayOfMonth-1), max*0.8, noteImg);
    }

    public static ChartPanel emptyPanel(){
        ChartPanel panel = new ChartPanel(null, false, true, true,false,true);
        panel.setMaximumDrawHeight(270);
        panel.setMaximumDrawWidth(1920);
        return panel;
    }

    public static class MonthChartInfo {
        public final JFreeChart chart;
        public final HashMap<LocalDate, CategoryImageAnnotation> annotations;
        public final YearMonth month;
        public final double max;

        public MonthChartInfo(JFreeChart chart, HashMap<LocalDate, CategoryImageAnnotation> annotations, YearMonth month, double max) {
            this.chart = chart;
            this.annotations = annotations;
            this.month = month;
            this.max = max;
        }
    }

    public static MonthChartInfo createMonthBarChart(MonthSummary monthSummary){
        final JFreeChart x = createStackedBarChart("Day of month", "Time spent (hours)",
                monthSummary.getMonthChart().getDataset(),
                monthSummary.getMonthChart().getMeta());
        final Color fg = new Color(Main.currentTheme.foreground);
        final int target = UserConfig.userGInt(TRACKING, DAILY_TARGET_SECOND);
        final ValueMarker marker = new ValueMarker(target/3600f, fg, new BasicStroke(
                1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1.0f, new float[] {6.0f, 6.0f}, 0.0f));

        marker.setLabel("Target");
        marker.setLabelPaint(new Color(Main.currentTheme.background));
        marker.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT);
        marker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
        marker.setLabelOffset(new RectangleInsets(0, 0, 13, 4));
        marker.setLabelFont(new Font("Tahoma", 0, 11));
        marker.setLabelBackgroundColor(fg);

        final CategoryPlot plot = x.getCategoryPlot();

        plot.addRangeMarker(marker);

        final CategoryAxis axis = x.getCategoryPlot().getDomainAxis();
        final YearMonth ym = monthSummary.getMonth();
        final DefaultCategoryDataset ratingSet = new DefaultCategoryDataset();

        axis.setCategoryLabelPositions(
                CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 3d));
        axis.setTickLabelFont(new Font("Tahoma", Font.BOLD, 10));

        ZoneId z = ZoneId.systemDefault();
        HashMap<LocalDate, CategoryImageAnnotation> annotations = new HashMap<>();
        double max = 0;

        CategoryDataset originalDataset = plot.getDataset(0);

        for (int i = 0; i < ym.lengthOfMonth(); i++) {
            LocalDate date = ym.atDay(i+1);
            ChartData dayChart = monthSummary.getDayCharts().get(date);
            FlexibleLocale unitLabel = ChartType.DAY_IN_MONTH.unitLabel(ym, i);
            if(dayChart != null){
                ChartMeta meta = dayChart.getMeta();
                int ratio = meta.getLogDur() == 0 ? 0 : (meta.getLogDur() < target ? meta.getWorkDur() * 100 / meta.getLogDur() : meta.getWorkDur() * 100 / target);
                if (UserConfig.userGWDay(date.get(ChronoField.DAY_OF_WEEK))) {
                    Color ratioFG = interpolateNicely((float) ratio / 100f, bad, neu, gut);
                    axis.setTickLabelPaint(unitLabel,Main.currentTheme.dark ? ratioFG : multiply(gray, ratioFG));
                } else {
                    axis.setTickLabelPaint(unitLabel,Main.currentTheme.dark ? gray : light_gray);
                }
                ratingSet.addValue((Number) ((target/3600f) * (ratio / 100f)), "Rating", unitLabel);
            } else {
                ratingSet.addValue((Number)0, "Rating", unitLabel);
            }
            double total = 0;
            for (int j = 0; j < originalDataset.getRowCount(); j++) {
                final Number value = originalDataset.getValue(j, i);
                if(value==null)continue;
                total = total + value.doubleValue();
            }
            if(total>max)max=total;
        }

        for (int i = 0; i < ym.lengthOfMonth(); i++) {
            LocalDate date = ym.atDay(i+1);
            if(YearNotes.getNote(z, date) != null) {
                CategoryImageAnnotation ann = monthAnnotation(ym, i+1,max);
                annotations.put(date, ann);
                plot.addAnnotation(ann);
            }
        }

        plot.setDataset(1, plot.getDataset(0));
        plot.setRenderer(1, plot.getRenderer(0));

        plot.setDataset(0, ratingSet);
        plot.setRenderer(0, new LineAndShapeRenderer());
        plot.getRenderer(0).setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator());

        return new MonthChartInfo(x, annotations, ym, max);
    }

    public static JFreeChart createYearBarChart(YearSummary ys){
        return createStackedBarChart("Month of year", "Time spent (hours)",
                ys.getYearChart().getDataset(), ys.getYearChart().getMeta());
    }

    public static JFreeChart createDayBarChart(ChartData dayChart){
        return createStackedBarChart("Hour of day", "Time spent (minute)",
                dayChart.getDataset(), dayChart.getMeta());
    }

    public static JFreeChart createStackedBarChart(String domainAxisLabel, String rangeAxisLabel, DefaultCategoryDataset dataset, ChartMeta colors){
        return createStackedBarChart("", domainAxisLabel, rangeAxisLabel,
                dataset, colors, PlotOrientation.VERTICAL, true, true, false);
    }

    private static JFreeChart createStackedBarChart(String title, String domainAxisLabel, String rangeAxisLabel,
                                                   CategoryDataset dataset, ChartMeta colors, PlotOrientation orientation,
                                                   boolean showLegend, boolean tooltips, boolean urls) {

        CategoryAxis categoryAxis = new CategoryAxis(domainAxisLabel);
        ValueAxis valueAxis = new NumberAxis(rangeAxisLabel);
        StackedBarRenderer renderer = new StackedBarRenderer();
        if (tooltips) renderer.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator());
        if (urls) renderer.setDefaultItemURLGenerator(new StandardCategoryURLGenerator());
        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis, renderer);
        plot.setOrientation(orientation);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, showLegend);
        renderer.setBarPainter(new SherlockBarPainter());
        renderer.setShadowVisible(true);
        int i =0;
        for (Object x:dataset.getRowKeys()) {
            if(x.equals("")){
                renderer.setSeriesVisibleInLegend(i,false);
            }else {
                int y = dataset.getRowIndex((Comparable) x);
                renderer.setSeriesPaint(y, colors.get(x));
            }
            i++;
        }

        Color fg = new Color(Main.currentTheme.foreground);
        Color bg = new Color(Main.currentTheme.background);
        Color leanToBg;
        Color leanToFg;
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
        Font smallFont = new Font("Tahoma", 0, 11);

        categoryAxis.setTickLabelPaint(fg);
        categoryAxis.setLabelPaint(fg);
        categoryAxis.setLabelFont(regularFont);
        categoryAxis.setTickLabelFont(smallFont);

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

    public static class CategoryImageAnnotation extends AbstractAnnotation implements CategoryAnnotation {
        private final Image image;
        private final Comparable category;
        private final CategoryAnchor categoryAnchor;
        private final double value;
        private final RectangleAnchor anchor;

        public CategoryImageAnnotation(Comparable category, double value, Image image) {
            this(category, value, image, RectangleAnchor.CENTER);
        }

        public CategoryImageAnnotation(Comparable category, double value, Image image,  RectangleAnchor anchor) {
            this.image = image;
            this.category = category;
            this.categoryAnchor = CategoryAnchor.MIDDLE;
            this.value = value;
            this.anchor = anchor;
        }

        @Override
        public void draw(Graphics2D g2, CategoryPlot plot, Rectangle2D dataArea, CategoryAxis categoryAxis, ValueAxis valueAxis) {
            CategoryDataset dataset = plot.getDataset();
            int catIndex = dataset.getColumnIndex(this.category);
            int catCount = dataset.getColumnCount();
            PlotOrientation orientation = plot.getOrientation();
            AxisLocation domainAxisLocation = plot.getDomainAxisLocation();
            AxisLocation rangeAxisLocation = plot.getRangeAxisLocation();
            RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(domainAxisLocation, orientation);
            RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(rangeAxisLocation, orientation);
            float j2DX = (float)categoryAxis.getCategoryJava2DCoordinate(this.categoryAnchor, catIndex, catCount, dataArea, domainEdge);
            float j2DY = (float)valueAxis.valueToJava2D(this.value, dataArea, rangeEdge);
            float xx = 0.0F;
            float yy = 0.0F;
            if (orientation == PlotOrientation.HORIZONTAL) {
                xx = j2DY;
                yy = j2DX;
            } else if (orientation == PlotOrientation.VERTICAL) {
                xx = j2DX;
                yy = j2DY;
            }

            int w = this.image.getWidth(null);
            int h = this.image.getHeight(null);
            Rectangle2D imageRect = new Rectangle2D.Double(0.0D, 0.0D, (double)w, (double)h);
            Point2D anchorPoint = this.anchor.getAnchorPoint(imageRect);
            xx -= (float)anchorPoint.getX();
            yy -= (float)anchorPoint.getY();
            g2.drawImage(this.image, (int)xx, (int)yy, null);
//            if (toolTip != null || url != null) {
//                this.addEntity(info, new Rectangle2D.Float(xx, yy, (float)w, (float)h), rendererIndex, toolTip, url);
//            }
        }
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
