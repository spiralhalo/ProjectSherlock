package xyz.spiralhalo.sherlock.report;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DateSelectorModel implements ComboBoxModel<DateSelectorEntry> {
    private ArrayList<DateSelectorEntry> list;
    private DateSelectorEntry selected;
    private ListDataListener l;

    public DateSelectorModel(ArrayList<LocalDate> list) {
        this.list = new ArrayList<>();
        for (LocalDate l:list) {
            this.list.add(new DateSelectorEntry(l));
        }
    }

    public DateSelectorModel(ArrayList<LocalDate> list, DateTimeFormatter formatter) {
        this.list = new ArrayList<>();
        for (LocalDate l:list) {
            this.list.add(new DateSelectorEntry(l, formatter));
        }
    }

    @Override
    public void setSelectedItem(Object obj) {
        if (obj instanceof DateSelectorEntry) {
            int x = list.indexOf(obj);
            if (x != -1) {
                selected = list.get(x);
                if(l!=null){
                    l.contentsChanged(new ListDataEvent(this,ListDataEvent.CONTENTS_CHANGED,-1,-1));
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
    public DateSelectorEntry getElementAt(int index) {
        if (index < 0 || index >= list.size()) return null;
        return list.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        this.l = l;
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        this.l = null;
    }
}
