package xyz.spiralhalo.sherlock.report.factory.summary;

import xyz.spiralhalo.sherlock.persist.cache.Cache;

import java.io.Serializable;
import java.time.Year;
import java.util.ArrayList;

@Cache
public class YearList extends ArrayList<Year> implements Serializable {
    public static final long serialVersionUID = 1L;
    public static final String CACHE_ID = "year_list";
}
