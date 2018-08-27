package xyz.spiralhalo.sherlock;

//import com.jgoodies.looks.windows.WindowsLookAndFeel;
import org.jfree.data.category.DefaultCategoryDataset;
import xyz.spiralhalo.sherlock.persist.cache.CacheId;
import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserInt;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserNode;
import xyz.spiralhalo.sherlock.report.*;
import xyz.spiralhalo.sherlock.report.Charts;
import xyz.spiralhalo.sherlock.report.persist.AllReportRows;
import xyz.spiralhalo.sherlock.report.persist.ChartMeta;
import xyz.spiralhalo.sherlock.report.persist.DateList;
import xyz.spiralhalo.sherlock.report.persist.ReportRows;
import xyz.spiralhalo.sherlock.util.FormatUtil;
import xyz.spiralhalo.sherlock.util.ImgUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

import static xyz.spiralhalo.sherlock.util.ColorUtil.*;

public class MainView {

    private JPanel rootPane;
    private JButton btnNew;
    private JTabbedPane tabs;
    private JProgressBar progress;
    private JTable tblActive;
    private JTable tblFinished;
    private JTable tblDaily;
    private JTable tblMonthly;
    private JLabel lblRefresh;
    private JPanel pnlStatus;
    private JButton btnRefresh;
    private JPanel pnlRefreshing;
    private JLabel lblTracking;
    private JButton btnView;
    private JButton btnFinish;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnNextMonth;
    private JButton btnPrevMonth;
    private JPanel panelChart;
    private JButton btnResume;
    private JButton btnSettings;
    private JComboBox comboCharts;
    private JButton btnPrevChart;
    private JButton btnNextChart;
    private JTabbedPane tabr;
    private JLabel lblLogged;
    private JLabel lblWorktime;
    private JLabel lblRatio;
    private JButton btnPrevYear;
    private JButton btnNextYear;
    private JButton btnNewTag;
    private JTable tblUtilityTags;

    private final JFrame frame = new JFrame(Main.APP_NAME);

