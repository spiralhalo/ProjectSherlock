package xyz.spiralhalo.sherlock.bookmark;

import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.EnumerateWindows.WindowInfo;
import xyz.spiralhalo.sherlock.TrackerAccessor;
import xyz.spiralhalo.sherlock.TrackerListener;
import xyz.spiralhalo.sherlock.bookmark.persist.Bookmark;
import xyz.spiralhalo.sherlock.bookmark.persist.BookmarkType;
import xyz.spiralhalo.sherlock.persist.project.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static xyz.spiralhalo.sherlock.bookmark.BookmarkConfig.BookmarkInt.AUTO_SUBFOLDER;

public class AutoBookmarker implements TrackerListener, Runnable {

    private static final long FAILURE_DELAY_MILLIS = 10000;
    private final BookmarkMgr bookmarkMgr;
    private Project pQueue;
    private WindowInfo wQueue;
    private long scanStartMillis;
    private long lastFailedHash;
    private long lastFailureMillis;
    private boolean inProgress;

    public AutoBookmarker(BookmarkMgr bookmarkMgr, TrackerAccessor tracker) {
        this.bookmarkMgr = bookmarkMgr;
        tracker.addListener(this);
        new Thread(this).start();
    }

    @Override
    public void onTrackerLog(Project project, WindowInfo windowInfo) {

        if(project == null || project.isUtilityTag()) return;
        if(bookmarkMgr.contains(project)){
            if(bookmarkMgr.getOrAdd(project).size() > 0)return;
        }

        synchronized (this) {
            if(inProgress) return;
            pQueue = project;
            wQueue = windowInfo;
        }
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                if (pQueue == null || (pQueue.getHash() == lastFailedHash
                && ((System.currentTimeMillis() - lastFailureMillis) < FAILURE_DELAY_MILLIS))) {
                    continue;
                } else {
                    inProgress = true;
                }
            }

            scanStartMillis = System.currentTimeMillis();

            //"PaintTool SAI Ver.2"
            if (wQueue.exeName.equals("sai2.exe")) {
                Matcher m = Pattern.compile("((?<=- ).*)").matcher(wQueue.title);
                if (m.find()) {
                    String path = m.group(1).replaceAll(" / ", "\\\\");
                    int li = path.lastIndexOf("\\");
                    if (li == -1) return;
                    String filen = path.substring(li + 1).replace(" (*)", "");
                    path = path.substring(0, li + 1);
                    File file;
                    if (path.startsWith("Desktop")) {
                        file = new File(System.getProperty("user.home"), path + filen);
                    } else if (!path.startsWith("Local Disk")) {
                        file = new File(System.getProperty("user.home"), path.substring(path.indexOf("\\")) + filen);
                        if (!file.exists()) {
                            file = new File(System.getProperty("user.home") + "\\Desktop", path + filen);
                        }
                    } else {
                        char diskLetter = path.charAt(path.indexOf("(") + 1);
                        file = new File(String.format("%s:%s", diskLetter, path.substring(path.indexOf("\\"))), filen);
                    }
                    if (file.exists()) {
                        onSuccess("sai2", pQueue, new Bookmark(BookmarkType.FILE, file.getPath()));
                    } else {
                        onFailure();
                    }
                }
            } else {
                Matcher findExt = Pattern.compile("(.*)(\\.[a-zA-Z][a-zA-Z0-9]{1,10})").matcher(wQueue.title);
                if (findExt.find()) {
                    String ext = findExt.group();
                    File file = new File(ext);
                    if(file.exists()){
                        onSuccess("immediate", pQueue, new Bookmark(BookmarkType.FILE, file.getPath()));
                    } else {
                        ext = ext.substring(ext.lastIndexOf("\\") + 1);
                        ext = ext.substring(ext.lastIndexOf("/") + 1).trim().toLowerCase();
//                    String filen = ext.substring(ext.indexOf('-') + 1);
                        boolean success = false;
                        int depth = Math.max(BookmarkConfig.bkmkDInt(AUTO_SUBFOLDER), 0);
                        for (String pf : BookmarkConfig.bkmkGPFList()) {
                            File dir = new File(pf);
                            if (!dir.isDirectory()) continue;
                            File found = recursiveSearch(ext, dir, depth);
                            if (found != null) {
                                onSuccess("scanned", pQueue, new Bookmark(BookmarkType.FILE, found.getPath()));
                                success = true;
                                break;
                            }
                        }
                        if (!success) {
                            onFailure();
                        }
                    }
                }
            }
            synchronized (this) {
                pQueue = null;
                wQueue = null;
                inProgress = false;
            }
        }
    }

    //breadth-first search
    //search and sort aren't my forte !!
    private File recursiveSearch(String keywordLowerCase, File dir, int depth){
        File[] x = dir.listFiles();
        if(x == null)return null;
        final ArrayList<File> dirChildren;
        if(depth > 0) dirChildren = new ArrayList<>(); else dirChildren = null;
        for (File child:x) {
            if(child.isDirectory() && dirChildren != null){
                dirChildren.add(child);
            } else if(child.getName().toLowerCase().contains(keywordLowerCase)){
                return child;
            }
        }
        if(depth > 0){
            for (File dirChild:dirChildren) {
                File f = recursiveSearch(keywordLowerCase, dirChild, depth-1);
                if (f!=null) return f;
            }
        }
        return null;
    }

    private void onSuccess(String remark, Project p, Bookmark b){
        bookmarkMgr.getOrAdd(p).addOrReplace(b);
        bookmarkMgr.save();
        Debug.logImportant("Scanning project folder SUCCESS. remark: `"+remark+"` time: "+(System.currentTimeMillis()-scanStartMillis)+"ms found: " + b.getValue());
    }

    private void onFailure(){
        Debug.logImportant("Scanning project folder FAILED. time: "+(System.currentTimeMillis()-scanStartMillis)+"ms");
        if(pQueue != null){
            lastFailedHash = pQueue.getHash();
            lastFailureMillis = System.currentTimeMillis();
        }
    }
}
