package xyz.spiralhalo.sherlock.dialog;

import xyz.spiralhalo.sherlock.persist.settings.AppConfig;

import javax.swing.*;
import java.awt.event.*;

public class Quit extends JDialog {
    public enum QuitSelection {
        EXIT,
        MINIMIZE,
        CANCEL
    }

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton buttonMinimize;
    private JLabel label;
    private JCheckBox checkDoNotAsk;
    private QuitSelection selection = QuitSelection.CANCEL;

    public Quit(JFrame owner) {
        super(owner, "Confirm Exit");
        setIconImage(null);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        label.setIcon(UIManager.getIcon("OptionPane.questionIcon"));
        buttonOK.requestFocus();

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonMinimize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onMinimize();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        checkDoNotAsk.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                buttonMinimize.setEnabled(!checkDoNotAsk.isSelected());
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
        pack();
        setLocationRelativeTo(owner);
    }

    public QuitSelection getSelection() {
        return selection;
    }

    private void onOK() {
        selection = QuitSelection.EXIT;
        AppConfig.setBoolean(AppConfig.AppBool.ASK_BEFORE_QUIT, !checkDoNotAsk.isSelected());
        dispose();
    }

    private void onMinimize() {
        selection = QuitSelection.MINIMIZE;
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
