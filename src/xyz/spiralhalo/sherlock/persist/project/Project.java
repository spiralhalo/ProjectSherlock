package xyz.spiralhalo.sherlock.persist.project;

import xyz.spiralhalo.sherlock.util.MathUtil;
import xyz.spiralhalo.sherlock.util.MurmurHash;

import java.awt.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Random;

public class Project implements Serializable {
    public static final long serialVersionUID = 1L;
    public static final int PTYPE_PRODUCTIVE = 0;
    public static final int PTYPE_RECREATIONAL = 1;
    public static final int PTYPE_MISC = 2;
    protected static final String APPEND_PTYPE = "productivity_type";
    public static final String PRODUCTIVE_LABEL = "Productive";
    public static final String NON_PRODUCTIVE_LABEL = "Misc.";
    public static final String RECREATIONAL_LABEL = "Recreational";

    private long hash;
    private String name;
    private String category;
    private String[] tags;
    private ZonedDateTime startDate;
    private ZonedDateTime finishedDate;
    private boolean isFinished;
    private final HashMap<String, Object> appendix;

//    public Project(String name, String category, String tags) {
//        this(name, category, tags, true);
//    }

    public Project(String name, String category, String tags, int ptype) {
        this.hash = System.currentTimeMillis() ^ ((long)tags.hashCode() << 42);
        this.name = name;
        this.category = category;
        this.startDate = ZonedDateTime.now();
        this.appendix = new HashMap<>();
        setTags(tags);
        appendix.put(APPEND_PTYPE, ptype);
        resetColor(ptype == PTYPE_PRODUCTIVE);
    }

    public boolean isUtilityTag(){
        return this instanceof UtilityTag;
    }

    public boolean isProductive(){
        if(this.appendix.containsKey(APPEND_PTYPE)){
            return ((int)appendix.get(APPEND_PTYPE) == PTYPE_PRODUCTIVE);
        }
        return true;
    }

    public boolean isRecreational(){
        if(this.appendix.containsKey(APPEND_PTYPE)){
            return (!isProductive()) && ((int)appendix.get(APPEND_PTYPE) == PTYPE_RECREATIONAL);
        }
        return false;
    }


    protected HashMap<String, Object> _appendix(){
        return appendix;
    }

    protected void edit(String name, String category, String tags, int ptype){
        String oldCat = this.category;
        this.name = name;
        setTags(tags);
        if(!oldCat.equals(category) || this.isProductive() != (ptype==PTYPE_PRODUCTIVE)) {
            this.category = category;
            resetColor(ptype == PTYPE_PRODUCTIVE);
        }
        this.appendix.put(APPEND_PTYPE, ptype);
    }

    void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    void setFinishedDate(ZonedDateTime finishedDate) {
        this.finishedDate = finishedDate;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
        if(finished)finishedDate = ZonedDateTime.now();
    }

    public <T> void putAppendix(String name, T obj){
        appendix.put(name, obj);
    }

    public <T> T getAppendix(String name, Class<T> cls){
        if(appendix.get(name)==null || !cls.isInstance(appendix.get(name))){
            return null;
        } else {
            return cls.cast(appendix.get(name));
        }
    }

    public void resetColor(boolean productive){
        float r0 = new Random(MurmurHash.hash64(category)).nextFloat();
        float r1 = MathUtil.normalize(new Random(MurmurHash.hash64(name)).nextFloat(), 2f);
        float r2 = MathUtil.normalize(new Random(hash).nextFloat(), 3f);
        int color = Color.HSBtoRGB((r0-0.1f+r1),(1f-r2*2.6f)*(productive?1:0.6f),productive?1:0.8f);
        putAppendix("color", color);
    }

    private void setTags(String tag) {
        this.tags = tag.split(",");
        for (int i = 0; i < this.tags.length; i++) {
            this.tags[i] = this.tags[i].trim();
        }
    }

    public long getHash() { return hash; }

    public int getColor() {
        if(getAppendix("color", Integer.class) == null){
            resetColor(isProductive());
        }
        return getAppendix("color", Integer.class);
    }

    public String[] getTags() { return tags; }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public ZonedDateTime getFinishedDate() {
        return finishedDate;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean includes(ZonedDateTime date){ return !date.isBefore(startDate) && (!isFinished || date.isBefore(finishedDate)); }

    @Override
    public String toString() {
        return String.format("%s [%s]", name, category);
    }

    public int getPtype() {
        if(this.appendix.containsKey(APPEND_PTYPE)){
            return (int)appendix.get(APPEND_PTYPE);
        } else if(isProductive()) return PTYPE_PRODUCTIVE;
        else return PTYPE_MISC;
    }
}
