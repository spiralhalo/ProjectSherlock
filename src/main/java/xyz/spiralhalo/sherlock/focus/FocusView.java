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

import java.awt.*;

import javax.swing.*;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.util.ColorUtil;
import xyz.spiralhalo.sherlock.util.FormatUtil;
import xyz.spiralhalo.sherlock.util.swing.Dragger;

public class FocusView {
	private JLabel lblProject;
	private JPanel rootPanel;
	private JLabel lblHint;
	private JLabel lblHint2;
	private JLabel lblHMS;
	private JLabel lblTimeHint;
	private JFrame view;
	private Timer showTimer;
	private boolean showing = false;

	public FocusView(Project project) {
		if (project != null) {
			// radiance workaround
//            ActiveSummaryCell renderer = new ActiveSummaryCell();
//            renderer.setHorizontalAlignment(SwingConstants.CENTER);
//            tblProjectName.setDefaultRenderer(Object.class, renderer);
//            tblProjectName.setModel(new ActiveSummaryModel(Arrays.asList(project)));
//            tblProjectName.setRowSelectionAllowed(false);
//            tblProjectName.setColumnSelectionAllowed(false);
//            lblProject.setVisible(false);
			lblProject.setText(project.toString().toUpperCase());
			lblProject.setForeground(new Color(project.getColor()));
		} else {
//            tblProjectName.setVisible(false);
			lblProject.setText("UNKNOWN (error; please reconfigure)");
		}
		refreshDuration();
		lblHint.setForeground(ColorUtil.gray);
		lblHint2.setForeground(ColorUtil.gray);
		view = new JFrame();
		view.setFocusableWindowState(false);
		view.setContentPane(rootPanel);
		view.setUndecorated(true);
		view.pack();
		view.setMinimumSize(view.getSize());
		view.setLocationRelativeTo(null);
		view.setLocation(view.getX(), 24);
		view.setAlwaysOnTop(true);
		Dragger.makeDraggable(view, rootPanel);
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		boolean uniform = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);
		boolean perpixel = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT);
		if (uniform && !perpixel) {
			view.setOpacity(0.6f);
		} else if (perpixel) {
			view.setBackground(new Color(33, 33, 33, 150));
		}
	}

	public void setVisible(boolean visible) {
		if (visible && !showing && !view.isVisible()) {
			showing = true;
			if (showTimer != null) {
				showTimer.restart();
			} else {
				showTimer = new Timer(5000, e -> {
					refreshDuration();
					view.setVisible(true);
					showTimer.stop();
					showing = false;
				});
				showTimer.start();
			}
		} else if (!visible && view.isVisible()) {
			view.setVisible(false);
			showing = false;
		}
	}

	private void refreshDuration() {
		long duration = FocusState.getInstance().getDuration();
		if (duration >= 0) {
			lblTimeHint.setForeground(ColorUtil.gray);
			lblHMS.setText(FormatUtil.hmsHomoColon((int) (duration / 1000)));
		} else {
			lblTimeHint.setVisible(false);
			lblHMS.setVisible(false);
		}
	}

	public void dispose() {
		if (showTimer != null) {
			showTimer.stop();
		}
		view.dispose();
	}
}
