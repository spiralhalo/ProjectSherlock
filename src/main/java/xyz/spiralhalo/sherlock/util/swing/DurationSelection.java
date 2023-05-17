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

package xyz.spiralhalo.sherlock.util.swing;

import java.io.Serializable;

import xyz.spiralhalo.sherlock.util.FormatUtil;

public class DurationSelection implements Serializable {
	public enum HMSMode {
		Config, Strict, Colon, Long
	}

	private final int value;
	private final boolean unlimited;
	private final HMSMode mode;

	public DurationSelection() {
		this.unlimited = true;
		this.value = -1;
		this.mode = HMSMode.Config;
	}

	public DurationSelection(int value, HMSMode mode) {
		this.value = value;
		this.unlimited = false;
		this.mode = mode;
	}

	@Override
	public String toString() {
		if (unlimited) return "Unlimited";
		switch (mode) {
			case Long:
				return FormatUtil.hmsLong(value);
			case Strict:
				return FormatUtil.hmsStrict(value);
			case Colon:
				return FormatUtil.hmsHomoColon(value);
		}
		return FormatUtil.hms(value);
	}

	public int getValue() {
		return value;
	}
}
