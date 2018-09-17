package xyz.spiralhalo.sherlock.record;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.record.io.RecordFileAppend;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

public class DefaultRecordWriter extends AbstractRecordWriter {
    public static final String RECORD_FILE = "record.sherlock";
    private RecordFileAppend rfa;

    /**
     * Creates a new {@link DefaultRecordWriter}
     *
     * @param nRecordCapacity number of record entries the buffer can hold at most
     */
    protected DefaultRecordWriter(int nRecordCapacity) {
        super(nRecordCapacity);
    }

    @Override
    protected int getGranularityMillis() {
        return 1000;
    }

    @Override
    protected RecordFileAppend getRecordFile(Instant timestamp, long hash, boolean utilityTag, boolean productive) {
        if(rfa == null) {
            try {
                rfa = new RecordFileAppend(new File(Application.getSaveDir(), RECORD_FILE));
            } catch (IOException e) {
                Debug.log(e);
            }
        }
        return rfa;
    }

    @Override
    protected void onClosing() { }
}
