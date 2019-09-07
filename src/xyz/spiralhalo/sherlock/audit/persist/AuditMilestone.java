package xyz.spiralhalo.sherlock.audit.persist;

import java.io.Serializable;
import java.time.Instant;

public class AuditMilestone implements Serializable {
    public static final long serialVersionUID = 1L;

    private String name;
    private String comment;
    private Instant exactTime; // nullable

    public AuditMilestone(String name, String notes, Instant exactTime) {
        this.name = name;
        this.comment = notes;
        this.exactTime = exactTime;
    }

    public void edit(String name, String notes) {
        this.name = name;
        this.comment = notes;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public Instant getExactTime() { // nullable
        return exactTime;
    }
}
