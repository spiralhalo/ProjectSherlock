package xyz.spiralhalo.sherlock.focus;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.util.ColorUtil;
import xyz.spiralhalo.sherlock.util.FormatUtil;
import xyz.spiralhalo.sherlock.util.swing.Dragger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FocusView {
    private JLabel lblProject;
    private JPanel rootPanel;
    private JLabel lblHint;
    private JLabel lblHint2;
    private JLabel lblHMS;
    private JLabel lblTimeHint;
    private JFrame view;
    private Timer showTimer;
    private boolean showing = false;

    public FocusView(Project project){
        if(project != null) {
            //radiance workaround
//            ActiveSummaryCell renderer = new ActiveSummaryCell();
//            renderer.setHorizontalAlignment(SwingConstants.CENTER);
//            tblProjectName.setDefaultRenderer(Object.class, renderer);
//            tblProjectName.setModel(new ActiveSummaryModel(Arrays.asList(project)));
//            tblProjectName.setRowSelectionAllowed(false);
//            tblProjectName.setColumnSelectionAllowed(false);
//            lblProject.setVisible(false);
            lblProject.setText(project.toString().toUpperCase());
            lblProject.setForeground(new Color(project.getColor()));
        } else {
//            tblProjectName.setVisible(false);
            lblProject.setText("UNKNOWN (error; please reconfigure)");
        }
        refreshDuration();
        lblHint.setForeground(ColorUtil.gray);
        lblHint2.setForeground(ColorUtil.gray);
        view = new JFrame();
        view.setFocusableWindowState(false);
        view.setContentPane(rootPanel);
        view.setUndecorated(true);
        view.pack();
        view.setMinimumSize(view.getSize());
        view.setLocationRelativeTo(null);
        view.setLocation(view.getX(), 24);
        view.setAlwaysOnTop(true);
        Dragger.makeDraggable(view, rootPanel);
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        boolean uniform = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);
        boolean perpixel = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT);
        if(uniform && !perpixel) {
            view.setOpacity(0.6f);
        } else if (perpixel) {
            view.setBackground(new Color(33,33,33,150));
        }
    }

    public void setVisible(boolean visible) {
        if(visible && !showing && !view.isVisible()){
            showing = true;
            if(showTimer != null){
                showTimer.restart();
            } else {
                showTimer = new Timer(5000, e -> {
                    refreshDuration();
                    view.setVisible(true);
                    showTimer.stop();
                    showing = false;
                });
                showTimer.start();
            }
        } else if(!visible && view.isVisible()){
            view.setVisible(false);
            showing = false;
        }
    }

    private void refreshDuration() {
        long duration = FocusState.getInstance().getDuration();
        if(duration >= 0){
            lblTimeHint.setForeground(ColorUtil.gray);
            lblHMS.setText(FormatUtil.hmsColon((int)(duration/1000)));
        } else {
            lblTimeHint.setVisible(false);
            lblHMS.setVisible(false);
        }
    }

    public void dispose() {
        view.dispose();
    }
}
