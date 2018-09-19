package xyz.spiralhalo.sherlock;

import org.jfree.chart.ChartPanel;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserInt;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserNode;
import xyz.spiralhalo.sherlock.report.*;
import xyz.spiralhalo.sherlock.report.Charts;
import xyz.spiralhalo.sherlock.report.factory.charts.ChartData;
import xyz.spiralhalo.sherlock.report.factory.charts.ChartMeta;
import xyz.spiralhalo.sherlock.report.factory.summary.MonthSummary;
import xyz.spiralhalo.sherlock.report.factory.summary.YearList;
import xyz.spiralhalo.sherlock.report.factory.summary.YearSummary;
import xyz.spiralhalo.sherlock.report.factory.table.AllReportRows;
import xyz.spiralhalo.sherlock.util.FormatUtil;
import xyz.spiralhalo.sherlock.util.ImgUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.temporal.ChronoField;
import java.util.ArrayList;

import static xyz.spiralhalo.sherlock.util.ColorUtil.*;

public class MainView implements MainViewAccessor {

    private JPanel rootPane;
    private JTabbedPane tabs;
    private JProgressBar progress;
    private JTable tblActive;
    private JTable tblFinished;
    private JLabel lblRefresh;
    private JPanel pnlStatus;
    private JPanel pnlRefreshing;
    private JLabel lblTracking;
    private JPanel pnlDayChart;
    private JComboBox<DateSelection<LocalDate>> comboDayCharts;
    private JComboBox<DateSelection<YearMonth>> comboMonthCharts;
    private JComboBox<DateSelection<Year>> comboYearCharts;
    private JButton btnPrevChart;
    private JButton btnNextChart;
    private JTabbedPane tabr;
    private JLabel lblLogged;
    private JLabel lblWorktime;
    private JLabel lblRatio;
    private JTable tblUtilityTags;
    private JButton btnFirstChart;
    private JButton btnLastChart;
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
    private JLabel lblNoData;
    private JPanel pnlYearChart;
    private JPanel pnlMonthChart;
    private JLabel lblMonthNoData;
    private JLabel lblYearNoData;
    private JButton btnMonthFirstChart;
    private JButton btnMonthPrevChart;
    private JButton btnMonthNextChart;
    private JButton btnMonthLastChart;
    private JButton btnYearFirstChart;
    private JButton btnYearPrevChart;
    private JButton btnYearNextChart;
    private JButton btnYearLastChart;
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
    private JCommandButton cmdSettings;
    private JCommandButton cmdRefresh;
    private ChartPanel dayPanel;
    private ChartPanel monthPanel;
    private ChartPanel yearPanel;
    private Charts.MonthChartInfo monthChartInfo;

    private final ZoneId z = ZoneId.systemDefault();
    private final MainControl control;
    private final ArrayList<JComponent> enableOnSelect = new ArrayList<>();
    private JPopupMenu tablePopUpMenu;

    private final JFrame frame = new JFrame(Main.APP_TITLE);

