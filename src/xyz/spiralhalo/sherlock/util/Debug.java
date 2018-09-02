package xyz.spiralhalo.sherlock.util;

import xyz.spiralhalo.sherlock.Main.Arg;

import java.time.Instant;
import java.util.function.Supplier;

public class Debug {

    @Deprecated
    public static void log(Class caller, Throwable e){
        if(Arg.Debug.isEnabled()){
            e.printStackTrace();
        }else{
            StackTraceElement[] x = e.getStackTrace();
            int i = 0;
            for (i=0;i<x.length;i++) {
                if (x[i].toString().startsWith("xyz.spiralhalo")) break;
            }
            System.err.println(String.format("%s: %s at %s",caller.getCanonicalName(), e.toString(), x[i].toString()));
        }
    }

    public static void log(Throwable e){
        if(Arg.Debug.isEnabled()){
            e.printStackTrace();
        }else{
            StackTraceElement[] x = e.getStackTrace();
            int i = 0;
            for (i=0;i<x.length;i++) {
                if (x[i].toString().startsWith("xyz.spiralhalo")) break;
            }
            System.err.println(String.format("%15s %s: %s at %s", "error", FormatUtil.DTF_FULL.format(Instant.now()), e.toString(), x[i].toString()));
        }
    }

    public static void log(String x){
        if(Arg.Debug.isEnabled()) {
            System.out.println(String.format("%15s %s: %s", "debug", FormatUtil.DTF_FULL.format(Instant.now()), x));
        }
    }

    public static void logImportant(String x){
        if(Arg.Debug.isEnabled()) {
            System.out.println(String.format("%15s %s: %s", "important", FormatUtil.DTF_FULL.format(Instant.now()), x));
        }
    }

    public static void logVerbose(String x){
        if(Arg.Debug.isEnabled() && Arg.Verbose.isEnabled()) {
            System.out.println(String.format("%15s %s: %s", "verbose", FormatUtil.DTF_FULL.format(Instant.now()), x));
        }
    }
}