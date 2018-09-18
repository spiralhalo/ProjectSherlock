package xyz.spiralhalo.sherlock.report.factory.summary;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SummaryRow implements Serializable {

    public static final long serialVersionUID = 1L;
    private final long hash;
    private final int seconds;
    private final boolean productive;
    private final LocalDateTime earliest;

    public SummaryRow(long hash, int seconds, boolean productive, LocalDateTime earliest) {
        this.hash = hash;
        this.seconds = seconds;
        this.productive = productive;
        this.earliest = earliest;
    }

    public long getHash() {
        return hash;
    }

    public int getSeconds() {
        return seconds;
    }

    public boolean isProductive() {
        return productive;
    }

    public LocalDateTime getEarliest() {
        return earliest;
    }
}
