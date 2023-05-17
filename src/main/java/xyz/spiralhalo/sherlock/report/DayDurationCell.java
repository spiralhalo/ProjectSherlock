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

import static xyz.spiralhalo.sherlock.util.ColorUtil.foreground;
import static xyz.spiralhalo.sherlock.util.ColorUtil.interpolateNicely;
import static xyz.spiralhalo.sherlock.util.ColorUtil.multiply;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class DayDurationCell extends DefaultTableCellRenderer {
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (table.getModel() instanceof AllModel) {
			int i = table.convertRowIndexToModel(row);
			AllModel x = (AllModel) table.getModel();
			super.setForeground((isSelected ? interpolateNicely(0.5f, x.getColor(i), Color.white) : foreground));
			super.setBackground(isSelected ? multiply(Color.gray, x.getColor(i)) : x.getColor(i));
		}
		this.setText(String.format("%d days", (int) value));
		return this;
	}
}
