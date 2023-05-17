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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.record.DefaultRecordWriter;
import xyz.spiralhalo.sherlock.record.RecordScanner;
import xyz.spiralhalo.sherlock.record.io.RecordFileSeek;
import xyz.spiralhalo.sherlock.report.factory.charts.ChartBuilder;
import xyz.spiralhalo.sherlock.report.factory.summary.MonthSummary;
import xyz.spiralhalo.sherlock.report.factory.summary.SummaryBuilder;
import xyz.spiralhalo.sherlock.report.factory.summary.SummaryEntry;
import xyz.spiralhalo.sherlock.report.factory.summary.YearList;
import xyz.spiralhalo.sherlock.report.factory.summary.YearSummary;
import xyz.spiralhalo.sherlock.report.factory.table.AllReportRow;
import xyz.spiralhalo.sherlock.report.factory.table.AllReportRows;
import xyz.spiralhalo.sherlock.util.ListUtil;

public class ReportOnRefresh {
	public static boolean refreshReports(boolean forceReconstruct, boolean forceDelete, CacheMgr cache, ZoneId z, ProjectList projectList) {
		if (forceDelete) {
			cache.forceDiskCacheCleanup();
		}
		File recordFile = new File(Application.getSaveDir(), DefaultRecordWriter.RECORD_FILE);
		try (RecordFileSeek seeker = new RecordFileSeek(recordFile, false, cache)) {
			final LocalDateTime earliest = seeker.getCurrentTimestamp().atZone(z).toLocalDateTime();
			final int minYear = Year.from(earliest).getValue();
			int maxYear = minYear;
			YearMonth ym = YearMonth.from(earliest);
			// create missing MonthSummary by reading the record
			while (!seeker.eof()) {
				if (missingMonthSummary(ym, forceReconstruct, cache, z)) {
					SummaryBuilder summaryBuilder = new SummaryBuilder(ym, z, ym.isBefore(YearMonth.now(z)));
					RecordScanner scanner = new RecordScanner(seeker, ym);
					while (scanner.hasNext()) {
						summaryBuilder.readRecord(scanner.next());
					}
					cache.put(MonthSummary.cacheId(ym, z), summaryBuilder.finish(projectList));
				}
				maxYear = ym.getYear();
				ym = ym.plusMonths(1);
			}
			// create missing YearSummary by reading existing MonthSummary
			YearList yearList = new YearList();
			for (int year = minYear; year <= maxYear; year++) {
				final Year y = Year.of(year);
				yearList.add(y);
				if (missingYearSummary(y, forceReconstruct, cache, z)) {
					final ArrayList<YearMonth> months = new ArrayList<>();
					final ChartBuilder<Year> chartBuilder = new ChartBuilder<>(y, z, false);
					for (int month = 1; month <= 12; month++) {
						ym = YearMonth.of(year, month);
						MonthSummary x = cache.getObj(MonthSummary.cacheId(ym, z), MonthSummary.class);
						if (x != null) {
							months.add(ym);
							for (SummaryEntry entry : x) {
								chartBuilder.readSummary(entry);
							}
						}
					}
					cache.put(YearSummary.cacheId(y, z), new YearSummary(y, chartBuilder.finish(projectList), months, y.isBefore(Year.now())));
				}
			}
			cache.put(YearList.cacheId(z), yearList);
		} catch (Exception e) {
			Debug.log(e);
		}

		AllReportRows activeRows = new AllReportRows();
		AllReportRows finishedRows = new AllReportRows();
		AllReportRows utilityRows = new AllReportRows();
		Iterable<Project> projects = ListUtil.extensiveIterator(projectList.getActiveProjects(), projectList.getFinishedProjects(), projectList.getUtilityTags());
		for (Project p : projects) {
			ZonedDateTime startDate = p.getStartDateTime().withZoneSameInstant(z);
			YearMonth ym = YearMonth.from(startDate);
			int seconds = 0;
			int day = 0;
			LocalDate lastDay = null;
			YearMonth now = YearMonth.now(z), end;
			if (!p.isUtilityTag() && p.isFinished()) {
				end = YearMonth.from(p.getFinishedDate().withZoneSameInstant(z));
			} else {
				end = null;
			}
			while (!ym.isAfter(now) && (end == null || !ym.isAfter(end))) {
				MonthSummary x = cache.getObj(MonthSummary.cacheId(ym, z), MonthSummary.class);
				if (x != null) {
					ArrayList<Integer> indices = x.getDetails().getIndices().get(p.getHash());
					if (indices != null) {
						for (int i : indices) {
							// TO*NOT*DO: add effective day filter //don't do until a less confusing approach is found
							seconds += x.getDetails().get(i).getSummary().getSeconds();
							day += 1;
							lastDay = x.getDetails().get(i).getDate();
						}
					}
				}
				ym = ym.plusMonths(1);
			}
			long lastWorkedOnMillis;
			long startDateMillis = startDate.toEpochSecond() * 1000;
			if (lastDay != null) {
				if (p.isFinished() && lastDay.isEqual(p.getFinishedDate().withZoneSameInstant(z).toLocalDate())) {
					lastWorkedOnMillis = p.getFinishedDate().withZoneSameInstant(z).toEpochSecond() * 1000;
				} else {
					lastWorkedOnMillis = Math.max(lastDay.atStartOfDay(z).toEpochSecond() * 1000, startDateMillis);
				}
			} else {
				lastWorkedOnMillis = startDateMillis;
			}
			AllReportRow x;
			if (p.isUtilityTag()) {
				x = new AllReportRow(p.getHash(), p.getColor(), p.getName(), p.getCategory(), p.getPtype(), day,
						seconds, lastWorkedOnMillis);
			} else {
				x = new AllReportRow(p.getHash(), p.getColor(), p.getName(), p.getCategory(), p.getPtype(),
						LocalDateTime.from(p.getStartDateTime()),
						(p.isFinished() ? LocalDateTime.from(p.getFinishedDate()) : null),
						day, seconds, lastWorkedOnMillis);
			}
			if (p.isUtilityTag()) {
				utilityRows.add(x);
			} else if (p.isFinished()) {
				finishedRows.add(x);
			} else {
				activeRows.add(x);
			}
		}
		cache.put(AllReportRows.activeCacheId(z), activeRows);
		cache.put(AllReportRows.finishedCacheId(z), finishedRows);
		cache.put(AllReportRows.utilityCacheId(z), utilityRows);
		return true;
	}

	private static boolean missingMonthSummary(YearMonth month, boolean forceReconstruct, CacheMgr cache, ZoneId z) {
		if (forceReconstruct) return true;
		MonthSummary x = cache.getObj(MonthSummary.cacheId(month, z), MonthSummary.class);
		if (x == null) {
			return true;
		} else {
			return !x.isComplete();
		}
	}

	private static boolean missingYearSummary(Year year, boolean forceReconstruct, CacheMgr cache, ZoneId z) {
		if (forceReconstruct) return true;
		YearSummary x = cache.getObj(YearSummary.cacheId(year, z), YearSummary.class);
		if (x == null) {
			return true;
		} else {
			return !x.isComplete();
		}
	}
}
