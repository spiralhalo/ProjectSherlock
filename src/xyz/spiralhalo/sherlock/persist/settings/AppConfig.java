package xyz.spiralhalo.sherlock.persist.settings;

public class AppConfig {
    private static final String NODE = Nodes.NODE_APPLICATION.v;

    private static final String KEY_HMS_MODE = "HMS_MODE";
    private static final String KEY_THEME = "THEME";
    private static final String KEY_SOCKET_PORT = "SOCKET_PORT";
    private static final String KEY_PREFERRED_WINDOW_WIDTH = "PREFERRED_WINDOW_WIDTH";
    private static final String KEY_PREFERRED_WINDOW_HEIGHT = "PREFERRED_WINDOW_HEIGHT";
    private static final String KEY_WINDOW_LAST_MAXIMIZED = "WINDOW_LAST_MAXIMIZED";
    private static final String KEY_WINDOW_LAST_LOCATION_X = "WINDOW_LAST_LOCATION_X";
    private static final String KEY_WINDOW_LAST_LOCATION_Y = "WINDOW_LAST_LOCATION_Y";
    private static final String KEY_TAB_MAIN_SELECTION = "TAB_MAIN_SELECTION";
    private static final String KEY_TABR_SELECTION = "TABR_SELECTION";
    private static final String KEY_TABS_SELECTION = "TABS_SELECTION";
    private static final String KEY_LAST_CATEGORY = "LAST_CATEGORY";
    private static final String KEY_FILTER_CATEGORY = "FILTER_CATEGORY";
    private static final String KEY_FILTER_TYPE = "FILTER_TYPE";
    private static final String KEY_THUMB_SORT = "THUMB_SORT";

    public enum HMSMode{
        COLON("12:45 30"),
        STRICT("12h 45m 30s");
        public String text;
        HMSMode(String text){this.text=text;}
    }

    public enum Theme{
        BUSINESS(0, 0, 0xe5eaef, false,"Substance Business"),
        BUSINESS_BLUE(1, 0x406079, 0xf1f6fa, false, "Substance Business Blue Steel"),
        BUSINESS_BLACK(2, 0, 0xf1f6fa, false, "Substance Business Black Steel"),
        GRAPHITE(3, 0xffffff, 0x4d4d4d, true, "Substance Graphite"),
        TWILIGHT(4, 0xcccc99, 0x4C4A41, true, "Substance Twilight"),
        RAVEN(5, 0xffffff, 0x3A3A3A, true, "Substance Raven"),
        //        MIST_SILVER(3, 0, 0xebf0f4, false, "Substance Mist Silver"),
                SYSTEM(6, 0, 0xF0F0F0, false, "System Default Theme"),
        ;
        public int x;
        public int foreground;
        public int background;
        public boolean dark;
        public String label;
        Theme(int x, int foreground, int background, boolean dark, String label) {
            this.x = x;
            this.foreground = foreground;
            this.background = background;
            this.dark = dark;
            this.label = String.format("%s%s",label, (dark?" (dark)":""));
        }
    }

    public enum AppBool {
        ASK_BEFORE_QUIT,
        MINIMIZE_TO_TRAY,
        RUN_ON_STARTUP,
        RUN_MINIMIZED
    }

    public enum AppInt {
        REFRESH_TIMEOUT
    }

    public static HMSMode defaultHMSMode() {return HMSMode.STRICT;}

    public static int getPreferredWindowWidth() {
        return IniHandler.getInstance().getInt(NODE, KEY_PREFERRED_WINDOW_WIDTH, 0);}
    public static void setPreferredWindowWidth(int width) {
        IniHandler.getInstance().putInt(NODE, KEY_PREFERRED_WINDOW_WIDTH, width);}
    public static int getPreferredWindowHeight() {
        return IniHandler.getInstance().getInt(NODE, KEY_PREFERRED_WINDOW_HEIGHT, 0);}
    public static void setPreferredWindowHeight(int height) {
        IniHandler.getInstance().putInt(NODE, KEY_PREFERRED_WINDOW_HEIGHT, height);}
    public static int getWindowLastLocationX() {
        return IniHandler.getInstance().getInt(NODE, KEY_WINDOW_LAST_LOCATION_X, -1);}
    public static void setWindowLastLocationX(int x) {
        IniHandler.getInstance().putInt(NODE, KEY_WINDOW_LAST_LOCATION_X, x);}
    public static int getWindowLastLocationY() {
        return IniHandler.getInstance().getInt(NODE, KEY_WINDOW_LAST_LOCATION_Y, -1);}
    public static void setWindowLastLocationY(int y) {
        IniHandler.getInstance().putInt(NODE, KEY_WINDOW_LAST_LOCATION_Y, y);}
    public static boolean getWindowLastMaximized() {
        return IniHandler.getInstance().getBoolean(NODE, KEY_WINDOW_LAST_MAXIMIZED, false);}
    public static void setWindowLastMaximized(boolean maximized) {
        IniHandler.getInstance().putBoolean(NODE, KEY_WINDOW_LAST_MAXIMIZED, maximized);}

