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

import java.awt.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import xyz.spiralhalo.sherlock.report.factory.table.AllReportRows;
import xyz.spiralhalo.sherlock.util.FormatUtil;

public class AllModel extends AbstractTableModel {
	private static final String[] columnName = new String[]{"Priority", "Project", "Category", "Type", "Start", "Finish", "Days worked", "Time spent"};
	private static final String[] columnNameUtility = new String[]{"Priority", "Activity", "Category", "Type", "Time spent"};

	private AllReportRows data;
	private final boolean utility;

	public AllModel(AllReportRows data) {
		this(data, false);
	}

	public AllModel(AllReportRows data, boolean utility) {
		this.data = data;
		this.utility = utility;
	}

	public void reset(AllReportRows data) {
		this.data = data;
		fireTableDataChanged();
	}

	public void setTableColumnWidths(JTable table) {
		table.getColumnModel().getColumn(0).setPreferredWidth(24);
		table.getColumnModel().getColumn(0).setMaxWidth(60);
		table.getColumnModel().getColumn(1).setPreferredWidth(130);
		table.getColumnModel().getColumn(2).setPreferredWidth(80);
		table.getColumnModel().getColumn(3).setPreferredWidth(75);
		table.getColumnModel().getColumn(3).setMaxWidth(80);
		if (table.getColumnModel().getColumnCount() > 5) {
			table.getColumnModel().getColumn(4).setPreferredWidth(62);
			table.getColumnModel().getColumn(5).setPreferredWidth(62);
			table.getColumnModel().getColumn(6).setPreferredWidth(60);
			table.getColumnModel().getColumn(7).setPreferredWidth(80);
		}
	}

	public long getProjectHash(int rowIndex) {
		return data.get(rowIndex).getProjectHash();
	}

	public Color getColor(int rowIndex) {
		return new Color(data.get(rowIndex).getProjectColor());
	}

	@Override
	public String getColumnName(int column) {
		return utility ? columnNameUtility[column] : columnName[column];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (utility) {
			if (columnIndex == 4) return Integer.class;
		} else {
			if (columnIndex == 6) return Double.class;
			if (columnIndex == 7) return Integer.class;
		}
		return String.class;
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return utility ? columnNameUtility.length : columnName.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
			case 0:
				return String.format("%s", rowIndex + 1);
			case 1:
				return data.get(rowIndex).getProjectName();
			case 2:
				return data.get(rowIndex).getCategory();
			case 3:
				return data.get(rowIndex).getPTypeLabel();
		}
		if (!utility) {
			switch (columnIndex) {
				case 4:
					return data.get(rowIndex).getStartDate().format(FormatUtil.DTF_YMD);
				case 5:
					return data.get(rowIndex).getFinishDate() == null ? "ongoing" : data.get(rowIndex).getFinishDate().format(FormatUtil.DTF_YMD);
				case 6:
					return data.get(rowIndex).getDays();
			}
		}
		// default
		return data.get(rowIndex).getSeconds();
	}

	public int findIndex(long hash) {
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).getProjectHash() == hash) {
				return i;
			}
		}
		return -1;
	}
}
