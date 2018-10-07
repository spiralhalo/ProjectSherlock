package xyz.spiralhalo.sherlock;

import xyz.spiralhalo.sherlock.persist.project.Project;

public interface TrackerListener {
    void onLog(Project project, String windowTitle, String exe);
}
