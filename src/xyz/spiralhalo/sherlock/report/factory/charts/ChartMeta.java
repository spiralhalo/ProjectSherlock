package xyz.spiralhalo.sherlock.report.factory.charts;

import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;

public class ChartMeta extends HashMap<String, Paint> implements Serializable {
    public static final long serialVersionUID = 3L;
    private int logDur = 0;
    private int workDur = 0;
    private int breakDur = 0;

    void addLogDur(int logDur) {
        this.logDur += logDur;
    }

    void addWorkDur(int workDur) {
        this.workDur += workDur;
    }

    void addBreakDur(int breakDur) {
        this.breakDur += breakDur;
    }

    public int getLogDur() {
        return logDur;
    }

    public int getWorkDur() {
        return workDur;
    }

    public int getBreakDur() {
        return breakDur;
    }
}
