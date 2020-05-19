package xyz.spiralhalo.sherlock.bookmark;

import xyz.spiralhalo.sherlock.persist.settings.IniHandler;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookmarkConfig {
    private static final String NODE = "BOOKMARK";
    private static final String KEY_PFLIST = "PFLIST";
    private static final String KEY_PFEXCL_EXT = "PFEXCL_EXT";
    public enum BookmarkInt { HOTKEY, AUTO_SUBFOLDER }
    public enum BookmarkBool { ENABLED, CTRL, SHIFT, CLOSE_WINDOW, AUTO_BOOKMARK, AUTO_INCLUDE_EXISTING, DEL_NO_CONFIRM }

    public static boolean bkmkDBool(BookmarkBool key){return false;}

    public static boolean bkmkGBool(BookmarkBool key){
        return IniHandler.getInstance().getBoolean(NODE, key.name(), bkmkDBool(key));
    }

    public static void bkmkSBool(BookmarkBool key, boolean value) {
        IniHandler.getInstance().putBoolean(NODE, key.name(), value);
    }

    public static int bkmkDInt(BookmarkInt key){
        switch (key){
            case HOTKEY: return KeyEvent.VK_F7;
            default: return 0;
        }
    }

    public static int bkmkGInt(BookmarkInt key){
        return IniHandler.getInstance().getInt(NODE, key.name(), bkmkDInt(key));
    }

    public static void bkmkSInt(BookmarkInt key, int value){
        IniHandler.getInstance().putInt(NODE, key.name(), value);
    }

    private static ArrayList<String> cachedPFList;
    public static ArrayList<String> bkmkGPFList(){
        if(cachedPFList == null) {
            cachedPFList = new ArrayList<>();
            String x = IniHandler.getInstance().get(NODE, KEY_PFLIST, null);
            if (x != null) {
                for (String s : x.split("\\|")) {
                    File z = new File(s);
                    if (z.isDirectory()) cachedPFList.add(s);
                }
            }
        }
        return cachedPFList;
    }

    public static void bkmkSPFList(String[] pFList){
        cachedPFList = new ArrayList<>();
        for (String s : pFList) {
            File z = new File(s);
            if (z.isDirectory()) cachedPFList.add(s);
        }
        if(cachedPFList.size() == 0){
            IniHandler.getInstance().node(NODE).remove(KEY_PFLIST);
        } else {
            IniHandler.getInstance().put(NODE, KEY_PFLIST, String.join("|", cachedPFList.toArray(new String[]{})));
        }
    }

    private static List<String> cachedPFExclExt;
    public static List<String> bkmkGPFExclExt() {
        if(cachedPFExclExt == null){
            String w = IniHandler.getInstance().get(NODE, KEY_PFEXCL_EXT, null);
            if(w == null){
                cachedPFExclExt = bkmkDPFExclExt();
            } else {
                String[] x = w.split(",");
                cachedPFExclExt = new ArrayList<>(x.length);
                for (int i = 0; i < x.length; i++) {
                    cachedPFExclExt.add(x[i].toLowerCase().trim());
                }
            }
        }
        return cachedPFExclExt;
    }

    public static void bkmkSPFExclExt(String[] exclExts) {
        cachedPFExclExt = Arrays.asList(exclExts);
        IniHandler.getInstance().put(NODE, KEY_PFEXCL_EXT, String.join(",",exclExts));
    }

    public static List<String> bkmkDPFExclExt() {
        return Arrays.asList("png","jpg","jpeg","meta","db","dat","dll");
    }
}
