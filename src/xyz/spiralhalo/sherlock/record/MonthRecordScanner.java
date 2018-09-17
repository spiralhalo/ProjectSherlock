package xyz.spiralhalo.sherlock.record;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.record.io.RecordFileSeek;

import java.io.*;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Iterator;

public class MonthRecordScanner implements Iterator<RecordEntry>, AutoCloseable {
    private final File recordFile;
    private final RecordFileSeek seeker;
    private final YearMonth month;
    private final ZoneId z;
    private RecordEntry internalData;

    public MonthRecordScanner(YearMonth month) throws IOException {
        this.month = month;
        recordFile = new File(Application.getSaveDir(), DefaultRecordWriter.RECORD_FILE);
        if (!recordFile.exists()) {
            throw new FileNotFoundException();
        }
        seeker = new RecordFileSeek(recordFile, false);
        seeker.seekFirstOfMonth(month, z=ZoneId.systemDefault());
    }

    private RecordEntry getData(){
        if(internalData == null && seeker != null){
            try {
                internalData = seeker.read();
                if(!month.equals(YearMonth.from(internalData.getTime().atZone(z)))){
                    internalData = null;
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
    public void close() throws Exception {
        seeker.close();
    }
}
