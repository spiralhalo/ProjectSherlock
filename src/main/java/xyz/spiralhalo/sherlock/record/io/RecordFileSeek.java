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

import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;
import xyz.spiralhalo.sherlock.record.RecordEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.*;

public class RecordFileSeek extends RecordFileRW {

    private Instant currentTimestamp;
    private CacheMgr cache;

    public RecordFileSeek(File file, boolean writeable) throws FileNotFoundException {
        super(file, writeable);
    }

    public RecordFileSeek(File file, boolean writeable, CacheMgr cache) throws FileNotFoundException {
        super(file, writeable);
        this.cache = cache;
    }

    public boolean seek(Instant timestamp) throws IOException {
        reset();
        long min = 0, max = rafLength();
        while (!timestamp.equals(currentTimestamp)){
            if(min >= max) return false;
            if(timestamp.isAfter(currentTimestamp)){
                min = getPointerPos() + LENGTH;
            } else if(timestamp.isBefore(currentTimestamp)){
                max = getPointerPos();
            }
            jump(min, max);
        }
        return true;
    }

    public boolean seekLocal(ZonedDateTime zdt) throws IOException {
        reset();
        long min = 0, max = rafLength();
        ZoneId z = zdt.getZone();
        while (!zdt.equals(currentTimestamp.atZone(z))){
            if(min >= max) return false;
            if(zdt.isAfter(currentTimestamp.atZone(z))){
                min = getPointerPos() + LENGTH;
            } else if(zdt.isBefore(currentTimestamp.atZone(z))){
                max = getPointerPos();
            }
            jump(min, max);
        }
        return true;
    }

    public boolean seekFirstOfDay(LocalDate day, ZoneId z) throws IOException {
        reset();
        long min = 0, max = rafLength();
        checkIndex(YearMonth.from(day), z);
        while (!day.isEqual(currentTimestamp.atZone(z).toLocalDate())){
            if(min >= max) return false;
            if(day.isAfter(currentTimestamp.atZone(z).toLocalDate())){
                min = getPointerPos() + LENGTH;
            } else if(day.isBefore(currentTimestamp.atZone(z).toLocalDate())){
                max = getPointerPos();
            }
            jump(min, max);
        }
        while (day.isEqual(currentTimestamp.atZone(z).toLocalDate()) && getPointerPos() > 0){
            jump(- 1);
        }
        if(!day.isEqual(currentTimestamp.atZone(z).toLocalDate())){
            jump(1);
        }
        return true;
    }

    public boolean seekFirstOfMonth(YearMonth ym, ZoneId z) throws IOException {
        reset();
        long min = 0, max = rafLength();
        checkIndex(ym, z);
        while (!ym.equals(YearMonth.from(currentTimestamp.atZone(z)))){
            if(min >= max) return false;
            if(ym.isAfter(YearMonth.from(currentTimestamp.atZone(z)))){
                min = getPointerPos() + LENGTH;
            } else if(ym.isBefore(YearMonth.from(currentTimestamp.atZone(z)))){
                max = getPointerPos();
            }
            jump(min, max);
        }
        while (ym.equals(YearMonth.from(currentTimestamp.atZone(z))) && getPointerPos() > 0){
            jump(- 1);
        }
        if(!ym.equals(YearMonth.from(currentTimestamp.atZone(z)))){
            jump(1);
        }
        writeIndex(ym, z, getPointerPos());
        return true;
    }

    private void checkIndex(YearMonth month, ZoneId z) throws IOException {
        if (cache == null) return;
        final MonthIndex index = cache.getObj(MonthIndex.cacheId(z), MonthIndex.class);
        if (index != null && index.containsKey(month)) {
            long lastPos = getPointerPos();
            jumpUnsafe(index.get(month));
            if(!month.equals(YearMonth.from(currentTimestamp.atZone(z)))){
                jumpUnsafe(lastPos);
            }
        }
    }

    private void writeIndex(YearMonth month, ZoneId z, long pos) {
        if (cache == null) return;
        MonthIndex index = cache.getObj(MonthIndex.cacheId(z), MonthIndex.class);
        if(index == null) index = new MonthIndex();
        index.put(month, pos);
        cache.put(MonthIndex.cacheId(z), index);
    }

    private void jumpUnsafe(long destination) throws IOException {
        long realDest = destination - (destination % LENGTH);
        if(realDest == getPointerPos() || realDest < 0 || realDest > rafLength() - LENGTH) return;
        rafSeek(realDest);
        currentTimestamp = queryTimestamp();
    }

    private void jump(long min, long max) throws IOException {
        if(min >= max) return;
        long destination = (min + (max - min) / 2);
        rafSeek(destination - (destination % LENGTH));
        currentTimestamp = queryTimestamp();
    }

    private void jump(long numEntry) throws IOException {
        long destination = getPointerPos() + numEntry * LENGTH;
        if(destination < 0 || destination > rafLength() - LENGTH) return;
        rafSeek(destination);
        currentTimestamp = queryTimestamp();
    }

//    private boolean jumpForward(long max) throws IOException {
//        if(getPointerPos() < max){
//            long numEntriesBetween = (max - getPointerPos()) / LENGTH;
//            jump(numEntriesBetween / 2 + (numEntriesBetween % 2));
//            return true;
//        }
//        return false;
//    }
//
//    private boolean jumpBackward(long min) throws IOException {
//        if(getPointerPos() >= LENGTH){
//            long numEntriesBetween = (getPointerPos() - min) / LENGTH;
//            jump(-(numEntriesBetween / 2 + (numEntriesBetween % 2)));
//            return true;
//        }
//        return false;
//    }

    private void reset() throws IOException{
        if(currentTimestamp == null){
            currentTimestamp = queryTimestamp();
        }
    }

    private Instant queryTimestamp() throws IOException {
        Instant timestamp = RecordEntry.getTimestamp(readBytes());
        backOff();
        return timestamp;
    }

    public Instant getCurrentTimestamp() throws IOException {
        reset();
        return currentTimestamp;
    }

    @Override
    protected boolean backOff() throws IOException {
        currentTimestamp = null;
        return super.backOff();
    }

    @Override
    protected byte[] readBytes() throws IOException {
        currentTimestamp = null;
        return super.readBytes();
    }

    @Override
    public void writeBytes(byte[] bytes) throws IOException {
        currentTimestamp = null;
        super.writeBytes(bytes);
    }
}
