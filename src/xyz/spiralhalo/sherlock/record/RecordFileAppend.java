package xyz.spiralhalo.sherlock.record;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A wrapper for {@link RandomAccessFile} with read/write mode that must point to the end of file upon creation.
 */
public class RecordFileAppend {
    private final File file;
    private final RandomAccessFile raf;

    public RecordFileAppend(File file) throws IOException {
        this.file = file;
        this.raf = new RandomAccessFile(file, "rw");
        this.raf.seek(this.raf.length());
    }

    public String filename(){
        return file.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof RecordFileAppend) {
            return file.equals(((RecordFileAppend) obj).file);
        }
        return false;
    }

    public RandomAccessFile raf(){
        return raf;
    }

    @Override
    public String toString() {
        return file.getName();
    }

    public void close() throws IOException {
        raf.close();
    }
}
