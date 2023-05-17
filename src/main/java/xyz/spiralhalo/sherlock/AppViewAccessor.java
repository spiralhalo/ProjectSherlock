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

package xyz.spiralhalo.sherlock;

import java.awt.event.ItemEvent;
import java.time.LocalDate;
import java.util.ArrayList;

import javax.swing.*;

import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;
import xyz.spiralhalo.sherlock.report.Charts;

public interface AppViewAccessor {
	JFrame frame();

	void prePackInit();

	void refreshRefreshStatus(CacheMgr cache);

	void refreshTrackingStatus(String status);

	void refreshOverview(CacheMgr cache, String[] categories);

	void refreshThumbs(CacheMgr cache);

	void refreshProjects(CacheMgr cache, int index);

	void refreshDayChart(CacheMgr cache, ItemEvent event);

	void refreshMonthChart(CacheMgr cache, ItemEvent event);

	void refreshYearChart(CacheMgr cache, ItemEvent event);

	long selected();

	int selectedIndex();

	void setSelected(long hash);

	ArrayList<JComponent> enableOnSelect();

	JComponent toHideOnRefresh();

	JComponent getToShowOnRefresh();

	JTabbedPane getTabMain();

	JTabbedPane getTabProjects();

	JTabbedPane getTabReports();

	JComponent getButtonFinish();

	JComponent getButtonResume();

	JComponent getButtonBookmarks();

	JComponent getButtonUp();

	JComponent getButtonDown();

	void setTablePopUpMenu(JPopupMenu popupMenu);

	JPopupMenu getTablePopUpMenu();

	Charts.MonthChartInfo getMonthChartInfo();

	LocalDate getSelectedDayChart();

	JTable getTableFinished();

	JTable getTableActive();
}
