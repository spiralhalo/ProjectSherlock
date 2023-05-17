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

package xyz.spiralhalo.sherlock.util;

import java.util.Iterator;
import java.util.List;

public class ListUtil {
	private ListUtil() {
	}

	public static <T> Iterable<T> extensiveIterator(List<? extends T> list1, List<? extends T> list2) {
		return new ExtensiveIterator<>(new List[]{list1, list2});
	}

	public static <T> Iterable<T> extensiveIterator(List<? extends T> list1, List<? extends T> list2, List<? extends T> list3) {
		return new ExtensiveIterator<>(new List[]{list1, list2, list3});
	}

	private static class ExtensiveIterator<T> implements Iterable<T>, Iterator<T> {
		private Iterator<T>[] iterators;

		private ExtensiveIterator(List<T>[] lists) {
			this.iterators = new Iterator[lists.length];
			for (int i = 0; i < lists.length; i++) {
				this.iterators[i] = lists[i].iterator();
			}
		}

		@Override
		public boolean hasNext() {
			for (Iterator<T> i : iterators) {
				if (i.hasNext()) return true;
			}
			return false;
		}

		@Override
		public T next() {
			for (Iterator<T> i : iterators) {
				if (i.hasNext()) return i.next();
			}
			return null;
		}

		@Override
		public Iterator<T> iterator() {
			return this;
		}
	}
}
