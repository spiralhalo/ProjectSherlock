package xyz.spiralhalo.sherlock.record;

import xyz.spiralhalo.sherlock.Tracker;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig.AppInt;

public class RealtimeRecordWriter extends DefaultRecordWriter {
    private static final int RECORD_CAPACITY = 100;

    public RealtimeRecordWriter() {
        super(RECORD_CAPACITY);
    }

    public void log(Project p) {
        super.log(System.currentTimeMillis(), p);
    }

    @Override
    protected int getGranularityMillis() {
        return Tracker.TIMER_DELAY_MILLIS;
    }

    @Override
    protected int getEnforcedFlushDelayMillis() {
        return Math.min(super.getEnforcedFlushDelayMillis(), AppConfig.appGInt(AppInt.REFRESH_TIMEOUT) * 1000);
    }
}
