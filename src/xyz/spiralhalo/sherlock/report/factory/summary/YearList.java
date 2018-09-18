package xyz.spiralhalo.sherlock.report.factory.summary;

import xyz.spiralhalo.sherlock.persist.cache.Cache;
import xyz.spiralhalo.sherlock.util.MurmurHash;

import java.io.Serializable;
import java.time.Year;
import java.time.ZoneId;
import java.util.ArrayList;

@Cache
public class YearList extends ArrayList<Year> implements Serializable {
    public static final long serialVersionUID = 1L;
    public static String cacheId(ZoneId z) {
        return String.format("record_index_%s", Integer.toHexString(MurmurHash.hash32(z.getId())));
    }
}
