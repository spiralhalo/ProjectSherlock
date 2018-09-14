package xyz.spiralhalo.sherlock.persist.project;

import xyz.spiralhalo.sherlock.util.Debug;
import xyz.spiralhalo.sherlock.Application;

import java.io.*;

public class ProjectListIO {
    public static final String PROJECTS_FILE = "projects.dat";
    public static final String UTILITY_TAGS_FILE = "utility_tags.dat";

    private ProjectListIO(){}

    synchronized public static ProjectList load() {
        File file = new File(Application.getSaveDir(), PROJECTS_FILE);
        if(file.exists()){
            try (FileInputStream fis = new FileInputStream(file);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                Object x = ois.readObject();
                ProjectList y = (x instanceof ProjectList)?(ProjectList) x:legacy(x);
                loadUtilityTags(y);
                return y;
            } catch (ClassNotFoundException | IOException e) {
                Debug.log(e);
            }
        }
        return createNew();
    }

    private static ProjectList createNew(){
        return new ProjectList();
    }

    private static void loadUtilityTags(ProjectList projectList) {
        File file2 = new File(Application.getSaveDir(), UTILITY_TAGS_FILE);
        if(file2.exists()) {
            try (FileInputStream fis2 = new FileInputStream(file2);
                 ObjectInputStream ois2 = new ObjectInputStream(fis2)) {
                UtilityTagList utility = (UtilityTagList) ois2.readObject();
                projectList.setUtilityTags(utility);
            } catch (ClassNotFoundException | IOException e) {
                Debug.log(e);
            }
        }
    }

    private static ProjectList legacy(Object x){
        ProjectList n = new ProjectList();
        if(x instanceof projectlogger.ProjectList){
            for (projectlogger.Project p:((projectlogger.ProjectList)x).getProjects()) {
                Project m = new Project(p.getSupertag()+"/"+p.getTag(),p.getCategory(),p.getTag());
                m.setStartDate(p.getStartDate());
                if(p.isFinished()){
                    n.getFinishedProjects().add(m);
                    m.setFinished(true);
                    m.setFinishedDate(p.getFinishedDate());
                } else {
                    n.getActiveProjects().add(m);
                }
            }
        } else if(x instanceof projectlogger.persist.ProjectList){
            for (projectlogger.persist.Project p:((projectlogger.persist.ProjectList)x).getProjects()) {
                Project m = new Project(p.getGroup()+"/"+p.getTag(),p.getCategory(),p.getTag());
                m.setStartDate(p.getStartDate());
                if(p.isFinished()){
                    n.getFinishedProjects().add(m);
                    m.setFinished(true);
                    m.setFinishedDate(p.getFinishedDate());
                } else {
                    n.getActiveProjects().add(m);
                }
            }
        }
        save(n);
        return n;
    }

    synchronized static void save(ProjectList projectList){
        File file = new File(Application.getSaveDir(),PROJECTS_FILE);
        try(FileOutputStream fis = new FileOutputStream(file);
            ObjectOutputStream ois = new ObjectOutputStream(fis)){
            ois.writeObject(projectList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized static void saveUtilityTags(ProjectList projectList) {
        File file2 = new File(Application.getSaveDir(), UTILITY_TAGS_FILE);
        try (FileOutputStream fis2 = new FileOutputStream(file2);
             ObjectOutputStream ois2 = new ObjectOutputStream(fis2)) {
            ois2.writeObject(projectList.getUtilityTags());
        } catch (IOException e) {
            Debug.log(e);
        }
    }
}
