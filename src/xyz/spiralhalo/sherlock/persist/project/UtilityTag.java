package xyz.spiralhalo.sherlock.persist.project;

import java.io.Serializable;

public class UtilityTag extends Project implements Serializable {
    public static final long serialVersionUID = 1L;

    private boolean productive;

    public UtilityTag(String name, String category, String tags, int ptype) {
        super(name, category, tags, ptype);
        this.productive = (ptype == PTYPE_PRODUCTIVE);
    }

    @Override
    public boolean isProductive() {
        if(_appendix().containsKey(APPEND_PTYPE)){
            return ((int) _appendix().get(APPEND_PTYPE) == PTYPE_PRODUCTIVE);
        }
        return productive;
    }
//
//    public void edit(String name, String category, String tags, int productive){
//        super.edit(name, category, tags, productive);
//        this.productive = productive;
//    }
}
