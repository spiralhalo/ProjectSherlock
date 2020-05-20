package xyz.spiralhalo.sherlock.ocr.persist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class OCRTargetList extends ArrayList<OCRTargetApp> implements Serializable {
    public static final long serialVersionUID = 1L;

    public OCRTargetList(Collection<? extends OCRTargetApp> collection) {
        super(collection);
    }
}
