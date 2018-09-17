package xyz.spiralhalo.sherlock.record.io;

import xyz.spiralhalo.sherlock.record.RecordEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RecordFile {
    protected static final int LENGTH = RecordEntry.BYTES_UNIVERSAL;
    protected final File file;
    private final RandomAccessFile raf;
    private long pointerPos;

    public RecordFile(File file, String mode) throws FileNotFoundException {
        this.file = file;
        this.raf = new RandomAccessFile(file, mode);
    }

    protected void rafSeek(long destination) throws IOException {
        raf.seek(destination);
        pointerPos = destination;
    }

    protected void rafWrite(byte[] bytes) throws IOException {
        raf.write(bytes);
        pointerPos += bytes.length;
    }

    protected void rafRead(byte[] readTo) throws IOException {
        raf.readFully(readTo);
        pointerPos += readTo.length;
    }

    protected long rafLength() throws IOException {
        return raf.length();
    }

    protected long getPointerPos() {
        return pointerPos;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof RecordFileSeek) {
            return file.equals(((RecordFile) obj).file);
        }
        return false;
    }

    @Override
    public String toString() {
        return file.getName();
    }

    public void close() throws IOException {
        raf.close();
    }
}
