//
//    Copyright 2020 spiralhalo <re.nanashi95@gmail.com>
//
//    This file is part of Project Sherlock.
//
//    Project Sherlock is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Project Sherlock is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Project Sherlock.  If not, see <https://www.gnu.org/licenses/>.
//

package xyz.spiralhalo.sherlock.bookmark;

import xyz.spiralhalo.sherlock.bookmark.persist.ProjectBookmarks;

import javax.swing.table.AbstractTableModel;

public class ProjectBookmarksModel extends AbstractTableModel {
    private static final String[] columns = new String[]{"Hotkey", "Bookmark"};

    private final ProjectBookmarks bookmarks;

    public ProjectBookmarksModel(ProjectBookmarks bookmarks) {
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
        return Object.class;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            if (rowIndex >= 0 && rowIndex < 9) return rowIndex + 1;
            if (rowIndex == 9) return 0;
            return "";
        }
        return bookmarks.get(rowIndex);
    }
}