    private void createCommandButtons(MainControl control){
        if(Main.currentTheme == AppConfig.Theme.SYSTEM){
            Main.applyButtonTheme(btnNew, btnNewTag, btnView, btnFinish, btnResume, btnEdit, btnDelete, btnSettings,
                    btnBookmarks, btnInbox, btnRefresh);
            control.setToolbar(btnNew, btnNewTag, btnView, btnFinish, btnResume, btnEdit, btnDelete, btnUp, btnDown, btnSettings, tabs, tabr);
            control.setExtras(btnBookmarks);
            control.setRefresh(btnRefresh);
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
            cmdSettings = new JCommandButton("Settings");
            cmdRefresh = new JCommandButton("Refresh");

            JCommandButton[] iconButtons = new JCommandButton[]{
                    cmdNew, cmdView, cmdFinish, cmdResume, cmdEdit, cmdDelete, cmdUp, cmdDown, cmdBookmarks, cmdInbox, cmdSettings, cmdRefresh
            };
            for (JCommandButton btn : iconButtons) {
                btn.setIcon(ImgUtil.autoColorIcon(btn.getText().toLowerCase() + ".png", 24, 24));
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
            toolbarMain.add(cmdSettings);
            toolbarMain.add(Box.createHorizontalGlue());
            toolbarMain.add(cmdRefresh);
            control.setToolbar(cmdNew, cmdView, cmdFinish, cmdResume, cmdEdit, cmdDelete, cmdUp, cmdDown, cmdSettings, tabs, tabr);
            control.setExtras(cmdBookmarks);
            control.setRefresh(cmdRefresh);
        }
    }

    MainView(MainControl control){
        this.control = control;
        Main.applyButtonTheme(btnPrevChart, btnNextChart, btnFirstChart, btnLastChart);
        Main.applyButtonTheme(btnMonthPrevChart, btnMonthNextChart, btnMonthFirstChart, btnMonthLastChart);
        Main.applyButtonTheme(btnYearPrevChart, btnYearNextChart, btnYearFirstChart, btnYearLastChart);

        tblActive.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblActive.setDefaultRenderer(String.class, new ProjectCell());
        tblFinished.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblFinished.setDefaultRenderer(String.class, new ProjectCell());
        tblUtilityTags.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblUtilityTags.setDefaultRenderer(String.class, new ProjectCell());
//        tblDaily.setDefaultRenderer(String.class, new DefaultTableCellRenderer());
//        tblMonthly.setDefaultRenderer(String.class, new DefaultTableCellRenderer());
//        tblDaily.setDefaultRenderer(Integer.class, new DurationCell(true));
//        tblMonthly.setDefaultRenderer(Integer.class, new DurationCell());

        ((JLabel) comboDayCharts.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
        ((JLabel) comboMonthCharts.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
        ((JLabel) comboYearCharts.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
    }

    private ChartPanel getDayPanel() {
        if(dayPanel==null){
            dayPanel = Charts.emptyPanel();
        }
        return dayPanel;
    }

    private ChartPanel getMonthPanel() {
        if(monthPanel==null){
            monthPanel = Charts.emptyPanel();
            control.setMonthChart(monthPanel);
        }
        return monthPanel;
    }

    private ChartPanel getYearPanel() {
        if(yearPanel==null){
            yearPanel = Charts.emptyPanel();
        }
        return yearPanel;
    }

    public JFrame frame() {
        return frame;
    }

    @Override
    public void init() {
        createCommandButtons(control);
        control.setTables(tblActive, tblFinished, tblUtilityTags);
        control.setChart(comboDayCharts, btnPrevChart, btnNextChart, btnFirstChart, btnLastChart, this::refreshDayChart);
        control.setChart(comboMonthCharts, btnMonthPrevChart, btnMonthNextChart, btnMonthFirstChart, btnMonthLastChart, this::refreshMonthChart);
        control.setChart(comboYearCharts, btnYearPrevChart, btnYearNextChart, btnYearFirstChart, btnYearLastChart, this::refreshYearChart);

        frame.setContentPane(rootPane);
        frame.setMinimumSize(rootPane.getMinimumSize());
        frame.setPreferredSize(rootPane.getMinimumSize());
        frame.pack();
        frame.setLocationByPlatform(true);
    }

    public void refreshStatus(CacheMgr cache) {
        if (cache.getCreated(AllReportRows.activeCacheId(z)).equals(CacheMgr.NEVER)) {
            lblRefresh.setText("Last refresh: never");
            lblTracking.setText("Refresh required.");
        } else {
            int seconds = (int)cache.getElapsed(AllReportRows.activeCacheId(z));
            if (seconds == 0) {
                lblRefresh.setText("Last refresh: just now");
            } else {
                lblRefresh.setText(String.format("Last refresh: %s ago", FormatUtil.hmsStrict(seconds)));
            }
            int size = cache.getObj(AllReportRows.activeCacheId(z), AllReportRows.class).size();
            lblTracking.setText(String.format("Tracking %d project%s", size, size>1?"s":""));
        }
    }

    public void refreshOverview(CacheMgr cache) {
        refreshStatus(cache);
        if(cache.getCreated(AllReportRows.activeCacheId(z)).equals(CacheMgr.NEVER)){return;}
        refreshProjects(cache, 0);
        refreshProjects(cache, 1);
        refreshProjects(cache, 2);
//        final DayModel dayModel = new DayModel(cache.getObj(CacheId.DayRows, ReportRows.class));
//        final MonthModel monthModel = new MonthModel(cache.getObj(CacheId.MonthRows, ReportRows.class));
//        tblDaily.setModel(dayModel);
//        tblMonthly.setModel(monthModel);
        final YearList yl = cache.getObj(YearList.cacheId(z), YearList.class);
        if(yl == null)return;
        final ArrayList<LocalDate> dates = new ArrayList<>();
        final ArrayList<YearMonth> months = new ArrayList<>();
        for (Year y:yl) {
            final YearSummary ys = cache.getObj(YearSummary.cacheId(y, z), YearSummary.class);
            if(ys != null){
                months.addAll(ys.getMonthList());
                for (YearMonth m:ys.getMonthList()) {
                    final MonthSummary ms = cache.getObj(MonthSummary.cacheId(m, z), MonthSummary.class);
                    if(ms != null){
                        dates.addAll(ms.getDayList());
                    }
                }
            }
        }
        Object daySelected = comboDayCharts.getSelectedItem();
        comboDayCharts.setModel(new DateSelectorModel<>(dates));
        if(daySelected instanceof DateSelection && dates.contains(((DateSelection) daySelected).date)){
            comboDayCharts.setSelectedItem(daySelected);
        } else {
            comboDayCharts.setSelectedIndex(comboDayCharts.getModel().getSize() - 1);
        }
        Object monthSelected = comboMonthCharts.getSelectedItem();
        comboMonthCharts.setModel(new DateSelectorModel<>(months, FormatUtil.DTF_MONTH_SELECTOR));
        if(monthSelected instanceof DateSelection && months.contains(((DateSelection) monthSelected).date)){
            comboMonthCharts.setSelectedItem(monthSelected);
        } else {
            comboMonthCharts.setSelectedIndex(comboMonthCharts.getModel().getSize() - 1);
        }
        Object yearSelected = comboYearCharts.getSelectedItem();
        comboYearCharts.setModel(new DateSelectorModel<>(yl, FormatUtil.DTF_YEAR));
        if(yearSelected instanceof DateSelection && yl.contains(((DateSelection) yearSelected).date)){
            comboYearCharts.setSelectedItem(yearSelected);
        } else {
            comboYearCharts.setSelectedIndex(comboYearCharts.getModel().getSize() - 1);
        }
    }

    public void refreshProjects(CacheMgr cache, int index){
        switch (index){
            case 0:
                final AllReportRows activeRows = cache.getObj(AllReportRows.activeCacheId(z), AllReportRows.class);
                if(tblActive.getModel() instanceof AllModel){
                    ((AllModel) tblActive.getModel()).reset(activeRows);
                } else {
                    final AllModel allModel = new AllModel(activeRows);
                    tblActive.setModel(allModel);
                    allModel.setTableColumnWidths(tblActive);
                }
                break;
            case 1:
                final AllReportRows finishedRows = cache.getObj(AllReportRows.finishedCacheId(z), AllReportRows.class);
                if(tblFinished.getModel() instanceof AllModel){
                    ((AllModel) tblFinished.getModel()).reset(finishedRows);
                } else {
                    final AllModel allModel = new AllModel(finishedRows);
                    tblFinished.setModel(allModel);
                    allModel.setTableColumnWidths(tblFinished);
                }
                break;
            case 2:
                final AllReportRows utilityTagsRows = cache.getObj(AllReportRows.utilityCacheId(z), AllReportRows.class);
                if(tblUtilityTags.getModel() instanceof AllModel){
                    ((AllModel) tblUtilityTags.getModel()).reset(utilityTagsRows);
                } else {
                    final AllModel allModel = new AllModel(utilityTagsRows, true);
                    tblUtilityTags.setModel(allModel);
                    allModel.setTableColumnWidths(tblUtilityTags);
                }
                break;
        }
    }

    public void refreshDayChart(CacheMgr cache, ItemEvent event) {
        if(event.getStateChange() != ItemEvent.SELECTED) return;
        DateSelection<LocalDate> selected = (DateSelection<LocalDate>) comboDayCharts.getSelectedItem();
        pnlDayChart.removeAll();
        if (selected == null) { pnlDayChart.add(lblNoData); return;}
        final MonthSummary summary = cache.getObj(MonthSummary.cacheId(YearMonth.from(selected.date), z), MonthSummary.class);
        if (summary == null) { pnlDayChart.add(lblNoData); return;}
        final ChartData dayChart = summary.getDayCharts().get(selected.date);
        if (dayChart == null) { pnlDayChart.add(lblNoData); return;}
        final ChartMeta meta = dayChart.getMeta();
        getDayPanel().setChart(Charts.createDayBarChart(dayChart));
        pnlDayChart.setPreferredSize(new Dimension(-1, 220));
        pnlDayChart.add(getDayPanel());
        pnlDayChart.updateUI();
        int target = UserConfig.getInt(UserNode.TRACKING, UserInt.DAILY_TARGET_SECOND);
        int ratio = meta.getLogDur() == 0 ? 0 : (meta.getLogDur() < target ? meta.getWorkDur() * 100 / meta.getLogDur() : meta.getWorkDur() * 100 / target);
        lblLogged.setText(String.format("Logged: %s", FormatUtil.hms(meta.getLogDur())));
        lblWorktime.setText(String.format("Project: %s", FormatUtil.hms(meta.getWorkDur())));
        if (UserConfig.isWorkDay(selected.date.get(ChronoField.DAY_OF_WEEK))) {
            lblRatio.setText(String.format("Rating: %d%%", ratio));
            Color ratioFG = interpolateNicely((float) ratio / 100f, bad, neu, gut);
            lblRatio.setForeground(AppConfig.getTheme().dark ? ratioFG : multiply(gray, ratioFG));
        } else {
            lblRatio.setText(String.format("Rating: %d%% (holiday)", ratio));
            lblRatio.setForeground(gray);
        }
        btnPrevChart.setEnabled(comboDayCharts.getSelectedIndex() > 0);
        btnNextChart.setEnabled(comboDayCharts.getSelectedIndex() < comboDayCharts.getItemCount() - 1);
        btnFirstChart.setEnabled(comboDayCharts.getSelectedIndex() > 0);
        btnLastChart.setEnabled(comboDayCharts.getSelectedIndex() < comboDayCharts.getItemCount() - 1);
    }

    public void refreshMonthChart(CacheMgr cache, ItemEvent event){
        if(event.getStateChange() != ItemEvent.SELECTED) return;
        DateSelection<YearMonth> selected = (DateSelection<YearMonth>) comboMonthCharts.getSelectedItem();
        pnlMonthChart.removeAll();
        if (selected == null) { pnlMonthChart.add(lblMonthNoData); return;}
        final MonthSummary summary = cache.getObj(MonthSummary.cacheId(selected.date, z), MonthSummary.class);
        if (summary == null) { pnlMonthChart.add(lblMonthNoData); return;}
        Charts.MonthChartInfo chartInfo = Charts.createMonthBarChart(summary);
        monthChartInfo = chartInfo;
        getMonthPanel().setChart(chartInfo.chart);
        pnlMonthChart.setPreferredSize(new Dimension(-1, 220));
        pnlMonthChart.add(getMonthPanel());
        pnlMonthChart.updateUI();
        btnMonthPrevChart.setEnabled(comboMonthCharts.getSelectedIndex() > 0);
        btnMonthNextChart.setEnabled(comboMonthCharts.getSelectedIndex() < comboMonthCharts.getItemCount() - 1);
        btnMonthFirstChart.setEnabled(comboMonthCharts.getSelectedIndex() > 0);
        btnMonthLastChart.setEnabled(comboMonthCharts.getSelectedIndex() < comboMonthCharts.getItemCount() - 1);
    }

    public void refreshYearChart(CacheMgr cache, ItemEvent event){
        if(event.getStateChange() != ItemEvent.SELECTED) return;
        DateSelection<Year> selected = (DateSelection<Year>) comboYearCharts.getSelectedItem();
        pnlYearChart.removeAll();
        if (selected == null) { pnlYearChart.add(lblYearNoData); return;}
        final YearSummary summary = cache.getObj(YearSummary.cacheId(selected.date, z), YearSummary.class);
        if (summary == null) { pnlYearChart.add(lblYearNoData); return;}
        getYearPanel().setChart(Charts.createYearBarChart(summary));
        pnlYearChart.setPreferredSize(new Dimension(-1, 270));
        pnlYearChart.add(getYearPanel());
        pnlYearChart.updateUI();
        btnYearPrevChart.setEnabled(comboYearCharts.getSelectedIndex() > 0);
        btnYearNextChart.setEnabled(comboYearCharts.getSelectedIndex() < comboYearCharts.getItemCount() - 1);
        btnYearFirstChart.setEnabled(comboYearCharts.getSelectedIndex() > 0);
        btnYearLastChart.setEnabled(comboYearCharts.getSelectedIndex() < comboYearCharts.getItemCount() - 1);
    }

    public long selected(){
        if(tabs.getSelectedIndex()==0 && tblActive.getSelectedRow() != -1) {
            return ((AllModel) tblActive.getModel())
                    .getProjectHash(tblActive.convertRowIndexToModel(tblActive.getSelectedRow()));
        } else if(tabs.getSelectedIndex()==1 && tblFinished.getSelectedRow() != -1) {
            return ((AllModel) tblFinished.getModel())
                    .getProjectHash(tblFinished.convertRowIndexToModel(tblFinished.getSelectedRow()));
        } else if(tabs.getSelectedIndex()==2 && tblUtilityTags.getSelectedRow() != -1) {
            return ((AllModel) tblUtilityTags.getModel())
                    .getProjectHash(tblUtilityTags.convertRowIndexToModel(tblUtilityTags.getSelectedRow()));
        }
        return -1;
    }

    public int selectedIndex(){
        switch (tabs.getSelectedIndex()){
            case 0: return tblActive.convertRowIndexToModel(tblActive.getSelectedRow());
            case 1: return tblFinished.convertRowIndexToModel(tblFinished.getSelectedRow());
            case 2: return tblUtilityTags.convertRowIndexToModel(tblUtilityTags.getSelectedRow());
            default: return -1;
        }
    }

    public void setSelected(long hash){
        if(tabs.getSelectedIndex()==0 && tblActive.getModel() instanceof AllModel) {
            int i = tblActive.convertRowIndexToView(((AllModel) tblActive.getModel()).findIndex(hash));
            tblActive.setRowSelectionInterval(i, i);
        } else if(tabs.getSelectedIndex()==1 && tblFinished.getModel() instanceof AllModel) {
            int i = tblFinished.convertRowIndexToView(((AllModel) tblFinished.getModel()).findIndex(hash));
            tblFinished.setRowSelectionInterval(i, i);
        } else if(tabs.getSelectedIndex()==2 && tblUtilityTags.getModel() instanceof AllModel) {
            int i = tblUtilityTags.convertRowIndexToView(((AllModel) tblUtilityTags.getModel()).findIndex(hash));
            tblUtilityTags.setRowSelectionInterval(i, i);
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
    public JTabbedPane getTabProjects() {
        return tabs;
    }

    @Override
    public JTabbedPane getTabReports() {
        return tabr;
    }

    @Override
    public JComponent getButtonFinish() {
        if(cmdFinish != null) return cmdFinish;
        return btnFinish;
    }

    @Override
    public JComponent getButtonResume() {
        if(cmdResume != null) return cmdResume;
        return btnResume;
    }

    @Override
    public JComponent getButtonBookmarks() {
        if(cmdBookmarks != null) return cmdBookmarks;
        return btnBookmarks;
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
        return monthChartInfo;
    }

    private void createUIComponents() {
        comboDayCharts = new JComboBox<>();
        comboMonthCharts = new JComboBox<>();
        comboYearCharts = new JComboBox<>();
    }
}
