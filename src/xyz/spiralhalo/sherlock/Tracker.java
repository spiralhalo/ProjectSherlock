package xyz.spiralhalo.sherlock;

import com.sun.jna.platform.win32.*;
import xyz.spiralhalo.sherlock.EnumerateWindows.WindowInfo;
import xyz.spiralhalo.sherlock.Main.Arg;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig;
import xyz.spiralhalo.sherlock.record.RealtimeRecordWriter;
import xyz.spiralhalo.sherlock.util.FormatUtil;

import javax.swing.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

public class Tracker implements TrackerAccessor{
    public static final String SPLIT_DIVIDER = "::";
    public static final DateTimeFormatter DTF = FormatUtil.DTF_FULL;

    public static final int ONE_SECOND = 1000;
    public static final int TIMER_DELAY_SECONDS = 1;
    public static final int TIMER_DELAY_MILLIS = TIMER_DELAY_SECONDS*ONE_SECOND;

    private final AFKMonitor afkMonitor;
    private final List<TrackerListener> listeners;
    private long last;
    private WindowInfo tempa;
    private Timer timer;
    private ProjectList projectList;
    private RealtimeRecordWriter buffer;
    private Project lastTracked;

    public Tracker(ProjectList projectList){
        afkMonitor = new AFKMonitor();
        listeners = new LinkedList<>();
        this.projectList = projectList;
        if(Arg.Sandbox.isEnabled()) {
            Debug.logImportant("[Sandbox mode] Tracking has been set to mode hiatus.");
        } else {
            Runtime.getRuntime().addShutdownHook(new Thread(this::exit));
        }
    }

    public void start(){
        if(Arg.Sandbox.isEnabled()) {
            Debug.logImportant("[Sandbox mode] Starting tracker has been cancelled.");
        } else {
            buffer = new RealtimeRecordWriter();
            int seconds = LocalDateTime.now().get(ChronoField.SECOND_OF_MINUTE);
            timer = new Timer(TIMER_DELAY_MILLIS, e -> log(e.getWhen()));
            new java.util.Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Debug.logImportant("Tracker is started");
                    last = System.currentTimeMillis();
                    timer.start();
                }
            }, (seconds % TIMER_DELAY_SECONDS) * ONE_SECOND);
        }
    }

    private void exit() {
        Debug.logImportant("Terminating tracker");
        Debug.logImportant("Logging final entry");
        log(System.currentTimeMillis());
        try {
            buffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void log(long time){
        if(afkMonitor.isNotAFK()) {
            final ZonedDateTime now = ZonedDateTime.now();
            tempa = EnumerateWindows.getActiveWindowInfo();
            lastTracked = projectList.getActiveProjectOf(tempa.title, tempa.exeName, now);
            Debug.logVerbose(() -> String.format("%18s %s", "[ForegroundWindow]", tempa.title));
            if (lastTracked == null) {
                WinDef.HWND activeHwnd = tempa.hwndPointer;
                tempa = EnumerateWindows.getRootWindowInfo(activeHwnd);
                lastTracked = projectList.getActiveProjectOf(tempa.title, tempa.exeName, now);
                Debug.logVerbose(() -> String.format("%18s %s", "[GW_OWNER]", tempa.title));
            }
            final String pn = String.valueOf(lastTracked);
            Debug.logVerbose(() -> String.format("%18s Detected project: %s", "", pn));
            buffer.log(lastTracked);
            for(TrackerListener listener:listeners){
                listener.onTrackerLog(lastTracked, tempa);
            }
        }
        last = time;
    }

    public void flushRecordBuffer() {
        if(Arg.Sandbox.isEnabled()) {
            Debug.logImportant("[Sandbox mode] Flushing buffer has been cancelled.");
        } else {
            buffer.flushBuffer();
        }
    }

    @Override
    public void addListener(TrackerListener listener) {
        listeners.add(listener);
    }

    @Override
    public long getGranularityMillis() {
        return TIMER_DELAY_MILLIS;
    }

    private static class AFKMonitor {

        boolean isNotAFK() {
            return  getIdleTimeMillisWin32() <
                    UserConfig.userGInt(UserConfig.UserNode.GENERAL, UserConfig.UserInt.AFK_TIMEOUT_SECOND) * 1000;
        }

        static int getIdleTimeMillisWin32() {
            User32.LASTINPUTINFO lastInputInfo = new User32.LASTINPUTINFO();
            User32.INSTANCE.GetLastInputInfo(lastInputInfo);
            return Kernel32.INSTANCE.GetTickCount() - lastInputInfo.dwTime;
        }
    }
}
