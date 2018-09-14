package xyz.spiralhalo.sherlock.record.legacy;

import xyz.spiralhalo.sherlock.Tracker;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.record.MultiFileRecordWriter;
import xyz.spiralhalo.sherlock.util.Debug;
import xyz.spiralhalo.sherlock.Application;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.Scanner;

public class AutoImporter2 {
    public static final String OLD_LOG_FILENAME = "record2.txt";
    public static void importRecord(ProjectList projectList) {
        File file = new File(Application.getSaveDir(), OLD_LOG_FILENAME);
        File file2 = new File(Application.getSaveDir(), Application.RECDIR);
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
                            y.log(d.toInstant().toEpochMilli(), dur, "(Deleted)", hash, utility, productive);
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
        private long lastTimestamp = 0;
        private int lastDelayS = 0;
        ReconstructorWriter() {
            super(10000);
        }

        void log(long timestamp, int delayS, String debug_name, long hash, boolean utilityTag, boolean productive){
            if(timestamp < lastTimestamp) return; //prevent erroneous or duplicate record
            lastTimestamp = timestamp;
            lastDelayS = 0;
            super.log(timestamp, debug_name, hash, utilityTag, productive);
            lastDelayS = delayS;
            super.log(timestamp + delayS*1000, debug_name, hash, utilityTag, productive);
        }

        void log(long timestamp, int delayS, Project p) {
            if(timestamp < lastTimestamp) return; //prevent erroneous or duplicate record
            lastTimestamp = timestamp;
            lastDelayS = 0;
            super.log(timestamp, p);
            lastDelayS = delayS;
            super.log(timestamp + delayS*1000, p);
        }

        @Override
        protected int getGranularityMillis() {
            return lastDelayS * 1000;
        }

        @Override
        protected void onClosing() {
            lastDelayS = 0;
        }
    }
}
