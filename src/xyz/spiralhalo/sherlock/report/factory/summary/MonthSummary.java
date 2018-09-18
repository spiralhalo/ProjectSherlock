package xyz.spiralhalo.sherlock.report.factory.summary;

import xyz.spiralhalo.sherlock.persist.cache.Cache;
import xyz.spiralhalo.sherlock.report.factory.ReportCache;

import java.io.Serializable;
import java.time.YearMonth;
import java.util.ArrayList;

@Cache
public class MonthSummary extends ArrayList<SummaryRow> implements ReportCache, Serializable {
    public static final long serialVersionUID = 1L;
    private final YearMonth month;
    private final boolean complete;

    public MonthSummary(YearMonth month, boolean complete) {
        this.month = month;
        this.complete = complete;
    }

    @Override
    public boolean isComplete() {
        return complete;
    }
}
