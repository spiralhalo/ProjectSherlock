package xyz.spiralhalo.sherlock.audit.persist;

import java.io.Serializable;
import java.util.HashMap;

public class AuditFinances extends AuditEntry implements Serializable {
    public static final long serialVersionUID = 1L;

    public static final int TYPE_INCOME = 0;
    public static final int TYPE_REFUND = 1;
    public static final int TYPE_EXPENSE = 2;

    private int type;
    private double value;
    private String description;
    private final HashMap<String, Serializable> extras;

    public AuditFinances(int type, double value, String description) {
        this.type = type;
        this.value = value;
        this.description = description;
        this.extras = new HashMap<>();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Serializable getExtra(String key) {
        return extras.get(key);
    }

    public void setExtra(String key, Serializable value) {
        extras.put(key, value);
    }
}
