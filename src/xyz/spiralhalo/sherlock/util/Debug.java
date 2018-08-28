package xyz.spiralhalo.sherlock.util;

import xyz.spiralhalo.sherlock.Main;

import java.time.Instant;

public class Debug {

    public static void log(Class caller, Throwable e){
        if(Main.programArgs.debug){
            e.printStackTrace();
        }else{
            StackTraceElement[] x = e.getStackTrace();
            int i = 0;
            for (i=0;i<x.length;i++) {
                if (x[i].toString().startsWith("xyz.spiralhalo")) break;
            }
            System.out.println(String.format("%s: %s at %s",caller.getCanonicalName(), e.toString(), x[i].toString()));
        }
    }

    public static void log(Throwable e){
        if(Main.programArgs.debug){
            e.printStackTrace();
        }else{
            StackTraceElement[] x = e.getStackTrace();
            int i = 0;
            for (i=0;i<x.length;i++) {
                if (x[i].toString().startsWith("xyz.spiralhalo")) break;
            }
            System.out.println(String.format("%s: %s at %s",FormatUtil.DTF_FULL.format(Instant.now()), e.toString(), x[i].toString()));
        }
    }

    public static void log(String x){
        if(Main.programArgs.debug) {
            System.out.println(String.format("%s: %s", FormatUtil.DTF_FULL.format(Instant.now()), x));
        }
    }
}