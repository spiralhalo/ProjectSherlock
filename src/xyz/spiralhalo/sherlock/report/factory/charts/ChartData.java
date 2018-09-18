package xyz.spiralhalo.sherlock.report.factory.charts;

import org.jfree.data.category.DefaultCategoryDataset;
import xyz.spiralhalo.sherlock.util.FormatUtil;

import java.io.Serializable;
import java.time.LocalDate;

public class ChartData implements Serializable {
    public static final long serialVersionUID = 1L;

    public static String dayId(LocalDate date) {
        return String.format("dayChart_%s", FormatUtil.DTF_YMD.format(date));
    }

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
