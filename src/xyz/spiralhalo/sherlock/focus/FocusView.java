package xyz.spiralhalo.sherlock.focus;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.util.ColorUtil;

import javax.swing.*;
import java.awt.*;

public class FocusView {
    private JTextField lblProject;
    private JPanel rootPanel;
    private JLabel lblHint;
    private JFrame view;

    public FocusView(Project project){
        if(project != null) {
            lblProject.setText(project.toString());
            lblProject.setBackground(new Color(project.getColor()));
        } else {
            lblProject.setText("NULL (error, please disable)");
        }
        lblHint.setForeground(ColorUtil.gray);
        view = new JFrame();
        view.setContentPane(rootPanel);
        view.pack();
        view.setMinimumSize(view.getSize());
    }

    public JFrame getView() {
        return view;
    }
}
