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

package xyz.spiralhalo.sherlock.focus;

import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.EnumerateWindows;
import xyz.spiralhalo.sherlock.TrackerAccessor;
import xyz.spiralhalo.sherlock.TrackerListener;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.persist.project.ProjectList;

import javax.swing.*;
import java.awt.*;

public class FocusMgr implements TrackerListener {
    private final ProjectList projectList;
    private final TrayIcon tray;
    private final long trackerGranularityMillis;
    private FocusView focusView;

    public FocusMgr(ProjectList projectList, TrackerAccessor tracker, TrayIcon tray){
        this.projectList = projectList;
        this.tray = tray;
        trackerGranularityMillis = tracker.getGranularityMillis();
        tracker.addListener(this);
    }

    private FocusView getFocusView(){
        assert SwingUtilities.isEventDispatchThread();
        synchronized (this) {
            if (focusView == null) {
                focusView = new FocusView(FocusState.getInstance().getProject(projectList));
            }
        }
        return focusView;
    }

    private void setFocusViewVisible(boolean visible) {
        SwingUtilities.invokeLater(()-> getFocusView().setVisible(visible));
    }

    private void tryDisposeView(){
        SwingUtilities.invokeLater(()->{
            synchronized (this) {
                if (focusView != null) {
                    getFocusView().dispose();
                    focusView = null;
                }
            }
        });
    }

    ProjectList getProjectList() {
        return projectList;
    }

    public void turnOn(Project project, long durationMillis){
        FocusState state = FocusState.getInstance();
        if(state.isEnabled()){
            tryDisposeView();
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
        tryDisposeView();
    }

    @Override
    public void onTrackerLog(Project project, EnumerateWindows.WindowInfo windowInfo) {
        FocusState state = FocusState.getInstance();
        if(state.isEnabled()){
            if(project != state.getProject(projectList)) {
                setFocusViewVisible(true);
            } else {
                setFocusViewVisible(false);
                if(state.getDuration() >= 0){
                    long newDuration = state.getDuration() - trackerGranularityMillis;
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
            tryDisposeView();
        }
    }
}
