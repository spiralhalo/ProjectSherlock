package xyz.spiralhalo.sherlock.record;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.util.Debug;

import java.io.*;
import java.time.Instant;

public abstract class AbstractRecordWriter {
    private static final String DEBUG_OTHER = "(Other)";

    private static class Last{
        private String debug_name;
        private long hash = 0;
        private boolean productive;
        private boolean utilityTag;
        void set(String debug_name, long hash, boolean productive, boolean utilityTag){
            if(hash == -1){
                this.debug_name = DEBUG_OTHER;
                this.hash = -1;
                this.productive = false;
                this.utilityTag = false;
            } else {
                this.debug_name = debug_name;
                this.hash = hash;
                this.productive = productive;
                this.utilityTag = utilityTag;
            }
        }
    }

    private int lastRAF = 0;
    private final Last last = new Last();
    private final DiskOutput writer;
    private int accuMilli = 0;
    private long lastTime = 0;

    protected AbstractRecordWriter(int numOfRecordCapacity) {
        final int byteCapacity;
        try {
            byteCapacity = DiskOutput.getBytesOnDisk(RecordData.BYTES) * numOfRecordCapacity;
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Too big.");
        }
        writer = new DiskOutput(byteCapacity);
    }

    protected abstract int getMarginOfError();

    protected abstract RandomAccessFile getRafSeekLatest();

    protected abstract void closeRAF() throws IOException;

    protected final void logInternal(long timeMillis, long delay, String debug_name, long hash, boolean productive, boolean utilityTag){
        if(lastTime == 0){
            lastTime = timeMillis - delay;
        }
        if((timeMillis-lastTime > accuMilli+delay+getMarginOfError() && accuMilli != 0) || (hash!= last.hash && last.hash !=0)){
            flush(false);
            lastTime = timeMillis - delay;
        }
        last.set(debug_name, hash, productive, utilityTag);
        accuMilli += delay;
    }

    protected final void logInternal(long timeMillis, long delay, Project p){
        if(p==null){
            logInternal(timeMillis, delay, DEBUG_OTHER, -1, false, false);
        } else {
            logInternal(timeMillis, delay, p.getName(), p.getHash(), p.isProductive(), p.isUtilityTag());
        }
    }

    private void flush(boolean forced){
        Debug.log("[Buffer] Writing record entry for: "+last.debug_name);
        byte[] recordRaw = RecordData.serialize(Instant.ofEpochMilli(lastTime),
                accuMilli/1000, last.hash, last.utilityTag, last.productive);
        writer.put(recordRaw);
        accuMilli = 0;
        if(forced || writer.full() || (lastRAF != 0 && lastRAF != getRafSeekLatest().hashCode())){
            Debug.log("[Buffer] Flushing record");
            try {
                writer.flush(getRafSeekLatest());
            } catch (IOException e) {
                Debug.log(e);
            }
            lastRAF = getRafSeekLatest().hashCode();
        }
    }

    public final void close() throws IOException {
        if(accuMilli >0) {
            flush(true);
        }
        closeRAF();
    }
}
