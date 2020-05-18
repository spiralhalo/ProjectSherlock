package xyz.spiralhalo.sherlock.bookmark;

import xyz.spiralhalo.sherlock.EnumerateWindows;
import xyz.spiralhalo.sherlock.TrackerAccessor;
import xyz.spiralhalo.sherlock.TrackerListener;
import xyz.spiralhalo.sherlock.bookmark.persist.Bookmark;
import xyz.spiralhalo.sherlock.bookmark.persist.BookmarkType;
import xyz.spiralhalo.sherlock.persist.project.Project;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoBookmarker implements TrackerListener {

    private final BookmarkMgr bookmarkMgr;

    public AutoBookmarker(BookmarkMgr bookmarkMgr, TrackerAccessor tracker) {
        this.bookmarkMgr = bookmarkMgr;
        tracker.addListener(this);
    }

    @Override
    public void onTrackerLog(Project project, EnumerateWindows.WindowInfo windowInfo) {

        if(project == null || project.isUtilityTag()) return;
        if(bookmarkMgr.contains(project)){
            if(bookmarkMgr.getOrAdd(project).size() > 0)return;
        }

        //"PaintTool SAI Ver.2"
        if(windowInfo.exeName.equals("sai2.exe")){
            Matcher m = Pattern.compile("((?<=- ).*)").matcher(windowInfo.title);
            if(!m.find())return;
            String path = m.group(1).replaceAll(" / ", "\\\\");
            int li = path.lastIndexOf("\\");
            if(li == -1)return;
            String filen = path.substring(li+1).replace(" (*)","");
            path = path.substring(0, li+1);
            File file;
            if (path.startsWith("Desktop")) {
                file = new File(System.getProperty("user.home"), path + filen);
            } else if (!path.startsWith("Local Disk")) {
                file = new File(System.getProperty("user.home"), path.substring(path.indexOf("\\")) + filen);
                if (!file.exists()){
                    file = new File(System.getProperty("user.home")+"\\Desktop", path + filen);
                }
            } else {
                char diskLetter = path.charAt(path.indexOf("(") + 1);
                file = new File(String.format("%s:%s", diskLetter, path.substring(path.indexOf("\\"))), filen);
            }
            if(file.exists()){
                bookmarkMgr.getOrAdd(project).addOrReplace(new Bookmark(BookmarkType.FILE, file.getPath()));
            }
        }
    }
}
