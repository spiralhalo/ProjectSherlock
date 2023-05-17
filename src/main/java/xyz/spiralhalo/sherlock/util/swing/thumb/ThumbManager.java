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

package xyz.spiralhalo.sherlock.util.swing.thumb;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.event.ListSelectionListener;

public class ThumbManager {
	private long selectedThumbHash = -1;
	private final ArrayList<Thumb> thumbs;
	private final ArrayList<ListSelectionListener> listeners;
	private final ArrayList<MouseListener> mouseListeners;

	public ThumbManager() {
		thumbs = new ArrayList<>();
		listeners = new ArrayList<>();
		mouseListeners = new ArrayList<>();
	}

	public void newThumb(String projectName, long projectHash, long lastWorkedOnMillis) {
		thumbs.add(new Thumb(this, projectName, projectHash, lastWorkedOnMillis));
	}

	void setSelection(long hash) {
		selectedThumbHash = hash;
		for (Thumb t : thumbs) {
			t.onSelectionChanged(hash);
		}
		for (ListSelectionListener l : listeners) {
			l.valueChanged(null);
		}
	}

	public long getSelection() {
		return selectedThumbHash;
	}

	public Thumb getThumb(int i) {
		return thumbs.get(i);
	}

	public int size() {
		return thumbs.size();
	}

	public void addSelectionListener(ListSelectionListener listener) {
		listeners.add(listener);
	}

	public void addMouseListener(MouseListener mouseListener) {
		mouseListeners.add(mouseListener);
	}

	void mouseClicked(MouseEvent mouseEvent) {
		for (MouseListener l : mouseListeners) {
			l.mouseClicked(mouseEvent);
		}
	}

	void mousePressed(MouseEvent mouseEvent) {
		for (MouseListener l : mouseListeners) {
			l.mousePressed(mouseEvent);
		}
	}

	void mouseReleased(MouseEvent mouseEvent) {
		for (MouseListener l : mouseListeners) {
			l.mouseReleased(mouseEvent);
		}
	}

	void mouseEntered(MouseEvent mouseEvent) {
		for (MouseListener l : mouseListeners) {
			l.mouseEntered(mouseEvent);
		}
	}

	void mouseExited(MouseEvent mouseEvent) {
		for (MouseListener l : mouseListeners) {
			l.mouseExited(mouseEvent);
		}
	}
}
