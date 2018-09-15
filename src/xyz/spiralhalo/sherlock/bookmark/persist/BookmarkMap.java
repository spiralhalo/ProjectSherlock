package xyz.spiralhalo.sherlock.bookmark.persist;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.Debug;

import java.io.*;
import java.util.HashMap;

public class BookmarkMap extends HashMap<Long, ProjectBookmarks> {
    private static String BOOKMARKS_FILE = "bookmarks.dat";

    public synchronized static BookmarkMap load() {
        File file = new File(Application.getSaveDir(), BOOKMARKS_FILE);
        if(file.exists()){
            try (FileInputStream fis = new FileInputStream(file);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                return (BookmarkMap) ois.readObject();
            } catch (ClassNotFoundException | IOException | ClassCastException e) {
                Debug.log(e);
            }
        }
        return new BookmarkMap();
    }

    public synchronized static void save(BookmarkMap bookmarkMap){
        File file = new File(Application.getSaveDir(),BOOKMARKS_FILE);
        try(FileOutputStream fis = new FileOutputStream(file);
            ObjectOutputStream ois = new ObjectOutputStream(fis)){
            ois.writeObject(bookmarkMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
