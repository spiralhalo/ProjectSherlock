package xyz.spiralhalo.sherlock.report.persist;

import java.io.Serializable;
import java.time.LocalDate;

public class ReportRow implements Serializable {
    private LocalDate timestamp;
    private int seconds;

    public ReportRow(LocalDate timestamp, int seconds) {
        this.timestamp = timestamp;
        this.seconds = seconds;
    }

    public LocalDate getTimestamp() {
        return timestamp;
    }

    public int getSeconds() {
        return seconds;
    }
}
