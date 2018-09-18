package xyz.spiralhalo.sherlock.report.factory.summary;

import xyz.spiralhalo.sherlock.persist.cache.Cache;
import xyz.spiralhalo.sherlock.report.factory.ReportCache;
import xyz.spiralhalo.sherlock.report.factory.charts.ChartData;
import xyz.spiralhalo.sherlock.util.FormatUtil;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

@Cache
public class MonthSummary extends ArrayList<SummaryEntry> implements ReportCache, Serializable {
    public static String cacheId(YearMonth month){
        return String.format("summary_%s", FormatUtil.DTF_YM.format(month));
    }

    public static final long serialVersionUID = 1L;
    private final YearMonth month;
    private final MonthDetails details;
    private final ChartData monthChart;
    private final ArrayList<LocalDate> dayList;
    private final HashMap<LocalDate, ChartData> dayCharts;
    private final HashMap<Long, Integer> index;
    private final boolean complete;

    public MonthSummary(YearMonth month, ChartData monthChart, ArrayList<LocalDate> dayList, HashMap<LocalDate, ChartData> dayCharts, boolean complete) {
        this.month = month;
        this.details = new MonthDetails();
        this.monthChart = monthChart;
        this.dayList = dayList;
        this.dayCharts = dayCharts;
        this.complete = complete;
        this.index = new HashMap<>();
    }

    @Override
    public boolean add(SummaryEntry summaryEntry) {
        if(super.add(summaryEntry)){
            int pos = size() - 1;
            long hash = summaryEntry.getHash();
            index.putIfAbsent(hash, pos);
            return true;
        } else return false;
    }

    @Override
    public boolean addAll(Collection<? extends SummaryEntry> c) {
        int start = size();
        if(super.addAll(c)){
            int end = start + c.size();
            for (int i = start; i < end; i++) {
                int pos = i;
                long hash = get(i).getHash();
                index.putIfAbsent(hash, i);
            }
            return true;
        } else return false;
    }

    @Override
    public void add(int index, SummaryEntry element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends SummaryEntry> c) {
        throw new UnsupportedOperationException();
    }

    public HashMap<Long, Integer> getIndex() {
        return index;
    }

    public ChartData getMonthChart() {
        return monthChart;
    }

    public ArrayList<LocalDate> getDayList() {
        return dayList;
    }

    public HashMap<LocalDate, ChartData> getDayCharts() {
        return dayCharts;
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    public YearMonth getMonth() {
        return month;
    }

    public MonthDetails getDetails() {
        return details;
    }
}
