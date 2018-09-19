package xyz.spiralhalo.sherlock.persist.cache;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.util.FormatUtil;

import java.time.LocalDate;

public class CacheId{
    public static String ProjectDayRows(Project project){return Long.toHexString(project.getHash())+"_day_table";}
    public static String ProjectMonthRows(Project project){return Long.toHexString(project.getHash())+"_month_table";}
    public static String ChartData(String date){return date+"_chart_data";}
    public static String ChartMeta(String date){return date+"_chart_meta";}
    public static String ChartData(LocalDate date){return ChartData(FormatUtil.DTF_YMD.format(date));}
    public static String ChartMeta(LocalDate date){return ChartMeta(FormatUtil.DTF_YMD.format(date));}
}