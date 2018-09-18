package xyz.spiralhalo.sherlock.report.factoryOld;

import xyz.spiralhalo.sherlock.report.persist.AllReportRows;
import xyz.spiralhalo.sherlock.report.persist.ReportRows;

public class OverviewResult {
    public final AllReportRows activeRows;
    public final AllReportRows finishedRows;
    public final AllReportRows utilityRows;
    public final ReportRows dayRows;
    public final ReportRows monthRows;
    public final DatasetArray datasetArray;

    public OverviewResult(AllReportRows activeRows, AllReportRows finishedRows, AllReportRows utilityRows, ReportRows dayRows, ReportRows monthRows, DatasetArray datasetArray) {
        this.activeRows = activeRows;
        this.finishedRows = finishedRows;
        this.utilityRows = utilityRows;
        this.dayRows = dayRows;
        this.monthRows = monthRows;
        this.datasetArray = datasetArray;
    }
}
