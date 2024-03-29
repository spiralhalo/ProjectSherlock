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

import static xyz.spiralhalo.sherlock.persist.project.ProjectList.EXE_LIST;
import static xyz.spiralhalo.sherlock.persist.project.ProjectList.USE_EXE;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileFilter;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.persist.project.UtilityTag;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig;

public class EditProject extends JDialog {
	public enum Mode {
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
	private JLabel labelNameLabel;
	private JLabel helpIcon;
	private JPanel panelTag;
	private JComboBox comboTagType;
	private JButton buttonPick;
	private JCheckBox cbWhitelist;
	private JTextField fieldWhitelist;
	private JButton btnBrowseExe;
	private JCheckBox checkRevTracking;
	private JSpinner spinnerFixed;
	private JCheckBox checkHourly;
	private JCheckBox checkFixed;
	private JLabel lblRevNA;
	private JSpinner spinnerHourly;
	private JComboBox comboCurrency;
	private JPanel panelRevenue;
	private Mode mode;
	private Project p;
	private ProjectList projectList;
	private boolean result;
	private final boolean utilityTag;
	private JFileChooser fileChooser = new JFileChooser();

	public EditProject(JFrame owner, ProjectList projectList, boolean utilityTag) {
		this(owner, Mode.NEW, null, projectList, utilityTag);
	}

	public EditProject(JFrame owner, Project project, ProjectList projectList, boolean utilityTag) {
		this(owner, Mode.EDIT, project, projectList, utilityTag);
	}

	private static final String[] REV_TYPE_S = new String[]{
			"Total (Calculated at finish)",
			"Hourly (Worked hours only)",
			"Monthly (Calculated at end of month)"
	};

	private static final int[] REV_TYPE_I = new int[]{
			0,
			1,
			2,
			3
	};

	private EditProject getThis() {
		return this;
	}

	public EditProject(JFrame owner, Mode mode, Project project, ProjectList projectList, boolean utilityTag) {
		super(owner, (mode == Mode.NEW ? "New " : "Edit ") + (utilityTag ? "activity" : "project"));
		this.utilityTag = utilityTag;
		if (project != null && utilityTag != project.isUtilityTag()) {
			throw new IllegalArgumentException("No editing projects in utility tag mode or vice versa.");
		}
		this.mode = mode;
		this.p = project;
		this.projectList = projectList;

		if (utilityTag) {
			labelNameLabel.setText("Activity name:");
			comboTagType.setModel(new DefaultComboBoxModel<>(new String[]{Project.PRODUCTIVE_LABEL + " (Work)", Project.RECREATIONAL_LABEL + " (Break)", Project.NON_PRODUCTIVE_LABEL}));
		} else {
			comboTagType.setModel(new DefaultComboBoxModel<>(new String[]{Project.PRODUCTIVE_LABEL + " (Work)", Project.RECREATIONAL_LABEL + " (Break)"}));
		}
//        if(utilityTag){
//            panelTag.setVisible(true);
/////*            if(Main.currentTheme.foreground != 0){
////                helpIcon.setIcon(ImgUtil.createTintedIcon(((ImageIcon)helpIcon.getIcon()).getImage(), Main.currentTheme.foreground));
////            }
////            helpIcon.addMouseListener(new MouseAdapter() {
////                @Override
////                public void mouseClicked(MouseEvent e) {
////                    new Help(getThis(), "tag types", "help_tag_type.html").setVisible(true);
////                }
////            });*/
//            labelNameLabel.setText("Activity name:");
//            comboTagType.setModel(new DefaultComboBoxModel<>(new String[]{UtilityTag.PRODUCTIVE_LABEL, UtilityTag.NON_PRODUCTIVE_LABEL}));
//        } else {
//            panelTag.setVisible(false);
//        }
		comboCat.setModel(new DefaultComboBoxModel<>(projectList.getCategories().toArray(new String[0])));

		if (mode == Mode.EDIT) {
			fieldName.setText(p.getName());
			comboCat.setSelectedItem(p.getCategory());
			fieldTag.setText(String.join(", ", p.getTags()));
			comboTagType.setSelectedIndex(p.getPtype());
//            if(p.isUtilityTag()) {
//            }
			cbWhitelist.setSelected(p.getAppendix(USE_EXE, Boolean.class) != null && p.getAppendix(USE_EXE, Boolean.class));
			if (p.getAppendix(EXE_LIST, String[].class) != null) {
				fieldWhitelist.setText(String.join(", ", p.getAppendix(EXE_LIST, String[].class)));
			}
		} else {
			if (AppConfig.getLastCategory() != null) {
				comboCat.setSelectedItem(AppConfig.getLastCategory());
			}
		}

		CaretListener validator = e -> validateInput();
		fieldTag.addCaretListener(validator);
		fieldName.addCaretListener(validator);
		validateInput();

		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setAcceptAllFileFilterUsed(true);

		FileFilter exeFilter = new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(".exe") || f.isDirectory();
			}

