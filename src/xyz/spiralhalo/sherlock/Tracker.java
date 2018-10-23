package xyz.spiralhalo.sherlock;

import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;
import lc.kra.system.mouse.event.GlobalMouseAdapter;
import lc.kra.system.mouse.event.GlobalMouseEvent;
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

import static xyz.spiralhalo.sherlock.GlobalInputHook.GLOBAL_KEYBOARD_HOOK;
import static xyz.spiralhalo.sherlock.GlobalInputHook.GLOBAL_MOUSE_HOOK;

public class Tracker implements TrackerAccessor{
    public static final String SPLIT_DIVIDER = "::";
    public static final DateTimeFormatter DTF = FormatUtil.DTF_FULL;

    public static final int ONE_SECOND = 1000;
    public static final int TIMER_DELAY_SECONDS = 1;
    public static final int TIMER_DELAY_MILLIS = TIMER_DELAY_SECONDS*ONE_SECOND;

    private final AFKMonitor afkMonitor;
    private final List<TrackerListener> listeners;
    private long last;
    private String[] tempa;
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
            tempa = EnumerateWindows.getActiveWindowTitle();
            lastTracked = projectList.getActiveProjectOf(tempa[0], now);
            Debug.logVerbose(()->String.format("%18s %s", "[ForegroundWindow]", tempa[0]));
            if(lastTracked==null) {
                tempa = EnumerateWindows.getRootWindowTitle();
                lastTracked = projectList.getActiveProjectOf(tempa[0], now);
                Debug.logVerbose(()->String.format("%18s %s", "[GW_OWNER]", tempa[0]));
            }
            final String pn = String.valueOf(lastTracked);
            Debug.logVerbose(()->String.format("%18s Detected project: %s", "", pn));
            buffer.log(lastTracked);
            for(TrackerListener listener:listeners){
                listener.onLog(lastTracked, tempa[0], tempa[1]);
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
        private class MouseAdapter extends GlobalMouseAdapter {
            @Override public void mousePressed(GlobalMouseEvent event) { logNow(); mousePressed = true; }
            @Override public void mouseReleased(GlobalMouseEvent event) { logNow(); mousePressed = false; }
            @Override public void mouseMoved(GlobalMouseEvent event) { logNow(); }
            @Override public void mouseWheel(GlobalMouseEvent event) { logNow(); }
        }

        private class KeyAdapter extends GlobalKeyAdapter {
            @Override public void keyPressed(GlobalKeyEvent event) { logNow(); keyPressed = true; }
            @Override public void keyReleased(GlobalKeyEvent event) { logNow(); keyPressed = false; }
        }

        private void logNow(){ lastInput = System.currentTimeMillis(); }

        private long lastInput;
        private boolean keyPressed;
        private boolean mousePressed;

        AFKMonitor() {
            GLOBAL_KEYBOARD_HOOK.addKeyListener(new KeyAdapter());
            GLOBAL_MOUSE_HOOK.addMouseListener(new MouseAdapter());
        }

        boolean isNotAFK() {
            return keyPressed || mousePressed || (System.currentTimeMillis() - lastInput <
                    UserConfig.userGInt(UserConfig.UserNode.TRACKING, UserConfig.UserInt.AFK_TIMEOUT_SECOND) * 1000);
        }
    }
}
