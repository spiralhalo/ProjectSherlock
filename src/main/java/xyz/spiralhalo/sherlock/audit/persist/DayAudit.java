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

package xyz.spiralhalo.sherlock.audit.persist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class DayAudit implements Serializable {
	public static final long serialVersionUID = 1L;

	private HashMap<Long, ArrayList<AuditEntry>> projectAudits;

	public DayAudit() {
		projectAudits = new HashMap<>();
	}

	public void addProjectAudit(long projectId, AuditEntry entry) {
		if (!projectAudits.containsKey(projectId)) projectAudits.put(projectId, new ArrayList<>());

		if (!projectAudits.get(projectId).contains(entry)) projectAudits.get(projectId).add(entry);
	}

	public void removeProjectAudit(long projectId, AuditEntry entry) {
		if (!projectAudits.containsKey(projectId)) return;

		if (projectAudits.get(projectId).contains(entry)) projectAudits.get(projectId).remove(entry);
	}

	public Long[] getProjects() {
		return projectAudits.keySet().toArray(new Long[]{});
	}

	public AuditEntry[] getProjectAudits(long projectId) {
		return projectAudits.get(projectId).toArray(new AuditEntry[]{});
	}
}
