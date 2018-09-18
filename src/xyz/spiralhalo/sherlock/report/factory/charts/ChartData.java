package xyz.spiralhalo.sherlock.report.factory.charts;

import org.jfree.data.category.DefaultCategoryDataset;
import xyz.spiralhalo.sherlock.persist.cache.Cache;

import java.io.Serializable;

@Cache
public class ChartData implements Serializable {
    public static final long serialVersionUID = 1L;
    private final ChartMeta meta;
    private final DefaultCategoryDataset dataset;

    public ChartData(ChartMeta meta, DefaultCategoryDataset dataset) {
        this.meta = meta;
        this.dataset = dataset;
    }

    public ChartMeta getMeta() {
        return meta;
    }

    public DefaultCategoryDataset getDataset() {
        return dataset;
    }
}
