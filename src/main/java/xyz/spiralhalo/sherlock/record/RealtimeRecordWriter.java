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

package xyz.spiralhalo.sherlock.record;

import xyz.spiralhalo.sherlock.GConst;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig.AppInt;

public class RealtimeRecordWriter extends DefaultRecordWriter {
    private static final int RECORD_CAPACITY = 100;

    public RealtimeRecordWriter() {
        super(RECORD_CAPACITY);
    }

    public void log(Project p) {
        super.log(System.currentTimeMillis(), p);
    }

    @Override
    protected int getGranularityMillis() {
        return GConst.TRACKER_DELAY_MILLIS;
    }

    @Override
    protected int getEnforcedFlushDelayMillis() {
        return Math.min(super.getEnforcedFlushDelayMillis(), AppConfig.appGInt(AppInt.REFRESH_TIMEOUT) * 1000);
    }
}
