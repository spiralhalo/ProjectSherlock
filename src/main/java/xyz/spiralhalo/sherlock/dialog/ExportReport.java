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
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import xyz.spiralhalo.sherlock.async.AsyncTask;
import xyz.spiralhalo.sherlock.async.LoaderDialog;
import xyz.spiralhalo.sherlock.util.FormatUtil;

public class ExportReport extends JDialog {
	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JCheckBox checkActive;
	private JCheckBox checkFinished;
	private JCheckBox checkUseMachineColon;
	private JCheckBox checkUseGoogleDate;
	private JTable tActive, tFinished;

	public ExportReport(JFrame parent, JTable tActive, JTable tFinished) {
		super(parent);
		setLocationRelativeTo(parent);
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);

		this.tActive = tActive;
		this.tFinished = tFinished;

		ItemListener setOKEnabled = new ItemListener() {
			int count = 0;

			@Override
			public void itemStateChanged(ItemEvent itemEvent) {
				if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
					count++;
				} else if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
					count--;
				}
				buttonOK.setEnabled(count > 0);
			}
		};

		checkActive.addItemListener(setOKEnabled);
		checkFinished.addItemListener(setOKEnabled);

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

		// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

		// call onCancel() on ESCAPE
		contentPane.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		pack();
	}

	private void export(File dest) {
		AsyncTask<Boolean> exportTask = new AsyncTask<Boolean>() {
			private boolean succ;

			@Override
			protected void doRun() throws Exception {
				try (PrintWriter pw = new PrintWriter(new FileOutputStream(dest))) {
					for (int i = 0; i < tActive.getColumnCount(); i++) {
						pw.print(tActive.getColumnName(i));
						if (i != tActive.getColumnCount() - 1) {
							pw.print(',');
						}
					}
					pw.println();
					if (checkActive.isSelected()) {
						writeTable(pw, tActive);
					}
					if (checkFinished.isSelected()) {
						writeTable(pw, tFinished);
					}
					pw.flush();
					succ = true;
				} catch (Exception e) {
					succ = false;
					throw e;
				}
			}

			@Override
			protected Boolean getResult() {
				return succ;
			}

			private void writeTable(PrintWriter pw, JTable table) {
				for (int i = 0; i < table.getRowCount(); i++) {
					for (int j = 0; j < table.getColumnCount(); j++) {
						// bad hax
						if (checkUseGoogleDate.isSelected() && (j == 4 || j == 5)) {
							String[] date = table.getModel().getValueAt(i, j).toString().split("-");
							int year = Integer.parseInt(date[0]);
							int month = Integer.parseInt(date[1]);
							int day = Integer.parseInt(date[2]);
							pw.print(String.format("%d/%d/%d", day, month, year));
						}
						// okay hax
						else if (table.getModel().getColumnClass(j) == Integer.class) {
							if (checkUseMachineColon.isSelected()) {
								pw.print(FormatUtil.hmsMachineColon((Integer) table.getModel().getValueAt(i, j)));
							} else {
								pw.print(FormatUtil.hms((Integer) table.getModel().getValueAt(i, j)));
							}
						} else
							pw.print(table.getModel().getValueAt(i, j).toString());
						if (j != table.getColumnCount() - 1) {
							pw.print(',');
						}
					}
					pw.println();
				}
			}
		};
		LoaderDialog.execute(this, exportTask, (t, throwable) -> {
			if (t) {
				JOptionPane.showMessageDialog(this, "Tables have been exported successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
			} else if (throwable != null) {
				JOptionPane.showMessageDialog(this, "An unknown error occured while exporting.", "Failed", JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	private void onOK() {
		File dest = null;
		JFileChooser destChooser = new JFileChooser();
		destChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		destChooser.setAcceptAllFileFilterUsed(true);
		destChooser.setMultiSelectionEnabled(false);
		FileFilter csvFilter = new FileNameExtensionFilter("Comma-separated table (.csv)", "csv");
		destChooser.addChoosableFileFilter(csvFilter);
		destChooser.setFileFilter(csvFilter);
		while (true) {
			if (destChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
				dest = null;
				break;
			} else {
				dest = destChooser.getSelectedFile();

				if (dest == null) {
					break;
				}

				String filePath = dest.getAbsolutePath();
				if (destChooser.getFileFilter() == csvFilter && !filePath.toLowerCase().endsWith(".csv")) {
					dest = new File(filePath.concat(".csv"));
				}

				if (dest.isDirectory()) {
					// almost never called unless  jfilechooser implementation is somehow buggy
					JOptionPane.showMessageDialog(this, "A folder already exists with the same name. Please choose a different file.",
							"Information", JOptionPane.INFORMATION_MESSAGE);
					// continue;
				} else if (dest.exists()) {
					if (JOptionPane.showConfirmDialog(this, String.format("File %s already exists. Do you want to replace this file?",
							dest.getName()), "File already exists", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
							== JOptionPane.YES_OPTION) {
						break;
					} // else continue;
				} else {
					break;
				}
			}
		}
		if (dest != null) {
			export(dest);
			// add your code here
			dispose();
		}
	}

	private void onCancel() {
		// add your code here if necessary
		dispose();
	}
}
