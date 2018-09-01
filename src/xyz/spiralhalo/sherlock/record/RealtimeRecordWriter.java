package xyz.spiralhalo.sherlock.record;

import xyz.spiralhalo.sherlock.Tracker;
import xyz.spiralhalo.sherlock.persist.project.Project;

import java.time.YearMonth;

public class RealtimeRecordWriter extends MultiFileRecordWriter {
    private static final int RECORD_CAPACITY = 50;

    public RealtimeRecordWriter() {
        super(RECORD_CAPACITY);
    }

    public void log(long delay, Project p) {
        super.setWorkingYM(YearMonth.now());
        super.logInternal(System.currentTimeMillis(), delay, p);
    }

    @Override
    protected int getMarginOfError() {
        return Tracker.TIMER_DELAY_MILLIS / 2;
    }
}
