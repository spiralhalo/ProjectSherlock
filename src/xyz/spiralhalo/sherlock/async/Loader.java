package xyz.spiralhalo.sherlock.async;

import xyz.spiralhalo.sherlock.util.Debug;

import javax.swing.*;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class Loader<T> implements BiConsumer<T,Throwable> {
    private static final HashSet<String> runningIds = new HashSet<>();

    public static <T> void execute(String id, Supplier<T> supplier, BiConsumer<T, ? super Throwable> callback){
        execute(id, supplier, callback, null, null);
    }

    public static <T> void execute(String id, Supplier<T> supplier, BiConsumer<T, ? super Throwable> callback,
                                   JComponent toShow, JComponent toHide){
        if(runningIds.contains(id)){
            // silently rejects
            // callback.accept(null, new Exception("Another process is currently running."));
            return;
        }
        runningIds.add(id);
        CompletableFuture.supplyAsync(supplier).whenComplete(new Loader<>(callback,toShow, toHide, id));
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
        if(t!=null) Debug.log(Loader.class,t);
        callback.accept(e,t);
        if(toShow != null) {
            toShow.setVisible(false);
            toHide.setVisible(true);
        }
        runningIds.remove(id);
    }
}
