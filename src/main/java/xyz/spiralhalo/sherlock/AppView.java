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

import static xyz.spiralhalo.sherlock.util.ColorUtil.bad;
import static xyz.spiralhalo.sherlock.util.ColorUtil.gray;
import static xyz.spiralhalo.sherlock.util.ColorUtil.gut;
import static xyz.spiralhalo.sherlock.util.ColorUtil.interpolateNicely;
import static xyz.spiralhalo.sherlock.util.ColorUtil.multiply;
import static xyz.spiralhalo.sherlock.util.ColorUtil.neu;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.ArrayList;

import javax.swing.*;

import org.jfree.chart.ChartPanel;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserBool;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserInt;
import xyz.spiralhalo.sherlock.report.AllModel;
import xyz.spiralhalo.sherlock.report.Charts;
import xyz.spiralhalo.sherlock.report.DateSelection;
import xyz.spiralhalo.sherlock.report.DateSelectorModel;
import xyz.spiralhalo.sherlock.report.DayDurationCell;
import xyz.spiralhalo.sherlock.report.DurationCell;
import xyz.spiralhalo.sherlock.report.ProjectCell;
import xyz.spiralhalo.sherlock.report.StaticRankDecider;
import xyz.spiralhalo.sherlock.report.factory.charts.ChartData;
import xyz.spiralhalo.sherlock.report.factory.charts.ChartMeta;
import xyz.spiralhalo.sherlock.report.factory.summary.MonthSummary;
import xyz.spiralhalo.sherlock.report.factory.summary.YearList;
import xyz.spiralhalo.sherlock.report.factory.summary.YearSummary;
import xyz.spiralhalo.sherlock.report.factory.table.AllReportRow;
import xyz.spiralhalo.sherlock.report.factory.table.AllReportRows;
import xyz.spiralhalo.sherlock.util.FormatUtil;
import xyz.spiralhalo.sherlock.util.img.IconUtil;
import xyz.spiralhalo.sherlock.util.swing.WrapLayout;
import xyz.spiralhalo.sherlock.util.swing.thumb.ThumbManager;

public class AppView implements AppViewAccessor {

	private JPanel rootPane;
	private JTabbedPane tabs;
	private JProgressBar progress;
	private JTable tActive;
	private JTable tFinished;
	private JLabel lblRefresh;
	private JPanel pnlStatus;
	private JPanel pnlRefreshing;
	private JLabel lblTracking;
	private JPanel pnlDChart;
	private JComboBox<DateSelection<LocalDate>> comboD;
	private JComboBox<DateSelection<YearMonth>> comboM;
	private JComboBox<DateSelection<Year>> comboY;
	private JButton btnPrevD;
	private JButton btnNextD;
	private JTabbedPane tabr;
	private JLabel lDLogged;
	private JLabel lDWorktime;
	private JLabel lDRating;
	private JTable tUtility;
	private JButton btnFirstD;
	private JButton btnLastD;
	private JToolBar toolbarMain;
	private JButton btnNew;
	private JButton btnNewTag;
	private JButton btnView;
	private JButton btnFinish;
	private JButton btnResume;
	private JButton btnEdit;
	private JButton btnDelete;
	private JButton btnSettings;
	private JButton btnRefresh;
	private JButton btnInbox;
	private JButton btnBookmarks;
	private JButton btnUp;
	private JButton btnDown;
	private JLabel lDNoData;
	private JPanel pnlYChart;
	private JPanel pnlMChart;
	private JLabel lMNoData;
	private JLabel lYNoData;
	private JButton btnFirstM;
	private JButton btnPrevM;
	private JButton btnNextM;
	private JButton btnLastM;
	private JButton btnFirstY;
	private JButton btnPrevY;
	private JButton btnNextY;
	private JButton btnLastY;
	private JButton btnFocus;
	private JButton btnDayNote;
	private JButton btnDayAudit;
	private JPanel pnlDProgress;
	private JLabel lDBreaktime;
	private JButton btnDeepRefresh;
	private JButton btnExport;
	private JTabbedPane tabMain;
	private JPanel thumbsPane;
	private JComboBox<String> comboCate;
	private JComboBox comboSort;
	private JComboBox comboType;
	private JProgressBar pbBreakRatio;
	private JCommandButton cmdNew;
	private JCommandButton cmdEdit;
	private JCommandButton cmdDelete;
	private JCommandButton cmdView;
	private JCommandButton cmdFinish;
	private JCommandButton cmdResume;
	private JCommandButton cmdUp;
	private JCommandButton cmdDown;
	private JCommandButton cmdBookmarks;
	private JCommandButton cmdInbox;
	private JCommandButton cmdFocus;
	private JCommandButton cmdExport;
	private JCommandButton cmdSettings;
	private JCommandButton cmdRefresh;
	private ChartPanel dayProgressPanel;
	private ChartPanel dayPanel;
	private ChartPanel monthPanel;
	private ChartPanel yearPanel;
	private Charts.MonthChartInfo mChartInfo;
	private LocalDate today = LocalDate.now();
	private ThumbManager thumbManager = new ThumbManager();