			@Override
			public String getDescription() {
				return "Executable files (*.exe)";
			}
		};
		fileChooser.addChoosableFileFilter(exeFilter);
		fileChooser.setFileFilter(exeFilter);

		setContentPane(contentPane);
		setModal(true);
		setMinimumSize(contentPane.getMinimumSize());
		getRootPane().setDefaultButton(buttonOK);
		pack();
		setMinimumSize(getSize());
		setLocationRelativeTo(owner);

		btnBrowseExe.addActionListener(e -> onBrowse());
		buttonOK.addActionListener(e -> onOK());
		buttonCancel.addActionListener(e -> onCancel());
		buttonPick.addActionListener(e -> useTagPicker());

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

		contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	private void onBrowse() {
		if (fileChooser.showDialog(this, "Select") == JFileChooser.APPROVE_OPTION) {
			File[] selected = fileChooser.getSelectedFiles();
			if (selected == null || selected.length == 0) return;
			String[] y = new String[selected.length];
			for (int i = 0; i < selected.length; i++) {
				y[i] = selected[i].getName().toLowerCase();
			}
			String trimmed = fieldWhitelist.getText().trim();
			StringBuilder sb = new StringBuilder();
			sb.append(trimmed);
			if (trimmed.length() > 0) {
				if (!trimmed.endsWith(",")) {
					sb.append(",");
				}
				sb.append(" ");
			}
			sb.append(String.join(",", y));
			fieldWhitelist.setText(sb.toString());
		}
		fileChooser.setSelectedFile(new File(""));
	}

	private void useTagPicker() {
		String[] tags = TagPicker.select(this);
		if (tags == null || tags.length == 0) return;
		String trimmed = fieldTag.getText().trim();
		boolean addComma = !trimmed.endsWith(",");
		fieldTag.setText(String.format("%s%s %s", trimmed, addComma ? "," : "", String.join(", ", tags)));
	}

	private void validateInput() {
		boolean enabled = fieldName.getText().trim().length() > 0;
		boolean warning = false;
		int trimLen;
		for (String x : fieldTag.getText().split(",")) {
			trimLen = x.trim().length();
			if (trimLen > 0 && trimLen < 4) {
				warning = true;
			}
			if (trimLen < 1) {
				enabled = false;
				break;
			}
		}
		buttonOK.setEnabled(enabled);
		labelWarning.setVisible(warning);
	}

	private void onOK() {
		if (comboCat.getSelectedItem() == null) {
			JOptionPane.showMessageDialog(this, "Please choose a category.");
			return;
		}
		// save last category
		AppConfig.setLastCategory((String) comboCat.getSelectedItem());

		Project x;

		// create (NEW) or set project (EDIT)
		if (mode == Mode.EDIT) {
			x = p;
		} else {
			if (utilityTag) {
				x = new UtilityTag(fieldName.getText(), String.valueOf(comboCat.getSelectedItem()), fieldTag.getText(), comboTagType.getSelectedIndex());
			} else {
				x = new Project(fieldName.getText(), String.valueOf(comboCat.getSelectedItem()), fieldTag.getText(), comboTagType.getSelectedIndex());
			}
		}

		// alter project (NEW and EDIT)
		if (cbWhitelist.isSelected()) {
			x.putAppendix(USE_EXE, true);
		} else if (x.getAppendix(USE_EXE, Boolean.class) != null) {
			x.putAppendix(USE_EXE, false);
		}
		if (fieldWhitelist.getText() != null && fieldWhitelist.getText().length() > 0) {
			String exe[] = fieldWhitelist.getText().split(",");
			for (int i = 0; i < exe.length; i++) {
				exe[i] = exe[i].trim().toLowerCase();
			}
			x.putAppendix(EXE_LIST, exe);
		}

		// add project (NEW) or save edited project (EDIT)
		if (mode == Mode.EDIT) {
			String category = p.getCategory();
			if (utilityTag) {
				projectList.editUtilityTag((UtilityTag) p, fieldName.getText(), String.valueOf(comboCat.getSelectedItem()), category, fieldTag.getText(), comboTagType.getSelectedIndex());
			} else {
				projectList.editProject(p, fieldName.getText(), String.valueOf(comboCat.getSelectedItem()), category, fieldTag.getText(), comboTagType.getSelectedIndex());
			}
		} else {
			if (utilityTag) {
				projectList.addProject(x);
			} else {
				projectList.addProject(x);
			}
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
