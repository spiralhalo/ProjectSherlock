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

import xyz.spiralhalo.sherlock.persist.settings.UserConfig;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserInt;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserNode;
import xyz.spiralhalo.sherlock.util.FormatUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import static xyz.spiralhalo.sherlock.util.ColorUtil.*;

public class DurationCell extends DefaultTableCellRenderer {
    private boolean target;

    public DurationCell(boolean target) {
        super();
        this.target = target;
    }

    public DurationCell() {
        this(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if(table.getModel() instanceof AllModel){
            int i = table.convertRowIndexToModel(row);
            AllModel x = (AllModel) table.getModel();
            super.setForeground((isSelected?interpolateNicely(0.5f,x.getColor(i),Color.white):foreground));
            super.setBackground(isSelected?multiply(Color.gray,x.getColor(i)):x.getColor(i));
        } else if(target){
            Color baseColor;
            if(table.getModel() instanceof DayModel && !UserConfig.userGWDay(((DayModel) table.getModel()).getDay(row))){
                baseColor = light_gray;
            } else {
                baseColor = interpolateNicely(((float) (int) value) /
                        UserConfig.userGInt(UserNode.GENERAL, UserInt.DAILY_TARGET_SECOND), bad, neu, gut);
            }
            if(isSelected) {
                super.setBackground(multiply(super.getBackground(),baseColor));
//                super.setForeground(baseColor);
            } else {
                super.setForeground(foreground);
                super.setBackground(baseColor);
            }
        }
        super.setText(FormatUtil.hms((int)value));
        return this;
    }
}
