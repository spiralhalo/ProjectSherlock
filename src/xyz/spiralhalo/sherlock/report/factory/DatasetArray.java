package xyz.spiralhalo.sherlock.report.factory;

import org.jfree.data.category.DefaultCategoryDataset;
import xyz.spiralhalo.sherlock.report.DatasetColors;
import xyz.spiralhalo.sherlock.report.persist.DatasetList;

import java.time.LocalDate;
import java.util.ArrayList;

public class DatasetArray {
    public final ArrayList<DefaultCategoryDataset> datasets = new ArrayList<>();
    public final ArrayList<DatasetColors> datasetColors = new ArrayList<>();
    public final DatasetList datasetList = new DatasetList();
    public void add(LocalDate date, DefaultCategoryDataset dataset, DatasetColors colors){
        datasets.add(dataset);
        datasetList.add(date);
        datasetColors.add(colors);
    }
}
