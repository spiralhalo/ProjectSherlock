package xyz.spiralhalo.sherlock.report.factory.charts;

import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

public enum ChartType {
    HOUR_IN_DAY, DAY_IN_MONTH, MONTH_IN_YEAR;
    private static int SinM = 60;
    private static int SinH = 3_600;
    private static int SinD = 86_400;
    public int numSPerUnit(Temporal y, ZonedDateTime zdt){
        switch (this){
            case HOUR_IN_DAY: return SinH;
            case DAY_IN_MONTH: return SinD;
            case MONTH_IN_YEAR:
                if(!(y instanceof Year)){
                    throw new IllegalArgumentException("Wrong temporal type.");
                }
                return ((Year) y).atMonth(zdt.getMonth()).lengthOfMonth() * SinD;
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

    public int pointInUnit(ZonedDateTime zdt){
        switch (this){
            case HOUR_IN_DAY: return zdt.getMinute()*SinM + zdt.getSecond();
            case DAY_IN_MONTH: return zdt.getHour()*SinH + zdt.getMinute()*SinM + zdt.getSecond();
            case MONTH_IN_YEAR: return (zdt.getDayOfMonth()-1)*SinD + zdt.getHour()*SinH + zdt.getMinute()*SinM + zdt.getSecond();
        }
        return 0;
    }

    public int unit(ZonedDateTime zdt) {
        switch (this){
            case HOUR_IN_DAY: return zdt.getHour();
            case DAY_IN_MONTH: return zdt.getDayOfMonth()-1;
            case MONTH_IN_YEAR: return zdt.getMonthValue()-1;
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
