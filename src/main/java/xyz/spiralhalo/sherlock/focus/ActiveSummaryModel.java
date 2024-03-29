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

package xyz.spiralhalo.sherlock.focus;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import xyz.spiralhalo.sherlock.persist.project.Project;

public class ActiveSummaryModel extends AbstractTableModel {
	private final Project[] list;

	public ActiveSummaryModel(List<? extends Project> activeProjects) {
		list = new Project[activeProjects.size()];
		for (int i = 0; i < list.length; i++) {
			list[i] = activeProjects.get(i);
		}
	}

	@Override
	public String getColumnName(int column) {
		return "Project";
	}

	@Override
	public int getRowCount() {
		return list.length;
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return String.valueOf(list[rowIndex]);
	}

	public Project getProject(int rowIndex) {
		if (rowIndex < 0 || rowIndex >= list.length) return null;
		return list[rowIndex];
	}
}
