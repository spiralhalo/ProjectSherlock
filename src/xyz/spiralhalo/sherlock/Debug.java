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

import xyz.spiralhalo.sherlock.Main.Arg;

import java.io.IOException;
import java.time.Instant;
import java.util.function.Supplier;
import java.util.logging.*;

import static xyz.spiralhalo.sherlock.util.FormatUtil.DTF_FULL;

public class Debug {

    private static Logger logger;

    public static class CustomLogManager extends LogManager {
        @Override public void reset() { } // prevent reset on shutdown
    }

    static {
        System.setProperty("java.util.logging.manager", CustomLogManager.class.getName());
    }

    private static Logger getLogger(){
        if (logger == null) {
            logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            if(Arg.Debug.isEnabled()) {
                if (Arg.Verbose.isEnabled()) {
                    logger.setLevel(Level.ALL);
                } else {
                    logger.setLevel(Level.CONFIG);
                }
            } else {
                logger.setLevel(Level.WARNING);
            }
            try {
                FileHandler logFile = new FileHandler("%t/sherlock%g_%u.log");
                logFile.setFormatter(new SimplerFormatter());
                logger.addHandler(logFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ConsoleHandler console = new ConsoleHandler();
            if(Arg.Console.isEnabled()) {
                console.setLevel(Level.ALL);
            }
            console.setFormatter(new SimplerFormatter());
            logger.addHandler(console);
            logger.setUseParentHandlers(false);
        }
        return logger;
    }

    private static String errorVerbose(Throwable e){
        StringBuilder builder = new StringBuilder();
        StackTraceElement[] x = e.getStackTrace();
        for (StackTraceElement y:x) {
            builder.append("\nat ").append(y.toString());
            if(y.getClassName().startsWith("xyz.spiralhalo.sherlock.Main")) break;
        }
        return String.format("%s %s", e.toString(), builder.toString());
    }

    public static void log(Throwable e) {
        if (e instanceof Error) log((Error) e);
        else if (e instanceof Exception) log((Exception) e);
        else getLogger().warning(e::toString);
    }

    public static void log(Error e){
        if (Arg.Debug.isEnabled()) log(Level.SEVERE, errorVerbose(e), Thread.currentThread().getStackTrace()[2]);
        else log(Level.SEVERE, e.toString(), Thread.currentThread().getStackTrace()[2]);
    }

    public static void log(Exception e){
        if (Arg.Debug.isEnabled()) log(Level.WARNING, errorVerbose(e), Thread.currentThread().getStackTrace()[2]);
        else log(Level.WARNING, e.toString(), Thread.currentThread().getStackTrace()[2]);
    }

    public static void log(String x){
        log(Level.CONFIG, x, Thread.currentThread().getStackTrace()[2]);
    }

    public static void logImportant(String x){
        log(Level.INFO, x, Thread.currentThread().getStackTrace()[2]);
    }

    private static void log(Level level, String x, StackTraceElement f){
        String n = f.getClassName();
        getLogger().logp(level, n.substring(n.lastIndexOf('.') + 1), f.getMethodName(), x);
    }

    public static void logVerbose(Supplier<String> x){
        getLogger().fine(x);
    }

    private static class SimplerFormatter extends Formatter{
        @Override
        public String format(LogRecord r) {
            if(r.getLevel().equals(Level.FINE)){
                return String.format("%7s %s: %s\n", r.getLevel().getName(),
                        DTF_FULL.format(Instant.ofEpochMilli(r.getMillis())), r.getMessage());
            } else {
                return String.format("%7s %s: %s %s: %s\n", r.getLevel().getName(),
                        DTF_FULL.format(Instant.ofEpochMilli(r.getMillis())),
                        r.getSourceClassName(), r.getSourceMethodName(), r.getMessage());
            }
        }
    }
}