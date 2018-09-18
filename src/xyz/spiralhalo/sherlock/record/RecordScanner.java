package xyz.spiralhalo.sherlock.record;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.record.io.RecordFileSeek;

import java.io.*;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Iterator;

public class RecordScanner implements Iterator<RecordEntry>, AutoCloseable {
    private final RecordFileSeek seeker;
    private final YearMonth month;
    private final ZoneId z;
    private RecordEntry internalData;

    public RecordScanner(RecordFileSeek seeker) throws IOException {
        this(seeker, null);
    }

    public RecordScanner(RecordFileSeek seeker, YearMonth month) throws IOException {
        this.month = month;
        this.seeker = seeker;
        if (month != null) {
            seeker.seekFirstOfMonth(month, z = ZoneId.systemDefault());
        } else { z = null; }
    }

    private RecordEntry getData(){
        if(internalData == null && seeker != null) {
            try {
                if(!seeker.eof()) {
                    if(month == null){
                        internalData = seeker.read();
                    } else {
                        Instant currentTimestamp = seeker.getCurrentTimestamp();
                        if (month.equals(YearMonth.from(currentTimestamp.atZone(z)))) {
                            internalData = seeker.read();
                        }
                    }
                }
            } catch (IOException e) {
                Debug.log(e);
            }
        }
        return internalData;
    }

    private void resetData(){
        internalData = null;
    }

    @Override
    public boolean hasNext() {
        RecordEntry data = getData();
        return data != null;
    }

    @Override
    public RecordEntry next() {
        RecordEntry data = getData();
        resetData();
        return data;
    }

    @Override
    public void close() throws IOException {
        seeker.close();
    }
}
