package xyz.spiralhalo.sherlock;

import com.jgoodies.looks.windows.WindowsLookAndFeel;
import org.jfree.data.category.DefaultCategoryDataset;
import xyz.spiralhalo.sherlock.persist.cache.CacheId;
import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;
import xyz.spiralhalo.sherlock.report.*;
import xyz.spiralhalo.sherlock.report.persist.AllReportRows;
import xyz.spiralhalo.sherlock.report.persist.ReportRows;
import xyz.spiralhalo.sherlock.util.FormatUtil;

import javax.swing.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public class Main {
    public static final String APP_NAME = "Project Sherlock 2";
    public static final String APP_NAME_NOSPACE = "Project Sherlock 2".replace(" ", "");
    public static final String ARG_MINIMIZED = "-minimized";

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
    private JButton btnRight;
    private JButton btnLeft;
    private JPanel panelChart;
    private JButton btnResume;
    private JButton btnSettings;

    private final JFrame frame = new JFrame(APP_NAME);
    private final MainControl control = new MainControl(this);

    private Main(){
        frame.setContentPane(rootPane);
        frame.setMinimumSize(rootPane.getMinimumSize());
        frame.pack();
        frame.setLocationByPlatform(true);
        control.setToolbar(btnNew,btnView,btnFinish,btnResume,btnEdit,btnDelete,btnSettings,tabs);
        control.setRefresh(btnRefresh, pnlRefreshing, lblRefresh);
        control.setTables(tblActive, tblFinished);
        tblActive.setDefaultRenderer(String.class, new ProjectCell());
        tblFinished.setDefaultRenderer(String.class, new ProjectCell());
        tblDaily.setDefaultRenderer(Integer.class, new DurationCell(true));
        tblMonthly.setDefaultRenderer(Integer.class, new DurationCell());
    }

    JFrame getFrame() {
        return frame;
    }

    void refreshStatus(CacheMgr cache) {
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

    void refreshOverview(CacheMgr cache) {
        refreshStatus(cache);
        if(cache.getCreated(CacheId.ActiveRows).equals(CacheMgr.NEVER)) return;
        final AllModel activeModel = new AllModel(cache.getObj(CacheId.ActiveRows, AllReportRows.class));
        final AllModel finishedModel = new AllModel(cache.getObj(CacheId.FinishedRows, AllReportRows.class));
        final DayModel dayModel = new DayModel(cache.getObj(CacheId.DayRows, ReportRows.class));
        final MonthModel monthModel = new MonthModel(cache.getObj(CacheId.MonthRows, ReportRows.class));
        final DefaultCategoryDataset dataset = cache.getObj(CacheId.ChartData(LocalDate.now()),DefaultCategoryDataset.class);
        final DatasetColors colors = cache.getObj(CacheId.ChartColor(LocalDate.now()),DatasetColors.class);
        tblActive.setModel(activeModel);
        tblFinished.setModel(finishedModel);
        tblDaily.setModel(dayModel);
        tblMonthly.setModel(monthModel);
        panelChart.removeAll();
        panelChart.add(Charts.createDayBarChart(dataset, colors, ZonedDateTime.now()));
        panelChart.updateUI();
    }

    long getSelectedProject(){
        if(tabs.getSelectedIndex()==0 && tblActive.getSelectedRow() != -1) {
            return ((AllModel) tblActive.getModel())
                    .getProjectHash(tblActive.getRowSorter().convertRowIndexToModel(tblActive.getSelectedRow()));
        } else if(tabs.getSelectedIndex()==1 && tblFinished.getSelectedRow() != -1) {
            return ((AllModel) tblFinished.getModel())
                    .getProjectHash(tblFinished.getRowSorter().convertRowIndexToModel(tblFinished.getSelectedRow()));
        }
        return -1;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new WindowsLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(SysIntegration::createOrDeleteStartupRegistry));
        Main m = new Main();
        for (String arg:args) {
            if(arg.toLowerCase().equals(Main.ARG_MINIMIZED)){
                return;
            }
        }
        m.frame.setVisible(true);
    }

}
