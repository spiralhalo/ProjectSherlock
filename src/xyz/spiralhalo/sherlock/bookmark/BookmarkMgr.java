package xyz.spiralhalo.sherlock.bookmark;

import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;
import xyz.spiralhalo.sherlock.GlobalInputHook;
import xyz.spiralhalo.sherlock.TrackerAccessor;
import xyz.spiralhalo.sherlock.bookmark.BookmarkConfig.BookmarkBool;
import xyz.spiralhalo.sherlock.bookmark.BookmarkConfig.BookmarkInt;
import xyz.spiralhalo.sherlock.bookmark.persist.BookmarkMap;
import xyz.spiralhalo.sherlock.bookmark.persist.ProjectBookmarks;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectsOnly;

import javax.swing.*;

import java.util.Arrays;

import static lc.kra.system.keyboard.event.GlobalKeyEvent.*;

public class BookmarkMgr {
    public static final int[] ALLOWED_VK = new int[]{
            VK_F1, VK_F2, VK_F3, VK_F4, VK_F5, VK_F6, VK_F7, VK_F8, VK_F9, VK_F10, VK_F11, VK_F12 };

    public static final String[] ALLOWED_VK_NAME = new String[]{
            "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12" };

    private final BookmarkMap bookmarkMap;
    private final TrackerAccessor tracker;
    private final KeyAdapter keyAdapter = new KeyAdapter();

    public BookmarkMgr(TrackerAccessor tracker) {
        bookmarkMap = BookmarkMap.load();
        this.tracker = tracker;
        GlobalInputHook.GLOBAL_KEYBOARD_HOOK.addKeyListener(keyAdapter);
    }

    @ProjectsOnly
    public ProjectBookmarks getOrAdd(Project p){
        assureProject(p, "getOrAdd");
        long hash = p.getHash();
        if(!bookmarkMap.containsKey(hash)){
            bookmarkMap.put(hash, new ProjectBookmarks());
        }
        return bookmarkMap.get(hash);
    }

    public void save(){
        BookmarkMap.save(bookmarkMap);
    }

    public void resetHotketState(){
        keyAdapter.hotkeyPressed = false;
    }

    private BookmarkMgr getThis(){
        return this;
    }

    private static void assureProject(Project p, String methodName){
        if(p.isUtilityTag()) throw new IllegalArgumentException(methodName+"() don't accept utility tags");
    }

    private class KeyAdapter extends GlobalKeyAdapter{
        private boolean hotkeyPressed;

        private int getHotkey(){
            int hotkey_vk = BookmarkConfig.getInt(BookmarkInt.HOTKEY);
            if(Arrays.binarySearch(ALLOWED_VK, hotkey_vk)==-1){
                hotkey_vk = BookmarkConfig.defaultInt(BookmarkInt.HOTKEY);
            }
            return hotkey_vk;
        }

        @Override public void keyPressed(GlobalKeyEvent event) {
            int hotkey = getHotkey();
            if (event.getVirtualKeyCode() == hotkey
                    && (!BookmarkConfig.getBool(BookmarkBool.CTRL) || event.isControlPressed())
                    && (!BookmarkConfig.getBool(BookmarkBool.SHIFT) || event.isShiftPressed())){
                if(!hotkeyPressed && tracker.lastTracked() != null && !tracker.lastTracked().isUtilityTag()) {
                    hotkeyPressed = true;
                    SwingUtilities.invokeLater(() -> {
                        ProjectBookmarkList dialog = ProjectBookmarkList.getDialog(getThis(), tracker.lastTracked());
                        dialog.forceShow();
                    });
                }
            }
        }

        @Override
        public void keyReleased(GlobalKeyEvent event) {
            if (event.getVirtualKeyCode() == getHotkey()){
                hotkeyPressed = false;
            }
        }
    }
}
