package xyz.spiralhalo.sherlock.report;

import xyz.spiralhalo.sherlock.report.persist.ReportRows;
import xyz.spiralhalo.sherlock.util.FormatUtil;

import javax.swing.table.AbstractTableModel;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

public class DayModel extends AbstractTableModel {
    private static final String[] columnName = new String[]{"Date","Time spent"};
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd EEE").withZone(ZoneId.systemDefault());

    private final ReportRows data;

    public DayModel(ReportRows data) {
        this.data = data;
    }

    public int getDay(int rowIndex){
        return data.get(rowIndex).getTimestamp().get(ChronoField.DAY_OF_WEEK);
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
