package xyz.spiralhalo.sherlock.report.factory;

import xyz.spiralhalo.sherlock.report.persist.AllReportRows;
import xyz.spiralhalo.sherlock.report.persist.ReportRows;

public class OverviewResult {
    public AllReportRows activeRows;
    public AllReportRows finishedRows;
    public AllReportRows utilityRows;
    public ReportRows dayRows;
    public ReportRows monthRows;
    public DatasetArray datasetArray;

    public OverviewResult(AllReportRows activeRows, AllReportRows finishedRows, AllReportRows utilityRows, ReportRows dayRows, ReportRows monthRows, DatasetArray datasetArray) {
        this.activeRows = activeRows;
        this.finishedRows = finishedRows;
        this.utilityRows = utilityRows;
        this.dayRows = dayRows;
        this.monthRows = monthRows;
        this.datasetArray = datasetArray;
    }
}
