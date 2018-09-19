package xyz.spiralhalo.sherlock;

import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;
import xyz.spiralhalo.sherlock.report.Charts;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;

public interface MainViewAccessor {
    JFrame frame();
    void init();
    void refreshStatus(CacheMgr cache);
    void refreshOverview(CacheMgr cache);
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
    JTabbedPane getTabProjects();
    JTabbedPane getTabReports();
    JComponent getButtonFinish();
    JComponent getButtonResume();
    JComponent getButtonBookmarks();
    void setTablePopUpMenu(JPopupMenu popupMenu);
    JPopupMenu getTablePopUpMenu();
    Charts.MonthChartInfo getMonthChartInfo();
}
