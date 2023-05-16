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

package xyz.spiralhalo.sherlock;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import xyz.spiralhalo.sherlock.EnumerateWindows.WindowInfo;
import xyz.spiralhalo.sherlock.Main.Arg;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.record.RealtimeRecordWriter;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

import static xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserInt.AFK_TIMEOUT_SECOND;

class Tracker implements TrackerAccessor{

    private final AFKMonitor afkMonitor;
    private final List<TrackerListener> listeners;
    private WindowInfo tempa;
    private boolean running;
    private ProjectList projectList;
    private RealtimeRecordWriter recordWriter;

    Tracker(ProjectList projectList){
        afkMonitor = new AFKMonitor();
        listeners = new LinkedList<>();
        this.projectList = projectList;
        if(Arg.Sandbox.isEnabled()) {
            Debug.logImportant("[Sandbox mode] Tracking has been set to mode hiatus.");
        } else {
            Application.addShutdownHook(this::exit, "TrackerShutdownHook");
        }
    }

    void start(){
        if(Arg.Sandbox.isEnabled()) {
            Debug.logImportant("[Sandbox mode] Starting tracker has been cancelled.");
        } else {
            recordWriter = new RealtimeRecordWriter();
            Runnable toRun = () -> {
                threadSleep();
                Debug.logImportant("Tracker is started");
                running = true;
                while(running) {
                    log(); // LE IMPORTANT
                    threadSleep(); //another LE IMPORTANT
                }
            };
            new Thread(toRun, "TrackerThread").start();
        }
    }

    private static void threadSleep() {
        try {
            Thread.sleep(GConst.TRACKER_DELAY_MILLIS-(System.currentTimeMillis() % GConst.TRACKER_DELAY_MILLIS));
        } catch (InterruptedException e) {
            Debug.log(e);
        }
    }

    private void exit() {
        running = false;
        Debug.logImportant("Terminating tracker");
        Debug.logImportant("Logging final entry");
        log();
        try {
            recordWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void log(){
        if(afkMonitor.isNotAFK()) {
            final ZonedDateTime now = ZonedDateTime.now();
            tempa = EnumerateWindows.getActiveWindowInfo();
            Project tracked = projectList.getActiveProjectOf(tempa.title, tempa.exeName, now);
            Debug.logVerbose(() -> String.format("%18s %s", "[ForegroundWindow]", tempa.title));
            if (tracked == null) {
                WinDef.HWND activeHwnd = tempa.hwndPointer;
                tempa = EnumerateWindows.getRootWindowInfo(activeHwnd);
                tracked = projectList.getActiveProjectOf(tempa.title, tempa.exeName, now);
                Debug.logVerbose(() -> String.format("%18s %s", "[GW_OWNER]", tempa.title));
            }
            final String pn = String.valueOf(tracked);
            Debug.logVerbose(() -> String.format("%18s Detected project: %s", "", pn));
            recordWriter.log(tracked);
            for(TrackerListener listener:listeners){
                listener.onTrackerLog(tracked, tempa);
            }
        }
    }

    void flushRecordBuffer() {
        if(Arg.Sandbox.isEnabled()) {
            Debug.logImportant("[Sandbox mode] Flushing buffer has been cancelled.");
        } else {
            recordWriter.flushBuffer();
        }
    }

    @Override
    public void addListener(TrackerListener listener) {
        listeners.add(listener);
    }

    @Override
    public long getGranularityMillis() {
        return GConst.TRACKER_DELAY_MILLIS;
    }

    private static class AFKMonitor {
        boolean isNotAFK() {
            return getIdleTimeMillisWin32() < AFK_TIMEOUT_SECOND.get() * 1000;
        }

        static int getIdleTimeMillisWin32() {
            final User32.LASTINPUTINFO lastInputInfo = new User32.LASTINPUTINFO();
            User32.INSTANCE.GetLastInputInfo(lastInputInfo);
            return Kernel32.INSTANCE.GetTickCount() - lastInputInfo.dwTime;
        }
    }
}
