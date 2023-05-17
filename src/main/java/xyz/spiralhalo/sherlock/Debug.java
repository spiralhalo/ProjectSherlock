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

import static xyz.spiralhalo.sherlock.util.FormatUtil.DTF_HMS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import xyz.spiralhalo.sherlock.Main.Arg;

public class Debug {

	private static Logger logger;

	public static class CustomLogManager extends LogManager {
		private static CustomLogManager instance;

		public CustomLogManager() {
			super();
			instance = this;
		}

		// Prevents reset during shutdown hook sequence.
		@Override
		public void reset() {
		}

		public void reset0() {
			super.reset();
		}
	}

	private static class CustomFileHandler extends FileHandler {
		private String filename;

		public CustomFileHandler(String pattern) throws IOException, SecurityException {
			super(pattern);
			filename = pattern;
		}

		private void compress() {
			try (FileInputStream fis = new FileInputStream(filename);
				 FileOutputStream fos = new FileOutputStream(gz(filename));
				 GZIPOutputStream gzos = new GZIPOutputStream(fos)) {
				byte[] buffer = new byte[1024];
				int len;

				while ((len = fis.read(buffer)) != -1) {
					gzos.write(buffer, 0, len);
				}

				// if successful, delete original file
				new File(filename).deleteOnExit();
				Debug.log("Log compression successful!");
			} catch (IOException e) {
				Debug.log(e);
			}
		}
	}

	static {
		System.setProperty("java.util.logging.manager", CustomLogManager.class.getName());
	}

	private static String gz(String file) {
		return file + ".gz";
	}

	private static Logger getLogger() {
		if (logger == null) {
			logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
			logger.setLevel(Arg.Verbose.isEnabled() ? Level.ALL : Level.CONFIG);
			logger.setUseParentHandlers(false);
			logger.addHandler(new ConsoleHandler());

			try {
				final DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.systemDefault());
				final String date = f.format(Instant.now());
				final String pattern = Application.getLogDir() + "/sherlock_" + date + "_%d.log";
				final int HARD_LIMIT = Arg.Debug.isEnabled() ? 86_400 : 999;

				int fileCounter = 0;

				for (; fileCounter < HARD_LIMIT; fileCounter++) {
					final String name = String.format(pattern, fileCounter);
					final File testFile = new File(name);
					final File testFileGz = new File(gz(name));

					if (!testFile.exists() && !testFileGz.exists()) {
						break;
					}

					if (fileCounter + 1 == HARD_LIMIT) {
						throw new IOException("Too many log files in one day! Limit: " + HARD_LIMIT);
					}
				}

				logger.addHandler(new CustomFileHandler(String.format(pattern, fileCounter)));
			} catch (IOException e) {
				logger.warning(e.toString());
			}

			final SimplerFormatter formatter = new SimplerFormatter();

			for (Handler h : logger.getHandlers()) {
				h.setFormatter(formatter);
				h.setLevel(logger.getLevel());
			}
		}

		return logger;
	}

	public static void shutdownFinally() {
		for (Handler handler : getLogger().getHandlers()) {
			if (handler instanceof CustomFileHandler) {
				final CustomFileHandler fileHandler = (CustomFileHandler) handler;
				getLogger().removeHandler(fileHandler);
				fileHandler.close();
				fileHandler.compress();
			}
		}

		if (CustomLogManager.instance != null) {
			CustomLogManager.instance.reset0();
		}
	}

	/**
	 * Generic Throwable logging. Redirects to {@link #log(Error)} or {@link #log(Exception)} automatically or
	 * defaults to printing a WARNING log message.
	 *
	 * @param e thrown Throwable.
	 */
	public static void log(Throwable e) {
		if (e instanceof Error) log((Error) e);
		else if (e instanceof Exception) log((Exception) e);
		else getLogger().warning(e::toString);
	}

	/**
	 * Error logging. Prints a SEVERE log message that always appear in any configuration.
	 *
	 * @param e thrown error.
	 */
	public static void log(Error e) {
		final String message = Arg.Debug.isEnabled() ? errorVerbose(e) : e.toString();

		logDebugInner(Level.SEVERE, message, Thread.currentThread().getStackTrace()[2]);
	}

	/**
	 * Exception logging. Prints a WARNING log message that always appear in any configuration.
	 *
	 * @param e thrown exception.
	 */
	public static void log(Exception e) {
		final String message = Arg.Debug.isEnabled() ? errorVerbose(e) : e.toString();

		logDebugInner(Level.WARNING, message, Thread.currentThread().getStackTrace()[2]);
	}

	/**
	 * Fast logging for known events. Logs class and method name when `-debug` command line argument is enabled.
	 *
	 * @param x log message.
	 */
	public static void log(String x) {
		if (Arg.Debug.isEnabled()) {
			logDebugInner(Level.CONFIG, x, Thread.currentThread().getStackTrace()[2]);
		} else {
			getLogger().config(x);
		}
	}

	/**
	 * Slower logging for important events. Class and method names are always logged.
	 *
	 * @param x log message.
	 */
	public static void logImportant(String x) {
		logDebugInner(Level.INFO, x, Thread.currentThread().getStackTrace()[2]);
	}

	/**
	 * Slower logging for known warnings. Class and method names are always logged.
	 *
	 * @param x log message.
	 */
	public static void logWarning(String x) {
		logDebugInner(Level.WARNING, x, Thread.currentThread().getStackTrace()[2]);
	}

	/**
	 * Special case logging for events that happen rapidly and may spam the log. Only prints when `-verbose` command
	 * line argument is enabled.
	 *
	 * @param x log message supplier.
	 */
	public static void logVerbose(Supplier<String> x) {
		getLogger().fine(x);
	}

	private static String errorVerbose(Throwable e) {
		StringBuilder builder = new StringBuilder();
		StackTraceElement[] x = e.getStackTrace();
		for (StackTraceElement y : x) {
			builder.append("\nat ").append(y.toString());
			if (y.getClassName().startsWith("xyz.spiralhalo.sherlock.Main")) break;
		}
		return String.format("%s %s", e.toString(), builder.toString());
	}

	private static void logDebugInner(Level level, String x, StackTraceElement f) {
		String n = f.getClassName();
		getLogger().logp(level, n.substring(n.lastIndexOf('.') + 1), f.getMethodName(), x);
	}

	private static class SimplerFormatter extends Formatter {
		@Override
		public String format(LogRecord r) {
			final int baldMessageMaxLevel = Arg.Debug.isEnabled() ? Level.FINE.intValue() : Level.CONFIG.intValue();
			final String levelName = r.getLevel().getName();

			final String base = String.format("[%s] [%s/%s]",
					DTF_HMS.format(Instant.ofEpochMilli(r.getMillis())),
					Thread.currentThread().getName(),
					levelName.substring(0, Math.min(4, levelName.length())));

			if (r.getLevel().intValue() <= baldMessageMaxLevel) {
				return String.format("%s: %s\n", base, r.getMessage());
			} else {
				return String.format("%s (%s.%s): %s\n",
						base,
						r.getSourceClassName(),
						r.getSourceMethodName(),
						r.getMessage());
			}
		}
	}
}
