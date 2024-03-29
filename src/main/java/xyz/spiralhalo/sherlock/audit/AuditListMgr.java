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

package xyz.spiralhalo.sherlock.audit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.audit.persist.AuditList;
import xyz.spiralhalo.sherlock.audit.persist.CategoryMilestoneList;
import xyz.spiralhalo.sherlock.audit.persist.DayAudit;

public class AuditListMgr {

	public static boolean exists(ZoneId z, LocalDate date) {
		LocalDate d = convertToUTC(z, date);
		AuditList list = getList();
		return list.containsKey(d);
	}

	public static DayAudit getDayAudit(ZoneId z, LocalDate date) {
		LocalDate d = convertToUTC(z, date);
		AuditList list = getList();
		return list.get(d);
	}

	public static void setDayAuditAndSave(ZoneId z, LocalDate date, DayAudit dayAudit) {
		LocalDate d = convertToUTC(z, date);
		AuditList list = getList();
		list.put(d, dayAudit);
		saveList(list);
	}

	public ArrayList getCategoryMilestoneList(String category) {
		HashMap<String, Object> extras = getList().getExtras();
		if (extras == null || !extras.containsKey("CategoryMilestones")
				|| !(extras.get("CategoryMilestones") instanceof CategoryMilestoneList)) {
			return null;
		}
		CategoryMilestoneList categoryMilestoneList = (CategoryMilestoneList) extras.get("CategoryMilestones");
		return categoryMilestoneList.get(category);
	}

	private static WeakReference<AuditList> cache;

	private static void saveList(AuditList auditList) {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getAuditsFile()))) {
			oos.writeObject(auditList);
			cache = new WeakReference<>(auditList);
		} catch (IOException e) {
			Debug.log(e);
		}
	}

	private static AuditList getList() {
		if (cache != null && cache.get() != null) return cache.get();
		AuditList yn;
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getAuditsFile()))) {
			yn = (AuditList) ois.readObject();
		} catch (ClassCastException | ClassNotFoundException | IOException e) {
			yn = new AuditList();
			Debug.log(e);
		}
		cache = new WeakReference<>(yn);
		return yn;
	}

	private static LocalDate convertToUTC(ZoneId z, LocalDate adaptToThis) {
		if (z.equals(ZoneOffset.UTC)) return adaptToThis;
		return adaptToThis.atTime(12, 0).atZone(z).withZoneSameInstant(ZoneOffset.UTC).toLocalDate();
	}

	private static File getAuditsFile() {
		return new File(Application.getSaveDir(), "audit.dat");
	}
}
