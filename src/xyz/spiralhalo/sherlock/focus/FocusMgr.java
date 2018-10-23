package xyz.spiralhalo.sherlock.focus;

import xyz.spiralhalo.sherlock.TrackerAccessor;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;

public class FocusMgr {
    private final ProjectList projectList;
    private FocusView focusView;

    public FocusMgr(ProjectList projectList, TrackerAccessor tracker){
        this.projectList = projectList;
        tracker.addListener((project, windowTitle, exe) -> {
            FocusState state = FocusState.getInstance();
            if(state.isEnabled() && project != state.getProject(projectList)){
                if(!getFocusView().getView().isVisible()) {
                    getFocusView().getView().setVisible(true);
                }
            } else {
                attemptDisposeView();
            }
        });
    }

    private FocusView getFocusView(){
        if(focusView == null){
            focusView = new FocusView(FocusState.getInstance().getProject(projectList));
        }
        return focusView;
    }

    private void attemptDisposeView(){
        if(focusView != null) {
            getFocusView().getView().dispose();
            focusView = null;
        }
    }

    public void turnOn(Project project){
        FocusState state = FocusState.getInstance();
        state.setProject(project.getHash());
        state.setEnabled(true);
    }

    public void turnOff(){
        FocusState state = FocusState.getInstance();
        state.setProject(-1);
        state.setEnabled(false);
        attemptDisposeView();
    }
}
