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

package xyz.spiralhalo.sherlock.persist.cache;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
// import java.util.WeakHashMap;

public class CacheMgr {
	public static final Instant NEVER = Instant.EPOCH;

	// Somehow WeakHashMap always return null, making the cache completely useless
	private final HashMap<String, CachedObj> cache = new HashMap<>();
	// private final WeakHashMap<String, CachedObj> cache = new WeakHashMap<>();

	public synchronized <T extends Serializable> void put(String id, T object) {
		cache.put(id, CacheHandler.writeCache(id, object));
	}

	public Instant getCreated(String id) {
		CachedObj x = getInternal(id);
		if (x == null || x.getObject() == null) {
			return NEVER;
		}
		return x.getCreated();
	}

	public long getElapsed(String id) {
		CachedObj x = getInternal(id);
		if (x == null || x.getObject() == null) {
			return Long.MAX_VALUE;
		}
		return x.getElapsed();
	}

	public <T extends Serializable> T getObj(String id, Class<T> clazz) {
		CachedObj x = getInternal(id);
		if (x == null) {
			return null;
		}
		if (clazz.isInstance(x.getObject())) {
			return clazz.cast(x.getObject());
		} else return null;
	}

	private synchronized CachedObj getInternal(String id) {
		CachedObj x = cache.get(id);
		if (x == null) {
			x = CacheHandler.readCache(id);
			if (x != null) {
				cache.put(id, x);
			}
		}
		return x;
	}

	public void forceDiskCacheCleanup() {
		CacheHandler.deleteAllCacheFiles();
	}
}
