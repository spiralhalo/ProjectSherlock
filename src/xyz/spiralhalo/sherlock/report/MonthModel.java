package xyz.spiralhalo.sherlock.report;

import xyz.spiralhalo.sherlock.report.persist.ReportRow;
import xyz.spiralhalo.sherlock.util.FormatUtil;

import javax.swing.table.AbstractTableModel;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MonthModel extends AbstractTableModel {
    private static final String[] columnName = new String[]{"Month", "Time spent"};
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM MMM").withZone(ZoneId.systemDefault());

    private final ArrayList<ReportRow> data;

    public MonthModel(ArrayList<ReportRow> data) {
        this.data = data;
    }

    @Override
    public String getColumnName(int column) {
        return columnName[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if(columnIndex ==1) return Integer.class; else return String.class;
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