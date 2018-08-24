package xyz.spiralhalo.sherlock.report;

import xyz.spiralhalo.sherlock.report.persist.AllReportRows;
import xyz.spiralhalo.sherlock.util.FormatUtil;

import javax.swing.table.AbstractTableModel;
import java.awt.*;

public class AllModel extends AbstractTableModel {
    private static final String[] columnName = new String[]{"Project","Period","Days worked","Time spent"};

    private final AllReportRows data;

    public AllModel(AllReportRows data) {
        this.data = data;
    }

    public long getProjectHash(int rowIndex){
        return data.get(rowIndex).getProjectHash();
    }

    public Color getColor(int rowIndex){
        return new Color(data.get(rowIndex).getProjectColor());
    }

    @Override
    public String getColumnName(int column) {
        return columnName[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
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
                return data.get(rowIndex).getProjectName();
            case 1:
                String fromDate = data.get(rowIndex).getStartDate().format(FormatUtil.DTF_YMD);
                String toDate = (data.get(rowIndex).getFinishDate()==null?"ongoing":data.get(rowIndex).getFinishDate().format(FormatUtil.DTF_YMD));
                return String.format("%s ~ %s",fromDate,toDate);
            case 2:
                return String.format("%d days", data.get(rowIndex).getDays());
            default:
                return FormatUtil.hms(data.get(rowIndex).getSeconds());
        }
    }
}