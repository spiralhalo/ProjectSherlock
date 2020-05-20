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
