package xyz.spiralhalo.sherlock.record.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A wrapper for {@link RandomAccessFile} with read/write mode that must point to the end of file upon creation.
 */
public class RecordFileAppend extends RecordFile{
    public RecordFileAppend(File file) throws IOException {
        super(file, "rw");
        rafSeek(rafLength());
    }

    public void writeBytes(byte[] bytes) throws IOException {
        if(bytes.length % LENGTH != 0) throw new IllegalArgumentException("Invalid data type.");
        rafWrite(bytes);
    }
}
