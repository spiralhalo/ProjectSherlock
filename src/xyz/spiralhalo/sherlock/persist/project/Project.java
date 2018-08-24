package xyz.spiralhalo.sherlock.persist.project;

import xyz.spiralhalo.sherlock.util.MurmurHash;

import java.awt.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Random;

public class Project implements Serializable {
    private static final HashMap<Long,Integer> colors = new HashMap<>();
    public static final long serialVersionUID = 1L;

    private long hash;
    private String name;
    private String category;
    private String[] tags;
    private ZonedDateTime startDate;
    private ZonedDateTime finishedDate;
    private boolean isFinished;
    private HashMap<String, Object> appendix;

    public Project(String name, String category, String tags) {
        this.hash = System.currentTimeMillis() ^ ((long)tags.hashCode() << 42);
        this.name = name;
        this.category = category;
        this.startDate = ZonedDateTime.now();
        this.appendix = new HashMap<>();
        setTags(tags);
        resetColor();
    }

    public void edit(String name, String category, String tags){
        this.name = name;
        this.category = category;
        setTags(tags);
        resetColor();
    }

    private static int getColor(String name, String category){
        Random rnd1 = new Random(MurmurHash.hash64(name));
        float r1 = rnd1.nextFloat();
        float r2 = new Random(MurmurHash.hash64(category)).nextFloat();
        return Color.HSBtoRGB((r2-0.05f+r1*0.2f)%1f,0.3f+r1*0.3f,1f);
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

    public void putAppendix(String name, Object obj){
        appendix.put(name, obj);
    }

    public Object getAppendix(String name){
        return appendix.get(name);
    }

    public void resetColor(){ colors.put(hash,getColor(name, category)); }

    public void setTags(String tag) {
        this.tags = tag.split(",");
        for (int i = 0; i < this.tags.length; i++) {
            this.tags[i] = this.tags[i].trim();
        }
    }

    public long getHash() { return hash; }

    public int getColor() {
        if(colors.get(hash) == null)resetColor();
        return colors.get(hash); }

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
