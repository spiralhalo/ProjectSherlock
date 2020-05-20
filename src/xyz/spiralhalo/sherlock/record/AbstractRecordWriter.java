//
//    Copyright 2020 spiralhalo <re.nanashi95@gmail.com>
//
//    This file is part of Project Sherlock.
//
//    Project Sherlock is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Project Sherlock is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Project Sherlock.  If not, see <https://www.gnu.org/licenses/>.
//

package xyz.spiralhalo.sherlock.record;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.record.io.RecordFileAppend;
import xyz.spiralhalo.sherlock.util.FormatUtil;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
        private int hour;
        private Instant timestamp;
        private long lastUpdated = 0;
        private String debug_name;
        private long hash = 0;
        private boolean productive;
        private boolean utilityTag;

        private TempRecord() { }

        private TempRecord(Instant timestamp, String debug_name, long hash, boolean utilityTag, boolean productive) {
            reset(timestamp, debug_name, hash, utilityTag, productive);
        }

        void reset(Instant timestamp, String debug_name, long hash, boolean utilityTag, boolean productive){
            this.hour = LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC).getHour();
            this.timestamp = timestamp;
            this.lastUpdated = timestamp.toEpochMilli();
            this.debug_name = debug_name;
            this.hash = hash;
            this.utilityTag = utilityTag;
            this.productive = productive;
        }

        void update(long timeUpdated){
            this.lastUpdated = timeUpdated;
        }

        protected int getDuration(){
            return (int)(lastUpdated - timestamp.toEpochMilli());
        }
    }

    private final TempRecord working = new TempRecord();
    private final RecordOutputBuffer buffer;
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
            byteCapacity = RecordEntry.BYTES_UNIVERSAL * nRecordCapacity;
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Too big.");
        }
        this.buffer = new RecordOutputBuffer(byteCapacity);
        this.nRecordCapacity = nRecordCapacity;
        Debug.logImportant(String.format("[Buffer] New buffer created with class: %s", this.getClass().getSimpleName()));
    }

    /**
     * Returns the granularity (the period of time between virtual data points) of the record.
     * Note: it can't be more than an hour (3600000 milliseconds).
     *
     * @return the granularity of the record in milliseconds
     */
    protected abstract int getGranularityMillis();

    private int getGranularityMillisInternal(){
        int granularity = getGranularityMillis();
        if(granularity > 3600000){
            throw new RuntimeException("Unsupported implementation");
        }
        return granularity % 3600000;
    }

    /**
     * Retrieves and returns the appropriate record file for a specified record entry.
     *
     * @param timestamp timestamp of the record entry
     * @param hash hash of the record entry
     * @param utilityTag whether the record entry is a utility tag
     * @param productive whether the record entry is productive
     * @return the corresponding record file
     */
    protected abstract RecordFileAppend getRecordFile(Instant timestamp, long hash, boolean utilityTag, boolean productive);

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
        return Math.round(getGranularityMillisInternal() * 1.5f);
    }

    /**
     * Returns minimum record on buffer before flushing is enforced.
     * This rule exists as a safety net to prevent the buffer from overflowing.
     *
     * @return default getValue of half of record capacity
     */
    protected int getEnforcedMaxTimesWritten(){
        return nRecordCapacity/2;
    }

    /**
     * Returns minimum delay since the last flushing before flushing is enforced.
     * This rule exists to ensure a good User Experience (UX).
     *
     * @return default getValue of exactly 5 minutes in milliseconds
     */
    protected int getEnforcedFlushDelayMillis(){
        return 5 * 60 * 1000;
    }

    /**
     * Returns whether a different {@link RandomAccessFile} is returned by {@code getRecordFile()}.
     * This rule exists to ensure that records are written to the right file.
     * In that case, the current record is written to the last file instead of the current one.
     *
     * @param timestamp timestamp of the record entry
     * @param hash hash of the record entry
     * @param utilityTag whether the record entry is a utility tag
     * @param productive whether the record entry is productive
     * @return whether the current file is different from the last
     */
    private boolean fileChanged(Instant timestamp, long hash, boolean utilityTag, boolean productive){
        RecordFileAppend currentFile = getRecordFile(timestamp, hash, utilityTag, productive);
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
     * @param timestampMilli time of the virtual data point
     * @param debug_name name of the project or tag (for debugging)
     * @param hash hash of the project or tag
     * @param utilityTag whether it is a tag
     * @param productive whether the tag is productive
     */
    protected synchronized final void log(long timestampMilli, String debug_name, long hash, boolean utilityTag, boolean productive) {
        Instant timestamp = Instant.ofEpochMilli(timestampMilli);
        int hour = LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC).getHour();

        if (lastFile == null) {
            lastFile = getRecordFile(timestamp, hash, utilityTag, productive);
        }
        if (working.timestamp == null) {
            working.reset(timestamp, debug_name, hash, utilityTag, productive);
        }
        if (lastFlushTimestamp == 0) {
            lastFlushTimestamp = timestampMilli;
        }

        boolean segmented = hour != working.hour;
        boolean discontinuous = (timestampMilli - working.lastUpdated > getMaxDelay());
        boolean differentHash = (hash != working.hash && working.hash != 0);
        boolean fileChanged = fileChanged(timestamp, hash, utilityTag, productive);

        if (segmented || discontinuous || differentHash || fileChanged) {

            flush( fileChanged );

            working.reset(timestamp, debug_name, hash, utilityTag, productive);

            if(fileChanged){
                try {
                    lastFile.close();
                } catch (IOException e) {
                    Debug.log(e);
                }
                lastFile = getRecordFile(timestamp, hash, utilityTag, productive);
            }
        }

        working.update(timestampMilli);
    }

    /**
     * Logs a virtual data point within a record entry.
     *
     * @param timestampMilli time of the virtual data point
     * @param p the project or tag
     */
    protected final void log(long timestampMilli, Project p){
        if(p==null){
            log(timestampMilli, DEBUG_OTHER, -1, false, false);
        } else {
            log(timestampMilli, p.getName(), p.getHash(), p.isUtilityTag(), p.isProductive());
        }
    }

    /**
     * Flushes the current record entry into the buffer and attempts to flush the buffer into disk.
     *
     * @param forced whether to force flushing the buffer into disk
     */
    private void flush(boolean forced){
        final int recordElapsed = (getGranularityMillisInternal() + working.getDuration())/1000;
        final Instant recordTime = working.timestamp;
        final String debug_name = working.debug_name;

        Debug.logVerbose(()->String.format("[Buffer] Writing record entry for: %s\n%8s %s, %s", debug_name,
                "", FormatUtil.DTF_FULL.format(recordTime), recordElapsed));

        buffer.put(RecordEntry.serialize(recordTime, recordElapsed, working.hash, working.utilityTag, working.productive));

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
            Debug.logImportant("[Buffer] No file to write to");
            return;
        }
        lastFlushTimestamp = System.currentTimeMillis();
        Debug.log("[Buffer] Flushing record");
        try {
            buffer.flush(lastFile);
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
        if(working.timestamp != null) {
            flush(true);
        }
        if(lastFile!=null){
            lastFile.close();
        }
    }
}
