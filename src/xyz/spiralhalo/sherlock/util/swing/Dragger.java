package xyz.spiralhalo.sherlock.util.swing;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Dragger implements MouseListener, MouseMotionListener {
    private int pX, pY;
    private Component draggable;

    public static void makeDraggable(Component draggable, Component dragArea) {
        new Dragger(draggable, dragArea);
    }

    private Dragger(Component draggable, Component dragArea) {
        this.draggable = draggable;
        dragArea.addMouseListener(this);
        dragArea.addMouseMotionListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        pX = e.getX();
        pY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        draggable.setLocation(draggable.getLocation().x + e.getX() - pX,
                draggable.getLocation().y + e.getY() - pY);
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
