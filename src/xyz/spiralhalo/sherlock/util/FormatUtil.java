package xyz.spiralhalo.sherlock.util;

import xyz.spiralhalo.sherlock.persist.settings.AppConfig;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class FormatUtil {
    public static final DateTimeFormatter DTF_FULL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter DTF_LONG_DATE = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
    public static final DateTimeFormatter DTF_DATE_SELECTOR = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
    public static final DateTimeFormatter DTF_YMD = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter DTF_YM = DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter DTF_HMA = DateTimeFormatter.ofPattern("hh:mm a").withZone(ZoneId.systemDefault());

    public static String hms(int seconds) {
        switch (AppConfig.getHMSMode()){
            case COLON:
                return hmsColon(seconds);
            case STRICT:
            default:
                return hmsStrict(seconds);
        }
    }

    public static String hmsColon(int seconds) {
        int s = seconds % 60;
        int m = (seconds / 60) % 60;
        int h = seconds / 3600;
        return String.format("%02d:%02d:%02d",h,m,s);
    }

    public static String hmsStrict(int seconds) {
        int s = seconds % 60;
        int m = (seconds / 60) % 60;
        int h = seconds / 3600;
        return (h > 0 ? h + "h " : "") + (m > 0 ? m + "m " : "") + (s > 0 ? s + "s" : (seconds > 0 ? "" : "0s"));
    }

    public static String hmsLong(int seconds) {
        int s = seconds % 60;
        int m = (seconds / 60) % 60;
        int h = seconds / 3600;
        return (h > 0 ? h + ( h > 1 ? " hours " : " hour ") : "")
                + (m > 0 ? m + ( m > 1 ? " minutes " : " minute ") : "")
                + (s > 0 ? s + ( s > 1 ? " seconds" : " second") : (seconds > 0 ? "" : "0 second"));
    }
}
