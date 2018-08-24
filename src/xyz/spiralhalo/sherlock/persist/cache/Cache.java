//package xyz.spiralhalo.sherlock.persist.cache;
//
//import org.jfree.data.category.CategoryDataset;
//import xyz.spiralhalo.sherlock.persist.project.Project;
//import xyz.spiralhalo.sherlock.report.DatasetColors;
//import xyz.spiralhalo.sherlock.report.persist.AllReportRows;
//import xyz.spiralhalo.sherlock.report.persist.ReportRows;
//import xyz.spiralhalo.sherlock.util.Debug;
//import xyz.spiralhalo.sherlock.util.PathUtil;
//
//import java.io.*;
//import java.util.HashMap;
//
//public class Cache implements Serializable {
//    public static final String CACHE_FILE = "cache";
//    public static final long serialVersionUID = 1L;
//
//    public static final String ActiveRows = "activeModel";
//    public static final String FinishedRows = "finishedModel";
//    public static final String DayRows = "dayModel";
//    public static final String MonthRows = "monthModel";
//    public static final String chartDatasetId = "chartDataset";
//    public static final String datasetColorsId = "datasetColors";
//    public static String ProjectDayRows(Project project){return project.getHash()+"_dayModel";}
//    public static String ProjectMonthRows(Project project){return project.getHash()+"_monthModel";}
//
//    private static final Cached NullCached = new Cached(null,null);
//
//    private HashMap<String, Cached> cache;
//
//    private Cache(){
//        cache = new HashMap<>();
//    }
//
//    public void putProjectReport(Project project, ReportRows dayModel, ReportRows monthModel){
//        cache.put(ProjectDayRows(project),null);
//        cache.put(ProjectMonthRows(project),null);
//        System.gc();
//        cache.put(ProjectDayRows(project), new Cached(dayModel,dayModel.getClass()));
//        cache.put(ProjectMonthRows(project), new Cached(monthModel,monthModel.getClass()));
//        save();
//    }
//
//    public void putAllReport(AllReportRows ActiveRows, AllReportRows FinishedRows,
//                             ReportRows dayModel, ReportRows monthModel, CategoryDataset dataset,
//                             DatasetColors datasetColors){
//        cache.put(ActiveRows,null);
//        cache.put(FinishedRows,null);
//        cache.put(DayRows,null);
//        cache.put(MonthRows,null);
//        cache.put(chartDatasetId,null);
//        cache.put(datasetColorsId,null);
//        System.gc();
//        cache.put(ActiveRows, new Cached(ActiveRows, ActiveRows.getClass()));
//        cache.put(FinishedRows, new Cached(FinishedRows,FinishedRows.getClass()));
//        cache.put(DayRows, new Cached(dayModel,dayModel.getClass()));
//        cache.put(MonthRows, new Cached(monthModel,monthModel.getClass()));
//        cache.put(chartDatasetId, new Cached(dataset,dataset.getClass()));
//        cache.put(datasetColorsId, new Cached(datasetColors,datasetColors.getClass()));
//        save();
//    }
//
//    public Cached[] get(String... ids){
//        Cached[] o = new Cached[ids.length];
//        for (int i = 0; i < ids.length; i++) {
//            o[i] = cache.getOrDefault(ids[i],NullCached);
//        }
//        return o;
//    }
//
//    synchronized private void save(){
//        File file = new File(PathUtil.getSaveDir(),CACHE_FILE);
//        try(FileOutputStream fis = new FileOutputStream(file);
//            ObjectOutputStream ois = new ObjectOutputStream(fis)){
//            ois.writeObject(this);
//        } catch (IOException e) {
//            Debug.log(Cache.class,e);
//        }
//    }
//
//    synchronized public static Cache load(){
//        File file = new File(PathUtil.getSaveDir(),CACHE_FILE);
//        try(FileInputStream fis = new FileInputStream(file);
//            ObjectInputStream ois = new ObjectInputStream(fis)){
//            return  (Cache) ois.readObject();
//        } catch (IOException | ClassNotFoundException e) {
//            Debug.log(Cache.class,e);
//            return new Cache();
//        }
//    }
//}
