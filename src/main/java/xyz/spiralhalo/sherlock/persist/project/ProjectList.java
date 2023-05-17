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

package xyz.spiralhalo.sherlock.persist.project;

import static xyz.spiralhalo.sherlock.persist.project.ProjectListIO.save;
import static xyz.spiralhalo.sherlock.persist.project.ProjectListIO.saveUtilityTags;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import xyz.spiralhalo.sherlock.util.ListUtil;

public class ProjectList implements Serializable {
	public static final long serialVersionUID = 1L;

	// DEFINED APPENDICES
	public static transient final String USE_EXE = "use_exe";
	public static transient final String EXE_LIST = "exe_list";
	public static transient final String USE_REV_TRACKING = "use_rev_tracking";
	public static transient final String USE_REV_HOURLY = "use_rev_hourly";
	public static transient final String USE_REV_FIXED = "use_rev_fixed";
	public static transient final String REV_HOURLY = "rev_hourly";
	public static transient final String REV_FIXED = "rev_fixed";


	private final ArrayList<Project> activeProjects;
	private final ArrayList<Project> finishedProjects;
	private transient UtilityTagList utilityTags;
	private transient HashMap<Long, Project> projectMap;
	private transient TreeSet<String> categories;

	ProjectList() {
		activeProjects = new ArrayList<>();
		finishedProjects = new ArrayList<>();
	}

	public void addProject(Project p) {
		if (p.isUtilityTag()) {
			getUtilityTags().add((UtilityTag) p);
			saveUtilityTags(this);
		} else {
			activeProjects.add(p);
			save(this);
		}
		getCategories().add(p.getCategory());
		getProjectMap().put(p.getHash(), p);
	}

	public boolean deleteProject(Project p) {
		if (p.isUtilityTag()) {
			return delete(getUtilityTags(), (UtilityTag) p);
		}
		if (activeProjects.contains(p)) {
			return delete(activeProjects, p);
		}
		if (finishedProjects.contains(p)) {
			return delete(finishedProjects, p);
		}
		return false;
	}

	@ProjectsOnly
	public void editProject(Project p, String name, String newCategory, String oldCategory, String tags, int ptype) {
		assureProject(p, "editProject");
		p.edit(name, newCategory, tags, ptype);
		save(this);
		resetCats(newCategory, oldCategory);
	}

	@ProjectsOnly
	public void setProjectFinished(long hash, boolean finished) {
		Project p = findByHash(hash);
		assureProject(p, "setProjectFinished");
		if (finished && !p.isFinished()) {
			activeProjects.remove(p);
			finishedProjects.add(p);
			p.setFinished(true);
		} else if (!finished && p.isFinished()) {
			activeProjects.add(p);
			finishedProjects.remove(p);
			p.setFinished(false);
		}
		save(this);
	}

	private void resetCats(String newCategory, String oldCategory) {
		if (!newCategory.equals(oldCategory)) {
			for (Project x : ListUtil.extensiveIterator(activeProjects, finishedProjects, getUtilityTags())) {
				if (x.getCategory().equals(oldCategory)) {
					return;
				}
			}
			getCategories().remove(oldCategory);
			categories.add(newCategory);
		}
	}

	public void editUtilityTag(UtilityTag p, String name, String newCategory, String oldCategory, String tags, int ptype) {
		p.edit(name, newCategory, tags, ptype);
		saveUtilityTags(this);
		resetCats(newCategory, oldCategory);
	}

	public Project getActiveProjectOf(String windowTitle, String executable, ZonedDateTime time) {
		if (windowTitle == null || windowTitle.length() == 0) return null;
		return getProjectOfInternal(windowTitle, executable, time, ListUtil.extensiveIterator(activeProjects, getUtilityTags()));
	}

	public Project getProjectOf(String windowTitle, String executable, ZonedDateTime time) {
		if (windowTitle == null || windowTitle.length() == 0) return null;
		return getProjectOfInternal(windowTitle, executable, time, ListUtil.extensiveIterator(activeProjects, finishedProjects, getUtilityTags()));
	}

