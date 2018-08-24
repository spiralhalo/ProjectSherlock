package xyz.spiralhalo.sherlock.persist.cache;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.util.FormatUtil;

import java.time.LocalDate;

public enum CacheId{
    ActiveRows("active_table"),
    FinishedRows("finished_table"),
    DayRows("day_table"),
    MonthRows("month_table"),
    ChartList("chart_dates")
    ;
    public final String v;
    CacheId(String v){ this.v = v; }
    public static String ProjectDayRows(Project project){return project.getHash()+"_day_table";}
    public static String ProjectMonthRows(Project project){return project.getHash()+"_month_table";}
    public static String ChartData(String date){return date+"_chart_data";}
    public static String ChartColor(String date){return date+"_chart_color";}
    public static String ChartData(LocalDate date){return ChartData(FormatUtil.DTF_YMD.format(date));}
    public static String ChartColor(LocalDate date){return ChartColor(FormatUtil.DTF_YMD.format(date));}
}