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

    private long hash;
    private String name;
    private String category;
    private String[] tags;
    private ZonedDateTime startDate;
    private ZonedDateTime finishedDate;
    private boolean isFinished;
    private final HashMap<String, Object> appendix;

    public Project(String name, String category, String tags) {
        this(name, category, tags, true);
    }

    protected Project(String name, String category, String tags, boolean productive) {
        this.hash = System.currentTimeMillis() ^ ((long)tags.hashCode() << 42);
        this.name = name;
        this.category = category;
        this.startDate = ZonedDateTime.now();
        this.appendix = new HashMap<>();
        setTags(tags);
        resetColor(productive);
    }

    public boolean isUtilityTag(){
        return this instanceof UtilityTag;
    }

    public boolean isProductive(){
        return true;
    }

    void edit(String name, String category, String tags){
        edit(name, category, tags, true);
    }

    protected void edit(String name, String category, String tags, boolean productive){
        String oldCat = this.category;
        this.name = name;
        setTags(tags);
        if(!oldCat.equals(category) || this.isProductive() != productive) {
            this.category = category;
            resetColor(productive);
        }
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
        float m = productive?1f:0.5f;
        System.out.println(productive);
        putAppendix("color", Color.HSBtoRGB((r0-0.1f+r1),(1f-r2*2.6f)*m,1f*m));
//        putAppendix("color", Color.HSBtoRGB((r0-0.1f+r1),0.8f/d,(1f-r2*2)/d));
//        putAppendix("color", Color.HSBtoRGB((r0-0.1f+r1),(1f-r2)/d,(0.7f+r2)/d));
    }

    public void setTags(String tag) {
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
}
