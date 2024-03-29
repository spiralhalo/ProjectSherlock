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

package xyz.spiralhalo.sherlock.audit;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

import javax.swing.*;

import xyz.spiralhalo.sherlock.audit.persist.DayAudit;
import xyz.spiralhalo.sherlock.focus.ActiveSummaryCell;
import xyz.spiralhalo.sherlock.focus.ActiveSummaryModel;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.util.FormatUtil;
import xyz.spiralhalo.sherlock.util.ListUtil;
import xyz.spiralhalo.sherlock.util.img.IconUtil;

public class ViewDayAudit extends JDialog {
	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JTable tblProjects;
	private JButton btnAddMilestone;
	private JButton btnAddFinances;
	private JLabel lblDate;
	private JButton btnEditMilestone;
	private JButton btnDeleteMilestone;
	private JTable table1;
	private JTable table2;
	private JTextPane textPane1;
	private JButton btnAddNote;
	private JButton btnEditNote;
	private JButton btnDeleteNote;
	private JButton btnEditFinances;
	private JButton btnDeleteFinances;

	private final DayAudit dayAudit;
	private final ProjectList projectList;
	private boolean result = false;

	public ViewDayAudit(JFrame parent, LocalDate date, ProjectList projectList) {
		super(parent);
		setContentPane(contentPane);
		String dateText = FormatUtil.DTF_DATE_SELECTOR.format(date);
		setTitle(String.format("Day Audit for: %s", dateText));
		lblDate.setText(dateText);

		setModal(true);
		getRootPane().setDefaultButton(buttonOK);

		buttonOK.addActionListener(e -> onOK());
		buttonCancel.addActionListener(e -> onCancel());

		// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

		final JButton[] iconButtons = new JButton[]{btnAddFinances, btnAddMilestone, btnAddNote, btnDeleteMilestone,
				btnDeleteNote, btnEditMilestone, btnEditNote, btnEditFinances, btnDeleteFinances};
		for (JButton btn : iconButtons) {
			btn.setIcon(IconUtil.autoColor(btn.getIcon()));
		}

		ZoneId z = ZoneId.systemDefault();
		ArrayList<Project> relevantProjects = new ArrayList<>();

		for (Project p : ListUtil.extensiveIterator(projectList.getActiveProjects(), projectList.getFinishedProjects())) {
			if (!p.getStartDateTime().withZoneSameInstant(z).toLocalDate().isAfter(date)
					&& (!p.isFinished() || !p.getFinishedDate().withZoneSameInstant(z).toLocalDate().isBefore(date))) {
				relevantProjects.add(p);
			}
		}

		tblProjects.setDefaultRenderer(Object.class, new ActiveSummaryCell());
		tblProjects.setModel(new ActiveSummaryModel(relevantProjects));
		tblProjects.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblProjects.getTableHeader().setReorderingAllowed(false);

		dayAudit = AuditListMgr.getDayAudit(z, date);
		this.projectList = projectList;

		pack();
		setMinimumSize(contentPane.getSize());
		setLocationRelativeTo(parent);
	}

	private void refresh() {
		for (long p : dayAudit.getProjects()) {

		}
	}

	private void onOK() {
		result = true;
		dispose();
	}

	private void onCancel() {
		// add your code here if necessary
		dispose();
	}

	public boolean isResult() {
		return result;
	}
}
