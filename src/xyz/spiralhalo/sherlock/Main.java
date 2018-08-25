package xyz.spiralhalo.sherlock;

//import com.jgoodies.looks.windows.WindowsLookAndFeel;
import com.sun.imageio.plugins.common.ImageUtil;
import org.jfree.data.category.DefaultCategoryDataset;
import org.pushingpixels.substance.api.skin.*;
import xyz.spiralhalo.sherlock.persist.cache.CacheId;
import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig.Theme;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserInt;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserNode;
import xyz.spiralhalo.sherlock.report.*;
import xyz.spiralhalo.sherlock.report.persist.AllReportRows;
import xyz.spiralhalo.sherlock.report.persist.ChartMeta;
import xyz.spiralhalo.sherlock.report.persist.DateList;
import xyz.spiralhalo.sherlock.report.persist.ReportRows;
import xyz.spiralhalo.sherlock.util.Debug;
import xyz.spiralhalo.sherlock.util.FormatUtil;
import xyz.spiralhalo.sherlock.util.ImgUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

import static xyz.spiralhalo.sherlock.util.ColorUtil.*;

public class Main implements MainView{
    public static final String APP_NAME_NOSPACE = "Project Sherlock 2".replace(" ", "");
    public static final String ARG_MINIMIZED = "-minimized";
    public static final String ARG_DELAYED = "-delayed";
    public static final String APP_NAME = "Project Sherlock 2";
    public static Theme currentTheme;

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

    private final JFrame frame = new JFrame(APP_NAME);

