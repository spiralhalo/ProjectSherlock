package xyz.spiralhalo.sherlock.bookmark.dialog;

import xyz.spiralhalo.sherlock.persist.project.Project;

import javax.swing.*;
import java.util.HashMap;

public class ShowBookmarks extends JDialog {
    private static final HashMap<Project, ShowBookmarks> dialogs = new HashMap<>();

    public static ShowBookmarks getDialog(JFrame owner, Project p){
        return dialogs.getOrDefault(p, new ShowBookmarks(owner, p));
    }

    private JPanel contentPane;
    private JList list1;
    private JButton newBookmarkButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton openButton;
    private JButton setPrimaryButton;

    private ShowBookmarks(JFrame owner, Project p) {
        super(owner, String.format("ShowBookmarks for `%s`", p.toString()));
        setContentPane(contentPane);
        //setModal(true);
        pack();
        setLocationRelativeTo(owner);
    }
}
