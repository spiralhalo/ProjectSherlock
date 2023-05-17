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

package xyz.spiralhalo.sherlock.focus;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.util.img.ImgUtil;
import xyz.spiralhalo.sherlock.util.swing.DurationSelection;

public class FocusConfig extends JDialog {
	private JPanel contentPane;
	private JButton buttonOK;
	private JTable tblProjects;
	private JLabel lblStatus;
	private JComboBox<DurationSelection> cbTime;
	private JButton buttonNotOK;
	private JLabel lblProject;
	private JLabel lblGoal;
	private final FocusMgr mgr;

	public FocusConfig(JFrame parent, FocusMgr mgr) {
		super(parent);
		this.mgr = mgr;
		setTitle("Focus mode configuration");
		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);

		buttonOK.addActionListener(e -> onStart());
		buttonNotOK.addActionListener(e -> onStop());
		tblProjects.setDefaultRenderer(Object.class, new ActiveSummaryCell());
		tblProjects.setModel(new ActiveSummaryModel(mgr.getProjectList().getActiveProjects()));
		tblProjects.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblProjects.getTableHeader().setReorderingAllowed(false);

		refresh();

		// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

		// call onCancel() on ESCAPE
		contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		pack();
		setLocationRelativeTo(parent);
	}

	private void refresh() {
		if (FocusState.getInstance().isEnabled()) {
			Project p = FocusState.getInstance().getProject(mgr.getProjectList());
			if (p != null) {
				final ImgUtil circleIcon = ImgUtil.create("solid_circle.png", "Focus active indicator");
				circleIcon.tint(p.getColor()).outline(lblStatus.getForeground().getRGB(), 1);

				lblStatus.setText(String.format("Focus mode is currently active for %s", p));
				lblStatus.setIcon(new ImageIcon(circleIcon));
			} else {
				lblStatus.setText("Focus mode status is unknown. Please deactivate and reconfigure.");
			}
			lblProject.setEnabled(false);
			lblGoal.setEnabled(false);
			tblProjects.setEnabled(false);
			cbTime.setEnabled(false);
			buttonNotOK.setVisible(true);
			buttonOK.setVisible(false);
		} else {
			lblProject.setEnabled(true);
			lblGoal.setEnabled(true);
			tblProjects.setEnabled(true);
			cbTime.setEnabled(true);
			lblStatus.setText("Focus mode is currently inactive.");
//            lblStatus.setOpaque(false);
			lblStatus.setIcon(null);
			buttonNotOK.setVisible(false);
			buttonOK.setVisible(true);
		}
		pack();
	}

	@Override
	public void pack() {
		super.pack();
		setMinimumSize(getSize());
	}

	private void onStart() {
		Project p;
		if (tblProjects.getSelectedRow() != -1) {
			p = ((ActiveSummaryModel) tblProjects.getModel()).getProject(tblProjects.getSelectedRow());
		} else {
			p = null;
		}
		if (p != null) {
			mgr.turnOn(p, 1000 * ((DurationSelection) cbTime.getSelectedItem()).getValue());
			refresh();
		} else {
			JOptionPane.showMessageDialog(this, "Please choose a project.");
		}
	}

	private void onStop() {
		mgr.turnOff();
		refresh();
	}

	private void onCancel() {
		dispose();
	}

	private void createUIComponents() {
		DurationSelection[] x = new DurationSelection[25];
		x[0] = new DurationSelection();
		for (int i = 1; i < 25; i++) {
			x[i] = new DurationSelection(1800 * i, DurationSelection.HMSMode.Config);
		}
		cbTime = new JComboBox<>(x);
	}
}
