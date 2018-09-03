package xyz.spiralhalo.sherlock.record;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.util.Debug;
import xyz.spiralhalo.sherlock.util.FormatUtil;

import java.io.*;
import java.time.Instant;

/**
 * An abstract class that represents a record writer with an internal buffer.
 *
 * This class handles the aggregation of virtual data points into record entries as well as the writing
 * of those entries into the disk when some of the conditions or rules are met.
 *
 * The file to which the records are written into is decided by the implementation.
 */
public abstract class AbstractRecordWriter {
    private static final String DEBUG_OTHER = "(Other)";

    protected static class TempRecord {
        private long timestamp = 0;
        private long lastUpdated = 0;
        private String debug_name;
        private long hash = 0;
        private boolean productive;
        private boolean utilityTag;

        private TempRecord() { }

        private TempRecord(long timestamp, String debug_name, long hash, boolean utilityTag, boolean productive) {
            reset(timestamp, debug_name, hash, utilityTag, productive);
        }

        void reset(long timestamp, String debug_name, long hash, boolean utilityTag, boolean productive){
            this.timestamp = timestamp;
            this.lastUpdated = timestamp;
            this.debug_name = debug_name;
            this.hash = hash;
            this.utilityTag = utilityTag;
            this.productive = productive;
        }

        void update(long timeUpdated){
            this.lastUpdated = timeUpdated;
        }

        protected int getDuration(){
            return (int)(lastUpdated - timestamp);
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
    private RecordFileAppend lastFile = null;
    private long lastFlushTimestamp = 0;

    /**
     * Creates a new {@link AbstractRecordWriter}
     *
     * @param nRecordCapacity number of record entries the buffer can hold at most
     */
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

    /**
     * Returns the granularity (the period of time between virtual data points) of the record.
     *
     * @return the granularity of the record in milliseconds
     */
    protected abstract int getGranularityMillis();

    /**
     * Retrieves and returns the appropriate record file for a specified record entry.
     *
     * @param forRecord the record entry to which this file would be relevant
     * @return the corresponding record file
     */
    protected abstract RecordFileAppend getRecordFile(TempRecord forRecord);

    /**
     * Method to be called right before the buffer is closed.
     */
    protected abstract void onClosing();

    /**
     * Returns the maximum delay between virtual data points of the same hash before they are treated
     * as belonging to separate record entries.
     *
     * @return the granularity of the record plus a margin of error of 50%
     */
    private int getMaxDelay(){
        return Math.round(getGranularityMillis() * 1.5f);
    }

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

    /**
     * Logs a virtual data point within a record entry.
     *
     * This method is synchronized due to the strict nature of its execution order.
     *
     * @param timestamp time of the virtual data point
     * @param debug_name name of the project or tag (for debugging)
     * @param hash hash of the project or tag
     * @param utilityTag whether it is a tag
     * @param productive whether the tag is productive
     */
    protected synchronized final void log(long timestamp, String debug_name, long hash, boolean utilityTag, boolean productive) {
        TempRecord potential = new TempRecord(timestamp, debug_name, hash, utilityTag, productive);

        if (lastFile == null) {
            lastFile = getRecordFile(potential);
        }
        if (working.timestamp == 0) {
            working.reset(timestamp, debug_name, hash, utilityTag, productive);
        }
        if (lastFlushTimestamp == 0) {
            lastFlushTimestamp = timestamp;
        }

        boolean discontinuous = (timestamp - working.lastUpdated > getMaxDelay());
        boolean differentHash = (hash != working.hash && working.hash != 0);
        boolean fileChanged = fileChanged(potential);

        if (discontinuous || differentHash || fileChanged) {

            flush( fileChanged );

            working.reset(timestamp, debug_name, hash, utilityTag, productive);

            if(fileChanged){
                try {
                    lastFile.close();
                } catch (IOException e) {
                    Debug.log(e);
                }
                lastFile = getRecordFile(potential);
            }
        }

        working.update(timestamp);
    }

    /**
     * Logs a virtual data point within a record entry.
     *
     * @param timestamp time of the virtual data point
     * @param p the project or tag
     */
    protected final void log(long timestamp, Project p){
        if(p==null){
            log(timestamp, DEBUG_OTHER, -1, false, false);
        } else {
            log(timestamp, p.getName(), p.getHash(), p.isUtilityTag(), p.isProductive());
        }
    }

    /**
     * Flushes the current record entry into the buffer and attempts to flush the buffer into disk.
     *
     * @param forced whether to force flushing the buffer into disk
     */
    private void flush(boolean forced){
        final int recordElapsed = (getGranularityMillis() + working.getDuration())/1000;
        final Instant recordTime = Instant.ofEpochMilli(working.timestamp);

        Debug.log(String.format("[Buffer] Writing record entry for: %s", working.debug_name));
        Debug.logVerbose(String.format("%8s %s, %s", "", FormatUtil.DTF_FULL.format(recordTime), recordElapsed));

        buffer.put(RecordData.serialize(recordTime, recordElapsed, working.hash, working.utilityTag, working.productive));

        boolean enforceMaxTimesWritten = buffer.getTimesWritten() >= getEnforcedMaxTimesWritten();
        boolean enforceFlushDelay = System.currentTimeMillis() - lastFlushTimestamp >= getEnforcedFlushDelayMillis();

        if (forced || buffer.full() || enforceMaxTimesWritten || enforceFlushDelay) {
            flushBuffer();
        }
    }

    /**
     * Flushes the buffer into disk.
     */
    public synchronized final void flushBuffer(){
        if(lastFile == null) {
            Debug.log(new RuntimeException("[Buffer] No file to write to"));
            return;
        }
        lastFlushTimestamp = System.currentTimeMillis();
        Debug.log("[Buffer] Flushing record");
        try {
            buffer.flush(lastFile.raf());
        } catch (IOException e) {
            Debug.log(e);
        }
    }

    /**
     * Closes this writer.
     *
     * @throws IOException if an I/O exception occurs
     */
    public final void close() throws IOException {
        Debug.logImportant("[Buffer] Closing buffer");
        onClosing();
        if(working.timestamp !=0) {
            flush(true);
        }
        if(lastFile!=null){
            lastFile.close();
        }
    }
}
