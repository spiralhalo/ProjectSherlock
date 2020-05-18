package xyz.spiralhalo.sherlock.bookmark;

import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.Provider;
import xyz.spiralhalo.sherlock.EnumerateWindows;
import xyz.spiralhalo.sherlock.TrackerAccessor;
import xyz.spiralhalo.sherlock.TrackerListener;
import xyz.spiralhalo.sherlock.bookmark.BookmarkConfig.BookmarkInt;
import xyz.spiralhalo.sherlock.bookmark.persist.BookmarkMap;
import xyz.spiralhalo.sherlock.bookmark.persist.ProjectBookmarks;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectsOnly;

import javax.swing.*;
import java.util.Arrays;

import static java.awt.event.KeyEvent.*;

public class BookmarkMgr implements HotKeyListener, TrackerListener {
    public static final int[] ALLOWED_VK = new int[]{
            VK_F1, VK_F2, VK_F3, VK_F4, VK_F5, VK_F6, VK_F7, VK_F8, VK_F9, VK_F10, VK_F11, VK_F12 };

    public static final String[] ALLOWED_VK_NAME = new String[]{
            "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12" };

    private final BookmarkMap bookmarkMap;
    private Project lastTracked;

    private static Provider hotKeyProvider = Provider.getCurrentProvider(false);

    public BookmarkMgr(TrackerAccessor tracker) {
        bookmarkMap = BookmarkMap.load();
        tracker.addListener(this);
        reinitHotkeyHook();
    }

    @ProjectsOnly
    public void invoke(Project p) {
        SwingUtilities.invokeLater(() -> {
            ProjectBookmarkList dialog = ProjectBookmarkList.getDialog(getThis(), p);
            dialog.forceShow();
        });
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

    private BookmarkMgr getThis(){
        return this;
    }

    private static void assureProject(Project p, String methodName){
        if(p.isUtilityTag()) throw new IllegalArgumentException(methodName+"() don't accept utility tags");
    }

    @Override
    public void onHotKey(HotKey hotKey) {
        if(lastTracked != null && !lastTracked.isUtilityTag()){
            invoke(lastTracked);
        }
    }

    private int getHotKeyVK(){
        int hotkeyVK = BookmarkConfig.bkmkGInt(BookmarkInt.HOTKEY);
        if(Arrays.binarySearch(ALLOWED_VK, hotkeyVK)==-1){
            hotkeyVK = BookmarkConfig.bkmkDInt(BookmarkInt.HOTKEY);
        }
        return hotkeyVK;
    }

    public void reinitHotkeyHook() {
        hotKeyProvider.reset();
        if(BookmarkConfig.bkmkGBool(BookmarkConfig.BookmarkBool.ENABLED)) {
            int hotkeyVK = getHotKeyVK();
            int CTRLmask = BookmarkConfig.bkmkGBool(BookmarkConfig.BookmarkBool.CTRL) ? CTRL_DOWN_MASK : 0;
            int SHIFTmask = BookmarkConfig.bkmkGBool(BookmarkConfig.BookmarkBool.SHIFT) ? SHIFT_DOWN_MASK : 0;
            int mask = CTRLmask | SHIFTmask;
            hotKeyProvider.register(KeyStroke.getKeyStroke(hotkeyVK, mask, false), this);
        }
    }

    @Override
    public void onTrackerLog(Project project, EnumerateWindows.WindowInfo windowInfo) {
        lastTracked = project;
    }
}
