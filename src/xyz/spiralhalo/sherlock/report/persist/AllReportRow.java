package xyz.spiralhalo.sherlock.report.persist;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AllReportRow implements Serializable {
    public static final long serialVersionUID = 1L;

    private final long projectHash;
    private final int projectColor;
    private final String projectName;
    private final LocalDate startDate;
    private final LocalDate finishDate;
    private final int days;
    private final int seconds;
    private final boolean productive;

    public AllReportRow(long projectHash, int projectColor, String projectName, LocalDateTime startDate, LocalDateTime finishDate, int days, int seconds) {
        this.projectHash = projectHash;
        this.projectColor = projectColor;
        this.projectName = projectName;
        this.startDate = LocalDate.from(startDate);
        this.finishDate = finishDate==null?null:LocalDate.from(finishDate);
        this.days = days;
        this.seconds = seconds;
        this.productive = true;
    }

    public AllReportRow(long projectHash, int projectColor, String projectName, boolean isProductive, int days, int seconds) {
        this.projectHash = projectHash;
        this.projectColor = projectColor;
        this.projectName = projectName;
        this.startDate = null;
        this.finishDate = null;
        this.days = days;
        this.seconds = seconds;
        this.productive = isProductive;
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

    public boolean isProductive() {
        return productive;
    }
}
