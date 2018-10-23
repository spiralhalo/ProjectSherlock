package xyz.spiralhalo.sherlock.focus;

import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.TrackerAccessor;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;

import javax.swing.*;
import java.awt.*;

public class FocusMgr {
    private final ProjectList projectList;
    private FocusView focusView;

    public FocusMgr(ProjectList projectList, TrackerAccessor tracker, TrayIcon tray){
        this.projectList = projectList;
        tracker.addListener((project, windowTitle, exe) -> {
            FocusState state = FocusState.getInstance();
            if(state.isEnabled()){
                if(project != state.getProject(projectList)) {
                    getFocusView().setVisible(true);
                } else {
                    getFocusView().setVisible(false);
                    if(state.getDuration() >= 0){
                        long newDuration = state.getDuration() - tracker.getGranularityMillis();
                        state.setDuration(newDuration);
                        if(newDuration <= 0){
                            turnOff();
                            if(tray != null) {
                                tray.displayMessage("Focus time is over",
                                        String.format("Focus time for \"%s\" is over! [Project Sherlock]",
                                                project.getName()), TrayIcon.MessageType.INFO);
                            }
                        }
                    }
                }
            } else {
                attemptDisposeView();
            }
        });
    }

    private FocusView getFocusView(){
        synchronized (this) {
            if (focusView == null) {
                LookAndFeel current = UIManager.getLookAndFeel();
                boolean changed = false;
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    changed = true;
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                    Debug.log(e);
                }
                focusView = new FocusView(FocusState.getInstance().getProject(projectList));
                if(changed){
                    try {
                        UIManager.setLookAndFeel(current);
                    } catch (UnsupportedLookAndFeelException e) {
                        Debug.log(e);
                    }
                }
            }
        }
        return focusView;
    }

    private void attemptDisposeView(){
        synchronized (this) {
            if (focusView != null) {
                getFocusView().dispose();
                focusView = null;
            }
        }
    }

    ProjectList getProjectList() {
        return projectList;
    }

    public void turnOn(Project project, long durationMillis){
        FocusState state = FocusState.getInstance();
        if(state.isEnabled()){
            attemptDisposeView();
        }
        if(durationMillis > 0){
            state.setDuration(durationMillis);
        } else {
            state.setDuration(-1);
        }
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
