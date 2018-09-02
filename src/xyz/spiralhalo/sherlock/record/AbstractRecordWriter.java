package xyz.spiralhalo.sherlock.record;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.util.Debug;
import xyz.spiralhalo.sherlock.util.FormatUtil;

import java.io.*;
import java.time.Instant;

public abstract class AbstractRecordWriter {
    private static final String DEBUG_OTHER = "(Other)";

    protected static class TempRecord {
        private long timestamp = 0;
        private String debug_name;
        private long hash = 0;
        private boolean productive;
        private boolean utilityTag;

        private TempRecord() { }

        private TempRecord(long timestamp, String debug_name, long hash, boolean productive, boolean utilityTag) {
            this.timestamp = timestamp;
            this.debug_name = debug_name;
            this.hash = hash;
            this.productive = productive;
            this.utilityTag = utilityTag;
        }

        void setTimestamp(long timestamp){
            this.timestamp = timestamp;
        }

        void set(String debug_name, long hash, boolean productive, boolean utilityTag){
            this.debug_name = debug_name;
            this.hash = hash;
            this.productive = productive;
            this.utilityTag = utilityTag;
        }

        protected long getTimestamp() {
            return timestamp;
        }

        protected String getDebug_name() {
            return debug_name;
        }

        protected long getHash() {
            return hash;
        }

        protected boolean isProductive() {
            return productive;
        }

        protected boolean isUtilityTag() {
            return utilityTag;
        }
    }

    private final TempRecord working = new TempRecord();
    private final DiskOutputBuffer buffer;
    private final int nRecordCapacity;
    private int accuMillis = 0;
    private RecordFileAppend lastFile = null;
    private long lastFlushTimestamp = 0;
    private boolean asyncFlushInProgress = false;

    protected AbstractRecordWriter(int nRecordCapacity) {
        final int byteCapacity;
        try {
            byteCapacity = DiskOutputBuffer.getBytesOnDisk(RecordData.BYTES) * nRecordCapacity;
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Too big.");
        }
        this.buffer = new DiskOutputBuffer(byteCapacity);
        this.nRecordCapacity = nRecordCapacity;
        Debug.logImportant(String.format("[Buffer] New buffer created with class: %s", this.getClass().getSimpleName()));
    }

    protected abstract int getMarginOfError();

    /**
     * Returns minimum record on buffer before flushing is enforced.
     * This rule exists as a safety net to prevent the buffer from overflowing.
     *
     * @return default value of half of record capacity
     */
    protected int getEnforcedMaxTimesWritten(){
        return nRecordCapacity/2;
    }

    /**
     * Returns minimum delay since the last flushing before flushing is enforced.
     * This rule exists to ensure a good User Experience (UX).
     *
     * @return default value of exactly 5 minutes in milliseconds
     */
    protected int getEnforcedFlushDelayMillis(){
        return 5 * 60 * 1000;
    }

    /**
     * Returns whether a different {@link RandomAccessFile} is returned by {@code getRecordFile()}.
     * This rule exists to ensure that records are written to the right file.
     * In that case, the current record is written to the last file instead of the current one.
     *
     * @return whether the current file is different from the last
     */
    private boolean fileChanged(TempRecord currentRecord){
        RecordFileAppend currentFile = getRecordFile(currentRecord);
        boolean changed = !lastFile.equals(currentFile);
        if(changed){
            Debug.logImportant(String.format("[Buffer] Record file is changed. Previous RF: %s, Current RF: %s",
                    lastFile.toString(), currentFile.toString()));
        }
        return changed;
    }

    protected abstract RecordFileAppend getRecordFile(TempRecord forRecord);

    protected final void logInternal(long timestamp, long delay, String debug_name, long hash, boolean productive, boolean utilityTag) {
        TempRecord potential = new TempRecord(timestamp - delay, debug_name, hash, productive, utilityTag);
        if (lastFile == null) {
            lastFile = getRecordFile(potential);
        }
        if (working.timestamp == 0) {
            working.setTimestamp(timestamp - delay);
        }
        if (lastFlushTimestamp == 0) {
            lastFlushTimestamp = timestamp;
        }
        boolean discontinuous = (timestamp - working.timestamp > accuMillis + delay + getMarginOfError() && accuMillis != 0);
        boolean differentHash = (hash != working.hash && working.hash != 0);
        boolean fileChanged = fileChanged(potential);
        if (discontinuous || differentHash || fileChanged) {
            flush(fileChanged == true);
            working.setTimestamp(timestamp - delay);
            if(fileChanged){
                try {
                    lastFile.close();
                } catch (IOException e) {
                    Debug.log(e);
                }
                lastFile = getRecordFile(potential);
            }
        }
        working.set(debug_name, hash, productive, utilityTag);
        accuMillis += delay;
    }

    protected final void logInternal(long timeMillis, long delay, Project p){
        if(p==null){
            logInternal(timeMillis, delay, DEBUG_OTHER, -1, false, false);
        } else {
            logInternal(timeMillis, delay, p.getName(), p.getHash(), p.isProductive(), p.isUtilityTag());
        }
    }

    private void flush(boolean forced){
        final Instant recordTime = Instant.ofEpochMilli(working.timestamp);
        final int recordElapsed = accuMillis/1000;
        Debug.log(String.format("[Buffer] Writing record entry for: %s", working.debug_name));
        Debug.logVerbose(String.format("%8s %s, %s", "", FormatUtil.DTF_FULL.format(recordTime), recordElapsed));
        byte[] recordRaw = RecordData.serialize(recordTime, recordElapsed, working.hash, working.utilityTag, working.productive);
        buffer.put(recordRaw);
        accuMillis = 0;
        boolean enforceMaxTimesWritten = buffer.getTimesWritten() >= getEnforcedMaxTimesWritten();
        boolean enforceFlushDelay = System.currentTimeMillis() - lastFlushTimestamp >= getEnforcedFlushDelayMillis();
        if(forced || buffer.full() || enforceMaxTimesWritten || enforceFlushDelay){
            flushToDisk();
        }
    }

    private void flushToDisk(){
        if(lastFile == null) {
            Debug.log(new RuntimeException("[Buffer] No file to write to"));
            return;
        }
        if(asyncFlushInProgress){
            Debug.logImportant("[Buffer] Async flush is in progress");
            return;
        }
        asyncFlushInProgress = true;
        lastFlushTimestamp = System.currentTimeMillis();
        Debug.log("[Buffer] Flushing record");
        try {
            buffer.flush(lastFile.raf());
        } catch (IOException e) {
            Debug.log(e);
        }
        asyncFlushInProgress = false;
    }

    public final void close() throws IOException {
        Debug.logImportant("[Buffer] Closing buffer");
        if(accuMillis >0) {
            flush(true);
        }
        if(lastFile!=null){
            lastFile.close();
        }
    }
}
