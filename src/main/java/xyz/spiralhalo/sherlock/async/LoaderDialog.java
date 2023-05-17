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

import java.awt.*;
import java.util.function.BiConsumer;

import javax.swing.*;

import xyz.spiralhalo.sherlock.Debug;

public class LoaderDialog extends JDialog {
	//    private static CompletableFuture completableFuture;
	private static LoaderDialog loaderDialog = new LoaderDialog();

//    private static void onCancel() {
//        completableFuture.cancel(true);
//    }

	public static <T> void execute(Component parent, AsyncTask<T> supplier, BiConsumer<T, ? super Throwable> callback) {
		supplier.start((e, t) -> {
			if (t != null) Debug.log(t);
//            completableFuture = null;
			loaderDialog.dispose();
			callback.accept(e, t);
		});
		loaderDialog.setLocationRelativeTo(parent);
		loaderDialog.setVisible(true);
	}

	private JPanel contentPane;
	private JButton buttonCancel;
	private JProgressBar progressBar;

	private LoaderDialog() {
		setUndecorated(true);
		setContentPane(contentPane);
		setModal(true);

//        buttonCancel.addActionListener(e -> onCancel());

		pack();
	}
}
