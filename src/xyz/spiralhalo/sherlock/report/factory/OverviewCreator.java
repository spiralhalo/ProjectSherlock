package xyz.spiralhalo.sherlock.report.factory;

import org.jfree.data.category.DefaultCategoryDataset;
import xyz.spiralhalo.sherlock.Tracker;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.persist.project.UtilityTag;
import xyz.spiralhalo.sherlock.report.persist.ChartMeta;
import xyz.spiralhalo.sherlock.report.persist.AllReportRow;
import xyz.spiralhalo.sherlock.report.persist.AllReportRows;
import xyz.spiralhalo.sherlock.report.persist.ReportRow;
import xyz.spiralhalo.sherlock.report.persist.ReportRows;
import xyz.spiralhalo.sherlock.util.ColorUtil;
import xyz.spiralhalo.sherlock.util.Debug;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Supplier;

import static xyz.spiralhalo.sherlock.report.factory.Const.MINIMUM_SECOND;

public class OverviewCreator implements Supplier<Object[]> {
    private final ProjectList projectList;
    private final HashMap<Long,Boolean> productiveMap = new HashMap<>();

    public OverviewCreator(ProjectList projectList) {
        this.projectList = projectList;
    }

    private int getElapsed(String[] s){return Integer.parseInt(s[1]);}
    private long getHash(String[] s){return Long.parseLong(s[2]);}
    private ZonedDateTime getTimestamp(String[] s){return ZonedDateTime.parse(s[0], Tracker.DTF);}
    private boolean isUtilityTag(String[] s){return s.length>3;}
    private boolean isProductive(String[] s){return !isUtilityTag(s) || Boolean.parseBoolean(s[3]);}

    @Override
    public Object[] get() {
        final DatasetCreator dc = new DatasetCreator(projectList, productiveMap);
        final ReportCreator rc = new ReportCreator(projectList, productiveMap);
        try(FileInputStream fis = new FileInputStream(Tracker.getRecordFile());
            Scanner sc = new Scanner(fis)){
            while(sc.hasNext()){
                String[] recordEntry=sc.nextLine().split(Tracker.SPLIT_DIVIDER);
                try {
                    ZonedDateTime timestamp = getTimestamp(recordEntry);
                    int dur = getElapsed(recordEntry);
                    long pHash = getHash(recordEntry);
                    boolean productive = isProductive(recordEntry);

                    Project p = projectList.findByHash(pHash);
                    LocalDate date = timestamp.toLocalDate();

                    if(productiveMap.get(pHash) == null) {
                        if(p == null) {
                            productiveMap.put(pHash, productive);
                        } else {
                            productiveMap.put(pHash, p.isProductive());
                        }
                    }

                    dc.process(date, timestamp, pHash, dur);
                    if(p==null) continue;
                    rc.process(date, pHash, dur);
                } catch (NumberFormatException e) {
                    Debug.log(OverviewCreator.class, e);
                }
            }
            rc.finalizeProcess();
            dc.finalizeProcess();
            return new Object[]{rc.activeRows,rc.finishedRows, rc.utilityRows, rc.dayRows,rc.monthRows,dc.datasetArray};
        } catch (IOException e1) {
            Debug.log(OverviewCreator.class,e1);
            throw new RuntimeException(e1);
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
        private HashMap<Long,Integer> projectAccuS = new HashMap<>();
        private HashMap<Long,Integer> projectAccuSD = new HashMap<>();
        private HashMap<Long,Integer> projectAccuD = new HashMap<>();

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
                if(projectAccuSD.get(z) > MINIMUM_SECOND || lastDate.equals(today)) {
                    projectAccuD.put(z, projectAccuD.getOrDefault(z, 0) + 1);
                    projectAccuS.put(z, projectAccuS.getOrDefault(z, 0)
                            + projectAccuSD.get(z));
                }
            }
            if (accuS >= MINIMUM_SECOND || lastDate.equals(today)) {
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
