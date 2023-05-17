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

import java.io.IOException;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Iterator;

import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.record.io.RecordFileSeek;

public class RecordScanner implements Iterator<RecordEntry>, AutoCloseable {
	private final RecordFileSeek seeker;
	private final YearMonth month;
	private final ZoneId z;
	private RecordEntry internalData;

	public RecordScanner(RecordFileSeek seeker) throws IOException {
		this(seeker, null);
	}

	public RecordScanner(RecordFileSeek seeker, YearMonth month) throws IOException {
		this.month = month;
		this.seeker = seeker;
		if (month != null) {
			seeker.seekFirstOfMonth(month, z = ZoneId.systemDefault());
		} else {
			z = null;
		}
	}

	private RecordEntry getData() {
		if (internalData == null && seeker != null) {
			try {
				if (!seeker.eof()) {
					if (month == null) {
						internalData = seeker.read();
					} else {
						Instant currentTimestamp = seeker.getCurrentTimestamp();
						if (month.equals(YearMonth.from(currentTimestamp.atZone(z)))) {
							internalData = seeker.read();
						}
					}
				}
			} catch (IOException e) {
				Debug.log(e);
			}
		}
		return internalData;
	}

	private void resetData() {
		internalData = null;
	}

	@Override
	public boolean hasNext() {
		RecordEntry data = getData();
		return data != null;
	}

	@Override
	public RecordEntry next() {
		RecordEntry data = getData();
		resetData();
		return data;
	}

	@Override
	public void close() throws IOException {
		seeker.close();
	}
}
