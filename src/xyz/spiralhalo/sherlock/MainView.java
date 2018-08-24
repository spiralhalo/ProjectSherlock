package xyz.spiralhalo.sherlock;

import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;

import javax.swing.*;

public interface MainView {
    JFrame getFrame();
    long getSelectedProject();
    void refreshChart(CacheMgr cache);
    void refreshOverview(CacheMgr cache);
    void refreshStatus(CacheMgr cache);
}
