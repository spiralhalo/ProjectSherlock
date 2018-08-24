package xyz.spiralhalo.sherlock;

import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.util.FormatUtil;
import xyz.spiralhalo.sherlock.util.PathUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.TimerTask;

public class Tracker implements ActionListener {
    public static final String LOG_FILENAME = "record2.txt";
    public static final String SPLIT_DIVIDER = "::";
    public static final DateTimeFormatter DTF = FormatUtil.DTF_FULL;

    public static final int ONE_SECOND = 1000;
    public static final int TIMER_DELAY_SECONDS = 5;
    public static final int TIMER_DELAY_MILLIS = TIMER_DELAY_SECONDS*ONE_SECOND;
    public static final int FLUSH_DELAY_MILLIS = TIMER_DELAY_MILLIS*40;

    private AFKMonitor afkMonitor;
    private long last;
    private String temps;
    private Timer timer;
    private ProjectList projectList;
    private RecordBuffer buffer;

    public Tracker(ProjectList projectList){
        afkMonitor = new AFKMonitor();
        this.projectList = projectList;
        Runtime.getRuntime().addShutdownHook(new Thread(this::exit));
    }

    public static String getRecordFile(){
        return PathUtil.getSaveDir()+ LOG_FILENAME;
    }

    public void start(){
        buffer = new RecordBuffer();
        int seconds = LocalDateTime.now().get(ChronoField.SECOND_OF_MINUTE);
        timer = new Timer(TIMER_DELAY_MILLIS, this);
        new java.util.Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                last = System.currentTimeMillis();
                timer.start();
            }
        },(seconds%TIMER_DELAY_SECONDS)*ONE_SECOND);
    }

    private void exit() {
        System.out.println("Exiting tracker");
        try {
            buffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(afkMonitor.isNotAFK() && projectList.getActiveProjects().size()>0) {
            temps = EnumerateWindows.getActiveWindowTitle();
            if(temps.length()!=0) {
                buffer.log(projectList.getProjectOf(temps, ZonedDateTime.now()), e.getWhen() - last);
            }
        }
        last = e.getWhen();
    }
}
