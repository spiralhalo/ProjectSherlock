//
//    Copyright 2020 spiralhalo <re.nanashi95@gmail.com>
//
//    This file is part of Project Sherlock.
//
//    Project Sherlock is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Project Sherlock is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Project Sherlock.  If not, see <https://www.gnu.org/licenses/>.
//

package xyz.spiralhalo.sherlock.report.ops;

import xyz.spiralhalo.sherlock.persist.cache.CacheMgr;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.report.factory.table.AllReportRow;
import xyz.spiralhalo.sherlock.report.factory.table.AllReportRows;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;

public class OverviewOps {
    public enum Type{
        ACTIVE, FINISHED, UTILITY;
        public static Type index(int i){switch(i){case 0:return ACTIVE;case 1:return FINISHED;case 2:return UTILITY;default:return null;}}
    }

    public static void refreshOrdering(CacheMgr cache, ProjectList projectList, Type type, ZoneId z){
        List<? extends Project> projects;
        String id;
        if(type==Type.ACTIVE){
            projects = projectList.getActiveProjects();
            id = AllReportRows.activeCacheId(z);
        } else if(type==Type.FINISHED){
            projects = projectList.getFinishedProjects();
            id = AllReportRows.finishedCacheId(z);
        } else if(type==Type.UTILITY){
            projects = projectList.getUtilityTags();
            id = AllReportRows.utilityCacheId(z);
        } else {
            throw new UnsupportedOperationException("Unknown type.");
        }
        AllReportRows arr = cache.getObj(id, AllReportRows.class);
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
        cache.put(id, arr);
    }
}
