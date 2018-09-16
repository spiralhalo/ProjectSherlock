package xyz.spiralhalo.sherlock;

import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public interface MainViewAccessor {
    JFrame frame();
    void init();
    void refreshStatus(CacheMgr cache);
    void refreshOverview(CacheMgr cache);
    void refreshProjects(CacheMgr cache, int index);
    void refreshChart(CacheMgr cache);
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
    void setTablePopUpMenu(PopupMenu popupMenu);
    PopupMenu getTablePopUpMenu();
}
