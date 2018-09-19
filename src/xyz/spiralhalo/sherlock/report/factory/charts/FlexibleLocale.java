package xyz.spiralhalo.sherlock.report.factory.charts;

import xyz.spiralhalo.sherlock.util.FormatUtil;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

public class FlexibleLocale implements Comparable, Serializable {
    public static final long serialVersionUID = 1L;
    private final Comparable toCompare;

    public FlexibleLocale(Comparable toCompare) {
        this.toCompare = toCompare;
    }

    @Override
    public int compareTo(Object o) {
        return toCompare.compareTo(o);
    }

    @Override
    public boolean equals(Object obj) {
        return hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return (toCompare.hashCode() << 1) | 1;
    }

    @Override
    public String toString() {
        if(toCompare instanceof LocalDate){
            return FormatUtil.DTF_MONTH_CHART.format((LocalDate) toCompare);
        } else if(toCompare instanceof YearMonth){
            return ((YearMonth) toCompare).getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());
        }
        return String.valueOf(toCompare);
    }
}
