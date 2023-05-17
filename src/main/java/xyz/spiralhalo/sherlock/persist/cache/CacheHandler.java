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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.Debug;

class CacheHandler {
	static CachedObj writeCache(String name, Serializable y) {
		File cacheFile = new File(Application.getCacheDir(), name);
		try (FileOutputStream fos = new FileOutputStream(cacheFile);
			 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
			CachedObj x = new CachedObj(y);
			oos.writeObject(x);
			return x;
		} catch (IOException e) {
			Debug.log(e);
		}
		return null;
	}

	static CachedObj readCache(String name) {
		File cacheFile = new File(Application.getCacheDir(), name);
		if (cacheFile.exists()) {
			try (FileInputStream fis = new FileInputStream(cacheFile);
				 ObjectInputStream ois = new ObjectInputStream(fis)) {
				return (CachedObj) ois.readObject();
			} catch (ClassCastException | ClassNotFoundException | IOException e) {
				Debug.log(e);
			}
		}
		return null;
	}

	static void deleteAllCacheFiles() {
		File cacheDir = new File(Application.getCacheDir());
		if (cacheDir.exists()) {
			File[] cacheFiles = cacheDir.listFiles();
			if (cacheFiles != null) {
				for (File file : cacheFiles) {
					// this function only deletes files without extensions
					if (!file.getName().contains(".") && !file.isDirectory()) {
						file.delete();
					}
				}
			}
		}
	}
}
