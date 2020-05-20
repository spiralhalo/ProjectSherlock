package xyz.spiralhalo.sherlock;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.ui.RectangleEdge;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import xyz.spiralhalo.sherlock.async.Loader;
import xyz.spiralhalo.sherlock.async.LoaderDialog;
import xyz.spiralhalo.sherlock.audit.ViewDayAudit;
import xyz.spiralhalo.sherlock.bookmark.AutoBookmarker;
import xyz.spiralhalo.sherlock.bookmark.BookmarkMgr;
import xyz.spiralhalo.sherlock.dialog.*;
import xyz.spiralhalo.sherlock.focus.FocusConfig;
import xyz.spiralhalo.sherlock.focus.FocusMgr;
import xyz.spiralhalo.sherlock.notes.EditNote;
import xyz.spiralhalo.sherlock.notes.YearNotes;
import xyz.spiralhalo.sherlock.persist.cache.CacheId;
import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;
import xyz.spiralhalo.sherlock.persist.project.ProjectListIO;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig.AppBool;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig.AppInt;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig;
import xyz.spiralhalo.sherlock.record.legacy.AutoImporter2;
import xyz.spiralhalo.sherlock.record.legacy.AutoImporter3;
import xyz.spiralhalo.sherlock.report.*;
import xyz.spiralhalo.sherlock.report.factory.ProjectViewCreator;
import xyz.spiralhalo.sherlock.report.factory.ProjectViewResult;
import xyz.spiralhalo.sherlock.report.factory.table.AllReportRows;
import xyz.spiralhalo.sherlock.report.ops.OverviewOps;
import xyz.spiralhalo.sherlock.report.factory.table.ReportRows;
import xyz.spiralhalo.sherlock.util.FormatUtil;
import xyz.spiralhalo.sherlock.util.ImgUtil;
import xyz.spiralhalo.sherlock.util.swing.thumb.ThumbManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.function.BiConsumer;

import static java.awt.Frame.ICONIFIED;
import static java.awt.Frame.NORMAL;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserInt.DOUBLE_CLICK_ACTION;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserNode.*;

public class AppControl implements ActionListener {

    private static AppControl instance;

    public static void create() {
        if(instance!=null)return;
        instance = new AppControl();
    }

    public void setThumbs(ThumbManager thumbManager) {
        thumbManager.addSelectionListener(projectSelectionListener);
        thumbManager.addMouseListener(projectMouseAdapter);
    }

    public enum Action{
        A_NEW,
        A_NEW_TAG,
        A_VIEW,
        A_FINISH,
        A_RESUME,
        A_EDIT,
        A_DELETE,
        A_UP,
        A_DOWN,
        A_EXPORT,
        A_SETTINGS,
        A_REFRESH,
        A_DEEP_REFRESH,
        A_EXTRA_FOCUS,
        A_EXTRA_BOOKMARKS,
        A_DAY_NOTE,
        A_DAY_AUDIT
    }
    private AppViewAccessor view;
    private final ProjectList projectList;
    private final Tracker tracker;
    private final CacheMgr cache;
    private final ScrSnapper snapper;
    private final TrayIcon trayIcon;
    private final boolean trayIconUsed;
    private final BookmarkMgr bookmark;
    private final AutoBookmarker autoBookmarker;
    private final FocusMgr focusMgr;
    private LocalDate monthNoteEditing;
    private final ZoneId z = Main.z;