	private final ZoneId z = Main.z;
	private final AppControl control;
	private final ArrayList<JComponent> enableOnSelect = new ArrayList<>();
	private JPopupMenu tablePopUpMenu;

	private final JFrame frame = new JFrame(Main.APP_TITLE);

	private void createCommandButtons(AppControl control) {
		if (Main.currentTheme == AppConfig.Theme.SYSTEM) {
			Main.applyButtonTheme(btnNew, btnNewTag, btnView, btnFinish, btnResume, btnEdit, btnDelete, btnSettings,
					btnBookmarks, btnInbox, btnRefresh);
			control.setToolbar(btnNew, btnNewTag, btnView, btnFinish, btnResume, btnEdit, btnDelete, btnUp, btnDown, btnExport, btnSettings, tabMain, tabs, tabr);
			control.setExtras(btnBookmarks, btnFocus);
			control.setRefresh(btnRefresh, btnDeepRefresh);
		} else {
			toolbarMain.removeAll();
			cmdNew = new JCommandButton("New");
			cmdEdit = new JCommandButton("Edit");
			cmdDelete = new JCommandButton("Delete");
			cmdView = new JCommandButton("View");
			cmdFinish = new JCommandButton("Finish");
			cmdResume = new JCommandButton("Resume");
			cmdBookmarks = new JCommandButton("Marks");
			cmdUp = new JCommandButton("Up");
			cmdDown = new JCommandButton("Down");
			cmdInbox = new JCommandButton("Inbox");
			cmdFocus = new JCommandButton("Focus");
			cmdExport = new JCommandButton("Export");
			cmdSettings = new JCommandButton("Settings");
			cmdRefresh = new JCommandButton("Refresh");

			JCommandButton[] iconButtons = new JCommandButton[]{
					cmdNew, cmdView, cmdFinish, cmdResume, cmdEdit, cmdDelete, cmdUp, cmdDown, cmdBookmarks, cmdInbox, cmdFocus, cmdExport, cmdSettings, cmdRefresh
			};
			for (JCommandButton btn : iconButtons) {
				btn.setIcon(IconUtil.createResizeableAutoColor(btn.getText().toLowerCase() + ".png", 24, 24));
			}

			toolbarMain.add(cmdNew);
			toolbarMain.add(cmdEdit);
			toolbarMain.add(cmdDelete);
			toolbarMain.addSeparator();
			toolbarMain.add(cmdView);
			toolbarMain.add(cmdFinish);
			toolbarMain.add(cmdResume);
			toolbarMain.add(cmdBookmarks);
			toolbarMain.add(cmdUp);
			toolbarMain.add(cmdDown);
			toolbarMain.addSeparator();
//            toolbarMain.add(cmdInbox);
			toolbarMain.add(cmdFocus);
			toolbarMain.add(cmdExport);
			toolbarMain.add(cmdSettings);
			toolbarMain.add(Box.createHorizontalGlue());
			toolbarMain.add(cmdRefresh);
			control.setToolbar(cmdNew, cmdView, cmdFinish, cmdResume, cmdEdit, cmdDelete, cmdUp, cmdDown, cmdExport, cmdSettings, tabMain, tabs, tabr);
			control.setExtras(cmdBookmarks, cmdFocus);
			control.setRefresh(cmdRefresh);
		}
	}

