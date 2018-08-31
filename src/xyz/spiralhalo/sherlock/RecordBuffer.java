package xyz.spiralhalo.sherlock;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.util.Debug;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.ZoneId;

public class RecordBuffer {
    private static final String DEBUG_OTHER = "(Other)";

    private static class Last{
        private String debug_name;
        private long hash = 0;
        private boolean productive;
        private boolean utilityTag;
        public void set(Project p){
            if(p==null){
                debug_name = DEBUG_OTHER;
                hash = -1;
                productive = false;
                utilityTag = false;
            } else {
                debug_name = p.toString();
                hash = p.getHash();
                productive = p.isProductive();
                utilityTag = p.isUtilityTag();
            }
        }
    }

    private FileWriter fw;
    private BufferedWriter bw;
    private PrintWriter out;
    private final Last last = new Last();
    private long accuS;
    private long elapsed;

    public RecordBuffer() {
        try {
            fw = new FileWriter(Tracker.getRecordFile(), true);
            bw = new BufferedWriter(fw);
            out = new PrintWriter(bw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void log(Project p, long delay){
        long hash = p==null?-1:p.getHash();
        if(hash!= last.hash && last.hash !=0){
            flush();
        }
        last.set(p);
        accuS +=(delay/1000);
        elapsed += delay;
    }

    public void flush(){
        Debug.log("[Buffer] Writing record entry for: "+last.debug_name);
        StringBuilder buffer = new StringBuilder();
        buffer.append(Tracker.DTF.format(Instant.ofEpochSecond(System.currentTimeMillis()/1000-accuS).atZone(ZoneId.systemDefault())))
            .append(Tracker.SPLIT_DIVIDER).append(accuS).append(Tracker.SPLIT_DIVIDER).append(last.hash);
        if(last.utilityTag) {
            buffer.append(Tracker.SPLIT_DIVIDER).append(last.productive);
        }
        out.println(buffer.toString());
        accuS = 0;
        if(elapsed>Tracker.FLUSH_DELAY_MILLIS){
            Debug.log("[Buffer] Flushing record");
            elapsed = 0;
            out.flush();
        }
    }

    public void close() throws IOException {
        if(accuS>0) {
            elapsed = Tracker.FLUSH_DELAY_MILLIS + 1;
            flush();
        }
        out.close();
        bw.close();
        fw.close();
    }
}
