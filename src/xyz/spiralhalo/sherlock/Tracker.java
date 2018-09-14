package xyz.spiralhalo.sherlock;

import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;
import lc.kra.system.mouse.GlobalMouseHook;
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
import java.util.TimerTask;

public class Tracker {
    public static final String SPLIT_DIVIDER = "::";
    public static final DateTimeFormatter DTF = FormatUtil.DTF_FULL;

    public static final int ONE_SECOND = 1000;
    public static final int TIMER_DELAY_SECONDS = 1;
    public static final int TIMER_DELAY_MILLIS = TIMER_DELAY_SECONDS*ONE_SECOND;

    private AFKMonitor afkMonitor;
    private long last;
    private String temps;
    private Timer timer;
    private ProjectList projectList;
    private RealtimeRecordWriter buffer;

    public Tracker(ProjectList projectList){
        afkMonitor = new AFKMonitor();
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
        if(afkMonitor.isNotAFK() && projectList.getActiveSize()>0) {
            final ZonedDateTime now = ZonedDateTime.now();
            temps = EnumerateWindows.getActiveWindowTitle();
            Project p = projectList.getActiveProjectOf(temps, now);
            Debug.logVerbose(String.format("%18s %s", "[ForegroundWindow]", temps));
            if(p==null) {
                temps = EnumerateWindows.getRootWindowTitle();
                p = projectList.getActiveProjectOf(temps, now);
                Debug.logVerbose(String.format("%18s %s", "[GW_OWNER]", temps));
            }
            final String pn = String.valueOf(p);
            Debug.logVerbose(String.format("%18s Detected project: %s", "", pn));
            buffer.log(p);
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

        private GlobalKeyboardHook keyboardHook;
        private GlobalMouseHook mouseHook;

        private long lastInput;
        private boolean keyPressed;
        private boolean mousePressed;

        AFKMonitor() {
            keyboardHook = new GlobalKeyboardHook(false);
            mouseHook = new GlobalMouseHook(false);
            keyboardHook.addKeyListener(new KeyAdapter());
            mouseHook.addMouseListener(new MouseAdapter());
        }

        boolean isNotAFK() {
            return keyPressed || mousePressed || (System.currentTimeMillis() - lastInput <
                    UserConfig.getInt(UserConfig.UserNode.TRACKING, UserConfig.UserInt.AFK_TIMEOUT_SECOND) * 1000);
        }
    }
}
