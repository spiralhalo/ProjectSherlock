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

package xyz.spiralhalo.sherlock.persist.project;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class ProjectComboModel implements ComboBoxModel<String>, ListDataListener {
	public static String ANY = "<Any project>";

	private DefaultComboBoxModel<String> wrapped;
	private boolean any;
	private EventListenerList listenerList = new EventListenerList();

	public ProjectComboModel(DefaultComboBoxModel<String> wrapped) {
		this.wrapped = wrapped;
		wrapped.addListDataListener(this);
	}

	@Override
	public void setSelectedItem(Object anItem) {
		if (anItem.equals(ANY)) {
			any = true;
		} else {
			any = false;
			wrapped.setSelectedItem(anItem);
		}
	}

	@Override
	public Object getSelectedItem() {
		if (any) {
			return ANY;
		} else {
			return wrapped.getSelectedItem();
		}
	}

	@Override
	public int getSize() {
		return wrapped.getSize() + 1;
	}

	@Override
	public String getElementAt(int index) {
		if (index == 0) {
			return ANY;
		} else {
			String s = wrapped.getElementAt(index - 1);
			if (s.length() > 26) {
				return s.substring(0, 22) + "...";
			}
			return s;
		}
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		listenerList.add(ListDataListener.class, l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listenerList.remove(ListDataListener.class, l);
	}

	@Override
	public void intervalAdded(ListDataEvent ex) {
		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ListDataListener.class) {
				if (e == null) {
					e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, ex.getIndex0() + 1, ex.getIndex1() + 1);
				}
				((ListDataListener) listeners[i + 1]).intervalAdded(e);
			}
		}
	}

	@Override
	public void intervalRemoved(ListDataEvent ex) {
		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ListDataListener.class) {
				if (e == null) {
					e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, ex.getIndex0() + 1, ex.getIndex1() + 1);
				}
				((ListDataListener) listeners[i + 1]).intervalRemoved(e);
			}
		}
	}

	@Override
	public void contentsChanged(ListDataEvent ex) {
		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ListDataListener.class) {
				if (e == null) {
					e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, ex.getIndex0(), ex.getIndex1());
				}
				((ListDataListener) listeners[i + 1]).contentsChanged(e);
			}
		}
	}
}
