package xyz.spiralhalo.sherlock.report.factory;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.async.AsyncTask;
import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.record.DefaultRecordWriter;
import xyz.spiralhalo.sherlock.record.RecordScanner;
import xyz.spiralhalo.sherlock.record.io.RecordFileSeek;
import xyz.spiralhalo.sherlock.report.factory.charts.ChartBuilder;
import xyz.spiralhalo.sherlock.report.factory.summary.*;

import java.io.File;
import java.time.*;
import java.util.ArrayList;

public class ReportRefresher extends AsyncTask<Void> {

    private final CacheMgr cache;
    private final ProjectList projectList;
    private final boolean forceReconstruct;
    private final boolean deleteUnused;
    private final ZoneId z;

    public ReportRefresher(CacheMgr cache, ProjectList projectList) {
        this(cache, projectList, false, false);
    }

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
            final LocalDateTime earliest = seeker.getCurrentTimestamp().atZone(z).toLocalDateTime();
            final int minYear = Year.from(earliest).getValue();
            int maxYear = minYear;
            YearMonth ym = YearMonth.from(earliest);
            while(!seeker.eof()){
                if(missingMonthSummary(ym)){
                    SummaryBuilder summaryBuilder = new SummaryBuilder(ym, z, ym.isBefore(YearMonth.now(z)));
                    RecordScanner scanner = new RecordScanner(seeker, ym);
                    while (scanner.hasNext()) {
                        summaryBuilder.readRecord(scanner.next());
                    }
                    cache.put(MonthSummary.cacheId(ym), summaryBuilder.finish(projectList));
                }
                maxYear = ym.getYear();
                ym = ym.plusMonths(1);
            }
            for (int year = minYear; year <= maxYear; year++) {
                final Year y = Year.of(year);
                if(missingYearSummary(y)) {
                    final ArrayList<YearMonth> months = new ArrayList<>();
                    final ChartBuilder<Year> chartBuilder = new ChartBuilder<>(y,z,true);
                    for (int month = 1; month <= 12; month++) {
                        ym = YearMonth.of(year, month);
                        MonthSummary x = cache.getObj(MonthSummary.cacheId(ym), MonthSummary.class);
                        if (x != null) {
                            months.add(ym);
                            for (SummaryEntry entry: x) {
                                chartBuilder.readSummary(entry);
                            }
                        }
                    }
                    cache.put(YearSummary.cacheId(y), new YearSummary(y, chartBuilder.finish(projectList), months, y.isBefore(Year.now())));
                }
            }
        }
    }

    private boolean missingMonthSummary(YearMonth month){
        if(forceReconstruct) return true;
        MonthSummary x = cache.getObj(MonthSummary.cacheId(month), MonthSummary.class);
        if(x == null) { return true; }
        else { return !x.isComplete(); }
    }

    private boolean missingYearSummary(Year year){
        if(forceReconstruct) return true;
        YearSummary x = cache.getObj(YearSummary.cacheId(year), YearSummary.class);
        if(x == null) { return true; }
        else { return !x.isComplete(); }
    }

    @Override
    protected Void getResult() {
        return null;
    }
}
