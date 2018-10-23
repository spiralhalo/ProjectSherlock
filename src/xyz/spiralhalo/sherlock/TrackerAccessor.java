package xyz.spiralhalo.sherlock;

public interface TrackerAccessor {
    void addListener(TrackerListener listener);
    long getGranularityMillis();
}
