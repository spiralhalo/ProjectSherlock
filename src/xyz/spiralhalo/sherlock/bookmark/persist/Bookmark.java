package xyz.spiralhalo.sherlock.bookmark.persist;

import xyz.spiralhalo.sherlock.persist.project.Project;

import java.io.Serializable;

public class Bookmark implements Serializable {
    private transient Project p;
    private long projectHash;

}
