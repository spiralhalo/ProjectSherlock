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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import xyz.spiralhalo.sherlock.persist.settings.AppConfig;

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
		AppConfig.appSBool(AppConfig.AppBool.ASK_BEFORE_QUIT, !checkDoNotAsk.isSelected());
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
