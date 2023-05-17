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

package xyz.spiralhalo.sherlock.report;

import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;

import xyz.spiralhalo.sherlock.util.FormatUtil;

public class DateSelection<T extends Temporal> {
	public final T date;
	private final DateTimeFormatter formatter;

	public DateSelection(T date) {
		this(date, FormatUtil.DTF_DATE_SELECTOR);
	}

	public DateSelection(T date, DateTimeFormatter formatter) {
		this.date = date;
		this.formatter = formatter;
	}

	@Override
	public String toString() {
		return formatter.format(date);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DateSelection) {
			return ((DateSelection) obj).date.equals(date);
		}
		return false;
	}
}
