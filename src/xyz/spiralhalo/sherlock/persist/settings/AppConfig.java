package xyz.spiralhalo.sherlock.persist.settings;

public class AppConfig {
    private static final String NODE = Nodes.NODE_APPLICATION.v;

    private static final String KEY_HMS_MODE = "HMS_MODE";

    public enum HMSMode{
        COLON,
        STRICT
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
