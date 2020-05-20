//
//    Copyright 2020 spiralhalo <re.nanashi95@gmail.com>
//
//    This file is part of Project Sherlock.
//
//    Project Sherlock is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Project Sherlock is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Project Sherlock.  If not, see <https://www.gnu.org/licenses/>.
//

package xyz.spiralhalo.sherlock.util;

import xyz.spiralhalo.sherlock.persist.settings.AppConfig;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class FormatUtil {
    public static final DateTimeFormatter DTF_FULL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter DTF_LONG_DATE = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
    public static final DateTimeFormatter DTF_DATE_SELECTOR = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
    public static final DateTimeFormatter DTF_MONTH_CHART = DateTimeFormatter.ofPattern("EEE dd");
    public static final DateTimeFormatter DTF_MONTH_SELECTOR = DateTimeFormatter.ofPattern("MMMM yyyy");
    public static final DateTimeFormatter DTF_YEAR = DateTimeFormatter.ofPattern("yyyy");
    public static final DateTimeFormatter DTF_YMD = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter DTF_YM = DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter DTF_HMA = DateTimeFormatter.ofPattern("hh:mm a").withZone(ZoneId.systemDefault());

    public static String hms(int seconds) {
        switch (AppConfig.appHMS()){
            case COLON:
                return hmsHomoColon(seconds);
            case STRICT:
            default:
                return hmsStrict(seconds);
        }
    }

    public static String hmsHomoColon(int seconds) {
        int s = seconds % 60;
        int m = (seconds / 60) % 60;
        int h = seconds / 3600;
        return String.format("%02d:%02d %02d",h,m,s);
    }

    public static String hmsMachineColon(int seconds) {
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

    public static String vagueTimeAgo(long epochMillis) {
        ZonedDateTime time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
        ZonedDateTime now = ZonedDateTime.now();
        if(now.toEpochSecond() - time.toEpochSecond() < 60){
            return "just now";
        }
        //get num of months since 0 BC
        int kindaEpochMonthTime = time.getMonthValue() + time.getYear() * 12;
        int kindaEpochMonthNow = now.getMonthValue() + now.getYear() * 12;
        int monthDiff = kindaEpochMonthNow - kindaEpochMonthTime;
        if (monthDiff > 1){
            int monthAgo = monthDiff % 12;
            int yearAgo = monthDiff / 12;
            return String.format("%s%sago", (yearAgo > 0 ? (yearAgo == 1 ? "1 year ":yearAgo+" years ") : ""),
                    (monthAgo > 0 ? (monthAgo == 1 ? "1 month ":monthAgo+" months ") : ""));
        } else {
            int dayDiff = now.getDayOfYear() - time.getDayOfYear();
            if (dayDiff > 1){
                return dayDiff + " days ago";
            } else {
                return hmsStrict((int)(now.toEpochSecond()-time.toEpochSecond())) + " ago";
            }
        }
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