    MainView(){
        MainControl control = new MainControl(this);
        frame.setContentPane(rootPane);
        frame.setMinimumSize(rootPane.getMinimumSize());
        frame.setPreferredSize(rootPane.getMinimumSize());
        frame.pack();
        frame.setLocationByPlatform(true);

        control.setToolbar(btnNew,btnNewTag,btnView,btnFinish,btnResume,btnEdit,btnDelete,btnSettings,tabs, tabr);
        control.setRefresh(btnRefresh, pnlRefreshing, lblRefresh);
        control.setTables(tblActive, tblFinished, tblUtilityTags);
        control.setChart(comboCharts, btnPrevChart, btnNextChart);

        tblActive.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblActive.setDefaultRenderer(String.class, new ProjectCell());
        tblFinished.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblFinished.setDefaultRenderer(String.class, new ProjectCell());
        tblUtilityTags.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblUtilityTags.setDefaultRenderer(String.class, new ProjectCell());
        tblDaily.setDefaultRenderer(String.class, new DefaultTableCellRenderer());
        tblMonthly.setDefaultRenderer(String.class, new DefaultTableCellRenderer());
        tblDaily.setDefaultRenderer(Integer.class, new DurationCell(true));
        tblMonthly.setDefaultRenderer(Integer.class, new DurationCell());

        JButton[] iconButtons = new JButton[]{
                btnNew, btnNewTag, btnView, btnFinish, btnResume, btnEdit, btnDelete, btnSettings,
                btnRefresh, btnPrevChart, btnNextChart
        };
        if(Main.currentTheme.foreground !=0) {
            for (JButton btn : iconButtons) {
                if(Main.currentTheme.dark) {
                    btn.setRolloverIcon(btn.getIcon());
                }
                btn.setIcon(ImgUtil.createTintedIcon(((ImageIcon)btn.getIcon()).getImage(), Main.currentTheme.foreground));
            }
        }
        ((JLabel)comboCharts.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
    }

    public JFrame getFrame() {
        return frame;
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
        final AllModel activeModel = new AllModel(cache.getObj(CacheId.ActiveRows, AllReportRows.class));
        final AllModel finishedModel = new AllModel(cache.getObj(CacheId.FinishedRows, AllReportRows.class));
        final AllModel utilityTagsModel = new AllModel(cache.getObj(CacheId.UtilityRows, AllReportRows.class), true);
        final DayModel dayModel = new DayModel(cache.getObj(CacheId.DayRows, ReportRows.class));
        final MonthModel monthModel = new MonthModel(cache.getObj(CacheId.MonthRows, ReportRows.class));
        tblActive.setModel(activeModel);
        tblFinished.setModel(finishedModel);
        tblUtilityTags.setModel(utilityTagsModel);
        tblDaily.setModel(dayModel);
        tblMonthly.setModel(monthModel);
        comboCharts.setModel(new DateSelectorModel(cache.getObj(CacheId.ChartList, DateList.class)));
        comboCharts.setSelectedIndex(comboCharts.getModel().getSize()-1);
    }

    public void refreshChart(CacheMgr cache) {
        DateSelectorEntry selected = (DateSelectorEntry) comboCharts.getSelectedItem();
        if (selected == null) return;
        final DefaultCategoryDataset dataset = cache.getObj(CacheId.ChartData(selected.date), DefaultCategoryDataset.class);
        final ChartMeta meta = cache.getObj(CacheId.ChartMeta(selected.date), ChartMeta.class);
        panelChart.removeAll();
        if (dataset == null || meta == null) return;
        panelChart.setPreferredSize(new Dimension(-1, 220));
        panelChart.add(Charts.createDayBarChart(dataset, meta, ZonedDateTime.now()));
        panelChart.updateUI();
        int target = UserConfig.getInt(UserNode.TRACKING, UserInt.DAILY_TARGET_SECOND);
        int ratio = meta.logDur == 0 ? 0 : (meta.logDur < target ? meta.workDur * 100 / meta.logDur : meta.workDur * 100 / target);
        lblLogged.setText(String.format("Logged: %s", FormatUtil.hms(meta.logDur)));
        lblWorktime.setText(String.format("Project: %s", FormatUtil.hms(meta.workDur)));
        if (UserConfig.isWorkDay(selected.date.get(ChronoField.DAY_OF_WEEK))) {
            lblRatio.setText(String.format("Rating: %d%%", ratio));
            Color ratioFG = interpolateNicely((float) ratio / 100f, bad, neu, gut);
            lblRatio.setForeground(AppConfig.getTheme().dark ? ratioFG : multiply(gray, ratioFG));
        } else {
            lblRatio.setText(String.format("Rating: %d%% (holiday)", ratio));
            lblRatio.setForeground(AppConfig.getTheme().dark ? light_gray : gray);
        }
        btnPrevChart.setEnabled(comboCharts.getSelectedIndex() > 0);
        btnNextChart.setEnabled(comboCharts.getSelectedIndex() < comboCharts.getItemCount() - 1);
    }

    public long getSelectedProject(){
        if(tabs.getSelectedIndex()==0 && tblActive.getSelectedRow() != -1) {
            return ((AllModel) tblActive.getModel())
                    .getProjectHash(tblActive.getRowSorter().convertRowIndexToModel(tblActive.getSelectedRow()));
        } else if(tabs.getSelectedIndex()==1 && tblFinished.getSelectedRow() != -1) {
            return ((AllModel) tblFinished.getModel())
                    .getProjectHash(tblFinished.getRowSorter().convertRowIndexToModel(tblFinished.getSelectedRow()));
        } else if(tabs.getSelectedIndex()==2 && tblUtilityTags.getSelectedRow() != -1) {
            return ((AllModel) tblUtilityTags.getModel())
                    .getProjectHash(tblUtilityTags.getRowSorter().convertRowIndexToModel(tblUtilityTags.getSelectedRow()));
        }
        return -1;
    }
}
