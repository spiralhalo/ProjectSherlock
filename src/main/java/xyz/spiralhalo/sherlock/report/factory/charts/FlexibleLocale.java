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

package xyz.spiralhalo.sherlock.report.factory.charts;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

import xyz.spiralhalo.sherlock.util.FormatUtil;

public class FlexibleLocale implements Comparable, Serializable {
	public static final long serialVersionUID = 1L;
	private final Comparable toCompare;

	public FlexibleLocale(Comparable toCompare) {
		this.toCompare = toCompare;
	}

	@Override
	public int compareTo(Object o) {
		return toCompare.compareTo(o);
	}

	@Override
	public boolean equals(Object obj) {
		return hashCode() == obj.hashCode();
	}

	@Override
	public int hashCode() {
		return (toCompare.hashCode() << 1) | 1;
	}

	@Override
	public String toString() {
		if (toCompare instanceof LocalDate) {
			return FormatUtil.DTF_MONTH_CHART.format((LocalDate) toCompare);
		} else if (toCompare instanceof YearMonth) {
			return ((YearMonth) toCompare).getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());
		}
		return String.valueOf(toCompare);
	}

	public Comparable getToCompare() {
		return toCompare;
	}
}
