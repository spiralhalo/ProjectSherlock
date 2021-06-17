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
import java.util.function.BiConsumer;

/**
 * Represents a one-time asynchronous task that handles its results
 * and exceptions within the {@code EventDispatcher} thread.
 *
 * @param <Y> the getType of the result of the task
 */
public abstract class AsyncTask<Y> implements Runnable {
    private boolean started = false;
    private BiConsumer<Y,Throwable> callback;

    protected AsyncTask(){ }

    /**
     * Starts this task with the corresponding callback.
     *
     * This function may only be called once.
     *
     * @param callback the callback function to this task
     */
    public final void start(BiConsumer<Y,Throwable> callback){
        if(started){
            throwException("Running an AsyncTask twice is not allowed");
            return;
        }
        this.callback = callback;
        this.started = true;
        new Thread(this, "AsyncTask"+System.currentTimeMillis()).start();
    }

    /**
     * Represents the primary operation of this task.
     *
     * This method is executed once while the task is running.
     *
     * Any exception thrown by this method will be handled by the
     * callback function in the {@code EventDispatcher} thread.
     *
     * @throws Exception to be handled by the callback function
     */
    abstract protected void doRun() throws Throwable;

    /**
     * Returns the result of this task.
     *
     * Object returned by this function must be set within {@link #doRun()}.
     *
     * @return the result of this task
     */
    abstract protected Y getResult();

    @Override
    public final void run() {
        if(!started) {
            throwException("Running an AsyncTask manually is not allowed");
            return;
        }
        try {
            doRun();
            finish(getResult(), null);
        } catch (Throwable x){
            Debug.log(x);
            finish(null, x);
        }
    }

    /**
     * Throws an {@link AsyncTaskException} to be handled by the callback function.
     *
     * @param message the message for this exception
     */
    private void throwException(String message){
        finish(null, new AsyncTaskException(message));
    }

    /**
     * Finishes the task and execute the callback function in the
     * {@code EventDispatcher} thread with the given parameters.
     *
     * @param result the result of the task
     * @param throwable the exception to be handled by the callback
     */
    private void finish(Y result, Throwable throwable){
        SwingUtilities.invokeLater(() -> callback.accept(result, throwable));
    }

    public static class AsyncTaskException extends Exception {
        public AsyncTaskException(String message) {
            super(message);
        }
    }
}
