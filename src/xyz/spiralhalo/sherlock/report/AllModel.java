package xyz.spiralhalo.sherlock.report;

import xyz.spiralhalo.sherlock.persist.project.UtilityTag;
import xyz.spiralhalo.sherlock.report.factory.table.AllReportRows;
import xyz.spiralhalo.sherlock.util.FormatUtil;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;

public class AllModel extends AbstractTableModel {
    private static final String[] columnName = new String[]{" ", "Project","Period","Days worked","Time spent"};
    private static final String[] columnNameUtility = new String[]{" ", "Tag", "Type", "Time spent"};

    private AllReportRows data;
    private final boolean utility;

    public AllModel(AllReportRows data) {
        this(data, false);
    }

    public AllModel(AllReportRows data, boolean utility){
        this.data = data;
        this.utility = utility;
    }

    public void reset(AllReportRows data){
        this.data = data;
        fireTableDataChanged();
    }

    public void setTableColumnWidths(JTable table){
        table.getColumnModel().getColumn(0).setMaxWidth(24);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
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
            case 0: return String.format("%s",rowIndex+1);
            case 1:
                return data.get(rowIndex).getProjectName();
            case 2:
                if(utility) return data.get(rowIndex).isProductive()? UtilityTag.PRODUCTIVE_LABEL:UtilityTag.NON_PRODUCTIVE_LABEL;
                String fromDate = data.get(rowIndex).getStartDate().format(FormatUtil.DTF_YMD);
                String toDate = (data.get(rowIndex).getFinishDate()==null?"ongoing":data.get(rowIndex).getFinishDate().format(FormatUtil.DTF_YMD));
                return String.format("%s ~ %s",fromDate,toDate);
            case 3:
                if(utility) return FormatUtil.hms(data.get(rowIndex).getSeconds());
                return String.format("%d days", data.get(rowIndex).getDays());
            default:
                return FormatUtil.hms(data.get(rowIndex).getSeconds());
        }
    }

    public int findIndex(long hash) {
        for (int i = 0; i < data.size(); i++) {
            if(data.get(i).getProjectHash() == hash){
                return i;
            }
        }
        return -1;
    }
}