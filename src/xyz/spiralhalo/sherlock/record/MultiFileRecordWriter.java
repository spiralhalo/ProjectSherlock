package xyz.spiralhalo.sherlock.record;

import xyz.spiralhalo.sherlock.util.Debug;
import xyz.spiralhalo.sherlock.util.FormatUtil;
import xyz.spiralhalo.sherlock.Application;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;

public abstract class MultiFileRecordWriter extends AbstractRecordWriter{
    private RecordFileAppend recordFile;
    private String workingFilename;

    protected MultiFileRecordWriter(int numOfRecordCapacity) {
        super(numOfRecordCapacity);
    }

    protected RecordFileAppend getRecordFile(TempRecord forRecord){
        YearMonth recordYM = YearMonth.from(Instant.ofEpochMilli(forRecord.getTimestamp()).atZone(ZoneId.systemDefault()));
        workingFilename = String.format("%s.record", FormatUtil.DTF_YM.format(recordYM));
        if(recordFile ==null || !recordFile.filename().equals(workingFilename)){
            Debug.log(String.format("Opening new record file: %s, old record file: %s", workingFilename, String.valueOf(recordFile)));
            createFile();
        }
        return recordFile;
    }

    private void createFile() {
        File file = new File(Application.getRecordDir(), workingFilename);
        try {
            recordFile = new RecordFileAppend(file);
        } catch (IOException e) {
            Debug.log(e);
        }
    }

}
