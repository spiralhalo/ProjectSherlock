package xyz.spiralhalo.sherlock.record.legacy;

import xyz.spiralhalo.sherlock.Tracker;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.record.MultiFileRecordWriter;
import xyz.spiralhalo.sherlock.util.Debug;
import xyz.spiralhalo.sherlock.util.PathUtil;

import java.io.*;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Scanner;

public class AutoImporter2 {
    public static final String OLD_LOG_FILENAME = "record2.txt";
    public static void importRecord(ProjectList projectList) {
        File file = new File(PathUtil.getSaveDir(), OLD_LOG_FILENAME);
        File file2 = new File(PathUtil.getSaveDir(), PathUtil.RECDIR);
        if (file.exists() && !file2.isDirectory()) {
            try (FileInputStream fis = new FileInputStream(file);
                 Scanner sc = new Scanner(fis)) {
                String[] temp;
                ZonedDateTime d;
                Project p;
                long hash;
                int dur;
                final ReconstructorWriter y = new ReconstructorWriter();
                while (sc.hasNext()){
                    try {
                        temp = sc.nextLine().split(Tracker.SPLIT_DIVIDER);
                        d = ZonedDateTime.parse(temp[0], Tracker.DTF);
                        dur = Integer.parseInt(temp[1]);
                        hash = Long.parseLong(temp[2]);
                        p = projectList.findByHash(hash);
                        if (p == null && hash != -1) {
                            boolean utility = temp.length > 3;
                            boolean productive;
                            if (utility) {
                                productive = Boolean.parseBoolean(temp[3]);
                            } else {
                                productive = true;
                            }
                            y.log(d.toInstant().toEpochMilli(), dur, "(Deleted)", hash, productive, utility);
                        } else {
                            y.log(d.toInstant().toEpochMilli(), dur, p);
                        }
                    } catch (NumberFormatException ex){
                        Debug.log(ex);
                    }
                }
                y.close();
            } catch (IOException e) {
                Debug.log(e);
            }
        }
    }

    private static class ReconstructorWriter extends MultiFileRecordWriter {
        ReconstructorWriter() {
            super(1000);
        }

        void log(long timeMillis, int delayS, String debug_name, long hash, boolean productive, boolean utilityTag){
            setWorkingDate(timeMillis);
            super.logInternal(timeMillis + delayS * 1000, delayS * 1000, debug_name, hash, productive, utilityTag);
        }

        void log(long timeMillis, int delayS, Project p) {
            setWorkingDate(timeMillis);
            super.logInternal(timeMillis + delayS * 1000, delayS * 1000, p);
        }

        private void setWorkingDate(long timeMillis){
            super.setWorkingYM(YearMonth.from(Instant.ofEpochMilli(timeMillis).atZone(ZoneId.systemDefault())));
        }

        @Override
        protected int getMarginOfError() {
            return 100;
        }
    }
}
