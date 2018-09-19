package xyz.spiralhalo.sherlock.report.factory;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.async.AsyncTask;
import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.record.DefaultRecordWriter;
import xyz.spiralhalo.sherlock.record.RecordScanner;
import xyz.spiralhalo.sherlock.record.io.RecordFileSeek;
import xyz.spiralhalo.sherlock.report.factory.charts.ChartBuilder;
import xyz.spiralhalo.sherlock.report.factory.summary.*;
import xyz.spiralhalo.sherlock.report.factory.table.AllReportRow;
import xyz.spiralhalo.sherlock.report.factory.table.AllReportRows;
import xyz.spiralhalo.sherlock.util.ListUtil;

import java.io.File;
import java.time.*;
import java.util.ArrayList;

public class ReportRefresher extends AsyncTask<Boolean> {

    private final CacheMgr cache;
    private final ProjectList projectList;
    private final boolean forceReconstruct;
    private final boolean deleteUnused;
    private final ZoneId z;
    private Boolean result = false;

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
        try(RecordFileSeek seeker = new RecordFileSeek(recordFile, false, cache)){
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
                    cache.put(MonthSummary.cacheId(ym, z), summaryBuilder.finish(projectList));
                }
                maxYear = ym.getYear();
                ym = ym.plusMonths(1);
            }
            YearList yearList = new YearList();
            for (int year = minYear; year <= maxYear; year++) {
                final Year y = Year.of(year);
                yearList.add(y);
                if(missingYearSummary(y)) {
                    final ArrayList<YearMonth> months = new ArrayList<>();
                    final ChartBuilder<Year> chartBuilder = new ChartBuilder<>(y,z,false);
                    for (int month = 1; month <= 12; month++) {
                        ym = YearMonth.of(year, month);
                        MonthSummary x = cache.getObj(MonthSummary.cacheId(ym, z), MonthSummary.class);
                        if (x != null) {
                            months.add(ym);
                            for (SummaryEntry entry: x) {
                                chartBuilder.readSummary(entry);
                            }
                        }
                    }
                    cache.put(YearSummary.cacheId(y, z), new YearSummary(y, chartBuilder.finish(projectList), months, y.isBefore(Year.now())));
                }
            }
            cache.put(YearList.cacheId(z), yearList);
        } catch (Exception e){
            Debug.log(e);
            throw e;
        }

        AllReportRows activeRows = new AllReportRows();
        AllReportRows finishedRows = new AllReportRows();
        AllReportRows utilityRows = new AllReportRows();
        Iterable<Project> projects = ListUtil.extensiveIterator(projectList.getActiveProjects(), projectList.getFinishedProjects(), projectList.getUtilityTags());
        for (Project p: projects) {
            YearMonth ym = YearMonth.from(p.getStartDate().withZoneSameInstant(z));
            int seconds = 0;
            int day = 0;
            YearMonth now = YearMonth.now(z), end = null;
            if(!p.isUtilityTag() && p.isFinished()) end = YearMonth.from(p.getFinishedDate().withZoneSameInstant(z));
            while (!ym.isAfter(now) && (end == null || !ym.isAfter(end))){
                MonthSummary x = cache.getObj(MonthSummary.cacheId(ym, z), MonthSummary.class);
                if(x!=null){
                    ArrayList<Integer> indices = x.getDetails().getIndex().get(p.getHash());
                    if(indices != null){
                        for (int i:indices) {
                            //TODO: add effective day filter
                            seconds += x.getDetails().get(i).getSummary().getSeconds();
                            day += 1;
                        }
                    }
                }
                ym = ym.plusMonths(1);
            }
            AllReportRow x;
            if(p.isUtilityTag()) {
                x = new AllReportRow(p.getHash(), p.getColor(), p.toString(), p.isProductive(), day, seconds);
            } else {
                x = new AllReportRow(p.getHash(), p.getColor(), p.toString(),
                        LocalDateTime.from(p.getStartDate()),
                        (p.isFinished() ? LocalDateTime.from(p.getFinishedDate()) : null),
                        day, seconds);
            }
            if(p.isUtilityTag()){
                utilityRows.add(x);
            } else if(p.isFinished()){
                finishedRows.add(x);
            } else {
                activeRows.add(x);
            }
        }
        cache.put(AllReportRows.activeCacheId(z), activeRows);
        cache.put(AllReportRows.finishedCacheId(z), finishedRows);
        cache.put(AllReportRows.utilityCacheId(z), utilityRows);
        result = true;
    }

    private boolean missingMonthSummary(YearMonth month){
        if(forceReconstruct) return true;
        MonthSummary x = cache.getObj(MonthSummary.cacheId(month, z), MonthSummary.class);
        if(x == null) { return true; }
        else { return !x.isComplete(); }
    }

    private boolean missingYearSummary(Year year){
        if(forceReconstruct) return true;
        YearSummary x = cache.getObj(YearSummary.cacheId(year, z), YearSummary.class);
        if(x == null) { return true; }
        else { return !x.isComplete(); }
    }

    @Override
    protected Boolean getResult() {
        return result;
    }
}
