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

package xyz.spiralhalo.sherlock.record.legacy;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.record.DefaultRecordWriter;

public class ReconstructorWriter extends DefaultRecordWriter {
	private long lastTimestamp = 0;

	ReconstructorWriter() {
		super(10000);
	}

	void log(long timestamp, int elapsed, String debug_name, long hash, boolean utilityTag, boolean productive) {
		if (timestamp < lastTimestamp) return; // prevent erroneous or duplicate record
		lastTimestamp = timestamp;
		for (int i = 0; i < elapsed; i++) {
			super.log(timestamp + i * 1000, debug_name, hash, utilityTag, productive);
		}
	}

	void log(long timestamp, int elapsed, Project p) {
		if (timestamp < lastTimestamp) return; // prevent erroneous or duplicate record
		lastTimestamp = timestamp;
		for (int i = 0; i < elapsed; i++) {
			super.log(timestamp + i * 1000, p);
		}
	}
}
