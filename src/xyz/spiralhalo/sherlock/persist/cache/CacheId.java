package xyz.spiralhalo.sherlock.persist.cache;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.util.FormatUtil;

import java.time.LocalDate;

public enum CacheId{
    ActiveRows("active_table"),
    FinishedRows("finished_table"),
    UtilityRows("utility_table"),
    DayRows("day_table"),
    MonthRows("month_table"),
    ChartList("chart_dates")
    ;
    public final String v;
    CacheId(String v){ this.v = v; }
    public static String ProjectDayRows(Project project){return Long.toHexString(project.getHash())+"_day_table";}
    public static String ProjectMonthRows(Project project){return Long.toHexString(project.getHash())+"_month_table";}
    public static String ChartData(String date){return date+"_chart_data";}
    public static String ChartMeta(String date){return date+"_chart_meta";}
    public static String ChartData(LocalDate date){return ChartData(FormatUtil.DTF_YMD.format(date));}
    public static String ChartMeta(LocalDate date){return ChartMeta(FormatUtil.DTF_YMD.format(date));}
}