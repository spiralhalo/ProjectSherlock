package xyz.spiralhalo.sherlock.report.factory;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.report.persist.ReportRows;

public class ProjectViewResult {
    public Project p;
    public ReportRows dayRows;
    public ReportRows monthRows;

    public ProjectViewResult(Project p, ReportRows dayRows, ReportRows monthRows) {
        this.p = p;
        this.dayRows = dayRows;
        this.monthRows = monthRows;
    }
}
