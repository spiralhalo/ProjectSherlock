package xyz.spiralhalo.sherlock.notes;

import xyz.spiralhalo.sherlock.util.FormatUtil;

import javax.swing.*;
import java.awt.event.*;
import java.time.LocalDate;

public class EditNote extends JDialog {
    public static String getNote(JFrame owner, LocalDate date, String oldNote){
        EditNote x = new EditNote(owner, date, oldNote);
        x.setVisible(true);
        return x.result;
    }

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel lblDate;
    private JTextArea textArea1;
    private String result;

    private EditNote(JFrame owner, LocalDate date, String oldNote) {
        super(owner, "Notes");
        setContentPane(contentPane);
        setMinimumSize(contentPane.getMinimumSize());
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        lblDate.setText(String.format("Date: %s", FormatUtil.DTF_DATE_SELECTOR.format(date)));
        if(oldNote != null){
            textArea1.setText(oldNote);
        }
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        pack();
        setLocationRelativeTo(owner);
    }

    private void onOK() {
        result = textArea1.getText();
        dispose();
    }

    private void onCancel() {
        dispose();
    }
}
