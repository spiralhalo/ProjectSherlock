package xyz.spiralhalo.sherlock.bookmark;

import lc.kra.system.keyboard.event.GlobalKeyEvent;
import xyz.spiralhalo.sherlock.persist.settings.IniHandler;

public class BookmarkConfig {
    private static final String NODE = "BOOKMARK";
    public enum BookmarkInt { HOTKEY }
    public enum BookmarkBool { CTRL, SHIFT }

    public static boolean defaultBoolean(BookmarkBool key){
        switch (key){
            case CTRL:
            case SHIFT:
            default: return false;
        }
    }

    public static boolean getBool(BookmarkBool key){
        return IniHandler.getInstance().getBoolean(NODE, key.name(), defaultBoolean(key));
    }

    public static void setBoolean(BookmarkBool key, boolean value) {
        IniHandler.getInstance().putBoolean(NODE, key.name(), value);
    }

    public static int defaultInt(BookmarkInt key){
        switch (key){
            case HOTKEY: return GlobalKeyEvent.VK_F7;
            default: return 0;
        }
    }

    public static int getInt(BookmarkInt key){
        return IniHandler.getInstance().getInt(NODE, key.name(), defaultInt(key));
    }

    public static void setInt(BookmarkInt key, int value){
        IniHandler.getInstance().putInt(NODE, key.name(), value);
    }
}
