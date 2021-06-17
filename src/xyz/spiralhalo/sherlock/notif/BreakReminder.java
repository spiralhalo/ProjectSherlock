//
//    Copyright 2020 spiralhalo <re.nanashi95@gmail.com>
//
//    This file is part of Project Sherlock.
//
//    Project Sherlock is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Project Sherlock is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Project Sherlock.  If not, see <https://www.gnu.org/licenses/>.
//

package xyz.spiralhalo.sherlock.notif;

import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.EnumerateWindows;
import xyz.spiralhalo.sherlock.TrackerAccessor;
import xyz.spiralhalo.sherlock.TrackerListener;
import xyz.spiralhalo.sherlock.persist.project.Project;

import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.util.function.Supplier;

import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserBool.BREAK_ANY_USAGE;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserInt.BREAK_MAX_WORKDUR;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserInt.BREAK_MIN_BREAKDUR;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserStr.BREAK_MESSAGE;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserStr.HALF_BREAK_MESSAGE;

/**
 * Manager module that tracks for break and work durations and
 * provides notifications based on user configured parameters.
 */
public class BreakReminder implements TrackerListener {

    private long lastBreakOrReminder;
    private long lastLoggedWork;

    private final TrayIcon trayIcon;
    private final BreakVerbose breakVerboseMsg;
    private final BreakVerbose halfBreakVerboseMsg;
    private final BreakVerbose workVerboseMsg;

    /**
     * Break reminder manager.
     * @param tracker project tracker object.
     * @param appTrayIcon primary app tray icon.
     */
    public BreakReminder(TrackerAccessor tracker, TrayIcon appTrayIcon) {
        // What happens if tray icon is unsupported? -> shouldn't happen on Windows
        lastLoggedWork = lastBreakOrReminder = System.currentTimeMillis();
        trayIcon = appTrayIcon;
        breakVerboseMsg = new BreakVerbose("On break", "minimum");
        halfBreakVerboseMsg = new BreakVerbose("Reminder shown. On break", "maximum");
        workVerboseMsg = new BreakVerbose("Reminder shown. Was working", "maximum");
        tracker.addListener(this);
    }

    @Override
    public void onTrackerLog(Project projectOrNull, EnumerateWindows.WindowInfo windowInfo) {
        final long currentTime = System.currentTimeMillis();
        final BreakCheckResult breakCheckResult = runBreakCheck(currentTime);

        if (!breakCheckResult.wasOnBreak()) {
            final boolean isWorking = runWorkCheck(currentTime, projectOrNull);

            if (isWorking) {
                tryRemind(currentTime, breakCheckResult.breakDuration(), isWorking);
            }
        }
    }

    /**
     * Execute during logging. Checks if the user comes back from a break.
     * @param currentTime the current system time as of the logging in millis.
     * @return whether the user comes back from a break.
     */
    private BreakCheckResult runBreakCheck(long currentTime) {
        final long breakDuration = currentTime - lastLoggedWork;
        final long configuredMinDuration = configuredMinBreakDuration();

        if (breakDuration >= configuredMinDuration) {
            breakVerboseMsg.log(breakDuration, configuredMinDuration);
            lastBreakOrReminder = currentTime;
            return BreakCheckResult.set(true, breakDuration);
        } else {
            return BreakCheckResult.set(false, breakDuration);
        }
    }

    /**
     * Execute during logging. Checks if the user is working.
     * @param currentTime the current system time as of the logging in millis.
     * @param projectOrNull logged project or null.
     * @return whether the user is working.
     */
    private boolean runWorkCheck(long currentTime, Project projectOrNull) {
        final boolean monitorAny = BREAK_ANY_USAGE.get();
        final boolean isProductive = projectOrNull != null && projectOrNull.isProductive();

        if (monitorAny || isProductive) {
            lastLoggedWork = currentTime;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Execute during logging. Attempt to remind the user while they are working.
     * @param currentTime the current system time as of the logging in millis.
     * @param calculatedBreakDuration break duration part of break check result.
     * @param isWorking result of work check.
     */
    private void tryRemind(long currentTime, long calculatedBreakDuration, boolean isWorking) {
        assert isWorking;
        final long workDur = lastLoggedWork - lastBreakOrReminder;
        final long configuredMaxWork = configuredMaxWorkDuration();
        final long configuredMinBreak = configuredMinBreakDuration();

        if (calculatedBreakDuration >= configuredMinBreak / 2L) {
            halfBreakVerboseMsg.log(calculatedBreakDuration, configuredMinBreakDuration());
            lastBreakOrReminder = currentTime;
            trayIcon.displayMessage(HALF_BREAK_MESSAGE.get(), "Project Sherlock Reminder", MessageType.INFO);
            Debug.log(String.format("Half break message was invoked. Break duration: %d s, Configured min: %d s",
                    calculatedBreakDuration, configuredMinBreak));
        } else if (workDur >= configuredMaxWork) {
            workVerboseMsg.log(workDur, configuredMaxWork);
            lastBreakOrReminder = currentTime;
            trayIcon.displayMessage(BREAK_MESSAGE.get(), "Project Sherlock Reminder", MessageType.INFO);
        }
    }

    private long configuredMinBreakDuration() {
        return BREAK_MIN_BREAKDUR.get() * 1000L;
    }

    private long configuredMaxWorkDuration() {
        return BREAK_MAX_WORKDUR.get() * 1000L;
    }

    private static class BreakVerbose implements Supplier<String> {
        private long duration;
        private long configured;
        private final String baseMsg;
        private final String configMsg;

        private BreakVerbose(String baseMsg, String configMsg) {
            this.baseMsg = baseMsg;
            this.configMsg = configMsg;
        }

        public void log(long duration, long configured) {
            this.duration = duration;
            this.configured = configured;
            Debug.logVerbose(this);
        }

        @Override
        public String get() {
            return String.format("%s for %d s. Configured %s is %d s.",
                    baseMsg, (duration / 1000L), configMsg, (configured / 1000L));
        }
    }
}