    public static void setTabMainSelection(int i) { IniHandler.getInstance().putInt(NODE, KEY_TAB_MAIN_SELECTION, i); }
    public static void setTabReportsSelection(int i) { IniHandler.getInstance().putInt(NODE, KEY_TABR_SELECTION, i); }
    public static void setTabProjectsSelection(int i) { IniHandler.getInstance().putInt(NODE, KEY_TABS_SELECTION, i); }
    public static int getTabMainSelection() { return IniHandler.getInstance().getInt(NODE, KEY_TAB_MAIN_SELECTION, 0); }
    public static int getTabReportsSelection() { return IniHandler.getInstance().getInt(NODE, KEY_TABR_SELECTION, 0); }
    public static int getTabProjectsSelection() { return IniHandler.getInstance().getInt(NODE, KEY_TABS_SELECTION, 0); }

    public static void setLastCategory(String category) { IniHandler.getInstance().put(NODE, KEY_LAST_CATEGORY, category);}
    public static String getLastCategory() { return IniHandler.getInstance().get(NODE, KEY_LAST_CATEGORY, null);}

    public static void setFilterCategory(String category) { IniHandler.getInstance().put(NODE, KEY_FILTER_CATEGORY, category);}
    public static String getFilterCategory() { return IniHandler.getInstance().get(NODE, KEY_FILTER_CATEGORY, null);}
    public static void setFilterType(int type) { IniHandler.getInstance().putInt(NODE, KEY_FILTER_TYPE, type);}
    public static int getFilterType() { return IniHandler.getInstance().getInt(NODE, KEY_FILTER_TYPE, 0);}
    public static void setThumbSort(int sort) { IniHandler.getInstance().putInt(NODE, KEY_THUMB_SORT, sort);}
    public static int getThumbSort() { return IniHandler.getInstance().getInt(NODE, KEY_THUMB_SORT, 0);}

    public static int getSocketPort() {
        return IniHandler.getInstance().getInt(NODE, KEY_SOCKET_PORT, defaultSocketPort());
    }

    public static void setSocketPort(int port) {
        IniHandler.getInstance().putInt(NODE, KEY_SOCKET_PORT, port);
    }

    public static int defaultSocketPort() {
        return 8889;
    }

    public static HMSMode appHMS(){
        return HMSMode.valueOf(IniHandler.getInstance().get(NODE, KEY_HMS_MODE, defaultHMSMode().name()));
    }

    public static void appHMS(HMSMode mode){
        IniHandler.getInstance().put(NODE, KEY_HMS_MODE, mode.name());
    }

    public static Theme defaultTheme() {return Theme.BUSINESS;}

    public static Theme getTheme() {
        try {
            return Theme.valueOf(IniHandler.getInstance().get(NODE, KEY_THEME, defaultTheme().name()));
        } catch (IllegalArgumentException e) {
            return defaultTheme();
        }
    }

    public static void setTheme(int theme) {
        if(theme < 0 || theme >= Theme.values().length) return;
        IniHandler.getInstance().put(NODE, KEY_THEME, Theme.values()[theme].name());
    }

    public static boolean appDBool(AppBool key){
        switch (key){
            case ASK_BEFORE_QUIT: return true;
            case MINIMIZE_TO_TRAY: return true;
            case RUN_ON_STARTUP: return true;
            case RUN_MINIMIZED: return true;
            default: return false;
        }
    }

    public static boolean appGBool(AppBool key){
        return IniHandler.getInstance().getBoolean(NODE, key.name(), appDBool(key));
    }

    public static void appSBool(AppBool key, boolean value) {
        IniHandler.getInstance().putBoolean(NODE, key.name(), value);
    }

    public static int appDInt(AppInt key){
        switch (key){
            case REFRESH_TIMEOUT: return 10 * 60;
            default: return 0;
        }
    }

    public static int appGInt(AppInt key){
        return IniHandler.getInstance().getInt(NODE, key.name(), appDInt(key));
    }

    public static void appSInt(AppInt key, int value){
        IniHandler.getInstance().putInt(NODE, key.name(), value);
    }
}
