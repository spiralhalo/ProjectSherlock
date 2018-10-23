package xyz.spiralhalo.sherlock.focus;

import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;
import xyz.spiralhalo.sherlock.persist.settings.IniHandler;

import java.lang.ref.WeakReference;

public class FocusState {

    private static WeakReference<FocusState> instance;

    public static FocusState getInstance() {
        if(instance == null || instance.get() == null){
            instance = new WeakReference<>(new FocusState());
        }
        return instance.get();
    }

    private FocusState(){ }

    public Project getProject(ProjectList projectList){
        long h = IniHandler.getInstance().getLong("focus", "project", -1);
        Project p = projectList.findByHash(h);
        return p;
    }

    public void setProject(long hash){
        IniHandler.getInstance().putLong("focus", "project", hash);
    }

    public boolean isEnabled(){
        return IniHandler.getInstance().getBoolean("focus", "enabled", false);
    }

    public void setEnabled(boolean enabled){
        IniHandler.getInstance().putBoolean("focus", "enabled", enabled);
    }
}
