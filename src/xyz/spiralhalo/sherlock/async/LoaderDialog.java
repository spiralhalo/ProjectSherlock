package xyz.spiralhalo.sherlock.async;

import xyz.spiralhalo.sherlock.Debug;

import javax.swing.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class LoaderDialog extends JDialog {
    private static CompletableFuture completableFuture;
    private static LoaderDialog loaderDialog = new LoaderDialog();

    private static void onCancel() {
        completableFuture.cancel(true);
    }

    public static <T> void execute(JFrame owner, AsyncTask<T> supplier, BiConsumer<T, ? super Throwable> callback){
        supplier.start((e,t) -> {
            if(t!=null) Debug.log(t);
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

        buttonCancel.addActionListener(e -> onCancel());

        pack();
    }
}
