package xyz.spiralhalo.sherlock.report.factory.summary;

import xyz.spiralhalo.sherlock.persist.cache.Cache;

import java.io.Serializable;
import java.time.YearMonth;
import java.util.ArrayList;

@Cache
public class MonthDetails extends ArrayList<DetailsRow> implements Serializable {
    public static final long serialVersionUID = 1L;
    private final YearMonth month;

    public MonthDetails(YearMonth month) {
        this.month = month;
    }
}
