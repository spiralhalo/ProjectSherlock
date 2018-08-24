package xyz.spiralhalo.sherlock.dialog;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.event.*;
import java.beans.PropertyChangeListener;

public class EditProject extends JDialog {
    public enum Mode{
        NEW,
        EDIT
    }

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField fieldName;
    private JTextArea fieldTag;
    private JComboBox comboCat;
    private JLabel labelWarning;
    private Mode mode;
    private Project p;
    private ProjectList projectList;
    private boolean result;

    public EditProject(JFrame owner, ProjectList projectList){
        this(owner, Mode.NEW, null, projectList);
    }

    public EditProject(JFrame owner, Project project, ProjectList projectList){
        this(owner, Mode.EDIT, project, projectList);
    }

    public EditProject(JFrame owner, Mode mode, Project project, ProjectList projectList) {
        super(owner, mode==Mode.NEW?"New project":"Edit project");
        this.mode = mode;
        this.p=project;
        this.projectList = projectList;

        comboCat.setModel(new DefaultComboBoxModel<>(projectList.getCategories().toArray(new String[0])));

        if(mode == Mode.EDIT){
            fieldName.setText(p.getName());
            comboCat.setSelectedItem(p.getCategory());
            fieldTag.setText(String.join(", ",p.getTags()));
        }

        CaretListener validator = e -> validateInput();
        fieldTag.addCaretListener(validator);
        fieldName.addCaretListener(validator);
        validateInput();

        setContentPane(contentPane);
        setModal(true);
        setMinimumSize(contentPane.getMinimumSize());
        getRootPane().setDefaultButton(buttonOK);
        pack();
        setLocationRelativeTo(owner);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void validateInput(){
        boolean enabled = fieldName.getText().trim().length() > 0;
        boolean warning = false;
        int trimLen;
        for (String x:fieldTag.getText().split(",")) {
            trimLen = x.trim().length();
            if(trimLen > 0 && trimLen < 4){
                warning = true;
            }
            if(trimLen < 1){
                enabled = false;
                break;
            }
        }
        buttonOK.setEnabled(enabled);
        labelWarning.setVisible(warning);
    }

    private void onOK() {
        switch (mode) {
            case NEW:
                projectList.addProject(new Project(fieldName.getText(), String.valueOf(comboCat.getSelectedItem()), fieldTag.getText()));
                break;
            case EDIT:
                String category = p.getCategory();
                p.edit(fieldName.getText(), String.valueOf(comboCat.getSelectedItem()), fieldTag.getText());
                projectList.editProject(p, category);
                break;
        }
        result = true;
        dispose();
    }

    private void onCancel() {
        result = false;
        dispose();
    }

    public boolean getResult() {
        return result;
    }
}
