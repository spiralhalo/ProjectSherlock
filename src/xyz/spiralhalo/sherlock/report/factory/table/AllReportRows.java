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
