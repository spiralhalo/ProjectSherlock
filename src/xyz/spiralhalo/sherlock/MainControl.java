package xyz.spiralhalo.sherlock;

import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import xyz.spiralhalo.sherlock.async.Loader;
import xyz.spiralhalo.sherlock.async.LoaderDialog;
import xyz.spiralhalo.sherlock.bookmark.BookmarkMgr;
import xyz.spiralhalo.sherlock.dialog.EditProject;
import xyz.spiralhalo.sherlock.dialog.Quit;
import xyz.spiralhalo.sherlock.dialog.Settings;
import xyz.spiralhalo.sherlock.dialog.ViewProject;
import xyz.spiralhalo.sherlock.persist.cache.CacheId;
import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;
import xyz.spiralhalo.sherlock.persist.project.ProjectListIO;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig.AppBool;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig.AppInt;
import xyz.spiralhalo.sherlock.record.legacy.AutoImporter2;
import xyz.spiralhalo.sherlock.report.*;
import xyz.spiralhalo.sherlock.report.factory.*;
import xyz.spiralhalo.sherlock.report.ops.OverviewOps;
import xyz.spiralhalo.sherlock.report.persist.ReportRows;
import xyz.spiralhalo.sherlock.util.ImgUtil;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.Arrays;

import static java.awt.Frame.ICONIFIED;
import static java.awt.Frame.NORMAL;

public class MainControl implements ActionListener {

    private static MainControl instance;

    public static void create() {
        if(instance!=null)return;
        instance = new MainControl();
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
        A_SETTINGS,
        A_REFRESH,
        A_EXTRA_BOOKMARKS
    }
    private MainViewAccessor view;
    private final ProjectList projectList;
    private final Tracker tracker;
    private final CacheMgr cache;
    private final TrayIcon trayIcon;
    private final boolean trayIconUsed;
    private final BookmarkMgr bookmark;

    private MainControl() {
        cache = new CacheMgr();
        projectList = ProjectListIO.load();
        AutoImporter2.importRecord(projectList);
        tracker = new Tracker(projectList);
        tracker.start();
        bookmark = new BookmarkMgr(tracker);

        createView();
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
    }

    private void createView(){
        view = new MainView(this);
        view.init();
        view.frame().addWindowListener(windowAdapter);
        view.frame().addWindowStateListener(windowAdapter);
        view.frame().addWindowFocusListener(windowAdapter);
        view.frame().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        view.frame().setIconImages(Arrays.asList(ImgUtil.createImage("icon.png","App Icon Small"),
                ImgUtil.createImage("med_icon.png","App Icon")));
        view.refreshOverview(cache);
    }

