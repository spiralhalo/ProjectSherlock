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

package xyz.spiralhalo.sherlock.persist.settings;

import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserNode.*;

public class UserConfig {

    public enum UserNode {
        GENERAL("TRACKING"),
        VIEW("VIEW"),
        NOTIFICATIONS("NOTIFICATIONS");
        private String value;
        UserNode(String value){this.value=value;}
    }

    public enum UserStr {
        BREAK_MESSAGE(NOTIFICATIONS, "Remember to stay hydrated!");
        final UserNode node; public final String def;
        UserStr(UserNode node, String def){ this.node = node; this.def = def; }
        public String get(){
            return IniHandler.getInstance().get(node.value, name(), def);
        }
        public void set(String value){
            IniHandler.getInstance().put(node.value, name(), value);
        }
    }

    public enum UserInt {
        DAILY_TARGET_SECOND(GENERAL, 6 * 3600),
        AFK_TIMEOUT_SECOND(GENERAL, 5 * 60),
        WEEKLY_TARGET_DAYS(GENERAL, 5),
        DOUBLE_CLICK_ACTION(GENERAL),
        BREAK_MAX_WORKDUR(NOTIFICATIONS, 2 * 3600),
        BREAK_MIN_BREAKDUR(NOTIFICATIONS, 30 * 60);
        final UserNode node; public final int def;
        UserInt(UserNode node){
            this(node, 0);
        }
        UserInt(UserNode node, int def){ this.node = node; this.def = def; }
        public int get(){
            return IniHandler.getInstance().getInt(node.value, name(), def);
        }
        public int get(int min, int max, boolean useDef) {
            final int x = IniHandler.getInstance().getInt(node.value, name(), def);
            if (x < min) {
                if (useDef) return def;
                return min;
            } else if (x > max) {
                if (useDef) return def;
                return max;
            }
            return x;
        }
        public void set(int value){
            IniHandler.getInstance().putInt(node.value, name(), value);
        }
    }

    public enum UserBool {
        USE_RANK_MONTH_CHART(VIEW),
        LIMIT_MONTH_CHART_UPPER(VIEW, true),
        OLD_RATING(VIEW),
        DISABLE_MONTH_LINE(VIEW),
        ENABLE_YEAR_LINE(VIEW),
        EXCEED_100_PERCENT(VIEW),
        BREAK_REMINDER(NOTIFICATIONS),
        BREAK_ANY_USAGE(NOTIFICATIONS);
        final UserNode node; public final boolean def;
        UserBool(UserNode node){
            this(node, false);
        }
        UserBool(UserNode node, boolean def){ this.node = node; this.def = def; }
        public boolean get(){
            return IniHandler.getInstance().getBoolean(node.value, name(), def);
        }
        public void set(boolean value){
            IniHandler.getInstance().putBoolean(node.value, name(), value);
        }
    }

    //work day (legacy)
    private static final String KEY_WORK_DAY = "WORK_DAY_";
    private static final String[] DAY_NAMES = new String[]{
            "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN" };
    private static final boolean[] DEFAULT_WORK_DAYS = new boolean[]{
            false, true, true, true, true, true, false, false };

    public static boolean userDWDay(int day){
        return DEFAULT_WORK_DAYS[day];
    }

    public static boolean userGWDay(int day) {
        if(day<0 || day>7) return false;
        return IniHandler.getInstance().getBoolean(GENERAL.value, String.format("%s%s", KEY_WORK_DAY,DAY_NAMES[day]),
                userDWDay(day));
    }

    public static void userSWDay(int day, boolean isWorkDay){
        IniHandler.getInstance().putBoolean(GENERAL.value, String.format("%s%s", KEY_WORK_DAY,DAY_NAMES[day]), isWorkDay);
    }
}
