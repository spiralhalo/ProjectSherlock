//
//    Copyright 2020 spiralhalo <re.nanashi95@gmail.com>
//
//    This file is part of Project Sherlock.
//
//    Project Sherlock is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Project Sherlock is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Project Sherlock.  If not, see <https://www.gnu.org/licenses/>.
//

package xyz.spiralhalo.sherlock.record.io;

import xyz.spiralhalo.sherlock.record.RecordEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RecordFile implements AutoCloseable{
    protected static final int LENGTH = RecordEntry.BYTES_UNIVERSAL;
    protected final File file;
    private final RandomAccessFile raf;
    private long pointerPos;

    public RecordFile(File file, String mode) throws FileNotFoundException {
        this.file = file;
        this.raf = new RandomAccessFile(file, mode);
    }

    public boolean eof() throws IOException {
        return pointerPos == rafLength();
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
    public String toString() {
        return file.getName();
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }
}
