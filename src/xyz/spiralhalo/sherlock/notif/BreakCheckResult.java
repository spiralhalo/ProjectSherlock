package xyz.spiralhalo.sherlock.notif;

final class BreakCheckResult {
    private boolean wasOnBreak;
    private long breakDuration;

    public boolean wasOnBreak() {
        return wasOnBreak;
    }

    public long breakDuration() {
        return breakDuration;
    }

    public static BreakCheckResult set(boolean wasOnBreak, long breakDuration) {
        INSTANCE.wasOnBreak = wasOnBreak;
        INSTANCE.breakDuration = breakDuration;
        return INSTANCE;
    }

    private static final BreakCheckResult INSTANCE = new BreakCheckResult();
    private BreakCheckResult() {}
}
