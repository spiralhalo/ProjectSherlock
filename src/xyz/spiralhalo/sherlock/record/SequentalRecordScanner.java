package xyz.spiralhalo.sherlock.record;

import xyz.spiralhalo.sherlock.util.Debug;
import xyz.spiralhalo.sherlock.util.FormatUtil;
import xyz.spiralhalo.sherlock.util.PathUtil;

import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class SequentalRecordScanner implements Iterator<RecordData>, AutoCloseable {
    private final ArrayList<File> files = new ArrayList<>();
    private final LocalDate from;
    private final LocalDate until;

    private int filepos = 0;
    private RandomAccessFile raf;
    private RecordData internalData;

    public SequentalRecordScanner(){
        this(null, null);
    }

    public SequentalRecordScanner(LocalDate from, LocalDate until) {
        this.from = from;
        this.until = until;
        File[] records = new File(PathUtil.getRecordDir()).listFiles();
        if (records != null) {
            Arrays.sort(records);
            for (File f : records) {
                try {
                    YearMonth fDate = YearMonth.parse(f.getName().split("[.]")[0], FormatUtil.DTF_YM);
                    if((from == null || !fDate.isBefore(YearMonth.from(from)))
                            && (until == null || !fDate.isAfter(YearMonth.from(until)))){
                        files.add(f);
                    }
                } catch (DateTimeParseException e) {
                    Debug.log(e);
                }
            }
            if(files.size() != 0){
                createRaf();
            }
        }
    }

    private void createRaf() {
        if(filepos >= files.size()) return;
        try {
            raf = new RandomAccessFile(files.get(filepos), "r");
        } catch (FileNotFoundException e) {
            Debug.log(e);
        }
    }

    private void nextFile() {
        raf = null;
        filepos ++;
        createRaf();
    }

    private RecordData getData(){
        if(internalData == null && raf != null){
            byte[] raw = null;
            boolean eof = false;
            try {
                raw = DiskOutput.read(raf);
            } catch (EOFException e){
                Debug.log(e);
                nextFile();
                eof = true;
            } catch (IOException ignored) {
            }
            if(!eof) {
                try {
                    if (raf.getFilePointer() >= raf.length()) {
                        raf.close();
                        nextFile();
                    }
                } catch (IOException e) {
                    Debug.log(e);
                    nextFile();
                }
            }
            if(raw != null){
                internalData = RecordData.deserialize(raw);
                LocalDate date = internalData.getTime().atZone(ZoneId.systemDefault()).toLocalDate();
                if((from != null && date.isBefore(from)) || (until != null && date.isAfter(until))){
                    internalData = null;
                }
            }
        }
        return internalData;
    }

    private void resetData(){
        internalData = null;
    }

    @Override
    public boolean hasNext() {
        RecordData data = getData();
        return data != null;
    }

    @Override
    public RecordData next() {
        RecordData data = getData();
        resetData();
        return data;
    }

    @Override
    public void close() throws Exception {
        if(raf!=null){
            raf.close();
        }
    }
}
