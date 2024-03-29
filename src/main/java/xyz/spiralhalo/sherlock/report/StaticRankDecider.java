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

package xyz.spiralhalo.sherlock.report;

import static xyz.spiralhalo.sherlock.util.ColorUtil.excel;
import static xyz.spiralhalo.sherlock.util.ColorUtil.light_gray;
import static xyz.spiralhalo.sherlock.util.ColorUtil.lite;
import static xyz.spiralhalo.sherlock.util.ColorUtil.med;

import java.awt.*;

public class StaticRankDecider {
	public enum Rank {
		BREAK_DAY(0, 00, "Break Day", light_gray),
		LIGHT_WORK(1, 20, "Light Work", lite),
		WELL_DONE(2, 40, "Well Done", med),
		EXCELLENT(3, 90, "Excellent", excel);
		public final int index;
		public final int score;
		public final String label;
		public final Color color;

		Rank(int index, int score, String label, Color color) {
			this.index = index;
			this.score = score;
			this.label = label;
			this.color = color;
		}
	}

	public static Rank decide(int score) {
		Rank rankz = Rank.BREAK_DAY;
		for (Rank rank : Rank.values()) {
			if (score >= rank.score) {
				rankz = rank;
			} else break;
		}
		return rankz;
	}
}
