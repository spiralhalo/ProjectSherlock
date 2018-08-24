package xyz.spiralhalo.sherlock.report.factory;

import org.jfree.data.category.DefaultCategoryDataset;
import xyz.spiralhalo.sherlock.report.persist.ChartMeta;
import xyz.spiralhalo.sherlock.report.persist.DateList;

import java.time.LocalDate;
import java.util.ArrayList;

public class DatasetArray {
    public final ArrayList<DefaultCategoryDataset> datasets = new ArrayList<>();
    public final ArrayList<ChartMeta> datasetColors = new ArrayList<>();
    public final DateList dateList = new DateList();
    public void add(LocalDate date, DefaultCategoryDataset dataset, ChartMeta colors){
        datasets.add(dataset);
        dateList.add(date);
        datasetColors.add(colors);
    }
}
