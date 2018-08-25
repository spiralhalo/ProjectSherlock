package xyz.spiralhalo.sherlock.persist.settings;

public class AppConfig {
    private static final String NODE = Nodes.NODE_APPLICATION.v;

    private static final String KEY_HMS_MODE = "HMS_MODE";
    private static final String KEY_THEME = "THEME";

    public enum HMSMode{
        COLON,
        STRICT
    }

    public enum Theme{
        BUSINESS(0, 0, false,"Substance Business"),
        BUSINESS_BLUE(1, 0x406079, false, "Substance Business Blue Steel"),
        BUSINESS_BLACK(2, 0, false, "Substance Business Black Steel"),
        MIST_SILVER(3, 0, false, "Substance Mist Silver"),
        GRAPHITE(4, 0xffffff, true, "Substance Graphite"),
        RAVEN(5, 0xffffff, true, "Substance Raven"),
        TWILIGHT(6, 0xcccc99, true, "Substance Twilight"),
        SYSTEM(7, 0, false, "System Default Theme"),
        ;
        public int x;
        public int buttonColor;
        public boolean dark;
        public String label;
        Theme(int x, int buttonColor, boolean dark, String label) {
            this.x = x;
            this.buttonColor = buttonColor;
            this.dark = dark;
            this.label = label;
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

    public static HMSMode defaultHMSMode() {return HMSMode.COLON;}

    public static HMSMode getHMSMode(){
        return HMSMode.valueOf(IniHandler.getInstance().get(NODE, KEY_HMS_MODE, defaultHMSMode().name()));
    }

    public static void setHMSMode(HMSMode mode){
        IniHandler.getInstance().put(NODE, KEY_HMS_MODE, mode.name());
    }

    public static Theme defaultTheme() {return Theme.BUSINESS;}

    public static Theme getTheme() {
        return Theme.valueOf(IniHandler.getInstance().get(NODE, KEY_THEME, defaultTheme().name()));
    }

    public static void setTheme(int theme) {
        if(theme < 0 || theme >= Theme.values().length) return;
        IniHandler.getInstance().put(NODE, KEY_THEME, Theme.values()[theme].name());
    }

    public static boolean defaultBoolean(AppBool key){
        switch (key){
            case ASK_BEFORE_QUIT: return true;
            case MINIMIZE_TO_TRAY: return true;
            case RUN_ON_STARTUP: return true;
            case RUN_MINIMIZED: return true;
            default: return false;
        }
    }

    public static boolean getBool(AppBool key){
        return IniHandler.getInstance().getBoolean(NODE, key.name(), defaultBoolean(key));
    }

    public static void setBoolean(AppBool key, boolean value) {
        IniHandler.getInstance().putBoolean(NODE, key.name(), value);
    }

    public static int defaultInt(AppInt key){
        switch (key){
            case REFRESH_TIMEOUT: return 10 * 60;
            default: return 0;
        }
    }

    public static int getInt(AppInt key){
        return IniHandler.getInstance().getInt(NODE, key.name(), defaultInt(key));
    }

    public static void setInt(AppInt key, int value){
        IniHandler.getInstance().putInt(NODE, key.name(), value);
    }
}
