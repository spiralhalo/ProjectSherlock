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

package xyz.spiralhalo.sherlock.util.swing;

import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class IntSelectorModel implements ComboBoxModel<IntSelection> {
	private final ArrayList<ListDataListener> l = new ArrayList<>();
	private final IntSelection[] ints;
	private IntSelection selected;

	public IntSelectorModel(String[] intLabels, int[] intValues) {
		if (intLabels.length != intValues.length)
			throw new IllegalArgumentException("Int name and value array lengths mismatch.");
		if (intLabels.length == 0) throw new IllegalArgumentException("Zero length array.");

		ints = new IntSelection[intLabels.length];

		for (int i = 0; i < intLabels.length; i++) {
			ints[i] = new IntSelection(intLabels[i], intValues[i]);
		}
	}

	@Override
	public void setSelectedItem(Object obj) {
		int x = -1;
		if (obj instanceof IntSelection) {
			for (int i = 0; i < ints.length; i++) {
				if (ints[i].equals(obj)) {
					x = i;
					break;
				}
			}
		}
		if (x != -1) {
			selected = ints[x];
			for (ListDataListener l1 : l) {
				l1.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, -1, -1));
			}
		}
	}

	@Override
	public Object getSelectedItem() {
		return selected;
	}

	@Override
	public int getSize() {
		return ints.length;
	}

	@Override
	public IntSelection getElementAt(int index) {
		if (index < 0 || index >= ints.length) return null;
		return ints[index];
	}

	public int getIndexFor(int value) {
		for (int i = 0; i < ints.length; i++) {
			if (ints[i].getValue() == value) {
				return i;
			}
		}
		return 0;
	}


	@Override
	public void addListDataListener(ListDataListener l) {
		this.l.add(l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		this.l.remove(l);
	}
}
