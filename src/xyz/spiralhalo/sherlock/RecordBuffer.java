package xyz.spiralhalo.sherlock;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.ZoneId;

public class RecordBuffer {

    private FileWriter fw;
    private BufferedWriter bw;
    private PrintWriter out;
    private long lastHash;
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

    public void log(long hash, long delay){
        if(hash!= lastHash && lastHash !=0){
            flush();
        }
        lastHash = hash;
        accuS +=(delay/1000);
        elapsed += delay;
    }

    public void flush(){
        StringBuilder buffer = new StringBuilder();
        buffer.append(Tracker.DTF.format(Instant.ofEpochSecond(System.currentTimeMillis()/1000-accuS).atZone(ZoneId.systemDefault())))
            .append(Tracker.SPLIT_DIVIDER).append(accuS).append(Tracker.SPLIT_DIVIDER).append(lastHash);
        out.println(buffer.toString());
        accuS = 0;
        if(elapsed>Tracker.FLUSH_DELAY_MILLIS){
            elapsed = 0;
            out.flush();
        }
    }

    public void close() throws IOException {
        if(accuS>0) {
            elapsed = Tracker.FLUSH_DELAY_MILLIS + 1;
            flush();
        }
        out.flush();
        out.close();
        bw.close();
        fw.close();
    }
}
