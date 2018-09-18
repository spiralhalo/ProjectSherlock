package xyz.spiralhalo.sherlock.report.factory.charts;

import org.jfree.data.category.DefaultCategoryDataset;
import xyz.spiralhalo.sherlock.persist.cache.Cache;
import xyz.spiralhalo.sherlock.report.factory.ReportCache;

import java.io.Serializable;

@Cache
public class ChartData implements ReportCache, Serializable {
    public static final long serialVersionUID = 1L;
    private final ChartMeta meta;
    private final DefaultCategoryDataset dataset;
    private final boolean complete;

    public ChartData(ChartMeta meta, DefaultCategoryDataset dataset, boolean complete) {
        this.meta = meta;
        this.dataset = dataset;
        this.complete = complete;
    }

    public ChartMeta getMeta() {
        return meta;
    }

    public DefaultCategoryDataset getDataset() {
        return dataset;
    }

    @Override
    public boolean isComplete() {
        return complete;
    }
}
