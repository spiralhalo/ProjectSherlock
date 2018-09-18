package xyz.spiralhalo.sherlock.report.factory.summary;

import xyz.spiralhalo.sherlock.record.RecordEntry;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.HashMap;

public class SummaryBuilder {

    public static class SummaryResult {
        public MonthDetails details;
        public MonthSummary summary;

        public SummaryResult(MonthDetails details, MonthSummary summary) {
            this.details = details;
            this.summary = summary;
        }
    }

    private static class DayMap extends HashMap<Long,Integer>{}
    private static class DayEarliestMap extends HashMap<Long, LocalDateTime>{}

    private final YearMonth month;
    private final DayMap[] days;
    private final DayEarliestMap[] dayEarliestMaps;
    private final HashMap<Long,Boolean> productiveMap;
    private final ZoneId z;

    public SummaryBuilder(YearMonth month, ZoneId z) {
        this.month = month;
        this.days = new DayMap[month.lengthOfMonth()];
        this.dayEarliestMaps = new DayEarliestMap[month.lengthOfMonth()];
        this.productiveMap = new HashMap<>();
        this.z = z;
    }

    public void readEntry(RecordEntry entry){
        LocalDateTime ldt = entry.getTime().atZone(z).toLocalDateTime();
        if(!YearMonth.from(ldt).equals(month)) return;
        int day = ldt.getDayOfMonth()-1;
        DayMap map = getDayMap(day);
        map.put(entry.getHash(), map.getOrDefault(entry.getHash(), 0)+entry.getElapsed());
        productiveMap.putIfAbsent(entry.getHash(), entry.isProductive());
        getDayEarliestMap(day).putIfAbsent(entry.getHash(), ldt);
    }

    private DayMap getDayMap(int day){
        if(days[day] == null){
            days[day] = new DayMap();
        }
        return days[day];
    }

    private DayEarliestMap getDayEarliestMap(int day){
        if(dayEarliestMaps[day] == null){
            dayEarliestMaps[day] = new DayEarliestMap();
        }
        return dayEarliestMaps[day];
    }

    public SummaryResult finish(){
        final MonthSummary summary = new MonthSummary(month);
        final MonthDetails details = new MonthDetails(month);
        final HashMap<Long, Integer> monthTotal = new HashMap<>();
        final HashMap<Long, LocalDateTime> monthEarliest = new HashMap<>();
        for (int i = 0; i < month.lengthOfMonth(); i++) {
            for (Long l : days[i].keySet()) {
                monthTotal.put(l, monthTotal.getOrDefault(l, 0)+days[i].get(l));
                details.add(new DetailsRow(month.atDay(i+1), new SummaryRow(l, days[i].get(l), productiveMap.get(l), dayEarliestMaps[i].get(l))));
                monthEarliest.putIfAbsent(l, dayEarliestMaps[i].get(l));
            }
            for (Long l : monthTotal.keySet()){
                summary.add(new SummaryRow(l, monthTotal.get(l), productiveMap.get(l), monthEarliest.get(l)));
            }
        }
        return new SummaryResult(details, summary);
    }
}
