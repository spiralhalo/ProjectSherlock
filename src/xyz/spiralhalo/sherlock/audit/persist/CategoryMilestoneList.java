package xyz.spiralhalo.sherlock.audit.persist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class CategoryMilestoneList extends HashMap<String, ArrayList<String>> implements Serializable {
    public static final long serialVersionUID = 1L;
}
