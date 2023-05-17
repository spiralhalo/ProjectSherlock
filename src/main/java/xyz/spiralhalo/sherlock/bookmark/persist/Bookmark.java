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

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.*;

import xyz.spiralhalo.sherlock.Debug;

public class Bookmark implements Serializable {
	public static final long serialVersionUID = 1L;

	private final BookmarkType type;
	private final String value;

	public Bookmark(BookmarkType type, String value) {
		this.type = type;
		this.value = value;
	}

	public BookmarkType getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Bookmark) {
			return ((Bookmark) obj).type == type && ((Bookmark) obj).value.equals(value);
		}
		return super.equals(obj);
	}

	public boolean launch(JFrame origin) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			switch (type) {
				case FILE:
					try {
						desktop.open(new File(value));
						return true;
					} catch (IllegalArgumentException | FileNotFoundException e) {
						JOptionPane.showMessageDialog(origin, String.format("File not found: %s", value),
								"Failed to open bookmark", JOptionPane.ERROR_MESSAGE);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(origin, String.format("Can't open file: %s", value),
								"Failed to open bookmark", JOptionPane.ERROR_MESSAGE);
						Debug.log(e);
					}
					break;
				case URL:
					try {
						desktop.browse(new URI(value));
						return true;
					} catch (IllegalArgumentException | URISyntaxException e) {
						JOptionPane.showMessageDialog(origin, String.format("Invalid URL: %s", value),
								"Failed to open bookmark", JOptionPane.ERROR_MESSAGE);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(origin, String.format("Can't open URL: %s", value),
								"Failed to open bookmark", JOptionPane.ERROR_MESSAGE);
						Debug.log(e);
					}
					break;
			}
		} else {
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec("xdg-open " + value);
				return true;
			} catch (IOException e) {
				JOptionPane.showMessageDialog(origin, String.format("Can't open: %s", value),
						"Failed to open bookmark", JOptionPane.ERROR_MESSAGE);
				Debug.log(e);
			}
		}
		return false;
	}
}
