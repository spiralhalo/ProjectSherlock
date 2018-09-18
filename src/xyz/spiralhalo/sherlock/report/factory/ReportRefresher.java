package xyz.spiralhalo.sherlock.report.factory;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.async.AsyncTask;
import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.record.DefaultRecordWriter;
import xyz.spiralhalo.sherlock.record.RecordEntry;
import xyz.spiralhalo.sherlock.record.RecordScanner;
import xyz.spiralhalo.sherlock.record.io.RecordFileSeek;
import xyz.spiralhalo.sherlock.report.factory.charts.ChartBuilder;
import xyz.spiralhalo.sherlock.report.factory.charts.ChartData;
import xyz.spiralhalo.sherlock.report.factory.summary.MonthDetails;
import xyz.spiralhalo.sherlock.report.factory.summary.MonthSummary;
import xyz.spiralhalo.sherlock.report.factory.summary.SummaryBuilder;
import xyz.spiralhalo.sherlock.util.FormatUtil;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;

public class ReportRefresher extends AsyncTask<Void> {

    private final CacheMgr cache;
    private final ProjectList projectList;
    private final boolean forceReconstruct;
    private final boolean deleteUnused;
    private final ZoneId z;

    public ReportRefresher(CacheMgr cache, ProjectList projectList, boolean forceReconstruct, boolean deleteUnused) {
        this.cache = cache;
        this.projectList = projectList;
        this.forceReconstruct = forceReconstruct;
        this.deleteUnused = deleteUnused;
        this.z = ZoneId.systemDefault();
    }

    @Override
    protected void doRun() throws Exception {
        File recordFile = new File(Application.getSaveDir(), DefaultRecordWriter.RECORD_FILE);
        try(RecordFileSeek seeker = new RecordFileSeek(recordFile, false)){
            //-> set min time (first entry)
            final LocalDateTime min = seeker.getCurrentTimestamp().atZone(z).toLocalDateTime();
            //-> loop per month > min time until EOF
            YearMonth month = YearMonth.from(min);
            LocalDateTime max = null;
            while(!seeker.eof()){
                //--> if (month summary missing or incomplete) set month summary builder
                SummaryBuilder summaryBuilder = null;
                if(missingMonthSummary(month)){
                    summaryBuilder = new SummaryBuilder(month, ZoneId.systemDefault());
                }
                //--> loop per day
                RecordScanner scanner = new RecordScanner(seeker, month);
                LocalDate lastDay = null;
                ChartBuilder<LocalDate> dayChartBuilder = null;
                RecordEntry entry = null;
                while (scanner.hasNext()){
                    if(entry == null){
                        entry = scanner.next();
                    }
                    //---> if (day chart missing or incomplete) set day chart builder
                    LocalDate day = entry.getTime().atZone(z).toLocalDate();
                    if(!day.equals(lastDay)){
                        if(missingDayChart(day)) {
                            dayChartBuilder = new ChartBuilder<>(day, z, false);
                        }
                        lastDay = day;
                    }
                     //---> loop per record entry if (day chart builder is set or month summary builder is set)
                    if(summaryBuilder != null || dayChartBuilder != null){
                        //----> if (day chart builder is set) bikin day chart (ChartBuilder <- entry)
                        if(dayChartBuilder != null){
                            dayChartBuilder.readEntry(entry);
                        }
                        //----> if (month summary builder is set) bikin month details + month summary (SummaryBuilder <- entry)
                        if(summaryBuilder != null){
                            summaryBuilder.readEntry(entry);
                        }
                        //----> set max time
                        max = entry.getTime().atZone(z).toLocalDateTime();
                    }
                    LocalDate nextDay;
                    if(scanner.hasNext()){
                        entry = scanner.next();
                        nextDay = entry.getTime().atZone(z).toLocalDate();
                    } else {
                        nextDay = null;
                    }
                    if(!day.equals(nextDay) && dayChartBuilder != null){
                        dayChartBuilder.finish(projectList, nextDay!=null);
                        dayChartBuilder = null;
                    }
                }
                if(max == null){
                    max = month.atDay(1).atStartOfDay();
                }
                if(summaryBuilder != null){
                    boolean complete = month.isBefore(YearMonth.now(z));
                    cache.put(MonthSummary.cacheId(month), summaryBuilder.finish(complete));
                }
                month = month.plusMonths(1);
            }
            for (int year = min.getYear(); year <= max.getYear(); year++) {

            }
            //-> loop per year > min time until max time
            //--> if (year chart missing) set year chart builder
            //--> loop per month summary in year
            //---> if (month chart missing) set month chart builder
            //---> loop per month details
            //----> if (month chart builder is set) bikin month chart (ChartBuilder <- details)
            //---> if (year chart builder is set) bikin year chart (ChartBuilder <- summary)
        }
    }

    private boolean missingMonthSummary(YearMonth month){
        MonthSummary x = cache.getObj(MonthSummary.cacheId(month), MonthSummary.class);
        if(x == null) { return true; }
        else { return !x.isComplete(); }
    }

    private boolean missingDayChart(LocalDate date){
        ChartData x = cache.getObj(ChartData.dayId(date), ChartData.class);
        if(x == null) { return true; }
        else { return !x.isComplete(); }
    }

    @Override
    protected Void getResult() {
        return null;
    }
}
