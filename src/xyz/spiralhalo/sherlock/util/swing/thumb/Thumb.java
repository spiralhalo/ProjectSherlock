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

    Thumb(ThumbManager manager, String projectName, long projectHash, long lastWorkedOnMillis) {
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
        set(projectName, projectHash, lastWorkedOnMillis);
    }

    void onSelectionChanged(long newHash) {
        if(thumbsProjectHash != newHash){
            rootPane.setBackground(defClr);
        }
    }

    public void set(String projectName, long projectHash, long lastWorkedOnMillis){
        this.thumbsProjectHash = projectHash;
        BufferedImage thumb = ScrSnapper.getThumbImg(projectHash);
        if(thumb != null) {
            lblThumb.setIcon(new ImageIcon(Thumbnailator.createThumbnail(thumb, thumbDM.width, thumbDM.height)));
        } else {
            lblThumb.setIcon(defImg);
        }
        lblName.setText(projectName);
        lblTime.setText(FormatUtil.vagueTimeAgo(lastWorkedOnMillis));
    }

    public JPanel getPane() {
        return rootPane;
    }
}
