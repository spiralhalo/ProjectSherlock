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

import java.io.File;
import java.io.IOException;
import java.time.Instant;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.record.io.RecordFileAppend;

public class DefaultRecordWriter extends AbstractRecordWriter {
	public static final String RECORD_FILE = "record.sherlock";
	private RecordFileAppend rfa;

	/**
	 * Creates a new {@link DefaultRecordWriter}
	 *
	 * @param nRecordCapacity number of record entries the buffer can hold at most
	 */
	protected DefaultRecordWriter(int nRecordCapacity) {
		super(nRecordCapacity);
	}

	@Override
	protected int getGranularityMillis() {
		return 1000;
	}

	@Override
	protected RecordFileAppend getRecordFile(Instant timestamp, long hash, boolean utilityTag, boolean productive) {
		if (rfa == null) {
			File recordFile = new File(Application.getSaveDir(), RECORD_FILE);
			if (!recordFile.exists()) {
				try {
					recordFile.createNewFile();
				} catch (IOException e) {
					Debug.log(e);
				}
			}
			try {
				rfa = new RecordFileAppend(recordFile);
			} catch (IOException e) {
				Debug.log(e);
			}
		}
		return rfa;
	}

	@Override
	protected void onClosing() {
	}
}
