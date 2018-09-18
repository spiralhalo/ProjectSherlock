package xyz.spiralhalo.sherlock.report.factory.charts;

import org.jfree.data.category.DefaultCategoryDataset;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.record.RecordEntry;
import xyz.spiralhalo.sherlock.report.factory.statistics.SummaryRow;
import xyz.spiralhalo.sherlock.util.ColorUtil;

import java.awt.*;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.HashMap;

public class ChartBuilder<T extends Temporal> {
    private static final String TOTAL = "Total";
    private static final String OTHER = "Other";
    private static final String DELETED = "(Deleted)";

    private static class UnitMap extends HashMap<Long,Integer>{}

    private final T date;
    private final UnitMap[] units;
    private final HashMap<Long,Boolean> productiveMap;
    private final ChartType type;
    private final boolean inclTotal;

    public ChartBuilder(T date, boolean inclTotal) {
        if(date instanceof LocalDate){
            type = ChartType.HOUR_IN_DAY;
        } else if(date instanceof YearMonth){
            type = ChartType.DAY_IN_MONTH;
        } else if(date instanceof Year){
            type = ChartType.MONTH_IN_YEAR;
        } else {
            throw new IllegalArgumentException("Unsupported temporal unit");
        }
        this.date = date;
        this.productiveMap = new HashMap<>();
        this.units = new UnitMap[type.numUnits(date)];
        this.inclTotal = inclTotal;
    }

    public void readEntry(RecordEntry entry){
        LocalDateTime ldt = entry.getTime().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int unit = type.unit(ldt);
        int remainingS = type.numSPerUnit(date, ldt) - type.pointInUnit(ldt);
        int seconds = entry.getElapsed();
        while (seconds > 0 && unit < type.numUnits(date)) {
            int sWithinUnit = Math.min(remainingS, seconds);
            addToUnit(unit, entry.getHash(), sWithinUnit);
            seconds -= sWithinUnit;
            unit += 1;
            remainingS = type.numSPerUnit(date, ldt);
        }
        productiveMap.putIfAbsent(entry.getHash(), entry.isProductive());
    }

    public void readSummary(SummaryRow entry){
        if(date instanceof LocalDate) throw new UnsupportedOperationException("Unsupported temporal unit");
        LocalDateTime ldt = entry.getEarliest();
        int unit = type.unit(ldt);
        int remainingS = type.numSPerUnit(date, ldt) - type.pointInUnit(ldt);
        int seconds = entry.getSeconds();
        while (seconds > 0 && unit < type.numUnits(date)) {
            int sWithinUnit = Math.min(remainingS, seconds);
            addToUnit(unit, entry.getHash(), sWithinUnit);
            seconds -= sWithinUnit;
            unit += 1;
            remainingS = type.numSPerUnit(date, ldt);
        }
        productiveMap.putIfAbsent(entry.getHash(), entry.isProductive());
    }

    private void addToUnit(int unit, long hash, int seconds){
        if (unit < 0 || unit >= type.numUnits(date)) throw new IllegalArgumentException("Unit out of range.");
        getUnitMap(unit).put(hash, getUnitMap(unit).getOrDefault(hash, 0) + seconds);
    }

    private UnitMap getUnitMap(int unit){
        if (unit < 0 || unit >= type.numUnits(date)) throw new IllegalArgumentException("Unit out of range.");
        if(units[unit] == null){
            units[unit] = new UnitMap();
        }
        return units[unit];
    }

    public  ChartData finish(ProjectList projectList){
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        final ChartMeta meta = new ChartMeta();
        if(inclTotal) meta.put(TOTAL, ColorUtil.white);
        meta.put(OTHER, ColorUtil.gray);
        meta.put(DELETED, ColorUtil.med_red_gray);
        for (int i = 0; i < type.numUnits(date); i++) {
            int total = 0;
            if(units[i]==null) {
                dataset.addValue((Number)0,"Other",i);
            } else {
                for (Long l : units[i].keySet()) {
                    Project p = projectList.findByHash(l);

                    String label;
                    if (p != null) {
                        meta.putIfAbsent(p.getName(), new Color(p.getColor()));
                        label = p.getName();
                    } else if (l == -1 || !productiveMap.getOrDefault(l, false)) {
                        label = OTHER;
                    } else {
                        label = DELETED;
                    }
                    int s = units[i].get(l);

                    dataset.addValue((Number) (s / type.subunitNormalizer()), label, i);
                    meta.addWorkDur(productiveMap.getOrDefault(l, false) ? s : 0);
                    meta.addLogDur(s);
                    if (inclTotal) total += s;
                }
            }
            if(inclTotal) dataset.addValue((Number) (total / type.subunitNormalizer()), TOTAL, i);
        }
        return new ChartData(meta, dataset);
    }
}
