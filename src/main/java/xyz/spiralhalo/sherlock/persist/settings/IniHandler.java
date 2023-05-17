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

package xyz.spiralhalo.sherlock.persist.settings;

import java.io.File;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.ini4j.Ini;
import org.ini4j.IniPreferences;
import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.Debug;

public class IniHandler extends IniPreferences {
	private static final String SETTINGS_FILENAME = "settings.ini";
	private static IniHandler instance;

	public synchronized static IniHandler getInstance() {
		if (instance == null) {
			instance = new IniHandler(new Ini());
		}
		return instance;
	}

	private final Ini ini;

	private IniHandler(Ini ini) {
		super(ini);
		File iniFile = new File(Application.getSaveDir(), SETTINGS_FILENAME);
		ini.setFile(iniFile);
		try {
			if (iniFile.exists()) {
				ini.load();
			}
		} catch (IOException e) {
			Debug.log(e);
		}
		this.ini = ini;
	}

	public void reset(String node) {
		try {
			super.node(node).removeNode();
		} catch (BackingStoreException e) {
			Debug.log(e);
		}
	}

	@Override
	public void put(String key, String value) {
		getRootNode().put(key, value);
		save();
	}

	@Override
	public void putBoolean(String key, boolean value) {
		getRootNode().putBoolean(key, value);
		save();
	}

	@Override
	public void putByteArray(String key, byte[] value) {
		getRootNode().putByteArray(key, value);
		save();
	}

	@Override
	public void putDouble(String key, double value) {
		getRootNode().putDouble(key, value);
		save();
	}

	@Override
	public void putFloat(String key, float value) {
		getRootNode().putFloat(key, value);
		save();
	}

	@Override
	public void putInt(String key, int value) {
		getRootNode().putInt(key, value);
		save();
	}

	@Override
	public void putLong(String key, long value) {
		getRootNode().putLong(key, value);
		save();
	}

	@Override
	public String get(String key, String def) {
		return getRootNode().get(key, def);
	}

	@Override
	public boolean getBoolean(String key, boolean def) {
		return getRootNode().getBoolean(key, def);
	}

	@Override
	public byte[] getByteArray(String key, byte[] def) {
		return getRootNode().getByteArray(key, def);
	}

	@Override
	public double getDouble(String key, double def) {
		return getRootNode().getDouble(key, def);
	}

	@Override
	public float getFloat(String key, float def) {
		return getRootNode().getFloat(key, def);
	}

	@Override
	public int getInt(String key, int def) {
		return getRootNode().getInt(key, def);
	}

	@Override
	public long getLong(String key, long def) {
		return getRootNode().getLong(key, def);
	}

	public void put(String node, String key, String value) {
		super.node(node).put(key, value);
		save();
	}

	public void putBoolean(String node, String key, boolean value) {
		super.node(node).putBoolean(key, value);
		save();
	}

	public void putByteArray(String node, String key, byte[] value) {
		super.node(node).putByteArray(key, value);
		save();
	}

	public void putDouble(String node, String key, double value) {
		super.node(node).putDouble(key, value);
		save();
	}

	public void putFloat(String node, String key, float value) {
		super.node(node).putFloat(key, value);
		save();
	}

	public void putInt(String node, String key, int value) {
		super.node(node).putInt(key, value);
		save();
	}

	public void putLong(String node, String key, long value) {
		super.node(node).putLong(key, value);
		save();
	}

	public String get(String node, String key, String def) {
		return super.node(node).get(key, def);
	}

	public boolean getBoolean(String node, String key, boolean def) {
		return super.node(node).getBoolean(key, def);
	}

	public byte[] getByteArray(String node, String key, byte[] def) {
		return super.node(node).getByteArray(key, def);
	}

	public double getDouble(String node, String key, double def) {
		return super.node(node).getDouble(key, def);
	}

	public float getFloat(String node, String key, float def) {
		return super.node(node).getFloat(key, def);
	}

	public int getInt(String node, String key, int def) {
		return super.node(node).getInt(key, def);
	}

	public long getLong(String node, String key, long def) {
		return super.node(node).getLong(key, def);
	}

	private Preferences getRootNode() {
		return super.node("settings");
	}

	synchronized private void save() {
		try {
			ini.store();
		} catch (IOException e) {
			Debug.log(e);
		}
	}
}
