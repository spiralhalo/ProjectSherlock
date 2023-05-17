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

package xyz.spiralhalo.sherlock.persist.cache;

import java.time.LocalDate;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.util.FormatUtil;

public class CacheId {
	public static String ProjectDayRows(Project project) {
		return Long.toHexString(project.getHash()) + "_day_table";
	}

	public static String ProjectMonthRows(Project project) {
		return Long.toHexString(project.getHash()) + "_month_table";
	}

	public static String ChartData(String date) {
		return date + "_chart_data";
	}

	public static String ChartMeta(String date) {
		return date + "_chart_meta";
	}

	public static String ChartData(LocalDate date) {
		return ChartData(FormatUtil.DTF_YMD.format(date));
	}

	public static String ChartMeta(LocalDate date) {
		return ChartMeta(FormatUtil.DTF_YMD.format(date));
	}
}