    private AppControl() {
        cache = new CacheMgr();
        projectList = ProjectListIO.load();
        AutoImporter3.importRecords(projectList);
        AutoImporter2.importRecord(projectList);
        tracker = new Tracker(projectList);
        tracker.addListener((project, windowInfo) -> {
            if(view != null && project != null) {
                view.refreshTrackingStatus(String.format("Last tracked: %s (%s)", project.getName(), windowInfo.exeName));
            }
        });
        snapper = new ScrSnapper(tracker);
        tracker.start();
        bookmark = new BookmarkMgr(tracker);
        autoBookmarker = new AutoBookmarker(bookmark, tracker);

        createView();
        view.getTabMain().setSelectedIndex(AppConfig.getTabMainSelection() % 2);
        view.getTabReports().setSelectedIndex(AppConfig.getTabReportsSelection() % 3);
        view.getTabProjects().setSelectedIndex(AppConfig.getTabProjectsSelection() % 3);
        final ActionListener listenerTrayToggle = e -> {
            if(view == null || !view.frame().isVisible()) {
                showView();
            } else {
                minimizeToTray();
            }
        };
        final ActionListener listenerTrayExit = e -> {
            System.exit(0);
        };
        trayIcon = Application.createTrayIcon(listenerTrayToggle,listenerTrayExit);
        trayIconUsed = (trayIcon != null);
        focusMgr = new FocusMgr(projectList, tracker, trayIcon);

        if(!Main.Arg.Minimized.isEnabled()){
            showView();
        }
    }

    private void showView() {
        if(view == null){
            createView();
        }
        view.frame().setVisible(true);
        view.frame().setState(NORMAL);
        new FirstWizard(view.frame()).setVisible(true);
    }

    private void createView(){
        view = new AppView(this);
        view.prePackInit();
        Dimension defaultSize = view.frame().getPreferredSize();
        Dimension preferredSize = new Dimension(Math.max(defaultSize.width, AppConfig.getPreferredWindowWidth()),
                Math.max(defaultSize.height, AppConfig.getPreferredWindowHeight()));
        view.frame().setPreferredSize(preferredSize);
        view.frame().setLocationByPlatform(true);
        if(AppConfig.getWindowLastMaximized())
            view.frame().setExtendedState(view.frame().getExtendedState() | JFrame.MAXIMIZED_BOTH);
        if(AppConfig.getWindowLastLocationX() != -1 && AppConfig.getWindowLastLocationY() != -1)
            view.frame().setLocation(AppConfig.getWindowLastLocationX(), AppConfig.getWindowLastLocationY());
        view.frame().pack();
        view.frame().addWindowListener(windowAdapter);
        view.frame().addWindowStateListener(windowAdapter);
        view.frame().addWindowFocusListener(windowAdapter);
        view.frame().addComponentListener(windowAdapter2);
        view.frame().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        view.frame().setIconImages(Arrays.asList(ImgUtil.createImage("icon.png","App Icon Small"),
                ImgUtil.createImage("med_icon.png","App Icon")));
        view.refreshOverview(cache, projectList.getCategories().toArray(new String[]{}));
    }

