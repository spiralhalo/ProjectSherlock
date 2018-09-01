package xyz.spiralhalo.sherlock.report.factory;

import org.jfree.data.category.DefaultCategoryDataset;
import xyz.spiralhalo.sherlock.async.AsyncTask;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.persist.project.UtilityTag;
import xyz.spiralhalo.sherlock.record.RecordData;
import xyz.spiralhalo.sherlock.record.SequentalRecordScanner;
import xyz.spiralhalo.sherlock.report.persist.ChartMeta;
import xyz.spiralhalo.sherlock.report.persist.AllReportRow;
import xyz.spiralhalo.sherlock.report.persist.AllReportRows;
import xyz.spiralhalo.sherlock.report.persist.ReportRow;
import xyz.spiralhalo.sherlock.report.persist.ReportRows;
import xyz.spiralhalo.sherlock.util.ColorUtil;
import xyz.spiralhalo.sherlock.util.Debug;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;

import static xyz.spiralhalo.sherlock.report.factory.Const.MINIMUM_SECOND;

public class OverviewCreator extends AsyncTask<OverviewResult> {
    private final ProjectList projectList;
    private final HashMap<Long,Boolean> productiveMap = new HashMap<>();
    private final LocalDate from, until;

    public OverviewCreator(ProjectList projectList) {
        this(projectList, null, null);
    }

    public OverviewCreator(ProjectList projectList, LocalDate from, LocalDate until) {
        this.projectList = projectList;
        this.from = from;
        this.until = until;
    }

    private OverviewResult result;

    @Override
    public OverviewResult getResult() {
        return result;
    }

    @Override
    protected void doRun() throws Exception{
        final DatasetCreator dc = new DatasetCreator(projectList, productiveMap);
        final ReportCreator rc = new ReportCreator(projectList, productiveMap);
        try(SequentalRecordScanner sc = new SequentalRecordScanner(from, until)){
            while (sc.hasNext()) {
                RecordData recordData = sc.next();
                try {
                    ZonedDateTime timestamp = recordData.getTime().atZone(ZoneId.systemDefault());
                    int dur = recordData.getElapsed();
                    long pHash = recordData.getHash();
                    boolean productive = (pHash != -1) && (recordData.isProductive() || !recordData.isUtility());

                    Project p = projectList.findByHash(pHash);
                    LocalDate date = timestamp.toLocalDate();

                    if (productiveMap.get(pHash) == null) {
                        if (p == null) {
                            productiveMap.put(pHash, productive);
                        } else {
                            productiveMap.put(pHash, p.isProductive());
                        }
                    }

                    dc.process(date, timestamp, pHash, dur);
                    if (p == null) continue;
                    rc.process(date, pHash, dur);
                } catch (NumberFormatException | DateTimeParseException e) {
                    Debug.log(e);
                }
            }
            rc.finalizeProcess();
            dc.finalizeProcess();
            result = new OverviewResult(rc.activeRows,rc.finishedRows, rc.utilityRows, rc.dayRows,rc.monthRows,dc.datasetArray);
        }
    }

    private static class DatasetCreator{
        private static final String OTHER = "Other";
        private static final String DELETED = "(Deleted)";

        final DatasetArray datasetArray;
        private final HashMap<Long, Boolean> productiveMap;
        private LocalDate lastDate;
        private final ProjectList projectList;
        private HashMap<Long,Integer>[] hours;

        DatasetCreator(ProjectList projectList, HashMap<Long,Boolean> productiveMap){
            this.projectList = projectList;
            this.datasetArray = new DatasetArray();
            this.productiveMap = productiveMap;
            initialize();
        }

        void finalizeProcess(){
            createEntry();
        }

        void process(LocalDate currentDate, ZonedDateTime timestamp, long pHash, int dur){
            if(lastDate == null) {
                lastDate = currentDate;
            } else if(lastDate.isBefore(currentDate)){
                createEntry();
                lastDate = currentDate;
            }
            int hour = timestamp.getHour();
            int secondRemain = 3600-timestamp.getMinute()*60-timestamp.getSecond();
            int dur2 = dur;
            while (dur2 > 0 && hour < 24){
                if(hours[hour] == null){
                    hours[hour] = new HashMap<>();
                }
                int y = Math.min(dur2, secondRemain);
                hours[hour].put(pHash,hours[hour].getOrDefault(pHash,0)+y);
                dur2 -= y;
                hour += 1;
                secondRemain = 3600;
            }
        }

        private void initialize(){
            hours = new HashMap[24];
        }

