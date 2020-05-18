package xyz.spiralhalo.sherlock;

import xyz.spiralhalo.sherlock.util.FormatUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

public class Thumb {

    public interface ThumbManager {
        void setSelection(long hash);
        void getSelection(long hash);
    }

    private JLabel lblName;
    private JLabel lblTime;
    private JLabel lblThumb;
    private JPanel rootPane;
    private static Icon defImg = null;
    private ThumbManager thumbManager;
    private long thumbsProjectHash;
    private static Color defClr;
    private static final Color selClr = new Color(0,0, 255);

    public Thumb(ThumbManager manager, String projectName, long projectHash) {
        if(defImg == null){
            defImg = lblThumb.getIcon();
            defClr = rootPane.getBackground();
        }
        this.thumbManager = manager;
        MouseAdapter mouseAdapter = new MouseAdapter() {
            //very weird manual click detector because mouseClicked is buggy
            private boolean exited = false;
            @Override public void mouseEntered(MouseEvent mouseEvent) {exited = false;}
            @Override public void mouseExited(MouseEvent mouseEvent) {exited = true;}
            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                if(exited) return;
                //actual important code
                thumbManager.setSelection(thumbsProjectHash);
                rootPane.setBackground(selClr);
            }
        };
        rootPane.addMouseListener(mouseAdapter);
        set(projectName, projectHash);
    }

    public void onSelectionChanged(long newHash) {
        if(thumbsProjectHash != newHash){
            rootPane.setBackground(defClr);
        }
    }

    public void set(String projectName, long projectHash){
        this.thumbsProjectHash = projectHash;
        BufferedImage thumb = ScrSnapper.getThumbImg(projectHash);
        if(thumb != null) {
            BufferedImage newImage = new BufferedImage(240, 135, BufferedImage.TYPE_INT_RGB);

            Graphics g = newImage.createGraphics();
            g.drawImage(thumb, 0, 0, 240, 135, null);
            g.dispose();
            lblThumb.setIcon(new ImageIcon(newImage));
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
