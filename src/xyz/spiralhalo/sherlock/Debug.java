package xyz.spiralhalo.sherlock;

import xyz.spiralhalo.sherlock.Main.Arg;

import java.time.Instant;

import static xyz.spiralhalo.sherlock.util.FormatUtil.DTF_FULL;

public class Debug {

    public static void log(Throwable e){
        if(Arg.Debug.isEnabled()){
            StackTraceElement[] x = e.getStackTrace();
            int i = 0;
            for (i=0;i<x.length;i++) {
                if (x[i].toString().startsWith("xyz.spiralhalo")) break;
            }
            System.err.println(String.format("%15s %s: %s at %s", "error", DTF_FULL.format(Instant.now()), e.toString(), x[i].toString()));
        }
    }

    public static void log(String x){
        if(Arg.Debug.isEnabled()) {
            System.out.println(String.format("%15s %s: %s", "debug", DTF_FULL.format(Instant.now()), x));
        }
    }

    public static void logImportant(String x){
        if(Arg.Debug.isEnabled()) {
            System.out.println(String.format("%15s %s: %s", "important", DTF_FULL.format(Instant.now()), x));
        }
    }

    public static void logVerbose(String x){
        if(Arg.Debug.isEnabled() && Arg.Verbose.isEnabled()) {
            System.out.println(String.format("%15s %s: %s", "verbose", DTF_FULL.format(Instant.now()), x));
        }
    }
}