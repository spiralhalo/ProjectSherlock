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

import static xyz.spiralhalo.sherlock.util.FormatUtil.DTF_DATE_SELECTOR;
import static xyz.spiralhalo.sherlock.util.FormatUtil.DTF_HMS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.ErrorManager;
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

	public static Logger LOG;

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

		public CustomFileHandler(String pattern, Level level) throws IOException, SecurityException {
			super(pattern);
			filename = pattern;
			setLevel(level);
			setFormatter(SIMPLE_FORMATTER);
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
				Debug.LOG.info("Log compression successful!");
			} catch (IOException e) {
				Debug.log(e);
			}
		}
	}

	private static class CustomConsoleHandler extends Handler {
		private Writer outWriter;
		private Writer errWriter;
		private boolean doneHeader = false;

		public CustomConsoleHandler(Level level) {
			outWriter = new OutputStreamWriter(System.out);
			errWriter = new OutputStreamWriter(System.err);
			setLevel(level);
			setFormatter(SIMPLE_FORMATTER);
		}

		private void handleHeader() throws IOException {
			if (!doneHeader) {
				outWriter.write(getFormatter().getHead(this));
				outWriter.flush();
				doneHeader = true;
			}
		}

		@Override
		public void publish(LogRecord record) {
			if (!isLoggable(record)) {
				return;
			}

			String msg;

			try {
				msg = getFormatter().format(record);
			} catch (Exception ex) {
				reportError(null, ex, ErrorManager.FORMAT_FAILURE);
				return;
			}

			try {
				handleHeader();

				if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
					errWriter.write(msg);
					errWriter.flush();
				} else {
					outWriter.write(msg);
					outWriter.flush();
				}
			} catch (Exception ex) {
				reportError(null, ex, ErrorManager.WRITE_FAILURE);
			}
		}

		@Override
		public void flush() {
			try {
				errWriter.flush();
			} catch (Exception ex) {
				reportError(null, ex, ErrorManager.FLUSH_FAILURE);
			}

			try {
				outWriter.flush();
			} catch (Exception ex) {
				reportError(null, ex, ErrorManager.FLUSH_FAILURE);
			}
		}

		@Override
		public void close() throws SecurityException {
			try {
				handleHeader();
				errWriter.flush();
				outWriter.write(getFormatter().getTail(this));
				outWriter.flush();
				// IMPORTANT: we don't close System.out, System.err
				errWriter = null;
				outWriter = null;
			} catch (Exception ex) {
				reportError(null, ex, ErrorManager.CLOSE_FAILURE);
			}
		}
	}

	static {
		System.setProperty("java.util.logging.manager", CustomLogManager.class.getName());
	}

	private static String gz(String file) {
		return file + ".gz";
	}

	private static final Formatter SIMPLE_FORMATTER = new Formatter() {
		@Override
		public String getHead(Handler h) {
			return String.format("%s\nToday is %s\n\n", Main.APP_TITLE, DTF_DATE_SELECTOR.format(LocalDate.now()));
		}

		@Override
		public String getTail(Handler h) {
			return "\n" + "App exited successfully!\n";
		}

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
				final String className = r.getSourceClassName();
				return String.format("%s (%s.%s): %s\n",
						base,
						className.substring(className.lastIndexOf('.') + 1),
						r.getSourceMethodName(),
						r.getMessage());
			}
		}
	};

	static {
		LOG = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

		LOG.setLevel(Arg.Verbose.isEnabled() ? Level.ALL : Level.CONFIG);
		LOG.setUseParentHandlers(false);

		LOG.addHandler(new CustomConsoleHandler(LOG.getLevel()));

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

			final String logPath = String.format(pattern, fileCounter);
			LOG.addHandler(new CustomFileHandler(logPath, LOG.getLevel()));
		} catch (IOException e) {
			LOG.warning(e.toString());
		}
	}

	public static void shutdownFinally() {
		for (Handler handler : LOG.getHandlers()) {
			if (handler instanceof CustomFileHandler) {
				final CustomFileHandler fileHandler = (CustomFileHandler) handler;
				LOG.removeHandler(fileHandler);
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
		if (e instanceof Error) {
			log((Error) e);
		} else if (e instanceof Exception) {
			log((Exception) e);
		} else {
			LOG.warning(e::toString);
		}
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

	private static String errorVerbose(Throwable e) {
		StringBuilder builder = new StringBuilder();
		StackTraceElement[] x = e.getStackTrace();

		for (StackTraceElement y : x) {
			builder.append("\nat ").append(y.toString());
			if (y.getClassName().startsWith("xyz.spiralhalo.sherlock.Main")) break;
		}

		return String.format("%s %s", e, builder);
	}

	private static void logDebugInner(Level level, String x, StackTraceElement f) {
		String n = f.getClassName();
		LOG.logp(level, n.substring(n.lastIndexOf('.') + 1), f.getMethodName(), x);
	}
}
