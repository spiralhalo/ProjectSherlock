package xyz.spiralhalo.sherlock.report.ops;

import xyz.spiralhalo.sherlock.persist.cache.CacheId;
import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.report.persist.AllReportRow;
import xyz.spiralhalo.sherlock.report.persist.AllReportRows;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OverviewOps {
    public enum Type{
        ACTIVE(CacheId.ActiveRows), FINISHED(CacheId.FinishedRows), UTILITY(CacheId.UtilityRows);
        CacheId id; Type(CacheId x){id=x;}
        public static Type index(int i){switch(i){case 0:return ACTIVE;case 1:return FINISHED;case 2:return UTILITY;default:return null;}}
    }

    public static void refreshOrdering(CacheMgr cache, ProjectList projectList, Type type){
        List<? extends Project> projects;
        if(type==Type.ACTIVE){
            projects = projectList.getActiveProjects();
        } else if(type==Type.FINISHED){
            projects = projectList.getFinishedProjects();
        } else if(type==Type.UTILITY){
            projects = projectList.getUtilityTags();
        } else {
            throw new UnsupportedOperationException("Unknown type.");
        }
        AllReportRows arr = cache.getObj(type.id, AllReportRows.class);
        HashMap<Long, Integer> indices = new HashMap<>();
        int i = 0;
        for (Project x:projects) {
            indices.put(x.getHash(), i);
            i ++;
        }
        for (int j = 0; j < arr.size(); j++) {
            while(j!=indices.getOrDefault(arr.get(j).getProjectHash(),j)){
                AllReportRow r = arr.get(j);
                arr.remove(j);
                arr.add(indices.get(r.getProjectHash()), r);
            }
        }
        cache.put(type.id, arr);
    }
}
