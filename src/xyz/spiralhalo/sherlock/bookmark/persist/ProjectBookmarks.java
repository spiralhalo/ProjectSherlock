package xyz.spiralhalo.sherlock.bookmark.persist;

import xyz.spiralhalo.sherlock.bookmark.ModelAccessor;
import xyz.spiralhalo.sherlock.bookmark.ProjectBookmarksModel;

import java.io.Serializable;
import java.util.ArrayList;

public class ProjectBookmarks implements Serializable, ModelAccessor {
    public static final long serialVersionUID = 1L;

    private transient ProjectBookmarksModel model;
    private final ArrayList<Bookmark> bookmarks;

    public ProjectBookmarks() {
        bookmarks = new ArrayList<>();
    }

    public ProjectBookmarksModel getModel(){
        if(model == null){
            model = new ProjectBookmarksModel(this);
        }
        return model;
    }

    public Bookmark get(int i){
        return bookmarks.get(i);
    }

    public void addOrReplace(Bookmark bookmark){
        if(bookmarks.contains(bookmark))return;
        bookmarks.add(bookmark);
        getModel().fireTableRowsInserted(size()-1,size()-1);
    }

    public void remove(int x){
        bookmarks.remove(x);
        getModel().fireTableRowsDeleted(x,x);
    }

    public void editBookmark(int x, Bookmark newValue){
        if(x==-1)return;
        bookmarks.remove(x);
        bookmarks.add(x, newValue);
        getModel().fireTableRowsUpdated(x,x);
    }

    public void moveUp(int i){
        if(i>0){
            Bookmark x = bookmarks.get(i);
            bookmarks.remove(i);
            bookmarks.add(i-1, x);
            getModel().fireTableRowsUpdated(i-1,i);
        }
    }

    public void moveDown(int i){
        if(i<bookmarks.size()-1){
            Bookmark x = bookmarks.get(i);
            bookmarks.remove(i);
            bookmarks.add(i+1, x);
            getModel().fireTableRowsUpdated(i,i+1);
        }
    }

    @Override
    public int size() {
        return bookmarks.size();
    }

    @Override
    public String getType(int i) {
        switch (bookmarks.get(i).getType()){
            default:
            case URL:return "URL";
            case FILE:return "File";
        }
    }

    @Override
    public String getValue(int i) {
        return bookmarks.get(i).getValue();
    }
}
