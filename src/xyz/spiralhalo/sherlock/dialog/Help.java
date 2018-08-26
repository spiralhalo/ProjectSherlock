package xyz.spiralhalo.sherlock.dialog;

import xyz.spiralhalo.sherlock.Main;
import xyz.spiralhalo.sherlock.res.Res;
import xyz.spiralhalo.sherlock.util.Debug;

import java.awt.*;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.event.*;
import java.io.IOException;

public class Help extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JTextPane text;
    private JScrollPane scroll;

    public Help(JFrame parent, String topic, String textUrl) {
        super(parent);
        initialize(parent, topic, textUrl);
    }

    public Help(JDialog parent, String topic, String textUrl) {
        super(parent);
        initialize(parent, topic, textUrl);
    }

    private void initialize(Component parent, String topic, String textUrl) {
        setTitle("About "+topic);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        try {
            text.setPage(Res.class.getResource(textUrl));
            ((HTMLEditorKit)text.getEditorKit()).getStyleSheet()
                            .addRule(String.format("body {color:#%s; font-size:%dpt;}",
                                    Integer.toHexString(Main.currentTheme.foreground), getFont().getSize()));
        } catch (IOException e) {
            Debug.log(e);
        }
        setMinimumSize(contentPane.getMinimumSize());
        setPreferredSize(contentPane.getMinimumSize());
        pack();
        setLocationRelativeTo(parent);

        buttonOK.addActionListener(e -> onOK());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onOK();
            }
        });

        contentPane.registerKeyboardAction(e -> onOK(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        dispose();
    }
}
