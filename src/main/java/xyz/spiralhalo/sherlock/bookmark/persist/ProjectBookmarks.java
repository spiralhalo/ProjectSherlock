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

package xyz.spiralhalo.sherlock.bookmark.persist;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import xyz.spiralhalo.sherlock.bookmark.ModelAccessor;
import xyz.spiralhalo.sherlock.bookmark.ProjectBookmarksModel;

public class ProjectBookmarks implements Serializable, ModelAccessor {
	public static final long serialVersionUID = 1L;

	private transient ProjectBookmarksModel model;
	private final ArrayList<Bookmark> bookmarks;

	public ProjectBookmarks() {
		bookmarks = new ArrayList<>();
	}

	public ProjectBookmarksModel getModel() {
		if (model == null) {
			model = new ProjectBookmarksModel(this);
		}
		return model;
	}

	public Bookmark get(int i) {
		return bookmarks.get(i);
	}

	public void addOrReplaceUnsaved(Bookmark bookmark) {
		if (bookmarks.contains(bookmark)) return;
		bookmarks.add(bookmark);
		getModel().fireTableRowsInserted(size() - 1, size() - 1);
	}

	public void remove(int x) {
		bookmarks.remove(x);
		getModel().fireTableRowsDeleted(x, x);
	}

	public void editBookmark(int x, Bookmark newValue) {
		if (x == -1) return;
		bookmarks.remove(x);
		bookmarks.add(x, newValue);
		getModel().fireTableRowsUpdated(x, x);
	}

	public void moveUp(int i) {
		if (i > 0) {
			Bookmark x = bookmarks.get(i);
			bookmarks.remove(i);
			bookmarks.add(i - 1, x);
			getModel().fireTableRowsUpdated(i - 1, i);
		}
	}

	public void moveDown(int i) {
		if (i < bookmarks.size() - 1) {
			Bookmark x = bookmarks.get(i);
			bookmarks.remove(i);
			bookmarks.add(i + 1, x);
			getModel().fireTableRowsUpdated(i, i + 1);
		}
	}

	@Override
	public int size() {
		return bookmarks.size();
	}

	@Override
	public String getType(int i) {
		switch (bookmarks.get(i).getType()) {
			default:
			case URL:
				return "URL";
			case FILE: {
				if (new File(bookmarks.get(i).getValue()).isDirectory()) return "Folder";
				return "File";
			}
		}
	}

	@Override
	public String getValue(int i) {
		return bookmarks.get(i).getValue();
	}
}
