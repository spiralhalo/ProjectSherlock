package xyz.spiralhalo.sherlock.record.io;

import xyz.spiralhalo.sherlock.record.RecordEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class RecordFileRW extends RecordFile {
    private boolean writeable;

    public RecordFileRW(File file, boolean writeable) throws FileNotFoundException {
        super(file, writeable?"rw":"r");
        this.writeable = writeable;
    }

    public RecordEntry read() throws IOException {
        return RecordEntry.deserialize(readBytes());
    }

    protected boolean backOff() throws IOException{
        if(getPointerPos() >= LENGTH){
            rafSeek(getPointerPos() - LENGTH);
            return true;
        }
        return false;
    }

    protected byte[] readBytes() throws IOException {
        byte[] readTo = new byte[LENGTH];
        rafRead(readTo);
        return readTo;
    }

    public void writeBytes(byte[] bytes) throws IOException {
        if(!writeable) throw new UnsupportedOperationException("Not writeable.");
        if(bytes.length != LENGTH) throw new IllegalArgumentException("Invalid data type.");
        rafWrite(bytes);
    }
}
