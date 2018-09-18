package xyz.spiralhalo.sherlock.report.factory.charts;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.Temporal;

public enum ChartType {
    HOUR_IN_DAY, DAY_IN_MONTH, MONTH_IN_YEAR;
    private static int SinM = 60;
    private static int SinH = 3_600;
    private static int SinD = 86_400;
    public int numSPerUnit(Temporal y, LocalDateTime ldt){
        switch (this){
            case HOUR_IN_DAY: return SinH;
            case DAY_IN_MONTH: return SinD;
            case MONTH_IN_YEAR:
                if(!(y instanceof Year)){
                    throw new IllegalArgumentException("Wrong temporal type.");
                }
                return ((Year) y).atMonth(ldt.getMonth()).lengthOfMonth() * SinD;
        }
        return 0;
    }

    public int numUnits(Temporal y){
        switch (this){
            case HOUR_IN_DAY: return 24;
            case DAY_IN_MONTH:
                if(!(y instanceof YearMonth)){
                    throw new IllegalArgumentException("Wrong temporal type.");
                }
                return ((YearMonth) y).lengthOfMonth();
            case MONTH_IN_YEAR:
                if(!(y instanceof Year)){
                    throw new IllegalArgumentException("Wrong temporal type.");
                }
                return ((Year) y).length();
        }
        return 0;
    }

    public int pointInUnit(LocalDateTime ldt){
        switch (this){
            case HOUR_IN_DAY: return ldt.getMinute()*SinM + ldt.getSecond();
            case DAY_IN_MONTH: return ldt.getHour()*SinH + ldt.getMinute()*SinM + ldt.getSecond();
            case MONTH_IN_YEAR: return (ldt.getDayOfMonth()-1)*SinD + ldt.getHour()*SinH + ldt.getMinute()*SinM + ldt.getSecond();
        }
        return 0;
    }

    public int unit(LocalDateTime ldt) {
        switch (this){
            case HOUR_IN_DAY: return ldt.getHour();
            case DAY_IN_MONTH: return ldt.getDayOfMonth()-1;
            case MONTH_IN_YEAR: return ldt.getMonthValue()-1;
        }
        return -1;
    }

    public float subunitNormalizer() {
        switch (this){
            case HOUR_IN_DAY: return SinM;
            case DAY_IN_MONTH: return SinH;
            case MONTH_IN_YEAR: return SinD;
        }
        return 1f;
    }
}
