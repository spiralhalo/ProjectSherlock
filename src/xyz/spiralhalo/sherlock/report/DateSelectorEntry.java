package xyz.spiralhalo.sherlock.report;

import xyz.spiralhalo.sherlock.util.FormatUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateSelectorEntry {
    public final LocalDate date;
    private final DateTimeFormatter formatter;

    public DateSelectorEntry(LocalDate date) {
        this(date, FormatUtil.DTF_DATE_SELECTOR);
    }

    public DateSelectorEntry(LocalDate date, DateTimeFormatter formatter) {
        this.date = date;
        this.formatter = formatter;
    }

    @Override
    public String toString() {
        return formatter.format(date);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof DateSelectorEntry){
            return ((DateSelectorEntry)obj).date.isEqual(date);
        }
        return false;
    }
}
