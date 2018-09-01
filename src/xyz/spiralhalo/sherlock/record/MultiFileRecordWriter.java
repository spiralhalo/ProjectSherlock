package xyz.spiralhalo.sherlock.record;

import xyz.spiralhalo.sherlock.util.Debug;
import xyz.spiralhalo.sherlock.util.FormatUtil;
import xyz.spiralhalo.sherlock.util.PathUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.YearMonth;

public abstract class MultiFileRecordWriter extends AbstractRecordWriter{
    private RandomAccessFile raf;
    private YearMonth lastYM;
    private YearMonth workingYM;

    protected MultiFileRecordWriter(int numOfRecordCapacity) {
        super(numOfRecordCapacity);
    }

    protected YearMonth getWorkingYM() {
        return workingYM;
    }

    protected void setWorkingYM(YearMonth workingYM) {
        this.workingYM = workingYM;
    }

    @Override
    protected RandomAccessFile getRafSeekLatest() {
        if(getWorkingYM()==null) throw new UnsupportedOperationException("Cannot access RAF before setting working date.");
        if(raf==null || lastYM.getMonthValue() != getWorkingYM().getMonthValue() || lastYM.getYear() != getWorkingYM().getYear()){
            createRafSeekLatest();
        }
        return raf;
    }

    private void createRafSeekLatest() {
        if(raf != null){
            try {
                raf.close();
            } catch (IOException e) {
                Debug.log(e);
            }
        }
        lastYM = getWorkingYM();
        File file = new File(PathUtil.getRecordDir(), String.format("%s.record", FormatUtil.DTF_YM.format(lastYM)));
        try {
            raf = new RandomAccessFile(file, "rw");
            raf.seek(raf.length());
        } catch (IOException e) {
            Debug.log(e);
        }
    }

    @Override
    protected void closeRAF() throws IOException {
        if(raf != null){
            raf.close();
        }
    }

}
