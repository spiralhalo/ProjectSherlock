package xyz.spiralhalo.sherlock.report;

import xyz.spiralhalo.sherlock.util.FormatUtil;

import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;

public class DateSelection<T extends Temporal> {
    public final T date;
    private final DateTimeFormatter formatter;

    public DateSelection(T date) {
        this(date, FormatUtil.DTF_DATE_SELECTOR);
    }

    public DateSelection(T date, DateTimeFormatter formatter) {
        this.date = date;
        this.formatter = formatter;
    }

    @Override
    public String toString() {
        return formatter.format(date);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof DateSelection){
            return ((DateSelection)obj).date.equals(date);
        }
        return false;
    }
}
