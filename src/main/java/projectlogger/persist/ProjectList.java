package projectlogger.persist;

import java.io.*;
import java.util.ArrayList;

public class ProjectList implements Serializable {
    public static final long serialVersionUID = 1L;
    private ArrayList<Project> projects;

    public ArrayList<Project> getProjects() {
        return projects;
    }
}