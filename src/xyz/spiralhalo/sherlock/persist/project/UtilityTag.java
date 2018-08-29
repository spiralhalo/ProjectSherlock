package xyz.spiralhalo.sherlock.persist.project;

import java.io.Serializable;

public class UtilityTag extends Project implements Serializable {
    public static final long serialVersionUID = 1L;
    public static final String PRODUCTIVE_LABEL = "Productive";
    public static final String NON_PRODUCTIVE_LABEL = "Non-productive";

    private boolean productive;

    public UtilityTag(String name, String category, String tags, boolean productive) {
        super(name, category, tags, productive);
        this.productive = productive;
    }

    @Override
    public boolean isProductive() {
        return productive;
    }

    public void edit(String name, String category, String tags, boolean productive){
        super.edit(name, category, tags, productive);
        this.productive = productive;
    }
}
