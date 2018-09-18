package xyz.spiralhalo.sherlock.report.factory.summary;

import java.io.Serializable;
import java.time.LocalDate;

public class DetailsRow implements Serializable {
    public static final long serialVersionUID = 1L;
    private final LocalDate date;
    private final SummaryRow summary;

    public DetailsRow(LocalDate date, SummaryRow summary) {
        this.date = date;
        this.summary = summary;
    }

    public LocalDate getDate() {
        return date;
    }

    public SummaryRow getSummary() {
        return summary;
    }
}
