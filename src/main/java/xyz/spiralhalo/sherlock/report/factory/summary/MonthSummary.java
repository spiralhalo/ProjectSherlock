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

import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import xyz.spiralhalo.sherlock.persist.cache.Cache;
import xyz.spiralhalo.sherlock.report.factory.ReportCache;
import xyz.spiralhalo.sherlock.report.factory.charts.ChartData;
import xyz.spiralhalo.sherlock.util.FormatUtil;
import xyz.spiralhalo.sherlock.util.MurmurHash;

@Cache
public class MonthSummary extends ArrayList<SummaryEntry> implements ReportCache, Serializable {
	public static String cacheId(YearMonth month, ZoneId z) {
		return String.format("summary_%s_%s", FormatUtil.DTF_YM.format(month), Integer.toHexString(MurmurHash.hash32(z.getId())));
	}

	public static final long serialVersionUID = 1L;
	private final YearMonth month;
	private final MonthDetails details;
	private final ChartData monthChart;
	private final ArrayList<LocalDate> dayList;
	private final HashMap<LocalDate, ChartData> dayCharts;
	private final HashMap<Long, Integer> index;
	private final boolean complete;

	public MonthSummary(YearMonth month, ChartData monthChart, ArrayList<LocalDate> dayList, HashMap<LocalDate, ChartData> dayCharts, boolean complete) {
		this.month = month;
		this.details = new MonthDetails();
		this.monthChart = monthChart;
		this.dayList = dayList;
		this.dayCharts = dayCharts;
		this.complete = complete;
		this.index = new HashMap<>();
	}

	@Override
	public boolean add(SummaryEntry summaryEntry) {
		if (super.add(summaryEntry)) {
			int pos = size() - 1;
			long hash = summaryEntry.getHash();
			index.putIfAbsent(hash, pos);
			return true;
		} else return false;
	}

	@Override
	public boolean addAll(Collection<? extends SummaryEntry> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, SummaryEntry element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends SummaryEntry> c) {
		throw new UnsupportedOperationException();
	}

	public HashMap<Long, Integer> getIndex() {
		return index;
	}

	public ChartData getMonthChart() {
		return monthChart;
	}

	public ArrayList<LocalDate> getDayList() {
		return dayList;
	}

	public HashMap<LocalDate, ChartData> getDayCharts() {
		return dayCharts;
	}

	@Override
	public boolean isComplete() {
		return complete;
	}

	public YearMonth getMonth() {
		return month;
	}

	public MonthDetails getDetails() {
		return details;
	}
}
