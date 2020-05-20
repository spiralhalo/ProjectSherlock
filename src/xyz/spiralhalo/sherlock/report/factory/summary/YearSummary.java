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

import xyz.spiralhalo.sherlock.persist.cache.Cache;
import xyz.spiralhalo.sherlock.report.factory.ReportCache;
import xyz.spiralhalo.sherlock.report.factory.charts.ChartData;
import xyz.spiralhalo.sherlock.util.MurmurHash;

import java.io.Serializable;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;

@Cache
public class YearSummary implements ReportCache, Serializable {
    public static String cacheId(Year year, ZoneId z){
        return String.format("year_summary_%s_%s", year.getValue(), Integer.toHexString(MurmurHash.hash32(z.getId())));
    }

    public static final long serialVersionUID = 1L;
    private final Year year;
    private final ChartData yearChart;
    private final ArrayList<YearMonth> monthList;
    private final boolean complete;

    public YearSummary(Year year, ChartData yearChart, ArrayList<YearMonth> monthList, boolean complete) {
        this.year = year;
        this.yearChart = yearChart;
        this.monthList = monthList;
        this.complete = complete;
    }

    public Year getYear() {
        return year;
    }

    public ChartData getYearChart() {
        return yearChart;
    }

    public ArrayList<YearMonth> getMonthList() {
        return monthList;
    }

    @Override
    public boolean isComplete() {
        return complete;
    }
}
