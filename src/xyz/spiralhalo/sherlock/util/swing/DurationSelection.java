package xyz.spiralhalo.sherlock.util.swing;

import xyz.spiralhalo.sherlock.util.FormatUtil;

import java.io.Serializable;

public class DurationSelection implements Serializable {
    public enum HMSMode{
        Config, Strict, Colon, Long
    }

    private final int value;
    private final boolean unlimited;
    private final HMSMode mode;

    public DurationSelection() {
        this.unlimited = true;
        this.value = -1;
        this.mode = HMSMode.Config;
    }

    public DurationSelection(int value, HMSMode mode) {
        this.value = value;
        this.unlimited = false;
        this.mode = mode;
    }

    @Override
    public String toString() {
        if(unlimited) return "Unlimited";
        switch (mode){
            case Long: return FormatUtil.hmsLong(value);
            case Strict: return FormatUtil.hmsStrict(value);
            case Colon: return FormatUtil.hmsColon(value);
        }
        return FormatUtil.hms(value);
    }

    public int getValue() {
        return value;
    }
}
