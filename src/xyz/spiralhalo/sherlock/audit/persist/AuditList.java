package xyz.spiralhalo.sherlock.audit.persist;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;

public class AuditList extends HashMap<LocalDate, DayAudit> implements Serializable {
    public static final long serialVersionUID = 1L;

    private HashMap<String, Object> extras;

    public AuditList(){
        super();
        extras = new HashMap<>();
    }

    public HashMap<String, Object> getExtras() {
        return extras;
    }
}
