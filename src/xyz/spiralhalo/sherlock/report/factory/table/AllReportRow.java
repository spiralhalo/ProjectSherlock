package xyz.spiralhalo.sherlock.report.factory.table;

import xyz.spiralhalo.sherlock.persist.project.Project;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AllReportRow implements Serializable {
    public static final long serialVersionUID = 3L;

    private final long projectHash;
    private final int projectColor;
    private final String projectName;
    private final String category;
    private final LocalDate startDate;
    private final LocalDate finishDate;
    private final int days;
    private final int seconds;
    private final int ptype;

    public AllReportRow(long projectHash, int projectColor, String projectName, String category, int pType, LocalDateTime startDate, LocalDateTime finishDate, int days, int seconds) {
        this.projectHash = projectHash;
        this.projectColor = projectColor;
        this.projectName = projectName;
        this.category = category;
        this.startDate = LocalDate.from(startDate);
        this.finishDate = finishDate==null?null:LocalDate.from(finishDate);
        this.days = days;
        this.seconds = seconds;
        this.ptype = pType;
    }

    public AllReportRow(long projectHash, int projectColor, String projectName, String category, int pType, int days, int seconds) {
        this.projectHash = projectHash;
        this.projectColor = projectColor;
        this.projectName = projectName;
        this.category = category;
        this.startDate = null;
        this.finishDate = null;
        this.days = days;
        this.seconds = seconds;
        this.ptype = pType;
    }
    public long getProjectHash() { return projectHash; }

    public int getProjectColor() {
        return projectColor;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getCategory() {
        return category;
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

    public String getPTypeLabel() {
        switch (ptype){
            case Project.PTYPE_PRODUCTIVE: return Project.PRODUCTIVE_LABEL;
            case Project.PTYPE_RECREATIONAL: return Project.RECREATIONAL_LABEL;
            default: return Project.NON_PRODUCTIVE_LABEL;
        }
    }
}
