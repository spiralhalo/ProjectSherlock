package xyz.spiralhalo.sherlock.report.persist;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AllReportRow implements Serializable {
    private long projectHash;
    private int projectColor;
    private String projectName;
    private LocalDate startDate;
    private LocalDate finishDate;
    private int days;
    private int seconds;

    public AllReportRow(long projectHash, int projectColor, String projectName, LocalDateTime startDate, LocalDateTime finishDate, int days, int seconds) {
        this.projectHash = projectHash;
        this.projectColor = projectColor;
        this.projectName = projectName;
        this.startDate = LocalDate.from(startDate);
        this.finishDate = finishDate==null?null:LocalDate.from(finishDate);
        this.days = days;
        this.seconds = seconds;
    }

    public long getProjectHash() { return projectHash; }

    public int getProjectColor() {
        return projectColor;
    }

    public String getProjectName() {
        return projectName;
    }

    public int getDays() {
        return days;
    }

    public int getSeconds() {
        return seconds;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getFinishDate() {
        return finishDate;
    }
}