	private Project getProjectOfInternal(String windowTitle, String executable, ZonedDateTime time, Iterable<Project> toIterate) {
		for (Project p : toIterate) {
//            long start = System.currentTimeMillis();
			if ((!p.isFinished() || !time.isAfter(p.getFinishedDate())) && !time.isBefore(p.getStartDateTime())) {
				boolean foundTag = false;
				for (String tag : p.getTags()) {
					if (windowTitle.toLowerCase().contains(tag.toLowerCase())) {
//                        System.out.println("tag seek time:"+(System.currentTimeMillis() - start));
						foundTag = true;
						break;
					}
				}
				Boolean useExe = p.getAppendix(USE_EXE, Boolean.class);
				String[] exeList = p.getAppendix(EXE_LIST, String[].class);
				boolean exeOK = useExe == null || exeList == null || !useExe || exeList.length == 0;
				if (!exeOK) {
					for (String exe : exeList) {
						if (executable.equalsIgnoreCase(exe)) {
							exeOK = true;
							break;
						}
					}
				}
				if (foundTag && exeOK) {
					return p;
				}
			}
		}
		return null;
	}

	public Project findByHash(long hash) {
		if (hash == -1) return null;
		return getProjectMap().getOrDefault(hash, null);
	}

	public TreeSet<String> getCategories() {
		if (categories == null) {
			setCategories();
		}
		return categories;
	}

	public long moveUp(int list, int pos) {
		switch (list) {
			case 0:
				return move(activeProjects, pos, -1);
			case 1:
				return move(finishedProjects, pos, -1);
			case 2:
				return move(getUtilityTags(), pos, -1);
			default:
				return -1;
		}
	}

	public long moveDown(int list, int pos) {
		switch (list) {
			case 0:
				return move(activeProjects, pos, 1);
			case 1:
				return move(finishedProjects, pos, 1);
			case 2:
				return move(getUtilityTags(), pos, 1);
			default:
				return -1;
		}
	}

	private <T extends Project> long move(ArrayList<T> list, int pos, int moveBy) {
		int newPos = pos + moveBy;
		if (newPos < 0 || newPos >= list.size()) return -1;
		T moving = list.get(pos);
		list.remove(pos);
		list.add(newPos, moving);
		if (list.equals(activeProjects) || list.equals(finishedProjects)) {
			save(this);
		} else if (list.equals(getUtilityTags())) {
			saveUtilityTags(this);
		}
		return moving.getHash();
	}

	public int getTotalSize() {
		return getProjectMap().size();
	}

	private void setCategories() {
		categories = new TreeSet<>();
		for (Project p : ListUtil.extensiveIterator(activeProjects, finishedProjects, getUtilityTags())) {
			categories.add(p.getCategory());
		}
	}

	private <T extends Project> void deleteInternal(ArrayList<T> list, T p) {
		resetCats("", p.getCategory());
		getProjectMap().remove(p.getHash());
		list.remove(p);
	}

	private boolean delete(ArrayList<Project> list, Project p) {
		deleteInternal(list, p);
		save(this);
		return true;
	}

	private boolean delete(UtilityTagList list, UtilityTag p) {
		deleteInternal(list, p);
		saveUtilityTags(this);
		return true;
	}

	public ArrayList<Project> getActiveProjects() {
		return activeProjects;
	}

	public ArrayList<Project> getFinishedProjects() {
		return finishedProjects;
	}

	public int getActiveSize() {
		return activeProjects.size() + getUtilityTags().size();
	}

	void setUtilityTags(UtilityTagList utilityTags) {
		this.utilityTags = utilityTags;
	}

	public UtilityTagList getUtilityTags() {
		if (utilityTags == null) utilityTags = new UtilityTagList();
		return utilityTags;
	}

	private HashMap<Long, Project> getProjectMap() {
		if (projectMap == null) createProjectMap();
		return projectMap;
	}

	private void createProjectMap() {
		projectMap = new HashMap<>();
		for (Project project : ListUtil.extensiveIterator(activeProjects, finishedProjects, getUtilityTags())) {
			projectMap.put(project.getHash(), project);
		}
	}

	private static void assureProject(Project p, String methodName) {
		if (p.isUtilityTag()) throw new IllegalArgumentException(methodName + "() don't accept utility tags");
	}
}
