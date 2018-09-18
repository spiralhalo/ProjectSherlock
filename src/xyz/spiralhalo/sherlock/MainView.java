package xyz.spiralhalo.sherlock;

import org.jfree.data.category.DefaultCategoryDataset;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import xyz.spiralhalo.sherlock.persist.cache.CacheId;
import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserInt;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserNode;
import xyz.spiralhalo.sherlock.report.*;
import xyz.spiralhalo.sherlock.report.Charts;
import xyz.spiralhalo.sherlock.report.factory.charts.ChartMeta;
import xyz.spiralhalo.sherlock.report.factory.summary.MonthSummary;
import xyz.spiralhalo.sherlock.report.factory.summary.YearList;
import xyz.spiralhalo.sherlock.report.factory.summary.YearSummary;
import xyz.spiralhalo.sherlock.report.factory.table.AllReportRows;
import xyz.spiralhalo.sherlock.util.FormatUtil;
import xyz.spiralhalo.sherlock.util.ImgUtil;

import javax.swing.*;
import java.awt.*;
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
    private JComboBox comboList;
    private JLabel lblNoData;
    private JPanel pnlYearChart;
    private JPanel pnlMonthChart;

    private final MainControl control;
    private final ArrayList<JComponent> enableOnSelect = new ArrayList<>();
    private PopupMenu tablePopUpMenu;

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
            JCommandButton cmdNew = new JCommandButton("New");
            JCommandButton cmdEdit = new JCommandButton("Edit");
            JCommandButton cmdDelete = new JCommandButton("Delete");
            JCommandButton cmdView = new JCommandButton("View");
            JCommandButton cmdFinish = new JCommandButton("Finish");
            JCommandButton cmdResume = new JCommandButton("Resume");
            JCommandButton cmdUp = new JCommandButton("Up");
            JCommandButton cmdDown = new JCommandButton("Down");
            JCommandButton cmdBookmarks = new JCommandButton("Marks");
            JCommandButton cmdInbox = new JCommandButton("Inbox");
            JCommandButton cmdSettings = new JCommandButton("Settings");
            JCommandButton cmdRefresh = new JCommandButton("Refresh");

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
            toolbarMain.add(cmdUp);
            toolbarMain.add(cmdDown);
            toolbarMain.addSeparator();
            toolbarMain.add(cmdBookmarks);
            toolbarMain.add(cmdInbox);
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
    }

    public JFrame frame() {
        return frame;
    }

    @Override
    public void init() {
        createCommandButtons(control);
        control.setTables(tblActive, tblFinished, tblUtilityTags);
        control.setChart(comboDayCharts, btnPrevChart, btnNextChart, btnFirstChart, btnLastChart);

        frame.setContentPane(rootPane);
        frame.setMinimumSize(rootPane.getMinimumSize());
        frame.setPreferredSize(rootPane.getMinimumSize());
        frame.pack();
        frame.setLocationByPlatform(true);
    }

    public void refreshStatus(CacheMgr cache) {
        if (cache.getCreated(CacheId.ActiveRows).equals(CacheMgr.NEVER)) {
            lblRefresh.setText("Last refresh: never");
            lblTracking.setText("Refresh required.");
        } else {
            int seconds = (int)cache.getElapsed(CacheId.ActiveRows);
            if (seconds == 0) {
                lblRefresh.setText("Last refresh: just now");
            } else {
                lblRefresh.setText(String.format("Last refresh: %s ago", FormatUtil.hmsStrict(seconds)));
            }
            int size = cache.getObj(CacheId.ActiveRows, AllReportRows.class).size();
            lblTracking.setText(String.format("Tracking %d project%s", size, size>1?"s":""));
        }
    }

    public void refreshOverview(CacheMgr cache) {
        refreshStatus(cache);
        if(cache.getCreated(CacheId.ActiveRows).equals(CacheMgr.NEVER)){return;}
        refreshProjects(cache, 0);
        refreshProjects(cache, 1);
        refreshProjects(cache, 2);
//        final DayModel dayModel = new DayModel(cache.getObj(CacheId.DayRows, ReportRows.class));
//        final MonthModel monthModel = new MonthModel(cache.getObj(CacheId.MonthRows, ReportRows.class));
//        tblDaily.setModel(dayModel);
//        tblMonthly.setModel(monthModel);
        ZoneId z = ZoneId.systemDefault();
        final YearList yl = cache.getObj(YearList.cacheId(z), YearList.class);
        if(yl == null)return;
        final ArrayList<LocalDate> dates = new ArrayList<>();
        for (Year y:yl) {
            final YearSummary ys = cache.getObj(YearSummary.cacheId(y, z), YearSummary.class);
            if(ys != null){
                for (YearMonth m:ys.getMonthList()) {
                    final MonthSummary ms = cache.getObj(MonthSummary.cacheId(m, z), MonthSummary.class);
                    if(ms != null){
                        dates.addAll(ms.getDayList());
                    }
                }
            }
        }
        Object selected = comboDayCharts.getSelectedItem();
        comboDayCharts.setModel(new DateSelectorModel<>(dates));
        if(selected instanceof LocalDate && dates.contains(selected)){
            comboDayCharts.setSelectedItem(selected);
        } else {
            comboDayCharts.setSelectedIndex(comboDayCharts.getModel().getSize() - 1);
        }
    }

    public void refreshProjects(CacheMgr cache, int index){
        switch (index){
            case 0:
                final AllReportRows activeRows = cache.getObj(CacheId.ActiveRows, AllReportRows.class);
                if(tblActive.getModel() instanceof AllModel){
                    ((AllModel) tblActive.getModel()).reset(activeRows);
                } else {
                    final AllModel allModel = new AllModel(activeRows);
                    tblActive.setModel(allModel);
                    allModel.setTableColumnWidths(tblActive);
                }
                break;
            case 1:
                final AllReportRows finishedRows = cache.getObj(CacheId.FinishedRows, AllReportRows.class);
                if(tblFinished.getModel() instanceof AllModel){
                    ((AllModel) tblFinished.getModel()).reset(finishedRows);
                } else {
                    final AllModel allModel = new AllModel(finishedRows);
                    tblFinished.setModel(allModel);
                    allModel.setTableColumnWidths(tblFinished);
                }
                break;
            case 2:
                final AllReportRows utilityTagsRows = cache.getObj(CacheId.UtilityRows, AllReportRows.class);
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

    public void refreshDayChart(CacheMgr cache) {
        DateSelection<LocalDate> selected = (DateSelection<LocalDate>) comboDayCharts.getSelectedItem();
        pnlDayChart.removeAll();
        if (selected == null) { pnlDayChart.add(lblNoData); return;}
        final MonthSummary summary = cache.getObj(MonthSummary.cacheId(YearMonth.from(selected.date), ZoneId.systemDefault()), MonthSummary.class);
        if (summary == null) { pnlDayChart.add(lblNoData); return;}
        final DefaultCategoryDataset dataset = summary.getDayCharts().get(selected.date).getDataset();
        final ChartMeta meta = summary.getDayCharts().get(selected.date).getMeta();
        if (dataset == null || meta == null) { pnlDayChart.add(lblNoData); return;}
        pnlDayChart.setPreferredSize(new Dimension(-1, 220));
        pnlDayChart.add(Charts.createDayBarChart(dataset, meta, ZonedDateTime.now()));
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
            lblRatio.setForeground(AppConfig.getTheme().dark ? light_gray : gray);
        }
        btnPrevChart.setEnabled(comboDayCharts.getSelectedIndex() > 0);
        btnNextChart.setEnabled(comboDayCharts.getSelectedIndex() < comboDayCharts.getItemCount() - 1);
        btnFirstChart.setEnabled(comboDayCharts.getSelectedIndex() > 0);
        btnLastChart.setEnabled(comboDayCharts.getSelectedIndex() < comboDayCharts.getItemCount() - 1);
    }

    public void refreshMonthChart(CacheMgr cache){
        DateSelection<LocalDate> selected = (DateSelection<LocalDate>) comboDayCharts.getSelectedItem();
        pnlDayChart.removeAll();
        if (selected == null) { pnlDayChart.add(lblNoData); return;}
        final MonthSummary summary = cache.getObj(MonthSummary.cacheId(YearMonth.from(selected.date), ZoneId.systemDefault()), MonthSummary.class);
        if (summary == null) { pnlDayChart.add(lblNoData); return;}
        final DefaultCategoryDataset dataset = summary.getDayCharts().get(selected.date).getDataset();
        final ChartMeta meta = summary.getDayCharts().get(selected.date).getMeta();
        if (dataset == null || meta == null) { pnlDayChart.add(lblNoData); return;}
        pnlDayChart.setPreferredSize(new Dimension(-1, 220));
        pnlDayChart.add(Charts.createDayBarChart(dataset, meta, ZonedDateTime.now()));
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
            lblRatio.setForeground(AppConfig.getTheme().dark ? light_gray : gray);
        }
        btnPrevChart.setEnabled(comboDayCharts.getSelectedIndex() > 0);
        btnNextChart.setEnabled(comboDayCharts.getSelectedIndex() < comboDayCharts.getItemCount() - 1);
        btnFirstChart.setEnabled(comboDayCharts.getSelectedIndex() > 0);
        btnLastChart.setEnabled(comboDayCharts.getSelectedIndex() < comboDayCharts.getItemCount() - 1);
    }

    public void refreshYearChart(CacheMgr cache){
        DateSelection<LocalDate> selected = (DateSelection<LocalDate>) comboDayCharts.getSelectedItem();
        pnlDayChart.removeAll();
        if (selected == null) { pnlDayChart.add(lblNoData); return;}
        final MonthSummary summary = cache.getObj(MonthSummary.cacheId(YearMonth.from(selected.date), ZoneId.systemDefault()), MonthSummary.class);
        if (summary == null) { pnlDayChart.add(lblNoData); return;}
        final DefaultCategoryDataset dataset = summary.getDayCharts().get(selected.date).getDataset();
        final ChartMeta meta = summary.getDayCharts().get(selected.date).getMeta();
        if (dataset == null || meta == null) { pnlDayChart.add(lblNoData); return;}
        pnlDayChart.setPreferredSize(new Dimension(-1, 220));
        pnlDayChart.add(Charts.createDayBarChart(dataset, meta, ZonedDateTime.now()));
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
            lblRatio.setForeground(AppConfig.getTheme().dark ? light_gray : gray);
        }
        btnPrevChart.setEnabled(comboDayCharts.getSelectedIndex() > 0);
        btnNextChart.setEnabled(comboDayCharts.getSelectedIndex() < comboDayCharts.getItemCount() - 1);
        btnFirstChart.setEnabled(comboDayCharts.getSelectedIndex() > 0);
        btnLastChart.setEnabled(comboDayCharts.getSelectedIndex() < comboDayCharts.getItemCount() - 1);
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
        return btnFinish;
    }

    @Override
    public JComponent getButtonResume() {
        return btnResume;
    }

    @Override
    public JComponent getButtonBookmarks() {
        return btnBookmarks;
    }

    @Override
    public void setTablePopUpMenu(PopupMenu popUpMenu) {
        tablePopUpMenu = popUpMenu;
    }

    @Override
    public PopupMenu getTablePopUpMenu() {
        return tablePopUpMenu;
    }

    private void createUIComponents() {
        comboDayCharts = new JComboBox<>();
    }
}
