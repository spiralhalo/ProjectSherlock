package xyz.spiralhalo.sherlock.persist.project;

import java.io.Serializable;

public class UtilityTag extends Project implements Serializable {
    public static final long serialVersionUID = 1L;
    public static final String PRODUCTIVE_LABEL = "Supporting";
    public static final String NON_PRODUCTIVE_LABEL = "General";

    private boolean productive;

    public UtilityTag(String name, String tags, boolean productive) {
        super(name, productive?PRODUCTIVE_LABEL:NON_PRODUCTIVE_LABEL, tags);
        this.productive = productive;
    }

    @Override
    public boolean isProductive() {
        return productive;
    }

    public void edit(String name, String tags, boolean productive){
        super.edit(name, productive?PRODUCTIVE_LABEL:NON_PRODUCTIVE_LABEL, tags);
        this.productive = productive;
    }
}
