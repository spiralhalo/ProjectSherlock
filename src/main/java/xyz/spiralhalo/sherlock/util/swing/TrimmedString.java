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

public class TrimmedString {
	public static TrimmedString[] createArray(String[] arr, int limit) {
		TrimmedString[] res = new TrimmedString[arr.length];
		for (int i = 0; i < arr.length; i++) {
			res[i] = new TrimmedString(arr[i], limit);
		}
		return res;
	}

	private final String content;
	private final String label;

	public TrimmedString(String content, int limit) {
		this.content = content;
		int length = Math.min(content.length(), limit);
		this.label = content.substring(0, length) + (length < content.length() ? "..." : "");
	}

	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return label;
	}
}
