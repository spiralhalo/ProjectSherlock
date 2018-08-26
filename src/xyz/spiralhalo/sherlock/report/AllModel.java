package xyz.spiralhalo.sherlock.report;

import xyz.spiralhalo.sherlock.report.persist.AllReportRows;
import xyz.spiralhalo.sherlock.util.FormatUtil;

import javax.swing.table.AbstractTableModel;
import java.awt.*;

public class AllModel extends AbstractTableModel {
    private static final String[] columnName = new String[]{"Project","Period","Days worked","Time spent"};
    private static final String[] columnNameUtility = new String[]{"Tag","Time spent"};

    private final AllReportRows data;
    private final boolean utility;

    public AllModel(AllReportRows data) {
        this(data, false);
    }

    public AllModel(AllReportRows data, boolean utility){
        this.data = data;
        this.utility = utility;
    }

    public long getProjectHash(int rowIndex){
        return data.get(rowIndex).getProjectHash();
    }

    public Color getColor(int rowIndex){
        return new Color(data.get(rowIndex).getProjectColor());
    }

    @Override
    public String getColumnName(int column) {
        return utility?columnNameUtility[column]:columnName[column];
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
        return utility?columnNameUtility.length:columnName.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return data.get(rowIndex).getProjectName();
            case 1:
                if(utility) return FormatUtil.hms(data.get(rowIndex).getSeconds());
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