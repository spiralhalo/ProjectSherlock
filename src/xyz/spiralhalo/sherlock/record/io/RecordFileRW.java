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
        if(bytes.length % LENGTH != 0) throw new IllegalArgumentException("Invalid data type.");
        rafWrite(bytes);
    }
}
