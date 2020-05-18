package xyz.spiralhalo.sherlock.util.swing.thumb;

import net.coobird.thumbnailator.Thumbnailator;
import xyz.spiralhalo.sherlock.ScrSnapper;
import xyz.spiralhalo.sherlock.util.FormatUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;

public class Thumb {

    private JLabel lblName;
    private JLabel lblTime;
    private JLabel lblThumb;
    private JPanel rootPane;
    private static Icon defImg = null;
    private ThumbManager thumbManager;
    private long thumbsProjectHash;
    private static Color defClr;
    private static final Color selClr = new Color(0,0, 255);
    private static final Dimension thumbDM = new Dimension(240, 135);

    Thumb(ThumbManager manager, String projectName, long projectHash) {
        if(defImg == null){
            defImg = lblThumb.getIcon();
            defClr = rootPane.getBackground();
        }
        this.thumbManager = manager;
        MouseListener mouseAdapter = new MouseListener() {
            //very weird manual click detector because mouseClicked is buggy
            private boolean exited = false;
            @Override public void mouseEntered(MouseEvent mouseEvent) {
                exited = false;
                thumbManager.mouseEntered(mouseEvent); //relay all mouse events to thumbmanager
            }
            @Override public void mouseExited(MouseEvent mouseEvent) {
                exited = true;
                thumbManager.mouseExited(mouseEvent);
            }
            @Override public void mouseClicked(MouseEvent mouseEvent) { thumbManager.mouseClicked(mouseEvent); }
            @Override public void mousePressed(MouseEvent mouseEvent) { thumbManager.mousePressed(mouseEvent); }
            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                if(exited) return;
                //actual important code
                thumbManager.setSelection(thumbsProjectHash);
                rootPane.setBackground(selClr);
                thumbManager.mouseReleased(mouseEvent);
            }
        };
        lblThumb.setMinimumSize(thumbDM);
        rootPane.addMouseListener(mouseAdapter);
        set(projectName, projectHash);
    }

    void onSelectionChanged(long newHash) {
        if(thumbsProjectHash != newHash){
            rootPane.setBackground(defClr);
        }
    }

    public void set(String projectName, long projectHash){
        this.thumbsProjectHash = projectHash;
        BufferedImage thumb = ScrSnapper.getThumbImg(projectHash);
        if(thumb != null) {
            lblThumb.setIcon(new ImageIcon(Thumbnailator.createThumbnail(thumb, thumbDM.width, thumbDM.height)));
        } else {
            lblThumb.setIcon(defImg);
        }
        lblName.setText(projectName);
        File thumbFile =  ScrSnapper.getThumbFile(projectHash);
        if(thumbFile.exists()) {
            long elapsed = System.currentTimeMillis() - thumbFile.lastModified();
            if (elapsed < 60000) {
                lblTime.setText("just now");
            } else {
                lblTime.setText(String.format("%s ago", FormatUtil.hmsStrict((int) (elapsed / 1000))));
            }
        } else {
            lblTime.setText("a long time ago");
        }
    }

    public JPanel getPane() {
        return rootPane;
    }
}
