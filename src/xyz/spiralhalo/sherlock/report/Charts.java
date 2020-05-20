//
//    Copyright 2020 spiralhalo <re.nanashi95@gmail.com>
//
//    This file is part of Project Sherlock.
//
//    Project Sherlock is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Project Sherlock is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Project Sherlock.  If not, see <https://www.gnu.org/licenses/>.
//

package xyz.spiralhalo.sherlock.report;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.AbstractAnnotation;
import org.jfree.chart.annotations.CategoryAnnotation;
import org.jfree.chart.annotations.CategoryTextAnnotation;
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
import xyz.spiralhalo.sherlock.report.factory.charts.*;
import xyz.spiralhalo.sherlock.report.factory.summary.MonthSummary;
import xyz.spiralhalo.sherlock.report.factory.summary.YearSummary;
import xyz.spiralhalo.sherlock.util.ImgUtil;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.*;
import java.time.temporal.ChronoField;
import java.util.HashMap;

import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserInt.*;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserBool.*;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserNode.GENERAL;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserNode.VIEW;
import static xyz.spiralhalo.sherlock.util.ColorUtil.*;

public class Charts {
    private static Image noteImg = null;
    private static BufferedImage zzzImg = null;
    static {
        try {
            noteImg = ImgUtil.loadImage("sm_note.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            zzzImg = ImgUtil.loadImage("sm_zzz.png");
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
        final JFreeChart x = createStackedBarChart("", "Time spent (hours)",
                monthSummary.getMonthChart().getDataset(),
                monthSummary.getMonthChart().getMeta());

        final CategoryPlot plot = x.getCategoryPlot();
        //get config vars
        final int target = UserConfig.userGInt(GENERAL, DAILY_TARGET_SECOND);
        final boolean useRankChart = UserConfig.userGBool(VIEW, USE_RANK_MONTH_CHART)
                && !UserConfig.userGBool(VIEW, OLD_RATING);

        //set char upper limit if using rank chart or limit
        if(useRankChart || UserConfig.userGBool(VIEW, LIMIT_MONTH_CHART_UPPER)) {
            plot.getRangeAxis().setRange(0, target / 3600 + 1);
        }

        final Color fg = new Color(Main.currentTheme.foreground | 0x88000000, true);
        if(!UserConfig.userGBool(VIEW, DISABLE_MONTH_LINE)) {
            final ValueMarker marker = new ValueMarker(target/3600f, fg, new BasicStroke(
                    1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1.0f, new float[] {6.0f, 6.0f}, 0.0f));
            marker.setLabel("Target");
            marker.setLabelPaint(new Color(Main.currentTheme.background));
            marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
            marker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
            marker.setLabelOffset(new RectangleInsets(0, 0, 13, 4));
            marker.setLabelFont(new Font("Tahoma", 0, 11));
            marker.setLabelBackgroundColor(fg);

            plot.addRangeMarker(marker);
        }

        final CategoryAxis axis = x.getCategoryPlot().getDomainAxis();
        final YearMonth ym = monthSummary.getMonth();
        final DefaultCategoryDataset totalSet = new DefaultCategoryDataset();

        axis.setCategoryLabelPositions(
                CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 3d));
        axis.setTickLabelFont(new Font("Tahoma", Font.BOLD, 10));

        ZoneId z = ZoneId.systemDefault();
        HashMap<LocalDate, CategoryImageAnnotation> annotations = new HashMap<>();
        double visualUpperBound = plot.getRangeAxis().getRange().getUpperBound() * 0.94;

        int ij = 0;
        Color holidayFG = new Color(0x77000000 | Main.currentTheme.foreground, true); //holidayFG shared between old and new rating
        for (int i = 0; i < ym.lengthOfMonth(); i++) {
            LocalDate date = ym.atDay(i + 1);

            // create "Work Total" if not using rank chart
            if(!useRankChart) {
                ChartData dayChart = monthSummary.getDayCharts().get(date);
                FlexibleLocale unitLabel = ChartType.DAY_IN_MONTH.unitLabel(ym, i);
                if (dayChart != null) {
                    ChartMeta meta = dayChart.getMeta();
                    if (UserConfig.userGBool(VIEW, OLD_RATING)) {
                        int ratio = meta.getLogDur() == 0 ? 0 : (meta.getLogDur() < target ? meta.getWorkDur() * 100 / meta.getLogDur() : meta.getWorkDur() * 100 / target);
                        if (UserConfig.userGWDay(date.get(ChronoField.DAY_OF_WEEK))) {
                            Color ratioFG = interpolateNicely((float) ratio / 100f, bad, neu, gut);
                            axis.setTickLabelPaint(unitLabel, Main.currentTheme.dark ? ratioFG : multiply(gray, ratioFG));
                        } else {
                            axis.setTickLabelPaint(unitLabel, holidayFG);
                        }
                    } else {
                        int ratio = meta.getWorkDur() * 100 / target;
                        if (ratio < 20) {
                            axis.setTickLabelPaint(unitLabel, holidayFG);
//                        Color ratioFG = new Color(0xff77aa);
//                        axis.setTickLabelPaint(unitLabel, Main.currentTheme.dark ? ratioFG : multiply(gray, ratioFG));
                        } else if (ratio < 40) {
                            Color ratioFG = new Color(0xff66ff);
                            axis.setTickLabelPaint(unitLabel, Main.currentTheme.dark ? ratioFG : multiply(gray, ratioFG));
                        } else if (ratio < 90) {
                            Color ratioFG = new Color(0x00ff00);
                            axis.setTickLabelPaint(unitLabel, Main.currentTheme.dark ? ratioFG : multiply(gray, ratioFG));
                        } else {
                            Color ratioFG = new Color(0x00ffff);
                            axis.setTickLabelPaint(unitLabel, Main.currentTheme.dark ? ratioFG : multiply(gray, ratioFG));
                        }
                    }
                    totalSet.addValue((Number) (meta.getWorkDur() / 3600f), "Work Total", unitLabel);
                } else {
                    totalSet.addValue((Number) 0, "Work Total", unitLabel);
                }
            }

            if(YearNotes.getNote(z, date) != null) {
                CategoryImageAnnotation ann = monthAnnotation(ym, i+1,visualUpperBound);
                annotations.put(date, ann);
                plot.addAnnotation(ann);
            }

            if(date.getDayOfWeek().equals(DayOfWeek.MONDAY)){
                ij++;
                CategoryTextAnnotation textAnn = new CategoryTextAnnotation("w"+(ij), ChartType.DAY_IN_MONTH.unitLabel(ym, i), visualUpperBound);
                textAnn.setPaint(fg);
                plot.addAnnotation(textAnn);
            }

            if(date.isEqual(LocalDate.now())){
                CategoryTextAnnotation todayAnn = new CategoryTextAnnotation("today", ChartType.DAY_IN_MONTH.unitLabel(ym, i), visualUpperBound*0.7);
                todayAnn.setPaint(fg);
                plot.addAnnotation(todayAnn);
            }
        }

        if(!useRankChart) {
            // create "Work Total" on the legend if not using rank chart
            plot.setDataset(1, plot.getDataset(0));
            plot.setRenderer(1, plot.getRenderer(0));

            plot.setDataset(0, totalSet);
            plot.setRenderer(0, new LineAndShapeRenderer());
            plot.getRenderer(0).setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator());
        }

        return new MonthChartInfo(x, annotations, ym, visualUpperBound);
    }

