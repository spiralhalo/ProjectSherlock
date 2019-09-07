package xyz.spiralhalo.sherlock.persist.settings;

import static xyz.spiralhalo.sherlock.persist.settings.Nodes.*;

public class UserConfig {
    private static final String KEY_WORK_DAY = "WORK_DAY_";

    private static final String[] DAY_NAMES = new String[]{
            "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN" };
    private static final boolean[] DEFAULT_WORK_DAYS = new boolean[]{
            false, true, true, true, true, true, false, false };

    public enum UserInt {
        DAILY_TARGET_SECOND,
        AFK_TIMEOUT_SECOND,
        WEEKLY_TARGET_DAYS,
        OLD_RATING,
        DISABLE_MONTH_LINE,
        ENABLE_YEAR_LINE,
        EXCEED_100_PERCENT
    }

    public enum UserNode {
        GENERAL(NODE_TRACKING.v),
        VIEW(NODE_VIEW.v);

        private String value;
        UserNode(String value){this.value=value;}
    }

    public static boolean userDWDay(int day){
        return DEFAULT_WORK_DAYS[day];
    }

    public static boolean userGWDay(int day) {
        if(day<0 || day>7) return false;
        return IniHandler.getInstance().getBoolean(NODE_TRACKING.v, String.format("%s%s", KEY_WORK_DAY,DAY_NAMES[day]),
                userDWDay(day));
    }

    public static void userSWDay(int day, boolean isWorkDay){
        IniHandler.getInstance().putBoolean(NODE_TRACKING.v, String.format("%s%s", KEY_WORK_DAY,DAY_NAMES[day]), isWorkDay);
    }

    public static int userDInt(UserNode node, UserInt key){
        switch (node){
            case GENERAL:
                switch (key){
                    case DAILY_TARGET_SECOND: return 6 * 3600;
                    case AFK_TIMEOUT_SECOND: return 5 * 60;
                    case WEEKLY_TARGET_DAYS: return 5;
                }
            default: return 0;
        }
    }

    public static boolean userDBool(UserNode node, UserInt key){
        return false;
    }

    public static int userGInt(UserNode node, UserInt key){
        return IniHandler.getInstance().getInt(node.value, key.name(), userDInt(node, key));
    }

    public static int userGInt(UserNode node, UserInt key, int min, int max, boolean useDef){
        final int x = IniHandler.getInstance().getInt(node.value, key.name(), userDInt(node, key));
        if (x < min) {
            if (useDef) return userDInt(node, key);
            return min;
        } else if (x > max){
            if (useDef) return userDInt(node, key);
            return max;
        }
        return x;
    }

    public static void userSInt(UserNode node, UserInt key, int value){
        IniHandler.getInstance().putInt(node.value, key.name(), value);
    }

    public static boolean userGBool(UserNode node, UserInt key){
        return IniHandler.getInstance().getBoolean(node.value, key.name(), userDBool(node, key));
    }

    public static void userSBool(UserNode node, UserInt key, boolean value){
        IniHandler.getInstance().putBoolean(node.value, key.name(), value);
    }
}
