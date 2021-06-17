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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;
import java.util.logging.*;

import static xyz.spiralhalo.sherlock.util.FormatUtil.DTF_FULL;

public class Debug {

    private static Logger logger;

    public static class CustomLogManager extends LogManager {
        // Prevents reset during shutdown hook sequence. Let the OS handles it.
        @Override public void reset() { }
    }

    static {
        System.setProperty("java.util.logging.manager", CustomLogManager.class.getName());
    }

    private static Logger getLogger() {
        if (logger == null) {
            final Logger globalLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
            final ConsoleHandler consoleHandler = new ConsoleHandler();

            consoleHandler.setFormatter(new SimplerFormatter());

            globalLogger.addHandler(consoleHandler);
            globalLogger.setUseParentHandlers(false);

            try {
                final DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.systemDefault());
                final String pattern = Application.getLogDir() + "/sherlock%g_%u_" + f.format(Instant.now()) + ".log";
                final FileHandler logFileHandler = new FileHandler(pattern);

                logFileHandler.setFormatter(new SimplerFormatter());
                globalLogger.addHandler(logFileHandler);
            } catch (IOException e) {
                globalLogger.warning(e.toString());
            }

            if (Arg.Verbose.isEnabled()) {
                globalLogger.setLevel(Level.ALL);
            } else {
                globalLogger.setLevel(Level.CONFIG);
            }

            logger = globalLogger;
        }
        return logger;
    }

    /**
     * Generic Throwable logging. Redirects to {@link #log(Error)} or {@link #log(Exception)} automatically or
     * defaults to printing a WARNING log message.
     * @param e thrown Throwable.
     */
    public static void log(Throwable e) {
        if (e instanceof Error) log((Error) e);
        else if (e instanceof Exception) log((Exception) e);
        else getLogger().warning(e::toString);
    }

    /**
     * Error logging. Prints a SEVERE log message that always appear in any configuration.
     * @param e thrown error.
     */
    public static void log(Error e){
        final String message = Arg.Debug.isEnabled() ? errorVerbose(e) : e.toString();

        logDebugInner(Level.SEVERE, message, Thread.currentThread().getStackTrace()[2]);
    }

    /**
     * Exception logging. Prints a WARNING log message that always appear in any configuration.
     * @param e thrown exception.
     */
    public static void log(Exception e){
        final String message = Arg.Debug.isEnabled() ? errorVerbose(e) : e.toString();

        logDebugInner(Level.WARNING, message, Thread.currentThread().getStackTrace()[2]);
    }

    /**
     * Fast logging for known events. Logs class and method name when `-debug` command line argument is enabled.
     * @param x log message.
     */
    public static void log(String x){
        if (Arg.Debug.isEnabled()) {
            logDebugInner(Level.CONFIG, x, Thread.currentThread().getStackTrace()[2]);
        } else {
            logEventInner(Level.CONFIG, x);
        }
    }

    /**
     * Slower logging for important events. Class and method names are always logged.
     * @param x log message.
     */
    public static void logImportant(String x){
        logDebugInner(Level.INFO, x, Thread.currentThread().getStackTrace()[2]);
    }

    /**
     * Slower logging for known warnings. Class and method names are always logged.
     * @param x log message.
     */
    public static void logWarning(String x){
        logDebugInner(Level.WARNING, x, Thread.currentThread().getStackTrace()[2]);
    }

    /**
     * Special case logging for events that happen rapidly and may spam the log. Only prints when `-verbose` command
     * line argument is enabled.
     * @param x log message supplier.
     */
    public static void logVerbose(Supplier<String> x){
        getLogger().fine(x);
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

    private static void logEventInner(Level level, String x){
        getLogger().log(level, x);
    }

    private static void logDebugInner(Level level, String x, StackTraceElement f){
        String n = f.getClassName();
        getLogger().logp(level, n.substring(n.lastIndexOf('.') + 1), f.getMethodName(), x);
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