        private void createEntry() {
            if(lastDate==null){
                lastDate=LocalDate.now();
            }
            final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            final ChartMeta meta = new ChartMeta();
            meta.put(OTHER, ColorUtil.gray);
            meta.put(DELETED, ColorUtil.med_red_gray);
            for (int i = 0; i < 24; i++) {
                if(hours[i]==null) {
                    dataset.addValue((Number)0,"Other",i);
                    continue;
                }
                for (Long l:hours[i].keySet()) {
                    Project p = projectList.findByHash(l);

                    String label;
                    if(p != null){
                        meta.putIfAbsent(p.getName(), new Color(p.getColor()));
                        label = p.getName();
                    } else if(l == -1 || !productiveMap.getOrDefault(l,false)){
                        label = OTHER;
                    } else {
                        label = DELETED;
                    }
                    dataset.addValue((Number) (hours[i].get(l) / 60f), label, i);

                    meta.workDur += productiveMap.getOrDefault(l,false)?hours[i].get(l):0;
                    meta.logDur += hours[i].get(l);
                }
            }
            datasetArray.add(lastDate, dataset, meta);
            initialize();
        }
    }

    private static class ReportCreator{
        final ReportRows dayRows = new ReportRows();
        final ReportRows monthRows = new ReportRows();
        final AllReportRows activeRows = new AllReportRows();
        final AllReportRows finishedRows = new AllReportRows();
        final AllReportRows utilityRows = new AllReportRows();
        private final LocalDate today = LocalDate.now();
        private final ProjectList projectList;
        private final HashMap<Long, Boolean> productiveMap;

        private LocalDate lastDate, lastMonth;
        private int accuS = 0, accuSM = 0;
        private final HashMap<Long,Integer> projectAccuS = new HashMap<>();
        private final HashMap<Long,Integer> projectAccuSD = new HashMap<>();
        private final HashMap<Long,Integer> projectAccuD = new HashMap<>();

        ReportCreator(ProjectList projectList, HashMap<Long, Boolean> productiveMap) {
            this.projectList = projectList;
            this.productiveMap = productiveMap;
        }

        void finalizeProcess(){
            createDayEntry();
            createMonthEntry();
            for (Project p:projectList.getActiveProjects()) {
                activeRows.add(new AllReportRow(p.getHash(), p.getColor(), p.toString(),
                        LocalDateTime.from(p.getStartDate()),
                        (p.isFinished()?LocalDateTime.from(p.getFinishedDate()):null),
                        projectAccuD.getOrDefault(p.getHash(),0),
                        projectAccuS.getOrDefault(p.getHash(),0)));
            }
            for (Project p:projectList.getFinishedProjects()) {
                finishedRows.add(new AllReportRow(p.getHash(), p.getColor(), p.toString(),
                        LocalDateTime.from(p.getStartDate()),
                        (p.isFinished()?LocalDateTime.from(p.getFinishedDate()):null),
                        projectAccuD.getOrDefault(p.getHash(),0),
                        projectAccuS.getOrDefault(p.getHash(),0)));
            }
            for (UtilityTag p:projectList.getUtilityTags()) {
                utilityRows.add(new AllReportRow(p.getHash(), p.getColor(), p.toString(),
                        p.isProductive(),
                        projectAccuD.getOrDefault(p.getHash(),0),
                        projectAccuS.getOrDefault(p.getHash(),0)));
            }
        }

        void process(LocalDate date, long pHash, int dur){
            if(lastDate == null) {
                lastDate = date;
            }else if (lastDate.isBefore(date)) {
                createDayEntry();
                projectAccuSD.clear();
                lastDate = date;
                accuS = 0;
            }
            if(lastMonth == null){
                lastMonth = date;
            } else if(lastMonth.getMonthValue() != date.getMonthValue() || lastMonth.getYear() != date.getYear()) {
                createMonthEntry();
                lastMonth = date;
                accuSM = 0;
            }
            projectAccuSD.put(pHash,projectAccuSD.getOrDefault(pHash,0) + dur);
            if(productiveMap.getOrDefault(pHash, false)) {
                accuS += dur;
            }
        }

        private void createDayEntry(){
            for (long z : projectAccuSD.keySet()) {
                if (projectAccuSD.get(z) > MINIMUM_SECOND || lastDate.equals(today)) {
                    projectAccuD.put(z, projectAccuD.getOrDefault(z, 0) + 1);
                    projectAccuS.put(z, projectAccuS.getOrDefault(z, 0)
                            + projectAccuSD.get(z));
                }
            }
            if (accuS >= MINIMUM_SECOND || (lastDate != null && lastDate.equals(today))) {
                dayRows.add(new ReportRow(lastDate, accuS));
                accuSM += accuS;
            }
        }

        private void createMonthEntry(){
            if(accuSM >= MINIMUM_SECOND) {
                monthRows.add(new ReportRow(lastMonth, accuSM));
            }
        }
    }
}
