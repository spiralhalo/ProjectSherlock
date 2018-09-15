package xyz.spiralhalo.sherlock.persist.settings;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;

public class VkSelectorModel implements ComboBoxModel<VkSelection> {
    private final ArrayList<ListDataListener> l = new ArrayList<>();
    private final VkSelection[] vks;
    private VkSelection selected;

    public VkSelectorModel(String[] VK_names, int[] VK_values) {
        if(VK_names.length != VK_values.length) throw new IllegalArgumentException("VK name and value array lengths mismatch.");
        if(VK_names.length == 0) throw new IllegalArgumentException("Zero length array.");

        vks = new VkSelection[VK_names.length];

        for (int i=0;i<VK_names.length;i++) {
            vks[i]=new VkSelection(VK_names[i], VK_values[i]);
        }
    }

    @Override
    public void setSelectedItem(Object obj) {
        int x = -1;
        if (obj instanceof VkSelection) {
            for (int i = 0; i < vks.length; i++) {
                if(vks[i].equals(obj)){
                    x = i;
                    break;
                }
            }
        }
        if (x != -1) {
            selected = vks[x];
            for (ListDataListener l1:l) {
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
        return vks.length;
    }

    @Override
    public VkSelection getElementAt(int index) {
        if (index < 0 || index >= vks.length) return null;
        return vks[index];
    }

    public int getIndexFor(int value) {
        for (int i = 0; i < vks.length; i++) {
            if(vks[i].getValue() == value){
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