    public static JFreeChart createYearBarChart(YearSummary ys){
        JFreeChart x = createStackedBarChart("","", "Time spent (hours)",
                ys.getYearChart().getDataset(), ys.getYearChart().getMeta(), PlotOrientation.VERTICAL,
                false, true, false, true, true);

        final CategoryPlot plot = x.getCategoryPlot();
        final DefaultCategoryDataset totalSet = new DefaultCategoryDataset();
        final DefaultCategoryDataset data = ys.getYearChart().getDataset();

        for(int i=0;i<12;i++){
            double total = 0;
            for (int j=0;j<data.getRowCount();j++) {
                if(data.getValue(j, i) != null
                        //weirdest way to identify productive entries
                        && !CONST_OTHER_GRAY.equals(ys.getYearChart().getMeta().get(data.getRowKey(j)))
                        && !CONST_DELETED_RED_GRAY.equals(ys.getYearChart().getMeta().get(data.getRowKey(j)))
                        && !(ys.getYearChart().getMeta().get(data.getRowKey(j)) instanceof StripedPaint)) {
                    total += data.getValue(j, i).doubleValue();
                }
            }
            totalSet.addValue(total, "Work Total", data.getColumnKey(i));
        }

        if(UserConfig.userGBool(VIEW, ENABLE_YEAR_LINE)) {
            final Color fg = new Color(Main.currentTheme.foreground | 0x88000000, true);
            final int t = UserConfig.userGInt(GENERAL, DAILY_TARGET_SECOND);
            final int weeklyT = UserConfig.userGInt(GENERAL, WEEKLY_TARGET_DAYS, 1, 7, true);
            final ValueMarker marker = new ValueMarker((t/3600f)*4.348f*weeklyT, fg, new BasicStroke(
                    1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1.0f, new float[] {6.0f, 6.0f}, 0.0f));

            marker.setLabel("Monthly Target");
            marker.setLabelPaint(new Color(Main.currentTheme.background));
            marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
            marker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
            marker.setLabelOffset(new RectangleInsets(0, 0, 13, 4));
            marker.setLabelFont(new Font("Tahoma", 0, 11));
            marker.setLabelBackgroundColor(fg);

            plot.addRangeMarker(marker);
        }

        plot.setDataset(1, plot.getDataset(0));
        plot.setRenderer(1, plot.getRenderer(0));

        plot.setDataset(0, totalSet);
        plot.setRenderer(0, new LineAndShapeRenderer());
        plot.getRenderer(0).setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator());