    private Main(){
        MainControl control = new MainControl(this);
        frame.setContentPane(rootPane);
        frame.setMinimumSize(rootPane.getMinimumSize());
        frame.pack();
        frame.setLocationByPlatform(true);
        control.setToolbar(btnNew,btnView,btnFinish,btnResume,btnEdit,btnDelete,btnSettings,tabs, tabr);
        control.setRefresh(btnRefresh, pnlRefreshing, lblRefresh);
        control.setTables(tblActive, tblFinished);
        control.setChart(comboCharts, btnPrevChart, btnNextChart);
        tblActive.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblFinished.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblActive.setDefaultRenderer(String.class, new ProjectCell());
        tblFinished.setDefaultRenderer(String.class, new ProjectCell());
        tblDaily.setDefaultRenderer(String.class, new DefaultTableCellRenderer());
        tblMonthly.setDefaultRenderer(String.class, new DefaultTableCellRenderer());
        tblDaily.setDefaultRenderer(Integer.class, new DurationCell(true));
        tblMonthly.setDefaultRenderer(Integer.class, new DurationCell());
        if(AppConfig.getTheme().buttonColor!=0){
            Theme x = AppConfig.getTheme();
            btnNew.setIcon(ImgUtil.createTintedIcon("new.png", x.buttonColor));
            btnView.setIcon(ImgUtil.createTintedIcon("view.png", x.buttonColor));
            btnFinish.setIcon(ImgUtil.createTintedIcon("finish.png", x.buttonColor));
            btnResume.setIcon(ImgUtil.createTintedIcon("resume.png", x.buttonColor));
            btnEdit.setIcon(ImgUtil.createTintedIcon("edit.png", x.buttonColor));
            btnDelete.setIcon(ImgUtil.createTintedIcon("delete.png", x.buttonColor));
            btnSettings.setIcon(ImgUtil.createTintedIcon("settings.png", x.buttonColor));
            btnRefresh.setIcon(ImgUtil.createTintedIcon("refresh.png", x.buttonColor));
            btnPrevChart.setIcon(ImgUtil.createTintedIcon("left.png", x.buttonColor));
            btnNextChart.setIcon(ImgUtil.createTintedIcon("right.png", x.buttonColor));
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
        SwingUtilities.invokeLater(tableRefresher.setCache(cache));
    }

    public void refreshChart(CacheMgr cache) {
        SwingUtilities.invokeLater(chartCreator.setCache(cache));
        btnPrevChart.setEnabled(comboCharts.getSelectedIndex() > 0);
        btnNextChart.setEnabled(comboCharts.getSelectedIndex() < comboCharts.getItemCount() - 1);
    }

    private final UIUpdater tableRefresher = new UIUpdater() {
        @Override
        protected void doRun() {
            if(cache.getCreated(CacheId.ActiveRows).equals(CacheMgr.NEVER)) return;
            final AllModel activeModel = new AllModel(cache.getObj(CacheId.ActiveRows, AllReportRows.class));
            final AllModel finishedModel = new AllModel(cache.getObj(CacheId.FinishedRows, AllReportRows.class));
            final DayModel dayModel = new DayModel(cache.getObj(CacheId.DayRows, ReportRows.class));
            final MonthModel monthModel = new MonthModel(cache.getObj(CacheId.MonthRows, ReportRows.class));
            tblActive.setModel(activeModel);
            tblFinished.setModel(finishedModel);
            tblDaily.setModel(dayModel);
            tblMonthly.setModel(monthModel);
            comboCharts.setModel(new DateSelectorModel(cache.getObj(CacheId.ChartList, DateList.class)));
            comboCharts.setSelectedIndex(comboCharts.getModel().getSize()-1);
        }
    };
    private final UIUpdater chartCreator = new UIUpdater() {
        @Override
        protected void doRun() {
            DateSelectorEntry selected = (DateSelectorEntry) comboCharts.getSelectedItem();
            if (selected == null) return;
            final DefaultCategoryDataset dataset = cache.getObj(CacheId.ChartData(selected.date), DefaultCategoryDataset.class);
            final ChartMeta meta = cache.getObj(CacheId.ChartMeta(selected.date), ChartMeta.class);
            panelChart.removeAll();
            if (dataset == null || meta == null) return;
            panelChart.setPreferredSize(new Dimension(-1, 270));
            panelChart.add(Charts.createDayBarChart(dataset, meta, ZonedDateTime.now()));
            panelChart.updateUI();
            int target = UserConfig.getInt(UserNode.TRACKING, UserInt.DAILY_TARGET_SECOND);
            int ratio = meta.logDur < target ? meta.workDur * 100 / meta.logDur : meta.workDur * 100 / target;
            lblLogged.setText(String.format("Logged: %s", FormatUtil.hms(meta.logDur)));
            lblWorktime.setText(String.format("Project: %s", FormatUtil.hms(meta.workDur)));
            if(UserConfig.isWorkDay(selected.date.get(ChronoField.DAY_OF_WEEK))) {
                lblRatio.setText(String.format("Rating: %d%%", ratio));
                Color ratioFG = interpolateNicely((float) ratio / 100f, bad, neu, gut);
                lblRatio.setForeground(AppConfig.getTheme().dark?ratioFG:multiply(gray, ratioFG));
            } else {
                lblRatio.setText(String.format("Rating: %d%% (holiday)", ratio));
                lblRatio.setForeground(AppConfig.getTheme().dark?light_gray:gray);
            }
        }
    };

    private abstract class UIUpdater {
        CacheMgr cache;
        Runnable setCache(CacheMgr cache) {
            this.cache = cache;
            return x;
        }
        private Runnable x=()->{
            if(cache==null)return;
            doRun();
            cache=null;
        };
        protected abstract void doRun();
    }

    public long getSelectedProject(){
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
        for (String arg:args) {
            if(arg.toLowerCase().equals(Main.ARG_DELAYED)){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Debug.log(e);
                }
            }
        }
        Runnable r = () -> {
            try {
                switch (AppConfig.getTheme()){
                    case BUSINESS:
                        UIManager.setLookAndFeel(new SubstanceBusinessLookAndFeel());
                        break;
                    case BUSINESS_BLUE:
                        UIManager.setLookAndFeel(new SubstanceBusinessBlueSteelLookAndFeel());
                        break;
                    case BUSINESS_BLACK:
                        UIManager.setLookAndFeel(new SubstanceBusinessBlackSteelLookAndFeel());
                        break;
                    case MIST_SILVER:
                        UIManager.setLookAndFeel(new SubstanceMistSilverLookAndFeel());
                        break;
                    case GRAPHITE:
                        UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
                        break;
                    case RAVEN:
                        UIManager.setLookAndFeel(new SubstanceRavenLookAndFeel());
                        break;
                    case TWILIGHT:
                        UIManager.setLookAndFeel(new SubstanceTwilightLookAndFeel());
                        break;
                    case SYSTEM:
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        break;
                }
                currentTheme = AppConfig.getTheme();
//            UIManager.setLookAndFeel(new WindowsLookAndFeel());
            } catch (Exception e) {
                Debug.log(e);
            }

            Runtime.getRuntime().addShutdownHook(new Thread(SysIntegration::createOrDeleteStartupRegistry));
            Main m = new Main();
            for (String arg:args) {
                if(arg.toLowerCase().equals(Main.ARG_MINIMIZED)){
                    return;
                }
            }
            m.frame.setVisible(true);
        };

        SwingUtilities.invokeLater(r);
//        try {
//            UIDefaults x=UIManager.getLookAndFeelDefaults();
//            for (Object y:x.keySet()) {
//                Object z = UIManager.get(y);
//                if(z instanceof Color){
//                    Color x1 = ColorUtil.reverseBrightness((Color)z, 0.8f);
//                    if(z instanceof ColorUIResource){
//                        UIManager.put(y, new ColorUIResource(x1));
////                        System.out.println(String.format("UIManager.put(\"%s\", new ColorUIResource(0x%x));", y, x1.getRGB()));
//                    } else {
//                        UIManager.put(y, x1);
////                        System.out.println(String.format("UIManager.put(\"%s\", new Color(0x%x));", y, x1.getRGB()));
//                    }
//                }
//            }
//        } catch (Exception e) {
//            Debug.log(e);
//        }
    }
}
