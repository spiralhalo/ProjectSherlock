package xyz.spiralhalo.sherlock.util;

public class Debug {
    private static final boolean DEBUG = false;
    public static void log(Class caller, Throwable e){
        if(DEBUG){
            e.printStackTrace();
        }else{
            StackTraceElement[] x = e.getStackTrace();
            System.out.println(String.format("%s: %s at %s",caller.getCanonicalName(), e.getMessage(), x[x.length-1].toString()));
        }
    }
}