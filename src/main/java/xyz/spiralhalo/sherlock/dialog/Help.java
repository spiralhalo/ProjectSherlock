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

package xyz.spiralhalo.sherlock.dialog;

import xyz.spiralhalo.sherlock.Main;
import xyz.spiralhalo.sherlock.res.Res;
import xyz.spiralhalo.sherlock.Debug;

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
            text.setPage(getClass().getResource("/" + textUrl));
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
