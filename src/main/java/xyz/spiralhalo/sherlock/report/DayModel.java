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

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

import javax.swing.table.AbstractTableModel;

import xyz.spiralhalo.sherlock.report.factory.table.ReportRows;

public class DayModel extends AbstractTableModel {
	private static final String[] columnName = new String[]{"Date", "Time spent"};
	private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd EEE").withZone(ZoneId.systemDefault());

	private final ReportRows data;

	public DayModel(ReportRows data) {
		this.data = data;
	}

	public int getDay(int rowIndex) {
		return data.get(rowIndex).getTimestamp().get(ChronoField.DAY_OF_WEEK);
	}

	@Override
	public String getColumnName(int column) {
		return columnName[column];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 1) return Integer.class;
		else return String.class;
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return columnName.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
			case 0:
				return DTF.format(data.get(rowIndex).getTimestamp());
			default:
				return data.get(rowIndex).getSeconds();
		}
	}
}