	AppView(AppControl control) {
		this.control = control;
		Main.applyButtonTheme(btnPrevD, btnNextD, btnFirstD, btnLastD);
		Main.applyButtonTheme(btnPrevM, btnNextM, btnFirstM, btnLastM);
		Main.applyButtonTheme(btnPrevY, btnNextY, btnFirstY, btnLastY);

		tActive.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tActive.setDefaultRenderer(String.class, new ProjectCell());
		tActive.setDefaultRenderer(Double.class, new DayDurationCell());
		tActive.setDefaultRenderer(Integer.class, new DurationCell());
		tFinished.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tFinished.setDefaultRenderer(String.class, new ProjectCell());
		tFinished.setDefaultRenderer(Double.class, new DayDurationCell());
		tFinished.setDefaultRenderer(Integer.class, new DurationCell());
		tUtility.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tUtility.setDefaultRenderer(String.class, new ProjectCell());
		tUtility.setDefaultRenderer(Integer.class, new DurationCell());

		((JLabel) comboD.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
		((JLabel) comboM.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
		((JLabel) comboY.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
	}

	private ChartPanel getDayProgressPanel() {
		if (dayProgressPanel == null) {
			dayProgressPanel = new ChartPanel(null, false, false, false, false, true);
			dayProgressPanel.setMinimumDrawWidth(260);
		}
		return dayProgressPanel;
	}

	private ChartPanel getDayPanel() {
		if (dayPanel == null) {
			dayPanel = Charts.emptyPanel();
		}
		return dayPanel;
	}

	private ChartPanel getMonthPanel() {
		if (monthPanel == null) {
			monthPanel = Charts.emptyPanel();
			control.setMonthChart(monthPanel);
		}
		return monthPanel;
	}

	private ChartPanel getYearPanel() {
		if (yearPanel == null) {
			yearPanel = Charts.emptyPanel();
		}
		return yearPanel;
	}

	public JFrame frame() {
		return frame;
	}

	@Override
	public void prePackInit() {
		createCommandButtons(control);
		control.setThumbs(thumbManager);
		control.setThumbToolbar(comboSort, comboCate, comboType);
		control.setDayButtons(btnDayNote, btnDayAudit);
		control.setTables(tActive, tFinished, tUtility);
		control.setChart(comboD, btnPrevD, btnNextD, btnFirstD, btnLastD, this::refreshDayChart);
		control.setChart(comboM, btnPrevM, btnNextM, btnFirstM, btnLastM, this::refreshMonthChart);
		control.setChart(comboY, btnPrevY, btnNextY, btnFirstY, btnLastY, this::refreshYearChart);

		frame.setContentPane(rootPane);
		frame.setPreferredSize(rootPane.getPreferredSize());
	}

	public void refreshRefreshStatus(CacheMgr cache) {
		if (cache.getCreated(AllReportRows.activeCacheId(z)).equals(CacheMgr.NEVER)) {
			lblRefresh.setText("Last refresh: n/a");
		} else {
			int seconds = (int) cache.getElapsed(AllReportRows.activeCacheId(z));
			if (seconds == 0) {
				lblRefresh.setText("Last refresh: just now");
			} else {
				lblRefresh.setText(String.format("Last refresh: %s ago", FormatUtil.hmsStrict(seconds)));
			}
		}
	}

	@Override
	public void refreshTrackingStatus(String status) {
		lblTracking.setText(status);
	}

	@Override
	public void refreshOverview(CacheMgr cache, String[] categories) {
		refreshRefreshStatus(cache);
		if (cache.getCreated(AllReportRows.activeCacheId(z)).equals(CacheMgr.NEVER)) {
			return;
		}
		refreshProjects(cache, 0);
		refreshProjects(cache, 1);
		refreshProjects(cache, 2);
		final YearList yl = cache.getObj(YearList.cacheId(z), YearList.class);
		if (yl == null) return;
		final ArrayList<LocalDate> dates = new ArrayList<>();
		final ArrayList<YearMonth> months = new ArrayList<>();
		for (Year y : yl) {
			final YearSummary ys = cache.getObj(YearSummary.cacheId(y, z), YearSummary.class);
			if (ys != null) {
				months.addAll(ys.getMonthList());
				for (YearMonth m : ys.getMonthList()) {
					final MonthSummary ms = cache.getObj(MonthSummary.cacheId(m, z), MonthSummary.class);
					if (ms != null) {
						dates.addAll(ms.getDayList());
					}
				}
			}
		}
		Object daySelected = comboD.getSelectedItem();
		int numDay = comboD.getItemCount();
		comboD.setModel(new DateSelectorModel<>(dates));
		if (comboD.getItemCount() == numDay && daySelected instanceof DateSelection &&
				dates.contains(((DateSelection) daySelected).date)) {
			comboD.setSelectedItem(daySelected);
		} else {
			comboD.setSelectedIndex(comboD.getModel().getSize() - 1);
		}
		Object monthSelected = comboM.getSelectedItem();
		int numMonth = comboM.getItemCount();
		comboM.setModel(new DateSelectorModel<>(months, FormatUtil.DTF_MONTH_SELECTOR));
		if (comboM.getItemCount() == numMonth && monthSelected instanceof DateSelection &&
				months.contains(((DateSelection) monthSelected).date)) {
			comboM.setSelectedItem(monthSelected);
		} else {
			comboM.setSelectedIndex(comboM.getModel().getSize() - 1);
		}
		Object yearSelected = comboY.getSelectedItem();
		int numYear = comboY.getItemCount();
		comboY.setModel(new DateSelectorModel<>(yl, FormatUtil.DTF_YEAR));
		if (comboY.getItemCount() == numYear && yearSelected instanceof DateSelection &&
				yl.contains(((DateSelection) yearSelected).date)) {
			comboY.setSelectedItem(yearSelected);
		} else {
			comboY.setSelectedIndex(comboY.getModel().getSize() - 1);
		}
		today = LocalDate.now();

		// do thumbs stuff
		int selectedSort = AppConfig.getThumbSort();
		if (selectedSort < 0 || selectedSort > 1) {
			comboSort.setSelectedIndex(0);
		} else {
			comboSort.setSelectedIndex(selectedSort);
		}
		String selectedCate = AppConfig.getFilterCategory();
		int selectedCateI = 0;
		comboCate.setModel(new DefaultComboBoxModel<>());
		comboCate.addItem("Show All");
		int j = 1;
		for (String cate : categories) {
			comboCate.addItem(cate);
			if (cate.equals(selectedCate))
				selectedCateI = j;
			j++;
		}
		comboCate.setSelectedIndex(selectedCateI);
		int selectedType = AppConfig.getFilterType();
		if (selectedType < 0 || selectedType > 2) {
			comboType.setSelectedIndex(0);
		} else {
			comboType.setSelectedIndex(selectedType);
		}
		refreshThumbs(cache);
	}

	@Override
	public void refreshThumbs(CacheMgr cache) {
		final AllReportRows activeRows = cache.getObj(AllReportRows.activeCacheId(z), AllReportRows.class);
//        thumbsPane.removeAll();
		int j = 0;
		ArrayList<AllReportRow> current;
		// sort is surprisingly very fast
		// sort
		if (comboSort.getSelectedIndex() == 0) {
			current = activeRows;
		} else {
			current = new ArrayList<>(activeRows);
			current.sort((allReportRow, t1) -> {
				long diff = t1.getLastWorkedOnMillis() - allReportRow.getLastWorkedOnMillis();
				if (diff < 0) return -1;
				if (diff > 0) return 1;
				return 0;
			});
		}

		// on the other hand, populate was very slow. also it cant be async due to swing
		// UPDATE: the cause of slowness was reloading AND resizing of thumbnail
		// UPDATE 2: optimized by caching the thumbnails in memory !!
		// populate thumbs
		boolean showAllCats = comboCate.getSelectedIndex() == 0;
		boolean showAllTypes = comboType.getSelectedIndex() == 0;
		Object cat = comboCate.getSelectedItem();
		String type = (comboType.getSelectedItem() != null ? (String) comboType.getSelectedItem() : "");
		for (AllReportRow row : current) {
			// filter
			if ((showAllCats || row.getCategory().equals(cat))
					&& (showAllTypes || type.startsWith(row.getPTypeLabel()))) {
				if (thumbManager.size() <= j) {
					thumbManager.newThumb(row.getProjectName(), row.getProjectHash(), row.getLastWorkedOnMillis());
				} else {
					thumbManager.getThumb(j).set(row.getProjectName(), row.getProjectHash(), row.getLastWorkedOnMillis());
				}
				if (j >= thumbsPane.getComponentCount()) {
					thumbsPane.add(thumbManager.getThumb(j).getPane());
				}
				j++;
			}
		}
		for (int i = thumbsPane.getComponentCount() - 1; i >= j; i--) {
			thumbsPane.remove(i);
		}
		thumbsPane.updateUI();
	}

	// TODO: fix this dumb redundant code ????
	public void refreshProjects(CacheMgr cache, int index) {
		switch (index) {
			case 0:
				long sA = selected(0);
				final AllReportRows activeRows = cache.getObj(AllReportRows.activeCacheId(z), AllReportRows.class);
				if (tActive.getModel() instanceof AllModel) {
					((AllModel) tActive.getModel()).reset(activeRows);
				} else {
					final AllModel allModel = new AllModel(activeRows);
					tActive.setModel(allModel);
					allModel.setTableColumnWidths(tActive);
				}
				setSelected(0, sA);
				break;
			case 1:
				long sF = selected(1);
				final AllReportRows finishedRows = cache.getObj(AllReportRows.finishedCacheId(z), AllReportRows.class);
				if (tFinished.getModel() instanceof AllModel) {
					((AllModel) tFinished.getModel()).reset(finishedRows);
				} else {
					final AllModel allModel = new AllModel(finishedRows);
					tFinished.setModel(allModel);
					allModel.setTableColumnWidths(tFinished);
				}
				setSelected(1, sF);
				break;
			case 2:
				long sU = selected(2);
				final AllReportRows utilityTagsRows = cache.getObj(AllReportRows.utilityCacheId(z), AllReportRows.class);
				if (tUtility.getModel() instanceof AllModel) {
					((AllModel) tUtility.getModel()).reset(utilityTagsRows);
				} else {
					final AllModel allModel = new AllModel(utilityTagsRows, true);
					tUtility.setModel(allModel);
					allModel.setTableColumnWidths(tUtility);
				}
				setSelected(2, sU);
				break;
		}
	}

	private void notifyNoDayData() {
		notifyNoData(pnlDChart, lDNoData, comboD, btnPrevD, btnNextD, btnFirstD, btnLastD);
		pnlDProgress.removeAll();
	}

	public void refreshDayChart(CacheMgr cache, ItemEvent event) {
		if (event.getStateChange() != ItemEvent.SELECTED) return;
		DateSelection<LocalDate> s = (DateSelection<LocalDate>) comboD.getSelectedItem();
		if (s == null) {
			notifyNoDayData();
			return;
		}
		final MonthSummary ms = cache.getObj(MonthSummary.cacheId(YearMonth.from(s.date), z), MonthSummary.class);
		if (ms == null) {
			notifyNoDayData();
			return;
		}
		final ChartData cd = ms.getDayCharts().get(s.date);
		if (cd == null) {
			notifyNoDayData();
			return;
		}
		final ChartMeta meta = cd.getMeta();
		// Create day chart
		getDayPanel().setChart(Charts.createDayBarChart(cd));
		refreshChart(getDayPanel(), pnlDChart, comboD, btnPrevD, btnNextD, btnFirstD, btnLastD);
		// Create day progress chart
		pnlDProgress.removeAll();
		getDayProgressPanel().setChart(Charts.createDayProgressChart(cd, s.date.equals(today)));
		pnlDProgress.add(getDayProgressPanel());
		pnlDProgress.updateUI();
		// Create rating label
		int t = UserInt.DAILY_TARGET_SECOND.get();
		lDLogged.setText(String.format("Logged: %s", FormatUtil.hms(meta.getLogDur())));
		lDWorktime.setText(String.format("Work: %s", FormatUtil.hms(meta.getWorkDur())));
		if (meta.getBreakDur() > 0) {
			lDBreaktime.setVisible(true);
			lDBreaktime.setText(String.format("Break: %s", FormatUtil.hms(meta.getBreakDur())));
//            pbBreakRatio.setValue(meta.getWorkDur()*100/(meta.getWorkDur()+meta.getBreakDur()));
		} else {
//            pbBreakRatio.setValue(100);
			lDBreaktime.setVisible(false);
		}
		if (UserBool.OLD_RATING.get()) {
			int r = meta.getLogDur() == 0 ? 0 : (meta.getLogDur() < t ? meta.getWorkDur() * 100 / meta.getLogDur() : meta.getWorkDur() * 100 / t);
			if (!UserBool.EXCEED_100_PERCENT.get() && r > 100) r = 100;
			if (UserConfig.userGWDay(s.date.get(ChronoField.DAY_OF_WEEK))) {
				lDRating.setText(String.format("Rating: %d%%", r));
				Color ratioFG = interpolateNicely((float) r / 100f, bad, neu, gut);
				lDRating.setForeground(Main.currentTheme.dark ? ratioFG : multiply(gray, ratioFG));
			} else {
				lDRating.setText(String.format("Rating: %d%% (holiday)", r));
				lDRating.setForeground(new Color(0x77000000 | Main.currentTheme.foreground, true));
			}
		} else {
			int r = meta.getWorkDur() * 100 / t;
			if (!UserBool.EXCEED_100_PERCENT.get() && r > 100) r = 100;
			StaticRankDecider.Rank rank = StaticRankDecider.decide(r);
			if (rank == StaticRankDecider.Rank.BREAK_DAY) {
				lDRating.setText(rank.label.toUpperCase());
				lDRating.setForeground(new Color(0x77000000 | Main.currentTheme.foreground, true));
			} else {
				lDRating.setText(String.format("%s (%d)", rank.label.toUpperCase(), r));
				lDRating.setForeground(Main.currentTheme.dark ? rank.color : multiply(gray, rank.color));
			}
		}
	}

	public void refreshMonthChart(CacheMgr cache, ItemEvent event) {
		if (event.getStateChange() != ItemEvent.SELECTED) return;
		DateSelection<YearMonth> selected = (DateSelection<YearMonth>) comboM.getSelectedItem();
		if (selected == null) {
			notifyNoData(pnlMChart, lMNoData, comboM, btnPrevM, btnNextM, btnFirstM, btnLastM);
			return;
		}
		final MonthSummary s = cache.getObj(MonthSummary.cacheId(selected.date, z), MonthSummary.class);
		if (s == null) {
			notifyNoData(pnlMChart, lMNoData, comboM, btnPrevM, btnNextM, btnFirstM, btnLastM);
			return;
		}
		Charts.MonthChartInfo info = Charts.createMonthBarChart(s);
		mChartInfo = info;
		getMonthPanel().setChart(info.chart);
		refreshChart(getMonthPanel(), pnlMChart, comboM, btnPrevM, btnNextM, btnFirstM, btnLastM);
	}

	public void refreshYearChart(CacheMgr cache, ItemEvent event) {
		if (event.getStateChange() != ItemEvent.SELECTED) return;
		DateSelection<Year> s = (DateSelection<Year>) comboY.getSelectedItem();
		if (s == null) {
			notifyNoData(pnlYChart, lYNoData, comboY, btnPrevY, btnNextY, btnFirstY, btnLastY);
			return;
		}
		final YearSummary ys = cache.getObj(YearSummary.cacheId(s.date, z), YearSummary.class);
		if (ys == null) {
			notifyNoData(pnlYChart, lYNoData, comboY, btnPrevY, btnNextY, btnFirstY, btnLastY);
			return;
		}
		getYearPanel().setChart(Charts.createYearBarChart(ys));
		refreshChart(getYearPanel(), pnlYChart, comboY, btnPrevY, btnNextY, btnFirstY, btnLastY);
	}

	private void refreshChart(ChartPanel cp, JPanel p, JComboBox cmb, JButton pr, JButton nx, JButton fs, JButton ls) {
		p.removeAll();
		p.add(cp);
		p.updateUI();
		pr.setEnabled(cmb.getSelectedIndex() > 0);
		nx.setEnabled(cmb.getSelectedIndex() < cmb.getItemCount() - 1);
		fs.setEnabled(cmb.getSelectedIndex() > 0);
		ls.setEnabled(cmb.getSelectedIndex() < cmb.getItemCount() - 1);
	}

	private void notifyNoData(JPanel p, JLabel lND, JComboBox cmb, JButton pr, JButton nx, JButton fs, JButton ls) {
		p.removeAll();
		p.add(lND);
		p.updateUI();
		pr.setEnabled(cmb.getSelectedIndex() > 0);
		nx.setEnabled(cmb.getSelectedIndex() < cmb.getItemCount() - 1);
		fs.setEnabled(cmb.getSelectedIndex() > 0);
		ls.setEnabled(cmb.getSelectedIndex() < cmb.getItemCount() - 1);
	}

	public long selected() {
		if (tabMain.getSelectedIndex() == 1)
			return selected(tabs.getSelectedIndex());
		else
			return thumbManager.getSelection();
	}

	private long selected(int i) {
		if (i == 0 && tActive.getSelectedRow() != -1) {
			return ((AllModel) tActive.getModel())
					.getProjectHash(tActive.convertRowIndexToModel(tActive.getSelectedRow()));
		} else if (i == 1 && tFinished.getSelectedRow() != -1) {
			return ((AllModel) tFinished.getModel())
					.getProjectHash(tFinished.convertRowIndexToModel(tFinished.getSelectedRow()));
		} else if (i == 2 && tUtility.getSelectedRow() != -1) {
			return ((AllModel) tUtility.getModel())
					.getProjectHash(tUtility.convertRowIndexToModel(tUtility.getSelectedRow()));
		}
		return -1;
	}

	public int selectedIndex() {
		switch (tabs.getSelectedIndex()) {
			case 0:
				return tActive.convertRowIndexToModel(tActive.getSelectedRow());
			case 1:
				return tFinished.convertRowIndexToModel(tFinished.getSelectedRow());
			case 2:
				return tUtility.convertRowIndexToModel(tUtility.getSelectedRow());
			default:
				return -1;
		}
	}

	public void setSelected(long hash) {
		setSelected(tabs.getSelectedIndex(), hash);
	}

	private void setSelected(int ix, long hash) {
		if (ix == 0 && tActive.getModel() instanceof AllModel) {
			int x = ((AllModel) tActive.getModel()).findIndex(hash);
			if (x == -1) return;
			int i = tActive.convertRowIndexToView(x);
			tActive.setRowSelectionInterval(i, i);
		} else if (ix == 1 && tFinished.getModel() instanceof AllModel) {
			int x = ((AllModel) tFinished.getModel()).findIndex(hash);
			if (x == -1) return;
			int i = tFinished.convertRowIndexToView(x);
			tFinished.setRowSelectionInterval(i, i);
		} else if (ix == 2 && tUtility.getModel() instanceof AllModel) {
			int x = ((AllModel) tUtility.getModel()).findIndex(hash);
			if (x == -1) return;
			int i = tUtility.convertRowIndexToView(x);
			tUtility.setRowSelectionInterval(i, i);
		}
	}

	@Override
	public ArrayList<JComponent> enableOnSelect() {
		return enableOnSelect;
	}

	@Override
	public JComponent toHideOnRefresh() {
		return lblRefresh;
	}

	@Override
	public JComponent getToShowOnRefresh() {
		return pnlRefreshing;
	}

	@Override
	public JTabbedPane getTabMain() {
		return tabMain;
	}

	@Override
	public JTabbedPane getTabProjects() {
		return tabs;
	}

	@Override
	public JTabbedPane getTabReports() {
		return tabr;
	}

	@Override
	public JComponent getButtonFinish() {
		if (cmdFinish != null) return cmdFinish;
		return btnFinish;
	}

	@Override
	public JComponent getButtonResume() {
		if (cmdResume != null) return cmdResume;
		return btnResume;
	}

	@Override
	public JComponent getButtonBookmarks() {
		if (cmdBookmarks != null) return cmdBookmarks;
		return btnBookmarks;
	}

	@Override
	public JComponent getButtonUp() {
		if (cmdUp != null) return cmdUp;
		return btnUp;
	}

	@Override
	public JComponent getButtonDown() {
		if (cmdDown != null) return cmdDown;
		return btnDown;
	}

	@Override
	public void setTablePopUpMenu(JPopupMenu popUpMenu) {
		tablePopUpMenu = popUpMenu;
	}

	@Override
	public JPopupMenu getTablePopUpMenu() {
		return tablePopUpMenu;
	}

	@Override
	public Charts.MonthChartInfo getMonthChartInfo() {
		return mChartInfo;
	}

	@Override
	public LocalDate getSelectedDayChart() {
		return ((DateSelection<LocalDate>) comboD.getSelectedItem()).date;
	}

	@Override
	public JTable getTableFinished() {
		return tFinished;
	}

	@Override
	public JTable getTableActive() {
		return tActive;
	}

	private void createUIComponents() {
		comboD = new JComboBox<>();
		comboM = new JComboBox<>();
		comboY = new JComboBox<>();
		thumbsPane = new JPanel(new WrapLayout(FlowLayout.LEFT, 0, 0));
	}
}
