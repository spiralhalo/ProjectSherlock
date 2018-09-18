package xyz.spiralhalo.sherlock.record.io;

import xyz.spiralhalo.sherlock.persist.cache.Cache;
import xyz.spiralhalo.sherlock.util.MurmurHash;

import java.io.Serializable;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.HashMap;

@Cache
public class MonthIndex extends HashMap<YearMonth,Long> implements Serializable {
    public static final long serialVersionUID = 1L;
    public static String cacheId(ZoneId z) {
        return String.format("record_index_%s", Integer.toHexString(MurmurHash.hash32(z.getId())));
    }
}
