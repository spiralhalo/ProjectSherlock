package xyz.spiralhalo.sherlock.report.factoryOld;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.report.persist.ReportRows;

public class ProjectViewResult {
    public final Project p;
    public final ReportRows dayRows;
    public final ReportRows monthRows;

    public ProjectViewResult(Project p, ReportRows dayRows, ReportRows monthRows) {
        this.p = p;
        this.dayRows = dayRows;
        this.monthRows = monthRows;
    }
}
