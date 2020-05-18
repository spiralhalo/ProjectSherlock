package xyz.spiralhalo.sherlock.util.swing.thumb;

import javax.swing.event.ListSelectionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

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

    public void newThumb(String projectName, long projectHash){
        thumbs.add(new Thumb(this, projectName, projectHash));
    }
    void setSelection(long hash){
        selectedThumbHash = hash;
        for (Thumb t:thumbs) {
            t.onSelectionChanged(hash);
        }
        for (ListSelectionListener l:listeners){
            l.valueChanged(null);
        }
    }
    public long getSelection() {
        return selectedThumbHash;
    }
    public Thumb getThumb(int i){
        return thumbs.get(i);
    }
    public int size(){
        return thumbs.size();
    }
    public void addSelectionListener(ListSelectionListener listener){
        listeners.add(listener);
    }
    public void addMouseListener(MouseListener mouseListener){
        mouseListeners.add(mouseListener);
    }

    void mouseClicked(MouseEvent mouseEvent) {
        for (MouseListener l:mouseListeners) {
            l.mouseClicked(mouseEvent);
        }
    }

    void mousePressed(MouseEvent mouseEvent) {
        for (MouseListener l:mouseListeners) {
            l.mousePressed(mouseEvent);
        }
    }

    void mouseReleased(MouseEvent mouseEvent) {
        for (MouseListener l:mouseListeners) {
            l.mouseReleased(mouseEvent);
        }
    }

    void mouseEntered(MouseEvent mouseEvent) {
        for (MouseListener l:mouseListeners) {
            l.mouseEntered(mouseEvent);
        }
    }

    void mouseExited(MouseEvent mouseEvent) {
        for (MouseListener l:mouseListeners) {
            l.mouseExited(mouseEvent);
        }
    }
}
