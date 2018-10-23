package xyz.spiralhalo.sherlock.focus;

import xyz.spiralhalo.sherlock.persist.project.Project;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ActiveSummaryModel extends AbstractTableModel {
    private final Project[] list;

    public ActiveSummaryModel(List<? extends Project> activeProjects){
        list = new Project[activeProjects.size()];
        for (int i = 0; i < list.length; i++) {
            list[i] = activeProjects.get(i);
        }
    }

    @Override
    public String getColumnName(int column) {
        return "Project";
    }

    @Override
    public int getRowCount() {
        return list.length;
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return String.valueOf(list[rowIndex]);
    }

    public Project getProject(int rowIndex){
        if(rowIndex<0 || rowIndex>=list.length)return null;
        return list[rowIndex];
    }
}
