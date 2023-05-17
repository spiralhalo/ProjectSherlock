//
//    Copyright 2020 spiralhalo <re.nanashi95@gmail.com>
//
//    This file is part of Project Sherlock.
//
//    Project Sherlock is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Project Sherlock is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Project Sherlock.  If not, see <https://www.gnu.org/licenses/>.
//

package xyz.spiralhalo.sherlock.bookmark;

import static xyz.spiralhalo.sherlock.bookmark.BookmarkConfig.BookmarkBool.AUTO_INCLUDE_EXISTING;
import static xyz.spiralhalo.sherlock.bookmark.BookmarkConfig.BookmarkInt.AUTO_SUBFOLDER;

import java.io.File;
import java.util.ArrayList;

import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.bookmark.persist.Bookmark;
import xyz.spiralhalo.sherlock.bookmark.persist.BookmarkType;
import xyz.spiralhalo.sherlock.persist.project.Project;

public class AutoBookmarker {// implements TrackerListener, Runnable {

	public static boolean scanOnRefresh(ArrayList<Project> toScan, BookmarkMgr bookmarkMgr) {
		Debug.LOG.fine(() -> "Scanning on refresh");
		for (Project p : toScan) {
			if (configExclude(p, bookmarkMgr)) continue;
			int depth = Math.max(BookmarkConfig.bkmkGInt(AUTO_SUBFOLDER), 0);
			ArrayList<File> toPopulate = new ArrayList<>();
			for (String tag : p.getTags()) {
				for (String pf : BookmarkConfig.bkmkGPFList()) {
					File dir = new File(pf);
					if (!dir.isDirectory()) continue;
					recursiveSearchPopulate(tag.toLowerCase(), dir, depth, toPopulate);
				}
			}
			final int x = toPopulate.size();
			Debug.LOG.fine(() -> String.format("Found %d file(s) for %s", x, p));
			if (toPopulate.size() > 0) {
				for (File f : toPopulate) {
					bookmarkMgr.getOrAdd(p).addOrReplaceUnsaved(new Bookmark(BookmarkType.FILE, f.getPath()));
				}
				bookmarkMgr.save();
			}
		}
		return true;
	}

	private static boolean configExclude(Project project, BookmarkMgr bookmarkMgr) {
		if (!BookmarkConfig.bkmkGBool(AUTO_INCLUDE_EXISTING) && bookmarkMgr.contains(project)) {
			return bookmarkMgr.getOrAdd(project).size() > 0;
		}
		return false;
	}

	private static boolean configInclExt(String extLowerCase) {
		return BookmarkConfig.bkmkGPFExclExt().indexOf(extLowerCase) == -1;
	}

	// breadth-first search
	// search and sort aren't my forte !!
	private static void recursiveSearchPopulate(String keywordLowerCase, File dir, int depth, ArrayList<File> toPopulate) {
		Debug.LOG.fine(() -> String.format("Scanning %s for %s... depth: %d", dir.getName(), keywordLowerCase, depth));
		File[] x = dir.listFiles();
		if (x == null) return;
		final ArrayList<File> dirChildren;
		if (depth > 0) dirChildren = new ArrayList<>();
		else dirChildren = null;
		for (File child : x) {
			if (child.isDirectory() && dirChildren != null) {
				dirChildren.add(child);
			} else {
				String name = child.getName().toLowerCase();
				int lastIndexOf = name.lastIndexOf('.');
				String ext = lastIndexOf == -1 ? "" : name.substring(lastIndexOf + 1);
				if (configInclExt(ext) && name.contains(keywordLowerCase)) {
					toPopulate.add(child);
				}
			}
		}
		if (depth > 0) {
			for (File dirChild : dirChildren) {
				recursiveSearchPopulate(keywordLowerCase, dirChild, depth - 1, toPopulate);
			}
		}
	}

