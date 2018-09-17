package xyz.spiralhalo.sherlock.record.legacy;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.record.DefaultRecordWriter;

public class ReconstructorWriter extends DefaultRecordWriter {
    private long lastTimestamp = 0;

    ReconstructorWriter() {
        super(10000);
    }

    void log(long timestamp, int elapsed, String debug_name, long hash, boolean utilityTag, boolean productive){
        if(timestamp < lastTimestamp) return; //prevent erroneous or duplicate record
        lastTimestamp = timestamp;
        for (int i = 0; i < elapsed; i++) {
            super.log(timestamp + i * 1000, debug_name, hash, utilityTag, productive);
        }
    }

    void log(long timestamp, int elapsed, Project p) {
        if(timestamp < lastTimestamp) return; //prevent erroneous or duplicate record
        lastTimestamp = timestamp;
        for (int i = 0; i < elapsed; i++) {
            super.log(timestamp + i * 1000, p);
        }
    }
}