    private WindowAdapter windowAdapter = new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            if(AppConfig.getBool(AppBool.ASK_BEFORE_QUIT)) {
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
            if(trayIconUsed && AppConfig.getBool(AppBool.MINIMIZE_TO_TRAY) && (view.frame().getState() == ICONIFIED)){
                minimizeToTray();
            }
        }
    };

    private void minimizeToTray(){
        if(trayIconUsed) {
            trayIcon.getPopupMenu().getItem(0).setLabel("Restore");
            view.frame().setVisible(false);
        }
    }

    private void setToolbarInternal(JComponent btnNew, JComponent btnView, JComponent btnFinish, JComponent btnResume,
                                    JComponent btnEdit, JComponent btnDelete, JComponent btnUp, JComponent btnDown,
                                    JComponent btnSettings, JTabbedPane tabs, JTabbedPane tabr){
        btnNew.setName(Action.A_NEW.name());
        btnView.setName(Action.A_VIEW.name());
        btnFinish.setName(Action.A_FINISH.name());
        btnResume.setName(Action.A_RESUME.name());
        btnEdit.setName(Action.A_EDIT.name());
        btnDelete.setName(Action.A_DELETE.name());
        btnUp.setName(Action.A_UP.name());
        btnDown.setName(Action.A_DOWN.name());
        btnSettings.setName(Action.A_SETTINGS.name());
        addEnableOnSelect(btnView, btnEdit, btnDelete, btnUp, btnDown);
        btnFinish.setEnabled(false);
        setTabs(btnResume, tabs, tabr);
    }

    public void setToolbar(JButton btnNew, JButton btnNewTag, JButton btnView, JButton btnFinish, JButton btnResume,
                           JButton btnEdit, JButton btnDelete, JButton btnUp, JButton btnDown,
                           JButton btnSettings, JTabbedPane tabs, JTabbedPane tabr){
        setToolbarInternal(btnNew, btnView, btnFinish, btnResume, btnEdit, btnDelete, btnUp, btnDown, btnSettings, tabs, tabr);
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
        btnSettings.addActionListener(this);
    }

    public void setToolbar(JCommandButton btnNew, JCommandButton btnView, JCommandButton btnFinish, JCommandButton btnResume,
                           JCommandButton btnEdit, JCommandButton btnDelete, JCommandButton btnUp, JCommandButton btnDown,
                           JCommandButton btnSettings, JTabbedPane tabs, JTabbedPane tabr){
        createPopupNew(btnNew);
        setToolbarInternal(btnNew, btnView, btnFinish, btnResume, btnEdit, btnDelete, btnUp, btnDown, btnSettings, tabs, tabr);
        btnNew.addActionListener(this);
        btnView.addActionListener(this);
        btnFinish.addActionListener(this);
        btnResume.addActionListener(this);
        btnEdit.addActionListener(this);
        btnDelete.addActionListener(this);
        btnUp.addActionListener(this);
        btnDown.addActionListener(this);
        btnSettings.addActionListener(this);
    }

    public void setExtras(JButton btnBookmarks) {
        btnBookmarks.setName(Action.A_EXTRA_BOOKMARKS.name());
        btnBookmarks.addActionListener(this);
    }

    public void setExtras(JCommandButton btnBookmarks) {
        btnBookmarks.setName(Action.A_EXTRA_BOOKMARKS.name());
        btnBookmarks.addActionListener(this);
    }

    public void addEnableOnSelect(JComponent... components) {
        for (JComponent component:components) {
            view.enableOnSelect().add(component);
            component.setEnabled(false);
        }
    }

    private void setTabs(JComponent btnResume, JTabbedPane tabs, JTabbedPane tabr){
        tabs.addChangeListener(tabChangeListener);
        tabr.addChangeListener(tabChangeListener);
        btnResume.setVisible(false);
    }

    private void createPopupNew(JCommandButton cmdNew){
        JCommandMenuButton newP = new JCommandMenuButton("New project...", ImgUtil.autoColorIcon("new.png", 16, 16));
        JCommandMenuButton newT = new JCommandMenuButton("New tag...", ImgUtil.autoColorIcon("new_tag.png", 16, 16));

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
        JCommandMenuButton rForc = new JCommandMenuButton("Full refresh", null);
        JCommandMenuButton rAdvn = new JCommandMenuButton("Advanced...", null);

        rNorm.setName(Action.A_REFRESH.name());
        rNorm.addActionListener(this);

        JCommandPopupMenu menu = new JCommandPopupMenu();
        menu.addMenuButton(rNorm);
        menu.addMenuButton(rForc);
        menu.addMenuButton(rAdvn);
        cmdRefresh.setPopupCallback(jCommandButton -> menu);
        cmdRefresh.setCommandButtonKind(JCommandButton.CommandButtonKind.ACTION_AND_POPUP_MAIN_POPUP);
    }

    public void setRefresh(JButton button){
        button.setName(Action.A_REFRESH.name());
        button.addActionListener(this);
    }

    public void setRefresh(JCommandButton button){
        button.setName(Action.A_REFRESH.name());
        button.addActionListener(this);
    }

    public void setTables(JTable tableActive, JTable tableFinished, JTable tableUtilityTags){
        view.setTablePopUpMenu(new PopupMenu());
        MenuItem viewMenu = new MenuItem("View");
        MenuItem edit = new MenuItem("Edit");
        MenuItem delete = new MenuItem("Delete");
        viewMenu.addActionListener(e->viewProject());
        edit.addActionListener(e->editProject());
        delete.addActionListener(e->deleteProject());
        view.getTablePopUpMenu().add(viewMenu);
        view.getTablePopUpMenu().add(edit);
        view.getTablePopUpMenu().addSeparator();
        view.getTablePopUpMenu().add(delete);
//        view.getTabProjects().add(view.getTablePopUpMenu());
        tableActive.addMouseListener(tableAdapter);
        tableFinished.addMouseListener(tableAdapter);
        tableUtilityTags.addMouseListener(tableAdapter);
        tableActive.getSelectionModel().addListSelectionListener(tableSelectionListener);
        tableFinished.getSelectionModel().addListSelectionListener(tableSelectionListener);
        tableUtilityTags.getSelectionModel().addListSelectionListener(tableSelectionListener);
    }

    public void setChart(JComboBox comboCharts, JButton prev, JButton next, JButton first, JButton last){
        comboCharts.addItemListener(e->{
            if(comboCharts.getItemCount()==0)return;
            view.refreshChart(cache);
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
        view.refreshChart(cache);
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
                projectList.setProjectFinished(view.selected(), true);
                refresh();
                break;
            case A_RESUME:
                projectList.setProjectFinished(view.selected(), false);
                refresh();
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
                        OverviewOps.refreshOrdering(cache, projectList, OverviewOps.Type.index(view.getTabProjects().getSelectedIndex()));
                        view.refreshProjects(cache, view.getTabProjects().getSelectedIndex());
                        view.setSelected(hash);
                    }
                }
                break;
            case A_SETTINGS:
                Settings settings = new Settings(view.frame());
                settings.setVisible(true);
                if(settings.getResult()){
                    refresh();
                }
                break;
            case A_REFRESH:
                refresh();
                break;
            case A_EXTRA_BOOKMARKS:
                Project p = projectList.findByHash(view.selected());
                if(!p.isUtilityTag()){
                    bookmark.invoke(p);
                }
                break;
        }
    }

    private final MouseAdapter tableAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if(e.getButton()==3) {
                JTable source = (JTable) e.getSource();
                int row = source.rowAtPoint(e.getPoint());
                int column = source.columnAtPoint(e.getPoint());

                if (!source.isRowSelected(row))
                    source.changeSelection(row, column, false, false);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if(e.getButton() == 3){
                view.getTablePopUpMenu().show(e.getComponent(), e.getX(), e.getY());
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if(e.getClickCount()==2 && e.getButton() == 1){
                viewProject();
            }
        }
    };

    private final ListSelectionListener tableSelectionListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            boolean temp = view.selected()!=-1;
            for (JComponent x:view.enableOnSelect()) {
                x.setEnabled(temp);
            }
            view.getButtonFinish().setVisible(view.getTabProjects().getSelectedIndex()!=1 || !temp);
            view.getButtonFinish().setEnabled(view.getTabProjects().getSelectedIndex()==0 && temp);
            view.getButtonResume().setVisible(view.getTabProjects().getSelectedIndex()==1 && temp);
        }
    };

    private final ChangeListener tabChangeListener = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            boolean temp = view.selected()!=-1 && view.getTabReports().getSelectedIndex()==0;
            for (JComponent x:view.enableOnSelect()) {
                x.setEnabled(temp);
            }
            view.getButtonFinish().setVisible(view.getTabProjects().getSelectedIndex()!=1 || !temp);
            view.getButtonFinish().setEnabled(view.getTabProjects().getSelectedIndex()==0 && temp);
            view.getButtonResume().setVisible(view.getTabProjects().getSelectedIndex()==1 && temp);
            view.getButtonBookmarks().setEnabled(view.getTabProjects().getSelectedIndex()!=2 && temp);
        }
    };

    private MainControl getThis(){return this;}

    private void viewProject(){
        Project p = projectList.findByHash(view.selected());
        if(p==null) return;
        if(cache.getElapsed(CacheId.ProjectDayRows(p)) > AppConfig.getInt(AppInt.REFRESH_TIMEOUT)){
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
        if(cache.getElapsed(CacheId.ActiveRows) > AppConfig.getInt(AppInt.REFRESH_TIMEOUT)){
            refresh();
        } else view.refreshStatus(cache);
    }

    private void refreshProject(Project p){
        LoaderDialog.execute(view.frame(), new ProjectViewCreator(p), getThis()::projectCB);
    }

    private void refresh(){
        tracker.flushRecordBuffer();
        Loader.execute("refresh", new OverviewCreator(projectList), this::refreshCB,
                view.getToShowOnRefresh(), view.toHideOnRefresh());
    }

    private void showProject(Project p, ReportRows dayRows, ReportRows monthRows){
        SwingUtilities.invokeLater(() -> new ViewProject(view.frame(), p.toString(), String.join(", ",p.getTags())
                , new DayModel(dayRows), new MonthModel(monthRows)).setVisible(true));
    }

    private void projectCB(ProjectViewResult result, Throwable t){
        if(t != null){
            JOptionPane.showMessageDialog(view.frame(),
                    String.format("Failed to refresh the project due to an error.\nerror code:\n\t%s", t.toString()),
                    "Refresh failed", JOptionPane.ERROR_MESSAGE);
        } else if(result!=null) {
            cache.put(CacheId.ProjectDayRows(result.p), result.dayRows);
            cache.put(CacheId.ProjectMonthRows(result.p), result.monthRows);
            showProject(result.p, result.dayRows, result.monthRows);
        }
    }

    private void refreshCB(OverviewResult result, Throwable t){
        if(t != null){
            JOptionPane.showMessageDialog(view.frame(),
                    String.format("Failed to refresh due to an error.\nerror code:\n\t%s", t.toString()),
                    "Refresh failed", JOptionPane.ERROR_MESSAGE);
        } else if(result!=null){
            cache.put(CacheId.ActiveRows, result.activeRows);
            cache.put(CacheId.FinishedRows, result.finishedRows);
            cache.put(CacheId.UtilityRows, result.utilityRows);
            cache.put(CacheId.DayRows, result.dayRows);
            cache.put(CacheId.MonthRows, result.monthRows);
            DatasetArray x = result.datasetArray;
            for (int i = 0; i < x.dateList.size(); i++) {
                LocalDate date = x.dateList.get(i);
                cache.put(CacheId.ChartData(date), x.datasets.get(i));
                cache.put(CacheId.ChartMeta(date), x.datasetColors.get(i));
            }
            cache.put(CacheId.ChartList, x.dateList);
            view.refreshOverview(cache);
        }
    }

}
