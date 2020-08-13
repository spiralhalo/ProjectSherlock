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

import xyz.spiralhalo.sherlock.EnumerateWindows;
import xyz.spiralhalo.sherlock.TrackerAccessor;
import xyz.spiralhalo.sherlock.TrackerListener;
import xyz.spiralhalo.sherlock.persist.project.Project;

import java.awt.*;
import java.awt.TrayIcon.MessageType;

import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserBool.BREAK_ANY_USAGE;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserInt.BREAK_MAX_WORKDUR;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserInt.BREAK_MIN_BREAKDUR;
import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserStr.BREAK_MESSAGE;

public class BreakReminder implements TrackerListener {

    private long lastBreak;
    private long lastWork;
    private TrayIcon trayIcon;

    public BreakReminder(TrackerAccessor tracker, TrayIcon trayIcon) {
        lastWork = lastBreak = System.currentTimeMillis();
        this.trayIcon = trayIcon;
        tracker.addListener(this);
    }

    @Override
    public void onTrackerLog(Project projectOrNull, EnumerateWindows.WindowInfo windowInfo) {
        long current = System.currentTimeMillis();
        long breakDur = current - lastWork;
        if(breakDur >= BREAK_MIN_BREAKDUR.get() * 1000){
            lastBreak = current;
        }
        if (BREAK_ANY_USAGE.get() || (projectOrNull != null && projectOrNull.isProductive())) {
            lastWork = System.currentTimeMillis();
        }
        long workDur = lastWork - lastBreak;
        if (workDur >= BREAK_MAX_WORKDUR.get() * 1000) {
            lastBreak = current;
            trayIcon.displayMessage(BREAK_MESSAGE.get(), "Project Sherlock Reminder", MessageType.INFO);
        }
//        Debug.log("break:"+(breakDur/1000)+",work:"+(workDur/1000));
    }
}

