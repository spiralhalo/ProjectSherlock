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

package xyz.spiralhalo.sherlock.async;

import xyz.spiralhalo.sherlock.Debug;

import javax.swing.*;
import java.util.HashSet;
import java.util.function.BiConsumer;

public class Loader<T> implements BiConsumer<T,Throwable> {
    private static final HashSet<String> runningIds = new HashSet<>();

    public static <T> void execute(String id, AsyncTask<T> asyncTask, BiConsumer<T, ? super Throwable> callback,
                                   JComponent toShow, JComponent toHide){
        if(runningIds.contains(id)){
            // silently rejects
            // callback.accept(null, new Exception("Another process is currently running."));
            return;
        }
        runningIds.add(id);
        asyncTask.start(new Loader<>(callback,toShow, toHide, id));
        if(toShow == null)return;
        toShow.setVisible(true);
        toHide.setVisible(false);
    }

    private BiConsumer<T, ? super Throwable> callback;
    private JComponent toShow;
    private JComponent toHide;
    private String id;

    private Loader(BiConsumer<T, ? super Throwable> callback, JComponent toShow, JComponent toHide, String id) {
        this.callback = callback;
        this.toShow = toShow;
        this.toHide = toHide;
        this.id = id;
    }

    @Override
    public void accept(T e, Throwable t) {
        if(t!=null) Debug.log(t);
        try {
            callback.accept(e, t);
        } catch (Throwable ignored){}
        finally {
            if(toShow != null) {
                toShow.setVisible(false);
                toHide.setVisible(true);
            }
            runningIds.remove(id);
        }
    }
}
