package xyz.spiralhalo.sherlock.report;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

import static xyz.spiralhalo.sherlock.util.ColorUtil.foreground;
import static xyz.spiralhalo.sherlock.util.ColorUtil.interpolateNicely;
import static xyz.spiralhalo.sherlock.util.ColorUtil.multiply;

public class DayDurationCell extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if(table.getModel() instanceof AllModel){
            int i = table.convertRowIndexToModel(row);
            AllModel x = (AllModel) table.getModel();
            super.setForeground((isSelected?interpolateNicely(0.5f,x.getColor(i),Color.white):foreground));
            super.setBackground(isSelected?multiply(Color.gray,x.getColor(i)):x.getColor(i));
        }
        this.setText(String.format("%d days", (int)value));
        return this;
    }
}