package xyz.spiralhalo.sherlock.bookmark;

import javax.swing.table.AbstractTableModel;

public class ProjectBookmarksModel extends AbstractTableModel {
    private static final String[] columns = new String[]{"Hotkey", "Type", "Path"};

    private final ModelAccessor bookmarks;

    public ProjectBookmarksModel(ModelAccessor bookmarks) {
        this.bookmarks = bookmarks;
    }

    @Override
    public int getRowCount() {
        return bookmarks.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex){
            default:
            case 0:
                if(rowIndex >= 0 && rowIndex < 9) return rowIndex+1;
                if(rowIndex==9) return 0;
                return "";
            case 1:
                return bookmarks.getType(rowIndex);
            case 2:
                return bookmarks.getValue(rowIndex);
        }
    }
}
