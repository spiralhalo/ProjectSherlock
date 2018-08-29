package xyz.spiralhalo.sherlock.report;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import static xyz.spiralhalo.sherlock.util.ColorUtil.*;

public class ProjectCell extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if(table.getModel() instanceof AllModel){
            AllModel x = (AllModel) table.getModel();
            super.setForeground((isSelected?interpolateNicely(0.5f,x.getColor(row),Color.white):(x.isDark(row)?Color.white:foreground)));
            super.setBackground(isSelected?multiply(Color.gray,x.getColor(row)):x.getColor(row));
        }
        return this;
    }
}
