package xyz.spiralhalo.sherlock;

import xyz.spiralhalo.sherlock.EnumerateWindows.WindowInfo;
import xyz.spiralhalo.sherlock.persist.project.Project;

public interface TrackerListener {
    void onTrackerLog(Project projectOrNull, WindowInfo windowInfo);
}
