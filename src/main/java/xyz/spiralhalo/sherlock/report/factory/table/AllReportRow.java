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

package xyz.spiralhalo.sherlock.report.factory.table;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import xyz.spiralhalo.sherlock.ScrSnapper;
import xyz.spiralhalo.sherlock.persist.project.Project;

public class AllReportRow implements Serializable {
	public static final long serialVersionUID = 4L;

	private final long projectHash;
	private final int projectColor;
	private final String projectName;
	private final String category;
	private final LocalDate startDate;
	private final LocalDate finishDate;
	private final int days;
	private final int seconds;
	private final int ptype;
	private final long lastWorkedOnMillis;

	public AllReportRow(long projectHash, int projectColor, String projectName, String category, int pType,
						LocalDateTime startDate, LocalDateTime finishDate, int days, int seconds, long lastWorkedOnMillis) {
		this.projectHash = projectHash;
		this.projectColor = projectColor;
		this.projectName = projectName;
		this.category = category;
		this.startDate = LocalDate.from(startDate);
		this.finishDate = finishDate == null ? null : LocalDate.from(finishDate);
		this.days = days;
		this.seconds = seconds;
		this.ptype = pType;
		File thumbFile = ScrSnapper.getThumbFile(projectHash);
		// ILLEGAL HAXX
		if (thumbFile.exists()) {
			this.lastWorkedOnMillis = Math.max(lastWorkedOnMillis, thumbFile.lastModified());
		} else {
			this.lastWorkedOnMillis = lastWorkedOnMillis;
		}
	}

	public AllReportRow(long projectHash, int projectColor, String projectName, String category, int pType,
						int days, int seconds, long lastWorkedOnMillis) {
		this.projectHash = projectHash;
		this.projectColor = projectColor;
		this.projectName = projectName;
		this.category = category;
		this.startDate = null;
		this.finishDate = null;
		this.days = days;
		this.seconds = seconds;
		this.ptype = pType;
		File thumbFile = ScrSnapper.getThumbFile(projectHash);
		if (thumbFile.exists()) {
			this.lastWorkedOnMillis = Math.max(lastWorkedOnMillis, thumbFile.lastModified());
		} else {
			this.lastWorkedOnMillis = lastWorkedOnMillis;
		}
	}

	public long getProjectHash() {
		return projectHash;
	}

	public int getProjectColor() {
		return projectColor;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getCategory() {
		return category;
	}

	public int getDays() {
		return days;
	}

	public int getSeconds() {
		return seconds;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getFinishDate() {
		return finishDate;
	}

	public String getPTypeLabel() {
		switch (ptype) {
			case Project.PTYPE_PRODUCTIVE:
				return Project.PRODUCTIVE_LABEL;
			case Project.PTYPE_RECREATIONAL:
				return Project.RECREATIONAL_LABEL;
			default:
				return Project.NON_PRODUCTIVE_LABEL;
		}
	}

	public long getLastWorkedOnMillis() {
		return lastWorkedOnMillis;
	}
}
