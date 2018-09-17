package xyz.spiralhalo.sherlock.record.legacy;

import xyz.spiralhalo.sherlock.Tracker;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.Application;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.Scanner;

public class AutoImporter2 {
    public static final String OLD_LOG_FILENAME = "record2.txt";
    public static void importRecord(ProjectList projectList) {
        File oldRecordTxt = new File(Application.getSaveDir(), OLD_LOG_FILENAME);
        File newRecordFile = new File(Application.getSaveDir(), ReconstructorWriter.RECORD_FILE);
        if (oldRecordTxt.exists() && !newRecordFile.exists()) {
            try (FileInputStream fis = new FileInputStream(oldRecordTxt);
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
}