	// breadth-first search
	// search and sort aren't my forte !!
//    private static File recursiveSearchReturnOne(String keywordLowerCase, File dir, int depth){
//        Debug.LOG.fine(()->String.format("Scanning %s for %s... depth: %d", dir.getName(), keywordLowerCase, depth));
//        File[] x = dir.listFiles();
//        if(x == null)return null;
//        final ArrayList<File> dirChildren;
//        if(depth > 0) dirChildren = new ArrayList<>(); else dirChildren = null;
//        for (File child:x) {
//            if(child.isDirectory() && dirChildren != null){
//                dirChildren.add(child);
//            } else {
//                String name = child.getName().toLowerCase();
//                int lastIndexOf = name.lastIndexOf('.');
//                String ext = lastIndexOf==-1?"":name.substring(lastIndexOf+1);
//                if(configInclExt(ext) && name.contains(keywordLowerCase)){
//                    return child;
//                }
//            }
//        }
//        if(depth > 0){
//            for (File dirChild:dirChildren) {
//                File f = recursiveSearchReturnOne(keywordLowerCase, dirChild, depth-1);
//                if (f!=null) return f;
//            }
//        }
//        return null;
//    }

//    private static final long FAILURE_DELAY_MIN_MILLIS = 10000;
//    private static final long FAILURE_DELAY_MAX_MILLIS = 1000000;
//    private long failureDelayMillis = FAILURE_DELAY_MIN_MILLIS;
//    private final BookmarkMgr bookmarkMgr;
//    private Project pQueue;
//    private WindowInfo wQueue;
//    private long scanStartMillis;
//    private long lastFailedHash;
//    private long lastFailureMillis;
//    private boolean inProgress;
//
//    public AutoBookmarker(BookmarkMgr bookmarkMgr, TrackerAccessor tracker) {
//        this.bookmarkMgr = bookmarkMgr;
//        tracker.addListener(this);
//        new Thread(this).start();
//    }
//
//    @Override
//    public void onTrackerLog(Project project, WindowInfo windowInfo) {
//
//        if(project == null || project.isUtilityTag()) return;
//        if(configExclude(project, bookmarkMgr))return;
//
//        synchronized (this) {
//            if(inProgress) return;
//            pQueue = project;
//            wQueue = windowInfo;
//        }
//    }
//
//    @Override
//    public void run() {
//        while (true) {
//            synchronized (this) {
//                if (pQueue == null || (pQueue.getHash() == lastFailedHash
//                && ((System.currentTimeMillis() - lastFailureMillis) < failureDelayMillis))) {
//                    continue;
//                } else {
//                    inProgress = true;
//                }
//            }
//
//            scanStartMillis = System.currentTimeMillis();
//
//            //"PaintTool SAI Ver.2"
//            if (wQueue.exeName.equals("sai2.exe")) {
//                Matcher m = Pattern.compile("((?<=- ).*)").matcher(wQueue.title);
//                if (m.find()) {
//                    String path = m.group(1).replaceAll(" / ", "\\\\");
//                    int li = path.lastIndexOf("\\");
//                    if (li == -1) return;
//                    String filen = path.substring(li + 1).replace(" (*)", "");
//                    path = path.substring(0, li + 1);
//                    File file;
//                    if (path.startsWith("Desktop")) {
//                        file = new File(System.getProperty("user.home"), path + filen);
//                    } else if (!path.startsWith("Local Disk")) {
//                        file = new File(System.getProperty("user.home"), path.substring(path.indexOf("\\")) + filen);
//                        if (!file.exists()) {
//                            file = new File(System.getProperty("user.home") + "\\Desktop", path + filen);
//                        }
//                    } else {
//                        char diskLetter = path.charAt(path.indexOf("(") + 1);
//                        file = new File(String.format("%s:%s", diskLetter, path.substring(path.indexOf("\\"))), filen);
//                    }
//                    if (file.exists()) {
//                        onSuccess("sai2", pQueue, new Bookmark(BookmarkType.FILE, file.getPath()));
//                    } else {
//                        onFailure();
//                    }
//                }
//            } else {
//                Matcher findExt = Pattern.compile("(.*)(\\.[a-zA-Z][a-zA-Z0-9]{1,10})").matcher(wQueue.title);
//                if (findExt.find()) {
//                    String wordWithExt = findExt.group();
//                    File file = new File(wordWithExt);
//                    if(file.exists()){
//                        onSuccess("immediate", pQueue, new Bookmark(BookmarkType.FILE, file.getPath()));
//                    } else {
//                        wordWithExt = wordWithExt.substring(wordWithExt.lastIndexOf("\\") + 1);
//                        wordWithExt = wordWithExt.substring(wordWithExt.lastIndexOf("/") + 1).trim().toLowerCase();
////                    String filen = ext.substring(ext.indexOf('-') + 1);
//                        boolean success = false;
//                        int depth = Math.max(BookmarkConfig.bkmkGInt(AUTO_SUBFOLDER), 0);
//                        for (String pf : BookmarkConfig.bkmkGPFList()) {
//                            File dir = new File(pf);
//                            if (!dir.isDirectory()) continue;
//                            File found = recursiveSearchReturnOne(wordWithExt, dir, depth);
//                            if (found != null) {
//                                onSuccess("scanned", pQueue, new Bookmark(BookmarkType.FILE, found.getPath()));
//                                success = true;
//                                break;
//                            }
//                        }
//                        if (!success) {
//                            onFailure();
//                        }
//                    }
//                }
//            }
//            synchronized (this) {
//                pQueue = null;
//                wQueue = null;
//                inProgress = false;
//            }
//        }
//    }
//    private void onSuccess(String remark, Project p, Bookmark b){
//        bookmarkMgr.getOrAdd(p).addOrReplaceUnsaved(b);
//        bookmarkMgr.save();
//        Debug.LOG.info("Scanning project folder SUCCESS. remark: `"+remark+"` time: "+(System.currentTimeMillis()-scanStartMillis)+"ms found: " + b.getValue());
//        failureDelayMillis = FAILURE_DELAY_MIN_MILLIS;
//    }
//
//    private void onFailure(){
//        Debug.LOG.info("Scanning project folder FAILED. time: "+(System.currentTimeMillis()-scanStartMillis)+"ms");
//        if(pQueue != null){
//            lastFailedHash = pQueue.getHash();
//            lastFailureMillis = System.currentTimeMillis();
//        }
//        failureDelayMillis = Math.min(failureDelayMillis * 2, FAILURE_DELAY_MAX_MILLIS);
//    }
}
