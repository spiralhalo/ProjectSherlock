package xyz.spiralhalo.sherlock.report.factory.charts;

import org.jfree.data.category.DefaultCategoryDataset;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.record.RecordEntry;
import xyz.spiralhalo.sherlock.report.factory.summary.SummaryEntry;
import xyz.spiralhalo.sherlock.util.ColorUtil;

import java.awt.*;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.Locale;

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
    private final ZoneId z;

    public ChartBuilder(T date, ZoneId z, boolean inclTotal) {
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
        this.z = z;
    }

    public void readRecord(RecordEntry entry){
        LocalDateTime ldt = entry.getTime().atZone(z).toLocalDateTime();
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

    public void readSummary(SummaryEntry entry){
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

    public ChartData finish(ProjectList projectList){
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        final ChartMeta meta = new ChartMeta();
        final Locale locale = Locale.getDefault();
        if(inclTotal) meta.put(TOTAL, ColorUtil.white);
        meta.put(OTHER, ColorUtil.CONST_OTHER_GRAY);
        meta.put(DELETED, ColorUtil.CONST_DELETED_RED_GRAY);
        final int[] deleted = new int[type.numUnits(date)];
        final int[] other = new int[type.numUnits(date)];
        final HashMap<Long,Integer>[] nonProds = new HashMap[type.numUnits(date)];
        for (int i = 0; i < type.numUnits(date); i++) {
            int total = 0;
            FlexibleLocale unitLabel = type.unitLabel(date, i);
            dataset.addValue((Number)0,"",unitLabel);
            if (units[i] != null) {
                for (Long l : units[i].keySet()) {
                    Project p = projectList.findByHash(l);

                    boolean productive;
                    boolean recreational;
                    int s = units[i].get(l);
                    if (p != null) {
                        productive = p.isProductive();
                        recreational = p.isRecreational();
                        if(productive) {
                            meta.putIfAbsent(p.getName(), new Color(p.getColor()));
                            dataset.setValue((Number) (s / type.subunitNormalizer()), p.getName(), unitLabel);
                        } else {
                            if (recreational) {
                                meta.putIfAbsent(p.getName(), new StripedPaint(new Color(p.getColor()), ColorUtil.CONST_BREAK_GREEN));
                            } else {
                                meta.putIfAbsent(p.getName(), new StripedPaint(new Color(p.getColor()), ColorUtil.CONST_OTHER_GRAY));
                            }
                            if(nonProds[i] == null){
                                nonProds[i] = new HashMap<>();
                            }
                            nonProds[i].put(p.getHash(), s + nonProds[i].getOrDefault(p.getHash(), 0));
                        }
                    } else if (l == -1 || !productiveMap.getOrDefault(l, false)) {
                        recreational = false;
                        productive = false;
                        other[i] += s;
                    } else {
                        recreational = false;
                        productive = productiveMap.getOrDefault(l, false);
                        deleted[i] += s;
                    }

                    meta.addWorkDur(productive ? s : 0);
                    meta.addBreakDur(recreational ? s : 0);
                    meta.addLogDur(s);
                    if (inclTotal) total += s;
                }
            }
            if(inclTotal) dataset.addValue((Number) (total / type.subunitNormalizer()), TOTAL, unitLabel);
        }
        for (int i = 0; i < type.numUnits(date); i++) {
            FlexibleLocale unitLabel = type.unitLabel(date, i);
            if(nonProds[i] != null) {
                for (Long l : nonProds[i].keySet()) {
                    Project p = projectList.findByHash(l);
                    dataset.setValue((Number) (nonProds[i].get(l) / type.subunitNormalizer()), p.getName(), unitLabel);
                }
            }
        }
        for (int i = 0; i < type.numUnits(date); i++) {
            FlexibleLocale unitLabel = type.unitLabel(date, i);
            if(other[i] > 0) dataset.setValue((Number) (other[i] / type.subunitNormalizer()), OTHER, unitLabel);
            if(deleted[i] > 0) dataset.setValue((Number) (deleted[i] / type.subunitNormalizer()), DELETED, unitLabel);
        }
        return new ChartData(meta, dataset);
    }
}
