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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.Debug;

public class BookmarkMap extends HashMap<Long, ProjectBookmarks> {
	public static final long serialVersionUID = -5783581899062993176L;
	private static String BOOKMARKS_FILE = "bookmarks.dat";

	public synchronized static BookmarkMap load() {
		File file = new File(Application.getSaveDir(), BOOKMARKS_FILE);
		if (file.exists()) {
			try (FileInputStream fis = new FileInputStream(file);
				 ObjectInputStream ois = new ObjectInputStream(fis)) {
				return (BookmarkMap) ois.readObject();
			} catch (ClassNotFoundException | IOException | ClassCastException e) {
				Debug.log(e);
			}
		}
		return new BookmarkMap();
	}

	public synchronized static void save(BookmarkMap bookmarkMap) {
		File file = new File(Application.getSaveDir(), BOOKMARKS_FILE);
		try (FileOutputStream fis = new FileOutputStream(file);
			 ObjectOutputStream ois = new ObjectOutputStream(fis)) {
			ois.writeObject(bookmarkMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
