package xyz.spiralhalo.sherlock.extras.bookmark;

import lc.kra.system.keyboard.event.GlobalKeyEvent;
import xyz.spiralhalo.sherlock.persist.settings.IniHandler;

public class BookmarkConfig {
    private static final String NODE = "BOOKMARK";
    public enum BookmarkInt { HOTKEY }
    public enum BookmarkBool { ENABLED, CTRL, SHIFT, CLOSE_WINDOW }

    public static boolean bkmkDBool(BookmarkBool key){
        switch (key){
            case ENABLED:
            case CTRL:
            case SHIFT:
            case CLOSE_WINDOW:
            default: return false;
        }
    }

    public static boolean bkmkGBool(BookmarkBool key){
        return IniHandler.getInstance().getBoolean(NODE, key.name(), bkmkDBool(key));
    }

    public static void bkmkSBool(BookmarkBool key, boolean value) {
        IniHandler.getInstance().putBoolean(NODE, key.name(), value);
    }

    public static int bkmkDInt(BookmarkInt key){
        switch (key){
            case HOTKEY: return GlobalKeyEvent.VK_F7;
            default: return 0;
        }
    }

    public static int bkmkGInt(BookmarkInt key){
        return IniHandler.getInstance().getInt(NODE, key.name(), bkmkDInt(key));
    }

    public static void bkmkSInt(BookmarkInt key, int value){
        IniHandler.getInstance().putInt(NODE, key.name(), value);
    }
}
