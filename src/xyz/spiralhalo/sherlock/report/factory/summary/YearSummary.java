package xyz.spiralhalo.sherlock.report.factory.summary;

import xyz.spiralhalo.sherlock.report.factory.ReportCache;
import xyz.spiralhalo.sherlock.report.factory.charts.ChartData;

import java.io.Serializable;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;

public class YearSummary implements ReportCache, Serializable {
    public static String cacheId(Year year){
        return String.format("year_summary_%s", year.getValue());
    }

    public static final long serialVersionUID = 1L;
    private final Year year;
    private final ChartData yearChart;
    private final ArrayList<YearMonth> monthList;
    private final boolean complete;

    public YearSummary(Year year, ChartData yearChart, ArrayList<YearMonth> monthList, boolean complete) {
        this.year = year;
        this.yearChart = yearChart;
        this.monthList = monthList;
        this.complete = complete;
    }

    public Year getYear() {
        return year;
    }

    public ChartData getYearChart() {
        return yearChart;
    }

    public ArrayList<YearMonth> getMonthList() {
        return monthList;
    }

    @Override
    public boolean isComplete() {
        return complete;
    }
}