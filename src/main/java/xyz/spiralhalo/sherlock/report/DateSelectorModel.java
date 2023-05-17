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

package xyz.spiralhalo.sherlock.report;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.ArrayList;

public class DateSelectorModel<T extends Temporal> implements ComboBoxModel<DateSelection<T>> {
    private ArrayList<DateSelection<T>> list;
    private DateSelection selected;
    private final ArrayList<ListDataListener> l = new ArrayList<>();

    public DateSelectorModel(ArrayList<T> list) {
        this.list = new ArrayList<>();
        for (T l:list) {
            this.list.add(new DateSelection<>(l));
        }
    }

    public DateSelectorModel(ArrayList<T> list, DateTimeFormatter formatter) {
        this.list = new ArrayList<>();
        for (T l:list) {
            this.list.add(new DateSelection<>(l, formatter));
        }
    }

    @Override
    public void setSelectedItem(Object obj) {
        if (obj instanceof DateSelection) {
            int x = list.indexOf(obj);
            if (x != -1) {
                selected = list.get(x);
                for (ListDataListener l1:l) {
                    l1.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, -1, -1));
                }
            }
        }
    }

    @Override
    public Object getSelectedItem() {
        return selected;
    }

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public DateSelection<T> getElementAt(int index) {
        if (index < 0 || index >= list.size()) return null;
        return list.get(index);
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
