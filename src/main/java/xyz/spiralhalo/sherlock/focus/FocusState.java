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

package xyz.spiralhalo.sherlock.focus;

import java.lang.ref.WeakReference;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.persist.settings.IniHandler;

public class FocusState {

	private static WeakReference<FocusState> instance;

	public static FocusState getInstance() {
		if (instance == null || instance.get() == null) {
			instance = new WeakReference<>(new FocusState());
		}
		return instance.get();
	}

	private FocusState() {
	}

	public Project getProject(ProjectList projectList) {
		long h = IniHandler.getInstance().getLong("focus", "project", -1);
		Project p = projectList.findByHash(h);
		return p;
	}

	public void setProject(long hash) {
		IniHandler.getInstance().putLong("focus", "project", hash);
	}

	public boolean isEnabled() {
		return IniHandler.getInstance().getBoolean("focus", "enabled", false);
	}

	public void setEnabled(boolean enabled) {
		IniHandler.getInstance().putBoolean("focus", "enabled", enabled);
	}

	public void setDuration(long millis) {
		IniHandler.getInstance().putLong("focus", "limit", millis);
	}

	public long getDuration() {
		return IniHandler.getInstance().getLong("focus", "limit", -1);
	}
}
