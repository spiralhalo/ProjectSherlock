package xyz.spiralhalo.sherlock.persist.project;

import xyz.spiralhalo.sherlock.util.Debug;
import xyz.spiralhalo.sherlock.util.PathUtil;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class ProjectList implements Serializable {
    public static final long serialVersionUID = 1L;
    public static final String PROJECTS_FILE = "projects.dat";

//    private static final HashMap<ProjectList,DefaultComboBoxModel<String>> listModels = new HashMap<>();
    private static TreeSet<String> categories;

    private ArrayList<Project> activeProjects;
    private ArrayList<Project> finishedProjects;
    private transient HashMap<Long, Project> projectMap;

    public ProjectList(){
        activeProjects = new ArrayList<>();
        finishedProjects = new ArrayList<>();
    }

    public void addProject(Project p) {
        activeProjects.add(p);
        getCategories().add(p.getCategory());
        if(projectMap==null){
            createProjectMap();
        }
        projectMap.put(p.getHash(),p);
        save();
    }

    public void editProject(Project p, String oldCategory){
        save();
        if(!p.getCategory().equals(oldCategory)){
            for (Project x:activeProjects) {
                if(x!=p && x.getCategory().equals(oldCategory)){
                    return;
                }
            }
            for (Project x:finishedProjects) {
                if(x!=p && x.getCategory().equals(oldCategory)){
                    return;
                }
            }
            categories.remove(oldCategory);
        }
    }

    public long getProjectOf(String windowTitle, ZonedDateTime time){
        for (Project p : activeProjects) {
            if((!p.isFinished() || !time.isAfter(p.getFinishedDate())) && !time.isBefore(p.getStartDate())) {
                for (String tag : p.getTags()) {
                    if (windowTitle.toLowerCase().contains(tag.toLowerCase())) {
                        return p.getHash();
                    }
                }
            }
        }
        for (Project p : finishedProjects) {
            if((!p.isFinished() || !time.isAfter(p.getFinishedDate())) && !time.isBefore(p.getStartDate())) {
                for (String tag : p.getTags()) {
                    if (windowTitle.toLowerCase().contains(tag.toLowerCase())) {
                        return p.getHash();
                    }
                }
            }
        }
        return -1;
    }

    public TreeSet<String> getCategories(){
        if(categories==null){
            setCategories();
        }
        return categories;
    }

    private void setCategories(){
        categories = new TreeSet<>();
        for (Project p:activeProjects) {
            categories.add(p.getCategory());
        }
        for (Project p:finishedProjects) {
            categories.add(p.getCategory());
        }
    }

    public boolean deleteProject(Project p) {
        if(activeProjects.contains(p)){
            return delete(activeProjects, p);
        }
        if(finishedProjects.contains(p)){
            return delete(finishedProjects, p);
        }
        return false;
    }

    public int size(){
        return activeProjects.size() + finishedProjects.size();
    }

    public void setProjectFinished(long hash, boolean finished){
        Project p = findByHash(hash);
        if(finished && !p.isFinished()) {
            activeProjects.remove(p);
            finishedProjects.add(p);
            p.setFinished(true);
        } else if(!finished && p.isFinished()){
            activeProjects.add(p);
            finishedProjects.remove(p);
            p.setFinished(false);
        }
        save();
    }

    public ArrayList<Project> getActiveProjects() {
        return activeProjects;
    }

    public ArrayList<Project> getFinishedProjects() {
        return finishedProjects;
    }

    private boolean delete(ArrayList<Project> list, Project p){
        projectMap.remove(p.getHash());
        list.remove(p);
        save();
        return true;
    }

    public Project findByHash(long hash){
        if(hash==-1) return null;
        if(projectMap==null){
            createProjectMap();
        }
        return projectMap.getOrDefault(hash, null);
    }

    private void createProjectMap() {
        projectMap = new HashMap<>();
        for (Project project : activeProjects) {
            projectMap.put(project.getHash(),project);
        }
        for (Project project : finishedProjects) {
            projectMap.put(project.getHash(),project);
        }
    }

    synchronized private void save(){
        File file = new File(PathUtil.getSaveDir(),PROJECTS_FILE);
        try(FileOutputStream fis = new FileOutputStream(file);
            ObjectOutputStream ois = new ObjectOutputStream(fis)){
            ois.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized public static ProjectList load() {
        Object x = null;
        File file = new File(PathUtil.getSaveDir(), PROJECTS_FILE);
        try (FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis)) {
            x = ois.readObject();
            return (ProjectList) x;
        } catch (ClassNotFoundException | IOException e) {
            Debug.log(ProjectList.class, e);
            return new ProjectList();
        } catch (ClassCastException e) {
            Debug.log(ProjectList.class, e);
            return legacy(x);
        }
    }

    private static ProjectList legacy(Object x){
        ProjectList n = new ProjectList();
        if(x instanceof projectlogger.ProjectList){
            for (projectlogger.Project p:((projectlogger.ProjectList)x).getProjects()) {
                Project m = new Project(p.getSupertag()+"/"+p.getTag(),p.getCategory(),p.getTag());
                m.setStartDate(p.getStartDate());
                if(p.isFinished()){
                    n.finishedProjects.add(m);
                    m.setFinished(true);
                    m.setFinishedDate(p.getFinishedDate());
                } else {
                    n.activeProjects.add(m);
                }
            }
        } else if(x instanceof projectlogger.persist.ProjectList){
            for (projectlogger.persist.Project p:((projectlogger.persist.ProjectList)x).getProjects()) {
                Project m = new Project(p.getGroup()+"/"+p.getTag(),p.getCategory(),p.getTag());
                m.setStartDate(p.getStartDate());
                if(p.isFinished()){
                    n.finishedProjects.add(m);
                    m.setFinished(true);
                    m.setFinishedDate(p.getFinishedDate());
                } else {
                    n.activeProjects.add(m);
                }
            }
        }
        n.save();
        return n;
    }
}