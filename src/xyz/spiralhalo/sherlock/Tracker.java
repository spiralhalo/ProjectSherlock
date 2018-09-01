package xyz.spiralhalo.sherlock;

import xyz.spiralhalo.sherlock.Main.Arg;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.util.Debug;
import xyz.spiralhalo.sherlock.util.FormatUtil;
import xyz.spiralhalo.sherlock.util.PathUtil;

import javax.swing.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.TimerTask;

public class Tracker {
    public static final String LOG_FILENAME = "record2.txt";
    public static final String SPLIT_DIVIDER = "::";
    public static final DateTimeFormatter DTF = FormatUtil.DTF_FULL;

    public static final int ONE_SECOND = 1000;
    public static final int TIMER_DELAY_SECONDS = 1;
    public static final int TIMER_DELAY_MILLIS = TIMER_DELAY_SECONDS*ONE_SECOND;
    public static final int FLUSH_DELAY_MILLIS = TIMER_DELAY_MILLIS*200;

    private AFKMonitor afkMonitor;
    private long last;
    private String temps;
    private Timer timer;
    private ProjectList projectList;
    private RecordBuffer buffer;

    public Tracker(ProjectList projectList){
        afkMonitor = new AFKMonitor();
        this.projectList = projectList;
        if(Arg.Sandbox.isEnabled()) {
            Debug.log("[Sandbox mode] Tracking has been set to mode hiatus.");
        } else {
            Runtime.getRuntime().addShutdownHook(new Thread(this::exit));
        }
    }

    public static String getRecordFile(){
        return PathUtil.getSaveDir()+ LOG_FILENAME;
    }

    public void start(){
        if(Arg.Sandbox.isEnabled()) {
            Debug.log("[Sandbox mode] Starting tracker has been cancelled.");
        } else {
            buffer = new RecordBuffer();
            int seconds = LocalDateTime.now().get(ChronoField.SECOND_OF_MINUTE);
            timer = new Timer(TIMER_DELAY_MILLIS, e -> log(e.getWhen()));
            new java.util.Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Debug.log("Tracker is started");
                    last = System.currentTimeMillis();
                    timer.start();
                }
            }, (seconds % TIMER_DELAY_SECONDS) * ONE_SECOND);
        }
    }

    private void exit() {
        Debug.log("Terminating tracker");
        Debug.log("Logging final entry");
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
            Debug.log("[ForegroundWindow] "+temps);
            if(p==null) {
                temps = EnumerateWindows.getRootWindowTitle();
                p = projectList.getActiveProjectOf(temps, now);
                Debug.log("[GW_OWNER] "+temps);
            }
            Debug.log("Detected project: "+p);
            buffer.log(p, time - last);
        }
        last = time;
    }
}