    private ComponentAdapter windowAdapter2 = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent componentEvent) {
            if(!isMaximized()) {
                Dimension newSize = view.frame().getSize();
                AppConfig.setPreferredWindowHeight(newSize.height);
                AppConfig.setPreferredWindowWidth(newSize.width);
            }
        }

        @Override
        public void componentMoved(ComponentEvent componentEvent) {
            if(!isMaximized()) {
                Point location = view.frame().getLocation();
                AppConfig.setWindowLastLocationX(location.x);
                AppConfig.setWindowLastLocationY(location.y);
            }
        }
    };

    private WindowAdapter windowAdapter = new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            if(AppConfig.appGBool(AppBool.ASK_BEFORE_QUIT)) {
                Quit quit = new Quit(view.frame());
                quit.setVisible(true);
                switch (quit.getSelection()) {
                    case EXIT:
                        System.exit(0);
                        break;
                    case MINIMIZE:
                        minimizeToTray();
                        break;
                }
            } else {
                System.exit(0);
            }
        }

        @Override
        public void windowGainedFocus(WindowEvent e) {
            if(trayIconUsed) {
                trayIcon.getPopupMenu().getItem(0).setLabel("Minimize to tray");
            }
            decideRefresh();
        }

        @Override
        public void windowStateChanged(WindowEvent e) {
            if(view.frame().getState() == ICONIFIED){
                if(trayIconUsed && AppConfig.appGBool(AppBool.MINIMIZE_TO_TRAY)){
                    minimizeToTray();
                }
            } else {
                AppConfig.setWindowLastMaximized(isMaximized());
            }
        }
    };

    private boolean isMaximized() {
        return (view.frame().getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0;
    }

    private void minimizeToTray(){
        if(trayIconUsed) {
            trayIcon.getPopupMenu().getItem(0).setLabel("Restore");
            view.frame().setVisible(false);
        }
    }

    private void setToolbarInternal(JComponent btnNew, JComponent btnView, JComponent btnFinish, JComponent btnResume,
                                    JComponent btnEdit, JComponent btnDelete, JComponent btnUp, JComponent btnDown,
                                    JComponent btnExport, JComponent btnSettings,
                                    JTabbedPane tabMain, JTabbedPane tabs, JTabbedPane tabr){
        btnNew.setName(Action.A_NEW.name());
        btnView.setName(Action.A_VIEW.name());
        btnFinish.setName(Action.A_FINISH.name());
        btnResume.setName(Action.A_RESUME.name());
        btnEdit.setName(Action.A_EDIT.name());
        btnDelete.setName(Action.A_DELETE.name());
        btnUp.setName(Action.A_UP.name());
        btnDown.setName(Action.A_DOWN.name());
        btnExport.setName(Action.A_EXPORT.name());
        btnSettings.setName(Action.A_SETTINGS.name());
        addEnableOnSelect(btnView, btnEdit, btnDelete, btnUp, btnDown, btnFinish, btnResume);
        btnFinish.setEnabled(false);
        setTabs(btnResume, btnUp, btnDown, tabMain, tabs, tabr);
    }

    public void setToolbar(JButton btnNew, JButton btnNewTag, JButton btnView, JButton btnFinish, JButton btnResume,
                           JButton btnEdit, JButton btnDelete, JButton btnUp, JButton btnDown,
                           JButton btnExport, JButton btnSettings,
                           JTabbedPane tabMain, JTabbedPane tabs, JTabbedPane tabr){
        setToolbarInternal(btnNew, btnView, btnFinish, btnResume, btnEdit, btnDelete, btnUp, btnDown, btnExport, btnSettings, tabMain, tabs, tabr);
        btnNewTag.setName(Action.A_NEW_TAG.name());
        btnNew.addActionListener(this);
        btnNewTag.addActionListener(this);
        btnView.addActionListener(this);
        btnFinish.addActionListener(this);
        btnResume.addActionListener(this);
        btnEdit.addActionListener(this);
        btnDelete.addActionListener(this);
        btnUp.addActionListener(this);
        btnDown.addActionListener(this);
        btnExport.addActionListener(this);
        btnSettings.addActionListener(this);
    }

    public void setToolbar(JCommandButton btnNew, JCommandButton btnView, JCommandButton btnFinish, JCommandButton btnResume,
                           JCommandButton btnEdit, JCommandButton btnDelete, JCommandButton btnUp, JCommandButton btnDown,
                           JCommandButton btnExport, JCommandButton btnSettings,
                           JTabbedPane tabMain, JTabbedPane tabs, JTabbedPane tabr){
        createPopupNew(btnNew);
        setToolbarInternal(btnNew, btnView, btnFinish, btnResume, btnEdit, btnDelete, btnUp, btnDown, btnExport, btnSettings, tabMain, tabs, tabr);
        btnNew.addActionListener(this);
        btnView.addActionListener(this);
        btnFinish.addActionListener(this);
        btnResume.addActionListener(this);
        btnEdit.addActionListener(this);
        btnDelete.addActionListener(this);
        btnUp.addActionListener(this);
        btnDown.addActionListener(this);
        btnExport.addActionListener(this);
        btnSettings.addActionListener(this);
    }

    public void setThumbToolbar(JComboBox comboSort, JComboBox comboCat, JComboBox comboType){
        ItemListener thumbToolbarListener = itemEvent -> {
            if(itemEvent.getStateChange() != ItemEvent.SELECTED) return;
            //save selections
            if(itemEvent.getSource()==comboCat) {
                if (comboCat.getSelectedIndex() == 0) {
                    AppConfig.setFilterCategory("0");
                } else {
                    AppConfig.setFilterCategory((String) comboCat.getSelectedItem());
                }
            } else if(itemEvent.getSource()==comboType) {
                AppConfig.setFilterType(comboType.getSelectedIndex());
            } else if(itemEvent.getSource()==comboSort) {
                AppConfig.setThumbSort(comboSort.getSelectedIndex());
            }
            view.refreshThumbs(cache);
        };
        comboSort.addItemListener(thumbToolbarListener);
        comboCat.addItemListener(thumbToolbarListener);
        comboType.addItemListener(thumbToolbarListener);
    }

    public void setExtras(JButton btnBookmarks, JButton btnFocus) {
        btnBookmarks.setName(Action.A_EXTRA_BOOKMARKS.name());
        btnFocus.setName(Action.A_EXTRA_FOCUS.name());
        btnBookmarks.addActionListener(this);
        btnFocus.addActionListener(this);
        addEnableOnSelect(btnBookmarks);
    }

    public void setExtras(JCommandButton btnBookmarks, JCommandButton btnFocus) {
        btnBookmarks.setName(Action.A_EXTRA_BOOKMARKS.name());
        btnFocus.setName(Action.A_EXTRA_FOCUS.name());
        btnBookmarks.addActionListener(this);
        btnFocus.addActionListener(this);
        addEnableOnSelect(btnBookmarks);
    }

    public void addEnableOnSelect(JComponent... components) {
        for (JComponent component:components) {
            view.enableOnSelect().add(component);
            component.setEnabled(false);
        }
    }

    private void setTabs(JComponent btnResume, JComponent btnUp, JComponent btnDown, JTabbedPane tabMain,
                         JTabbedPane tabs, JTabbedPane tabr){
        tabs.addChangeListener(tabChangeListener);
        tabr.addChangeListener(tabChangeListener);
        tabMain.addChangeListener(tabChangeListener);
        btnResume.setVisible(false);
        btnUp.setVisible(false);
        btnDown.setVisible(false);
    }

    public void setDayButtons(JButton btnNote, JButton btnAudit) {
        btnNote.setName(Action.A_DAY_NOTE.name());
        btnAudit.setName(Action.A_DAY_AUDIT.name());
        btnNote.addActionListener(this);
        btnAudit.addActionListener(this);
    }

    public void setMonthChart(ChartPanel monthPanel) {
        JPopupMenu.Separator s = new JPopupMenu.Separator();
        monthPanel.getPopupMenu().add(s);
        JMenuItem editNote = new JMenuItem("Add note");
        JMenuItem removeNote = new JMenuItem("Remove note");
        monthPanel.getPopupMenu().add(editNote);
        monthPanel.getPopupMenu().add(removeNote);
        monthPanel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent e) {
                if(view.getMonthChartInfo() != null && e.getTrigger().getButton() == 3) {
                    final YearMonth month = view.getMonthChartInfo().month;
                    if(month == null)return;
                    final CategoryPlot plot = e.getChart().getCategoryPlot();
                    final RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), plot.getOrientation());
                    final int count = plot.getDataset(0).getColumnCount();
                    final double xStart = plot.getDomainAxis().getCategoryStart(0, count, monthPanel.getScreenDataArea(), domainEdge);
                    final double xEnd = plot.getDomainAxis().getCategoryEnd(count - 1, count, monthPanel.getScreenDataArea(), domainEdge);
                    final int x = e.getTrigger().getX();
                    if (x > xStart && x < xEnd) {
                        final double barWidth = (xEnd - xStart) / month.lengthOfMonth();
                        monthNoteEditing = month.atDay((int)Math.floor((x - xStart) / barWidth) + 1);
                        if(view.getMonthChartInfo().annotations.containsKey(monthNoteEditing)) {
                            editNote.setText(String.format("View note: %s", monthNoteEditing.format(FormatUtil.DTF_MONTH_CHART)));
                            removeNote.setVisible(true);
                        } else {
                            editNote.setText(String.format("Add note: %s", monthNoteEditing.format(FormatUtil.DTF_MONTH_CHART)));
                            removeNote.setVisible(false);
                        }
                        s.setVisible(true);
                        editNote.setVisible(true);
                    } else {
                        s.setVisible(false);
                        editNote.setVisible(false);
                        removeNote.setVisible(false);
                    }
                }
            }
            @Override
            public void chartMouseMoved(ChartMouseEvent e) {
            }
        });
        monthPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton()==3){
                    monthPanel.mouseClicked(e);
                }
            }
        });
        editNote.addActionListener(e->{
            if(monthNoteEditing != null) {
                String oldNote = YearNotes.getNote(z, monthNoteEditing);
                String note = EditNote.getNote(view.frame(), monthNoteEditing, oldNote);
                if (note != null) {
                    if (!view.getMonthChartInfo().annotations.containsKey(monthNoteEditing)) {
                        Charts.CategoryImageAnnotation annotation = Charts.monthAnnotation(
                                view.getMonthChartInfo().month,
                                monthNoteEditing.getDayOfMonth(),
                                view.getMonthChartInfo().max);
                        view.getMonthChartInfo().chart.getCategoryPlot().addAnnotation(annotation);
                        view.getMonthChartInfo().annotations.put(monthNoteEditing, annotation);
                    }
                    YearNotes.setNote(z, monthNoteEditing, note);
                    monthNoteEditing = null;
                }
            }
        });
        removeNote.addActionListener(e->{
            if(monthNoteEditing != null) {
                if (view.getMonthChartInfo().annotations.containsKey(monthNoteEditing)) {
                    view.getMonthChartInfo().chart.getCategoryPlot().removeAnnotation(
                            view.getMonthChartInfo().annotations.get(monthNoteEditing));
                    view.getMonthChartInfo().annotations.remove(monthNoteEditing);
                    YearNotes.removeNote(z, monthNoteEditing);
                    monthNoteEditing = null;
                }
            }
        });
    }

    private void createPopupNew(JCommandButton cmdNew){
        JCommandMenuButton newP = new JCommandMenuButton("New project...", ImgUtil.autoColorIcon("new.png", 16, 16));
        JCommandMenuButton newT = new JCommandMenuButton("New activity...", ImgUtil.autoColorIcon("new_tag.png", 16, 16));

        newP.setName(Action.A_NEW.name());
        newT.setName(Action.A_NEW_TAG.name());
        newP.addActionListener(this);
        newT.addActionListener(this);

        JCommandPopupMenu menu = new JCommandPopupMenu();
        menu.addMenuButton(newP);
        menu.addMenuButton(newT);
        cmdNew.setPopupCallback(jCommandButton -> menu);
        cmdNew.setCommandButtonKind(JCommandButton.CommandButtonKind.ACTION_AND_POPUP_MAIN_POPUP);
    }

    private void createPopupRefresh(JCommandButton cmdRefresh){
        JCommandMenuButton rNorm = new JCommandMenuButton("Refresh", ImgUtil.autoColorIcon("refresh.png", 16, 16));
        JCommandMenuButton rForc = new JCommandMenuButton("Deep refresh", null);
//        JCommandMenuButton rAdvn = new JCommandMenuButton("Advanced...", null);

        rNorm.setName(Action.A_REFRESH.name());
        rNorm.addActionListener(this);
        rForc.setName(Action.A_DEEP_REFRESH.name());
        rForc.addActionListener(this);

        JCommandPopupMenu menu = new JCommandPopupMenu();
        menu.addMenuButton(rNorm);
        menu.addMenuButton(rForc);
//        menu.addMenuButton(rAdvn);
        cmdRefresh.setPopupCallback(jCommandButton -> menu);
        cmdRefresh.setCommandButtonKind(JCommandButton.CommandButtonKind.ACTION_AND_POPUP_MAIN_POPUP);
    }

    public void setRefresh(JButton button, JButton deepButton){
        button.setName(Action.A_REFRESH.name());
        button.addActionListener(this);
        deepButton.setName(Action.A_DEEP_REFRESH.name());
        deepButton.addActionListener(this);
    }

    public void setRefresh(JCommandButton button){
        createPopupRefresh(button);
        button.setName(Action.A_REFRESH.name());
        button.addActionListener(this);
    }

    private JMenuItem finishResumeMenu = null;
    private JMenuItem bookmarksMenu = null;
    public void setTables(JTable tableActive, JTable tableFinished, JTable tableUtilityTags){
        view.setTablePopUpMenu(new JPopupMenu());
        JMenuItem viewMenu = new JMenuItem("View");
        JMenuItem edit = new JMenuItem("Edit");
        JMenuItem delete = new JMenuItem("Delete");
        finishResumeMenu = new JMenuItem("Finish");
        bookmarksMenu = new JMenuItem("Bookmarks");
        viewMenu.addActionListener(e->viewProject());
        edit.addActionListener(e->editProject());
        delete.addActionListener(e->deleteProject());
        finishResumeMenu.addActionListener(e->{
            finishOrResumeProject(finishResumeMenu.getText().equals("Finish"));
        });
        bookmarksMenu.addActionListener(e->{
            openBookmarks();
        });
        view.getTablePopUpMenu().add(viewMenu);
        view.getTablePopUpMenu().add(edit);
        view.getTablePopUpMenu().add(finishResumeMenu);
        view.getTablePopUpMenu().add(bookmarksMenu);
        view.getTablePopUpMenu().addSeparator();
        view.getTablePopUpMenu().add(delete);
//        tableActive.add(view.getTablePopUpMenu());
        tableActive.addMouseListener(projectMouseAdapter);
        tableFinished.addMouseListener(projectMouseAdapter);
        tableUtilityTags.addMouseListener(projectMouseAdapter);
        tableActive.getSelectionModel().addListSelectionListener(projectSelectionListener);
        tableFinished.getSelectionModel().addListSelectionListener(projectSelectionListener);
        tableUtilityTags.getSelectionModel().addListSelectionListener(projectSelectionListener);
    }

    public void setChart(JComboBox comboCharts, JButton prev, JButton next, JButton first, JButton last, BiConsumer<CacheMgr, ItemEvent> refreshMethod){
        comboCharts.addItemListener(e->{
            if(comboCharts.getItemCount()==0)return;
            refreshMethod.accept(cache, e);
        });
        prev.addActionListener(e->{
            if(comboCharts.getSelectedIndex()>0){
                comboCharts.setSelectedIndex(comboCharts.getSelectedIndex()-1);
            }
        });
        next.addActionListener(e->{
            if(comboCharts.getSelectedIndex()< comboCharts.getItemCount()-1){
                comboCharts.setSelectedIndex(comboCharts.getSelectedIndex()+1);
            }
        });
        first.addActionListener(e->{
            if(comboCharts.getSelectedIndex()>0){
                comboCharts.setSelectedIndex(0);
            }
        });
        last.addActionListener(e->{
            if(comboCharts.getSelectedIndex()<comboCharts.getItemCount()-1){
                comboCharts.setSelectedIndex(comboCharts.getItemCount()-1);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Action action = Action.valueOf(((JComponent)e.getSource()).getName());
        switch (action){
            case A_NEW:
            case A_NEW_TAG:
                EditProject x = new EditProject(view.frame(), projectList, action == Action.A_NEW_TAG);
                x.setVisible(true);
                if(x.getResult()){
                    refresh();
                }
                break;
            case A_VIEW:
                viewProject();
                break;
            case A_FINISH:
                finishOrResumeProject(true);
                break;
            case A_RESUME:
                finishOrResumeProject(false);
                break;
            case A_EDIT:
                editProject();
                break;
            case A_DELETE:
                deleteProject();
                break;
            case A_UP:
            case A_DOWN:
                int selected = view.selectedIndex();
                if(selected != -1) {
                    long hash;
                    if(action==Action.A_UP){
                        hash = projectList.moveUp(view.getTabProjects().getSelectedIndex(), selected);
                    } else {
                        hash = projectList.moveDown(view.getTabProjects().getSelectedIndex(), selected);
                    }
                    if(hash!=-1){
                        OverviewOps.refreshOrdering(cache, projectList, OverviewOps.Type.index(view.getTabProjects().getSelectedIndex()), z);
                        view.refreshProjects(cache, view.getTabProjects().getSelectedIndex());
                        view.setSelected(hash);
                    }
                }
                break;
            case A_EXPORT:
                new ExportReport(view.frame(), view.getTableActive(), view.getTableFinished()).setVisible(true);
                break;
            case A_SETTINGS:
                Settings settings = new Settings(view.frame(), bookmark);
                settings.setVisible(true);
                if(settings.getResult()){
                    refresh();
                }
                break;
            case A_REFRESH:
                refresh();
                break;
            case A_DEEP_REFRESH:
                refresh(true);
                break;
            case A_EXTRA_BOOKMARKS:
                openBookmarks();
                break;
            case A_EXTRA_FOCUS:
                FocusConfig focusConfig = new FocusConfig(view.frame(), focusMgr);
                focusConfig.setVisible(true);
                break;
            case A_DAY_AUDIT:
                if(view.getSelectedDayChart() != null) {
                    ViewDayAudit viewDayAudit = new ViewDayAudit(view.frame(), view.getSelectedDayChart(), projectList);
                    viewDayAudit.setVisible(true);
                    if(viewDayAudit.isResult()){
                        //changes saved
                        //do stuffs
                    }
                }
        }
    }

    private final MouseAdapter projectMouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if(e.getButton()==3) {
                if(e.getSource() instanceof JTable) {
                    JTable source = (JTable) e.getSource();
                    int row = source.rowAtPoint(e.getPoint());
                    int column = source.columnAtPoint(e.getPoint());

                    if (!source.isRowSelected(row))
                        source.changeSelection(row, column, false, false);
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if(e.getButton() == 3){
                if(view.getTabMain().getSelectedIndex() == 0 || view.getTabProjects().getSelectedIndex() == 0){
                    finishResumeMenu.setText("Finish");
                    finishResumeMenu.setVisible(true);
                    bookmarksMenu.setVisible(true);
                } else if(view.getTabProjects().getSelectedIndex() == 1){
                    finishResumeMenu.setText("Resume");
                    finishResumeMenu.setVisible(true);
                    bookmarksMenu.setVisible(true);
                } else {
                    bookmarksMenu.setVisible(false);
                    finishResumeMenu.setVisible(false);
                }
                view.getTablePopUpMenu().show(e.getComponent(), e.getX(), e.getY());
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if(e.getClickCount()==2 && e.getButton() == 1){
                int dblClickAction = UserConfig.userGInt(GENERAL, DOUBLE_CLICK_ACTION);
                switch (dblClickAction){
                    case 1:
                        Project p = projectList.findByHash(view.selected());
                        if(!p.isUtilityTag()){
                            bookmark.invoke(p);
                        }
                        break;
                    case 2:
                        Project q = projectList.findByHash(view.selected());
                        if(!q.isUtilityTag()){
                            if(!bookmark.contains(q) || bookmark.getOrAdd(q).size()<1){
                                bookmark.invoke(q);
                            } else {
                                bookmark.getOrAdd(q).get(0).launch(view.frame());
                            }
                        }
                        break;
                    default: viewProject(); break;
                }
            }
        }
    };

    private final ListSelectionListener projectSelectionListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            boolean temp = view.selected()!=-1;
            for (JComponent x:view.enableOnSelect()) {
                x.setEnabled(temp);
            }
        }
    };

    private final ChangeListener tabChangeListener = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            boolean temp = view.selected()!=-1;
            for (JComponent x:view.enableOnSelect()) {
                x.setEnabled(temp);
            }
            view.getButtonFinish().setVisible(view.getTabMain().getSelectedIndex()==0 || view.getTabProjects().getSelectedIndex()==0);
            view.getButtonResume().setVisible(view.getTabMain().getSelectedIndex()==1 && view.getTabProjects().getSelectedIndex()==1);
            view.getButtonBookmarks().setVisible(view.getTabMain().getSelectedIndex()==0 || view.getTabProjects().getSelectedIndex()!=2);
            view.getButtonUp().setVisible(view.getTabMain().getSelectedIndex()==1);
            view.getButtonDown().setVisible(view.getTabMain().getSelectedIndex()==1);

            if(e.getSource() == view.getTabMain()) {
                AppConfig.setTabMainSelection(view.getTabMain().getSelectedIndex());
            } else if(e.getSource() == view.getTabReports()) {
                AppConfig.setTabReportsSelection(view.getTabReports().getSelectedIndex());
            } else {
                AppConfig.setTabProjectsSelection(view.getTabProjects().getSelectedIndex());
            }
        }
    };

    private AppControl getThis(){return this;}

    private void viewProject(){
        Project p = projectList.findByHash(view.selected());
        if(p==null) return;
        if(cache.getElapsed(CacheId.ProjectDayRows(p)) > AppConfig.appGInt(AppInt.REFRESH_TIMEOUT)){
            refreshProject(p);
        } else {
            ReportRows dayRows = cache.getObj(CacheId.ProjectDayRows(p), ReportRows.class);
            ReportRows monthRows = cache.getObj(CacheId.ProjectMonthRows(p), ReportRows.class);
            showProject(p, dayRows, monthRows);
        }
    }

    private void editProject() {
        Project p = projectList.findByHash(view.selected());
        if(p==null) return;
        EditProject y = new EditProject(view.frame(), p, projectList, p.isUtilityTag());
        y.setVisible(true);
        if(y.getResult()){
            refresh();
        }
    }

    private void finishOrResumeProject(boolean finish) {
        projectList.setProjectFinished(view.selected(), finish);
        refresh();
    }

    private void openBookmarks(){
        Project p = projectList.findByHash(view.selected());
        if(!p.isUtilityTag()){
            bookmark.invoke(p);
        }
    }

    private void deleteProject() {
        Project px = projectList.findByHash(view.selected());
        if (px == null) return;
        int jopResult = JOptionPane.showConfirmDialog(view.frame(),
                String.format("Do you want to permanently delete %s `%s?`", px.isUtilityTag() ? "tag" : "project",
                        px.toString()), "Confirm deletion", JOptionPane.YES_NO_OPTION);
        if (jopResult == JOptionPane.YES_OPTION) {
            if (projectList.deleteProject(px)) {
                refresh();
            }
        }
    }

    private void decideRefresh(){
        if(cache.getElapsed(AllReportRows.activeCacheId(z)) > AppConfig.appGInt(AppInt.REFRESH_TIMEOUT)){
            refresh();
        } else view.refreshRefreshStatus(cache);
    }

    private void refreshProject(Project p){
        LoaderDialog.execute(view.frame(), new ProjectViewCreator(p), getThis()::projectCB);
    }

    private void refresh(){
        refresh(false);
    }

    private void refresh(boolean forceReconstructAndDelete){
        if(forceReconstructAndDelete && JOptionPane.showConfirmDialog(view.frame(),
                "Use deep refresh to expunge discrepancy from older charts. \nPerform deep refresh?",
                        "Confirmation", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
            return;
        }
        tracker.flushRecordBuffer();
        Loader.execute("refresh", new Refresher(cache, projectList, bookmark, forceReconstructAndDelete, forceReconstructAndDelete), this::refreshCB,
                view.getToShowOnRefresh(), view.toHideOnRefresh());
    }

    private void showProject(Project p, ReportRows dayRows, ReportRows monthRows){
        SwingUtilities.invokeLater(() -> new ViewProject(view.frame(), p.isUtilityTag(), p.toString(), String.join(", ",p.getTags())
                , new DayModel(dayRows), new MonthModel(monthRows)).setVisible(true));
    }

    private void projectCB(ProjectViewResult result, Throwable t){
        if(t != null){
            if(t.getMessage()!=null) {
                JOptionPane.showMessageDialog(view.frame(),
                        String.format("Could not view this item at the moment.\nreason: %s", t.getMessage()),
                        "View item", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(view.frame(),
                        String.format("Could not read data for this item due to an error.\nerror code: %s", t.toString()),
                        "View item", JOptionPane.ERROR_MESSAGE);
            }
        } else if(result!=null) {
            cache.put(CacheId.ProjectDayRows(result.p), result.dayRows);
            cache.put(CacheId.ProjectMonthRows(result.p), result.monthRows);
            showProject(result.p, result.dayRows, result.monthRows);
        }
    }

    private void refreshCB(Boolean result, Throwable t){
        if(result == null){
            if(t != null){
                Debug.log(t);
            }
        } else if(result){
            view.refreshOverview(cache, projectList.getCategories().toArray(new String[]{}));
        }
    }

}
