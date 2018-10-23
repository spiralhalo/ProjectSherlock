package xyz.spiralhalo.sherlock.focus;

import xyz.spiralhalo.sherlock.persist.project.Project;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

import static xyz.spiralhalo.sherlock.util.ColorUtil.foreground;
import static xyz.spiralhalo.sherlock.util.ColorUtil.interpolateNicely;
import static xyz.spiralhalo.sherlock.util.ColorUtil.multiply;

public class ActiveSummaryCell extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if(table.getModel() instanceof ActiveSummaryModel){
            int i = table.convertRowIndexToModel(row);
            Project x = ((ActiveSummaryModel) table.getModel()).getProject(i);
            super.setForeground((isSelected?interpolateNicely(0.5f,new Color(x.getColor()),Color.white):foreground));
            super.setBackground(isSelected?multiply(Color.gray,new Color(x.getColor())):new Color(x.getColor()));
        }
        return this;
    }
}
