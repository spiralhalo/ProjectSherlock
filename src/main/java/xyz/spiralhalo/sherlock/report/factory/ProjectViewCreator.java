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

package xyz.spiralhalo.sherlock.report.factory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.async.AsyncTask;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.record.DefaultRecordWriter;
import xyz.spiralhalo.sherlock.record.RecordEntry;
import xyz.spiralhalo.sherlock.record.RecordScanner;
import xyz.spiralhalo.sherlock.record.io.RecordFileSeek;
import xyz.spiralhalo.sherlock.report.factory.table.ReportRow;
import xyz.spiralhalo.sherlock.report.factory.table.ReportRows;

public class ProjectViewCreator extends AsyncTask<ProjectViewResult> {
	private static final int MINIMUM_SECOND = 0; //= 5*60; //disabled until a less confusing approach is found
	private ProjectViewResult result;
	private final Project p;

	public ProjectViewCreator(Project p) {
		this.p = p;
	}

	@Override
	public void doRun() throws Exception {
		File recordFile = new File(Application.getSaveDir(), DefaultRecordWriter.RECORD_FILE);
		RecordFileSeek seeker;
		try {
			seeker = new RecordFileSeek(recordFile, false);
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException("No record file.");
		}
		try {
			seeker.seekFirstOfDay(p.getStartDateTime().toLocalDate(), ZoneId.systemDefault());
		} catch (IOException e) {
			throw e;
		}
		try (RecordScanner sc = new RecordScanner(seeker)) {
			RecordEntry temp;
			ZonedDateTime c2;
			LocalDate cd = null, cm = null;
			int accuS = 0;
			int accuSM = 0;
			final ReportRows dayRows = new ReportRows();
			final ReportRows monthRows = new ReportRows();
			final LocalDate today = LocalDate.now();
			while (sc.hasNext()) {
				try {
					temp = sc.next();
					c2 = temp.getTime().atZone(ZoneId.systemDefault());
					if (p.isFinished() && c2.isAfter(p.getFinishedDate())) {
						break;
					}
					if (p.getHash() != temp.getHash()) continue;
					if (cd == null) cd = c2.toLocalDate();
					if (cm == null) cm = c2.toLocalDate();
					if (c2.toLocalDate().isAfter(cd)) {
						if (accuS >= MINIMUM_SECOND || cd.equals(today)) {
							dayRows.add(new ReportRow(cd, accuS));
							accuSM += accuS;
						}
						cd = c2.toLocalDate();
						accuS = 0;
					}
					if (cm.get(ChronoField.MONTH_OF_YEAR) != c2.get(ChronoField.MONTH_OF_YEAR)
							|| cm.get(ChronoField.YEAR) != c2.get(ChronoField.YEAR)) {
						if (accuSM >= MINIMUM_SECOND) {
							monthRows.add(new ReportRow(cm, accuSM));
						}
						cm = c2.toLocalDate();
						accuSM = 0;
					}
					accuS += temp.getElapsed();
				} catch (NumberFormatException e) {
					Debug.log(e);
				}
			}
			if (accuS >= MINIMUM_SECOND) {
				dayRows.add(new ReportRow(cd, accuS));
			}
			if (accuSM >= MINIMUM_SECOND) {
				monthRows.add(new ReportRow(cm, accuSM));
			}
			result = new ProjectViewResult(p, dayRows, monthRows);
		}
	}

	@Override
	protected ProjectViewResult getResult() {
		return result;
	}
}
