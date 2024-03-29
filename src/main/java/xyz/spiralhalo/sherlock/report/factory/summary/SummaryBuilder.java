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

package xyz.spiralhalo.sherlock.report.factory.summary;

import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserBool.OLD_RATING;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserBool.USE_RANK_MONTH_CHART;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;

import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.record.RecordEntry;
import xyz.spiralhalo.sherlock.report.factory.charts.ChartBuilder;
import xyz.spiralhalo.sherlock.report.factory.charts.ChartData;
import xyz.spiralhalo.sherlock.report.factory.charts.RankChartBuilder;

// Builds a MonthSummary.
// MonthSummary contains all charts for the days in that month and the overall chart for that month.
public class SummaryBuilder {

	private static class DayMap extends HashMap<Long, Integer> {
	}

	private static class DayEarliestMap extends HashMap<Long, LocalDateTime> {
	}

	private static class DayChartBuilder extends ChartBuilder<LocalDate> {
		public DayChartBuilder(LocalDate date, ZoneId z, boolean inclTotal) {
			super(date, z, inclTotal);
		}
	}

	private final YearMonth month;
	private final DayMap[] days;
	private final DayEarliestMap[] dayEarliestMaps;
	private final HashMap<Long, Boolean> productiveMap;
	private final ZoneId z;
	private final boolean complete;
	private final ChartBuilder<YearMonth> monthChartBuilder;
	private final DayChartBuilder[] dayChartBuilders;

	public SummaryBuilder(YearMonth month, ZoneId z, boolean complete) {
		this.month = month;
		this.days = new DayMap[month.lengthOfMonth()];
		this.dayEarliestMaps = new DayEarliestMap[month.lengthOfMonth()];
		this.productiveMap = new HashMap<>();
		this.z = z;
		this.complete = complete;
		if (USE_RANK_MONTH_CHART.get() && !OLD_RATING.get()) {
			this.monthChartBuilder = new RankChartBuilder(month, z, false);
		} else {
			this.monthChartBuilder = new ChartBuilder<>(month, z, false);
		}
		this.dayChartBuilders = new DayChartBuilder[month.lengthOfMonth()];
	}

	public void readRecord(RecordEntry entry) {
		monthChartBuilder.readRecord(entry);
		LocalDateTime ldt = entry.getTime().atZone(z).toLocalDateTime();
		if (!YearMonth.from(ldt).equals(month)) return;
		int day = ldt.getDayOfMonth() - 1;
		getDayChartBuilder(day).readRecord(entry);
		DayMap map = getDayMap(day);
		map.put(entry.getHash(), map.getOrDefault(entry.getHash(), 0) + entry.getElapsed());
		productiveMap.putIfAbsent(entry.getHash(), entry.isProductive());
		getDayEarliestMap(day).putIfAbsent(entry.getHash(), ldt);
	}

	private DayMap getDayMap(int day) {
		if (days[day] == null) {
			days[day] = new DayMap();
		}
		return days[day];
	}

	private DayEarliestMap getDayEarliestMap(int day) {
		if (dayEarliestMaps[day] == null) {
			dayEarliestMaps[day] = new DayEarliestMap();
		}
		return dayEarliestMaps[day];
	}

	private DayChartBuilder getDayChartBuilder(int day) {
		if (dayChartBuilders[day] == null) {
			dayChartBuilders[day] = new DayChartBuilder(month.atDay(day + 1), z, false);
		}
		return dayChartBuilders[day];
	}

	public MonthSummary finish(ProjectList projectList) {
		final ArrayList<LocalDate> dayList = new ArrayList<>();
		final HashMap<LocalDate, ChartData> dayCharts = new HashMap<>();
		for (int i = 0; i < month.lengthOfMonth(); i++) {
			if (days[i] != null) {
				LocalDate date = month.atDay(i + 1);
				dayList.add(date);
				dayCharts.put(date, dayChartBuilders[i].finish(projectList));
			}
		}
		final MonthSummary summary = new MonthSummary(month, monthChartBuilder.finish(projectList), dayList, dayCharts, complete);
		final MonthDetails details = summary.getDetails();
		final HashMap<Long, Integer> monthTotal = new HashMap<>();
		final HashMap<Long, LocalDateTime> monthEarliest = new HashMap<>();
		for (int i = 0; i < month.lengthOfMonth(); i++) {
			if (days[i] == null) continue;
			for (Long l : days[i].keySet()) {
				monthTotal.put(l, monthTotal.getOrDefault(l, 0) + days[i].get(l));
				details.add(new DetailsRow(month.atDay(i + 1), new SummaryEntry(l, days[i].get(l), productiveMap.get(l), dayEarliestMaps[i].get(l))));
				monthEarliest.putIfAbsent(l, dayEarliestMaps[i].get(l));
			}
		}
		for (Long l : monthTotal.keySet()) {
			summary.add(new SummaryEntry(l, monthTotal.get(l), productiveMap.get(l), monthEarliest.get(l)));
		}
		return summary;
	}
}
