package xyz.spiralhalo.sherlock;

import xyz.spiralhalo.sherlock.persist.project.Project;

public interface TrackerAccessor {
    Project lastTracked();
}
