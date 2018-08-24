package xyz.spiralhalo.sherlock.async;

import xyz.spiralhalo.sherlock.util.Debug;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class LoaderDialog extends JDialog {
    private static CompletableFuture completableFuture;
    private static LoaderDialog loaderDialog = new LoaderDialog();

    private static void onCancel() {
        completableFuture.cancel(true);
    }

    public static <T> void execute(JFrame owner, Supplier<T> supplier, BiConsumer<T, ? super Throwable> callback){
        completableFuture = CompletableFuture.supplyAsync(supplier).whenComplete((e,t) -> {
            if(t!=null) Debug.log(LoaderDialog.class,t);
            completableFuture = null;
            loaderDialog.dispose();
            callback.accept(e,t);
        });
        loaderDialog.setLocationRelativeTo(owner);
        loaderDialog.setVisible(true);
    }

    private JPanel contentPane;
    private JButton buttonCancel;
    private JProgressBar progressBar;

    private LoaderDialog() {
        setUndecorated(true);
        setContentPane(contentPane);
        setModal(true);

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        pack();
    }
}
