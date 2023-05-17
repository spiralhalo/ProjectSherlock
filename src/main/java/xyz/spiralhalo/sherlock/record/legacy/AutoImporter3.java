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

package xyz.spiralhalo.sherlock.record.legacy;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.record.RecordEntry;
import xyz.spiralhalo.sherlock.util.FormatUtil;

public class AutoImporter3 {
	public static final String OLD_RECORD_DIR = "record";

	public static void importRecords(ProjectList projectList) {
		File oldRecordDir = new File(Application.getSaveDir(), OLD_RECORD_DIR);
		File newRecordFile = new File(Application.getSaveDir(), ReconstructorWriter.RECORD_FILE);
		if (oldRecordDir.isDirectory() && !newRecordFile.exists()) {
			try (OldRecordScanner sc = new OldRecordScanner(oldRecordDir)) {
				final ReconstructorWriter writer = new ReconstructorWriter();
				while (sc.hasNext()) {
					RecordEntry entry = sc.next();
					Project p = projectList.findByHash(entry.getHash());
					if (p != null) {
						writer.log(entry.getTime().toEpochMilli(), entry.getElapsed(), p);
					} else {
						writer.log(entry.getTime().toEpochMilli(), entry.getElapsed(), "unknown", entry.getHash(), entry.isUtility(), entry.isProductive());
					}
				}
				writer.close();
			} catch (Exception e) {
				Debug.log(e);
			}
		}
	}

	public static class OldRecordScanner implements Iterator<RecordEntry>, AutoCloseable {
		private final ArrayList<File> files = new ArrayList<>();
		private final LocalDate from;
		private final LocalDate until;

		private int filepos = 0;
		private RandomAccessFile raf;
		private RecordEntry internalData;

		public OldRecordScanner(File recordDir) {
			this(recordDir, null, null);
		}

		public OldRecordScanner(File recordDir, LocalDate from, LocalDate until) {
			if (from != null && until != null && from.isAfter(until))
				throw new IllegalArgumentException("Illogical range.");
			this.from = from;
			this.until = until;
			File[] records = recordDir.listFiles();
			if (records != null) {
				Arrays.sort(records);
				for (File f : records) {
					try {
						YearMonth fDate = yearMonth(f.getName());
						if ((from == null || !fDate.isBefore(YearMonth.from(from)))
								&& (until == null || !fDate.isAfter(YearMonth.from(until)))) {
							files.add(f);
						}
					} catch (DateTimeParseException e) {
						Debug.log(e);
					}
				}
				if (files.size() != 0) {
					createRaf();
					if (raf != null && from != null) {
						seekToEarliest();
					}
				}
			}
		}

		private void seekToEarliest() {
			try {
				LocalDate date = null;
				while (date == null || date.isBefore(from)) {
					byte[] raw = readBytes(raf);
					date = RecordEntry.getTimestamp(raw).atZone(ZoneId.systemDefault()).toLocalDate();
					if (date.isEqual(from)) {
						internalData = RecordEntry.deserialize(raw);
					}
				}
			} catch (IOException e) {
				Debug.log(e);
			}
		}

		private void createRaf() {
			if (filepos >= files.size()) return;
			try {
				raf = new RandomAccessFile(files.get(filepos), "r");
			} catch (FileNotFoundException e) {
				Debug.log(e);
			}
		}

		private void nextFile() {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					Debug.log(e);
				}
			}
			raf = null;
			filepos++;
			createRaf();
		}

		private RecordEntry getData() {
			while (internalData == null && raf != null) {
				byte[] raw = null;
				while (raw == null && raf != null) {
					try {
						raw = readBytes(raf);
						if (raf.getFilePointer() >= raf.length()) {
							nextFile();
						}
					} catch (EOFException e) {
						Debug.log(e);
						nextFile();
					} catch (IOException ignored) {
						return null;
					}
				}
				if (raw != null) {
					LocalDate date = RecordEntry.getTimestamp(raw).atZone(ZoneId.systemDefault()).toLocalDate();
					if (until != null && date.isAfter(until)) {
						return null;
					} else {
						internalData = RecordEntry.deserialize(raw);
					}
				}
			}
			return internalData;
		}

		private void resetData() {
			internalData = null;
		}

		@Override
		public boolean hasNext() {
			RecordEntry data = getData();
			return data != null;
		}

		@Override
		public RecordEntry next() {
			RecordEntry data = getData();
			resetData();
			return data;
		}

		@Override
		public void close() throws Exception {
			if (raf != null) {
				raf.close();
			}
		}

		private static YearMonth yearMonth(String filename) {
			return YearMonth.parse(filename.split("[.]")[0], FormatUtil.DTF_YM);
		}

		private static byte[] readBytes(RandomAccessFile radFile) throws IOException {
			int meta = radFile.readUnsignedByte();
			byte[] toRead = new byte[meta];
			radFile.readFully(toRead);
			return toRead;
		}
	}
}
