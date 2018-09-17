package xyz.spiralhalo.sherlock.record.io;

import xyz.spiralhalo.sherlock.persist.cache.Cache;

import java.io.Serializable;
import java.time.YearMonth;
import java.util.HashMap;

@Cache
public class MonthIndex extends HashMap<YearMonth,Long> implements Serializable {
    public static final long serialVersionUID = 1L;
}
