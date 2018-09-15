package xyz.spiralhalo.sherlock.util.swing;

import javax.swing.*;

public class ArrayListModel<E> extends AbstractListModel<E> {
    private final E[] arr;

    public ArrayListModel(E[] arr) {
        this.arr = arr;
    }

    @Override
    public int getSize() {
        return arr.length;
    }

    @Override
    public E getElementAt(int index) {
        return arr[index];
    }
}