        return x;
    }

    public static JFreeChart createDayBarChart(ChartData dayChart){
        JFreeChart chart = createStackedBarChart("", "Time spent (minute)",
                dayChart.getDataset(), dayChart.getMeta());
        ((CategoryPlot)chart.getPlot()).getRangeAxis().setRange(0,60);
        return chart;
    }

    public static JFreeChart createDayProgressChart(ChartData dayChart, boolean today){
//        HashMap<String,Float> other = new HashMap<>();
        float other = 0;
        HashMap<String,Float> productive = new HashMap<>();
        for (Object x:dayChart.getDataset().getRowKeys()) {
            if(!x.equals("")){
                if(dayChart.getMeta().get(x).equals(CONST_OTHER_GRAY) ||
                        dayChart.getMeta().get(x) instanceof StripedPaint){
                    for (Object y:dayChart.getDataset().getColumnKeys()){
                        Float value = (Float)dayChart.getDataset().getValue((Comparable)x, (Comparable)y);
                        if(value!=null){
//                            other.put((String)x, other.getOrDefault((String)x, 0f)+value);
                            other += value;
                        }
                    }
                } else {
                    for (Object y:dayChart.getDataset().getColumnKeys()){
                        Float value = (Float)dayChart.getDataset().getValue((Comparable)x, (Comparable)y);
                        if(value!=null){
                            productive.put((String)x, productive.getOrDefault((String)x, 0f)+value);
                        }
                    }
                }
            }
        }
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        float prod = 0;
        for(String x:productive.keySet()){
            dataset.addValue((Number)(productive.get(x)/60f), x, "");
            prod += productive.get(x);
        }
        prod /= 60f;
        other /= 60f;
//        for(String x:other.keySet()){
//            dataset.addValue((Number)(other.get(x)/60f), x, 0);
//        }
        float idle, available;
        if(today){
            float now = LocalTime.now().toSecondOfDay()/3600f;
            idle = Math.max(now-other-prod,0);
            available = Math.max(24f-now-Math.max(8f-idle,0),0);
//            sleep = Math.min(8f-idle,8f);
        } else {
            idle = Math.max(24f-other-prod,0);
            available = 0;
//            sleep = Math.min(24f-other-prod,8f);
        }
        dataset.addValue((Number)available, "Available", "");
        dataset.addValue((Number)other, "Other", "");
        dataset.addValue((Number)idle, "Idle", "");
//        dataset.addValue((Number)sleep, "Sleep", "");
        ChartMeta colors = dayChart.getMeta();
        colors.put("Idle", CONST_IDLE_PAINT);
        colors.put("Available", CONST_LEFT_PAINT);
//        colors.put("Sleep", trans);
//        dataset.addValue((Number)(24f-8-prod), "Other", 0);
        JFreeChart chart = createStackedBarChart("", "", "", dataset, colors,
                PlotOrientation.VERTICAL, false, true, false, false, false);
        ((CategoryPlot)chart.getPlot()).getRangeAxis().setRange(0, 24);

        final BasicStroke thinStroke = new BasicStroke( 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1.0f, null, 0.0f);

        final int target = UserConfig.userGInt(GENERAL, DAILY_TARGET_SECOND);
        final Color fg = new Color(Main.currentTheme.foreground | 0x88000000, true);
        final ValueMarker marker = new ValueMarker(target/3600f, Color.black, thinStroke);
        ((CategoryPlot)chart.getPlot()).addRangeMarker(marker);

//        if(today) {
//        final BasicStroke thickStroke = new BasicStroke( 4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
//                1.0f, null, 0.0f);
//            LocalTime now = LocalTime.now();
//            final ValueMarker timeMarker = new ValueMarker(now.toSecondOfDay()/3600f, gut, thickStroke);
//            timeMarker.setLabelAnchor(RectangleAnchor.TOP);
//            ((CategoryPlot)chart.getPlot()).addRangeMarker(timeMarker);
//        }

        ((CategoryPlot)chart.getPlot()).getDomainAxis().setUpperMargin(0);
        ((CategoryPlot)chart.getPlot()).getDomainAxis().setLowerMargin(0);
        ((CategoryPlot)chart.getPlot()).setRangeGridlinesVisible(false);
        chart.getPlot().setBackgroundAlpha(0);
        chart.setBackgroundImage(zzzImg);
        chart.setBackgroundImageAlpha(1);
        return chart;
    }

    public static JFreeChart createStackedBarChart(String domainAxisLabel, String rangeAxisLabel, DefaultCategoryDataset dataset, ChartMeta colors){
        return createStackedBarChart("", domainAxisLabel, rangeAxisLabel,
                dataset, colors, PlotOrientation.VERTICAL, true, true, false, true, true);
    }

    private static JFreeChart createStackedBarChart(String title, String domainAxisLabel, String rangeAxisLabel,
                                                   CategoryDataset dataset, ChartMeta colors, PlotOrientation orientation,
                                                   boolean showLegend, boolean tooltips, boolean urls, boolean labels, boolean shadow) {

        CategoryAxis categoryAxis = new CategoryAxis(domainAxisLabel);
        ValueAxis valueAxis = new NumberAxis(rangeAxisLabel);
        StackedBarRenderer renderer = new StackedBarRenderer();
        valueAxis.setTickLabelsVisible(labels);
        categoryAxis.setTickLabelsVisible(labels);
        if (tooltips) renderer.setDefaultToolTipGenerator(new StandardCategoryToolTipGenerator());
        if (urls) renderer.setDefaultItemURLGenerator(new StandardCategoryURLGenerator());
        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis, renderer);
        plot.setOrientation(orientation);
        JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, showLegend);
        renderer.setBarPainter(new SherlockBarPainter());
        renderer.setShadowVisible(shadow);
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

        if (showLegend) {
            LegendTitle legend = chart.getLegend();
            legend.setBackgroundPaint(leanToBg);
            legend.setItemPaint(fg);
            legend.setFrame(new LineBorder(fg, new BasicStroke(), new RectangleInsets()));
            if (dataset.getRowCount() > 8) {
                legend.setItemFont(smallFont);
                legend.setItemLabelPadding(new RectangleInsets(-1, 1, -1, 2));
                legend.setLegendItemGraphicPadding(new RectangleInsets(-1, 2, -1, 1));
            } else {
                legend.setItemFont(regularFont);
            }
        }

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
