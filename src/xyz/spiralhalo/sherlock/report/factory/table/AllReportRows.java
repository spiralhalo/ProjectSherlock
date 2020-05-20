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

package xyz.spiralhalo.sherlock.report.factory.table;

import xyz.spiralhalo.sherlock.persist.cache.Cache;
import xyz.spiralhalo.sherlock.util.MurmurHash;

import java.io.Serializable;
import java.time.ZoneId;
import java.util.ArrayList;

@Cache
public class AllReportRows extends ArrayList<AllReportRow> implements Serializable {
    public static String activeCacheId(ZoneId z){
        return String.format("active_table_%s", Integer.toHexString(MurmurHash.hash32(z.getId())));
    }
    public static String finishedCacheId(ZoneId z){
        return String.format("finished_table_%s", Integer.toHexString(MurmurHash.hash32(z.getId())));
    }
    public static String utilityCacheId(ZoneId z){
        return String.format("utility_table_%s", Integer.toHexString(MurmurHash.hash32(z.getId())));
    }
    public static final long serialVersionUID = 1L;
}
