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

package xyz.spiralhalo.sherlock.record.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;

import javax.imageio.ImageIO;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.record.DefaultRecordWriter;
import xyz.spiralhalo.sherlock.record.RecordEntry;
import xyz.spiralhalo.sherlock.record.RecordScanner;
import xyz.spiralhalo.sherlock.record.io.RecordFileSeek;

public class Visualizer {
	public static void visualize(File saveAs, int granularitySeconds, int width, int height) {
		long origin = 0;
		File recordFile = new File(Application.getSaveDir(), DefaultRecordWriter.RECORD_FILE);
		try (RecordScanner scanner = new RecordScanner(new RecordFileSeek(recordFile, false))) {
			int i = 0;
			int g = granularitySeconds;
			int g1000 = g * 1000;
			HashMap<Integer, Integer> minutes = new HashMap<>();
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			LocalDate ld = null;
			while (scanner.hasNext() && i < width) {
				RecordEntry e = scanner.next();
				if (origin == 0) {
					origin = e.getTime().toEpochMilli();
				}
				i = (int) ((e.getTime().toEpochMilli() - origin) / g1000);
				LocalDate asd = e.getTime().atZone(ZoneId.systemDefault()).toLocalDate();
				int dur = e.getElapsed() / g + (e.getElapsed() % g > 0 ? 1 : 0);
				for (int j = 0; j < dur; j++) {
					int x = i + j;
					if (x >= width) break;
					if (x < 0) {
						System.out.println(asd);
						continue;
					}
					if (!minutes.containsKey(x)) {
						minutes.put(x, 0xffffff);
					} else {
						minutes.put(x, Math.max(minutes.get(x) - 0x88, 0));
					}
					for (int k = 0; k < height; k++) {
						image.setRGB(x, k, minutes.get(x));
					}
				}
				if (ld == null || ld.isBefore(asd)) {
					ld = asd;
					int x = (int) ((ld.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - origin) / g1000);
					int y = x + 2;
					while (x < width && x >= 0 && x < y) {
						for (int k = 0; k < height; k++) {
							image.setRGB(x, k, 0xff0000);
						}
						x++;
					}
				}
			}
			ImageIO.write(image, "png", saveAs);
		} catch (Exception e) {
			Debug.log(e);
		}
	}
}
