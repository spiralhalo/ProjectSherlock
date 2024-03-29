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

package xyz.spiralhalo.sherlock.bookmark;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

import xyz.spiralhalo.sherlock.Application;
import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.Main;
import xyz.spiralhalo.sherlock.bookmark.persist.Bookmark;
import xyz.spiralhalo.sherlock.bookmark.persist.BookmarkType;
import xyz.spiralhalo.sherlock.bookmark.persist.ProjectBookmarks;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.util.img.IconUtil;

public class ProjectBookmarkList extends JFrame {
	private static final HashMap<Project, ProjectBookmarkList> dialogs = new HashMap<>();

	public static ProjectBookmarkList getDialog(BookmarkMgr mgr, Project p) {
		if (!dialogs.containsKey(p)) {
			dialogs.put(p, new ProjectBookmarkList(mgr, p));
		}
		return dialogs.get(p);
	}

	private static class TrailingLabel extends DefaultTableCellRenderer {
		public TrailingLabel() {
			super();
		}

		@Override
		public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b1, int i, int i1) {
			if (o instanceof Bookmark) {
				super.getTableCellRendererComponent(jTable, ((Bookmark) o).getValue(), b, b1, i, i1);
				this.setHorizontalAlignment(LEADING);
				BookmarkType type = ((Bookmark) o).getType();
				if (type == BookmarkType.URL) {
					this.setIcon(iconURL);
				} else {
					if (new File(((Bookmark) o).getValue()).isDirectory()) {
						this.setIcon(iconFolder);
					} else {
						this.setIcon(iconFile);
					}
				}
				if (this.getFont() == null) {
					return this;
				}
				String inputText = ((Bookmark) o).getValue();
				String ellipses = "...";
				FontMetrics fm = this.getFontMetrics(this.getFont());
				int labelWidth = jTable.getColumnModel().getColumn(i1).getWidth() - 26;
				int inputWidth = fm.stringWidth(inputText);
				int ellipsesWidth = fm.stringWidth(ellipses);
				// super weird ellipses hax
				if (inputWidth > labelWidth && ellipsesWidth < labelWidth) {
					int inputLen = inputText.length();
					int newLen = Math.round((float) inputLen * (float) (labelWidth - ellipsesWidth) / (float) inputWidth) - 1;
					String result1 = inputText.substring(0, newLen / 2);
					String result2 = inputText.substring(inputLen - newLen / 2, inputLen);
					// try to trim nicely on slashes ??? should probably change this algorithm later
					int forslashindex1 = result1.lastIndexOf("/");
					if (forslashindex1 > 0 && forslashindex1 < result1.length() - 1) {
						result1 = result1.substring(0, forslashindex1 + 1);
					} else {
						int backslashindex1 = result1.lastIndexOf("\\");
						if (backslashindex1 > 0 && backslashindex1 < result1.length() - 1) {
							result1 = result1.substring(0, backslashindex1 + 1);
						}
					}
					super.setText(result1 + ellipses + result2);
				} else {
					super.setText(inputText);
				}
				return this;
			} else {
				super.getTableCellRendererComponent(jTable, "(" + o.toString() + ")", b, b1, i, i1);
				this.setHorizontalAlignment(CENTER);
				this.setIcon(null);
				return this;
			}
		}
	}

	private JPanel contentPane;
	private JTable tblBookmarks;
	private JButton btnLaunch;
	private JButton btnAdd;
	private JButton btnRemove;
	private JButton btnMoveUp;
	private JButton btnMoveDown;
	private JButton btnEdit;
	private JLabel lblProjectName;
	private JCheckBox checkDelNoConfirm;
	private JScrollPane scrollPane;
	private JPopupMenu tablePopUp;
	private JMenuItem menuLaunch;
	private JMenuItem menuOpenFolder;

	private final Project p;
	private final ProjectBookmarks bookmarks;
	private final BookmarkMgr manager;

	private static ImageIcon iconFile;
	private static ImageIcon iconFolder;
	private static ImageIcon iconURL;

	private ProjectBookmarkList(BookmarkMgr mgr, Project p) {
		super();
		setTitle("Bookmarks");// String.format("Bookmarks for `%s` - force keyword: %s", p.toString(), p.getTags()[0]));

		setIconImages(Application.getAppIcons());

		// load bookmark icons
		if (iconFile == null) {
			iconFile = IconUtil.createAutoColor("sm_file.png");
			iconFolder = IconUtil.createAutoColor("sm_folder.png");
			iconURL = IconUtil.createAutoColor("sm_url.png");
		}

		lblProjectName.setText("Bookmarks for: " + p.toString());
		this.p = p;
		manager = mgr;
		bookmarks = mgr.getOrAdd(p);
		setContentPane(contentPane);
		setMinimumSize(contentPane.getMinimumSize());
		setPreferredSize(new Dimension(BookmarkConfig.bkmkGInt(BookmarkConfig.BookmarkInt.PREFERRED_WINDOW_WIDTH), BookmarkConfig.bkmkGInt(BookmarkConfig.BookmarkInt.PREFERRED_WINDOW_HEIGHT)));
		tablePopUp = new JPopupMenu();
		tablePopUp.add(menuLaunch = new JMenuItem("Open"));
		tablePopUp.add(menuOpenFolder = new JMenuItem("Open file location"));
		tblBookmarks.addMouseListener(tblAdapter);
		btnLaunch.setEnabled(false);
		btnRemove.setEnabled(false);
		btnMoveUp.setEnabled(false);
		btnMoveDown.setEnabled(false);
		btnEdit.setEnabled(false);
		tblBookmarks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblBookmarks.setModel(bookmarks.getModel());
		tblBookmarks.getSelectionModel().addListSelectionListener(e -> tableSelectionChanged());
		tblBookmarks.getColumnModel().getColumn(0).setMaxWidth(50);
		tblBookmarks.setDefaultRenderer(Object.class, new TrailingLabel());
		Main.applyButtonTheme(btnLaunch, btnAdd, btnRemove, btnMoveUp, btnMoveDown, btnEdit);
		btnLaunch.addActionListener(e -> launch());
		menuLaunch.addActionListener(e -> launch());
		menuOpenFolder.addActionListener(e -> openFolder());
		btnAdd.addActionListener(e -> add());
		btnRemove.addActionListener(e -> remove());
		btnMoveUp.addActionListener(e -> moveUp());
		btnMoveDown.addActionListener(e -> moveDown());
		btnEdit.addActionListener(e -> edit());
		tblBookmarks.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() == 2) {
					launch();
				}
			}
		});
		scrollPane.setDropTarget(new DropTarget(scrollPane,
				new DropTargetAdapter() {
					@Override
					public void drop(DropTargetDropEvent dtde) {
						try {
							dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
							// intended for Windows only
							java.util.List<File> dropppedFiles = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
							File f = dropppedFiles.get(0);
							String path = f.getPath();
							BookmarkType type;
							String value;
							if (path.toLowerCase().endsWith(".url")) {
								type = BookmarkType.URL;
								String content = new Scanner(f).useDelimiter("\\Z").next();
								value = content.substring(content.indexOf("=") + 1);
							} else {
								type = BookmarkType.FILE;
								value = path;
							}
							bookmarks.addOrReplaceUnsaved(new Bookmark(type, value));
							manager.save();
						} catch (Exception e) {
							Debug.log(e);
						}
					}
				}));
		checkDelNoConfirm.setSelected(BookmarkConfig.bkmkGBool(BookmarkConfig.BookmarkBool.DEL_NO_CONFIRM));
		checkDelNoConfirm.addActionListener(actionEvent -> BookmarkConfig.bkmkSBool(BookmarkConfig.BookmarkBool.DEL_NO_CONFIRM, checkDelNoConfirm.isSelected()));
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close();
			}
		});
		for (int i = 0; i < 10; i++) {
			final int z = (i + 1) % 10;
			final int zz = i;
			contentPane.registerKeyboardAction(e -> onKeyPress(zz), KeyStroke.getKeyStroke(KeyEvent.VK_0 + z, 0),
					JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		}
		contentPane.registerKeyboardAction(e -> close(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		pack();
		setLocationRelativeTo(null);
	}

	private final MouseAdapter tblAdapter = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == 3) {
				int row = tblBookmarks.rowAtPoint(e.getPoint());
				int column = tblBookmarks.columnAtPoint(e.getPoint());

				if (!tblBookmarks.isRowSelected(row))
					tblBookmarks.changeSelection(row, column, false, false);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == 3) {
				int selected = tblBookmarks.getSelectedRow();
				if (selected >= 0 && selected < bookmarks.size() && bookmarks.get(selected).getType() == BookmarkType.FILE) {
					menuOpenFolder.setEnabled(true);
				} else {
					menuOpenFolder.setEnabled(false);
				}
				tablePopUp.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	};

	private void onKeyPress(int i) {
		if (i >= 0 && i < tblBookmarks.getRowCount()) {
			launchInternal(bookmarks.get(i));
		}
	}

	private void tableSelectionChanged() {
		if (getSelectedIndex() == -1) {
			btnLaunch.setEnabled(false);
			btnRemove.setEnabled(false);
			btnMoveUp.setEnabled(false);
			btnMoveDown.setEnabled(false);
			btnEdit.setEnabled(false);
		} else {
			btnLaunch.setEnabled(true);
			btnRemove.setEnabled(true);
			btnMoveUp.setEnabled(true);
			btnMoveDown.setEnabled(true);
			btnEdit.setEnabled(true);
			if (getSelectedIndex() <= 0) {
				btnMoveUp.setEnabled(false);
			} else if (getSelectedIndex() >= tblBookmarks.getRowCount() - 1) {
				btnMoveDown.setEnabled(false);
			}
		}
	}

	public void close() {
		Dimension size = getSize();
		BookmarkConfig.bkmkSInt(BookmarkConfig.BookmarkInt.PREFERRED_WINDOW_WIDTH, size.width);
		BookmarkConfig.bkmkSInt(BookmarkConfig.BookmarkInt.PREFERRED_WINDOW_HEIGHT, size.height);
		dispose();
	}

	private Bookmark getSelected() {
		int i = tblBookmarks.getSelectedRow();
		if (i == -1) return null;
		return bookmarks.get(i);
	}

	private int getSelectedIndex() {
		return tblBookmarks.getSelectedRow();
	}

	private void launch() {
		if (getSelected() != null) {
			launchInternal(getSelected());
		}
	}

	private void launchInternal(Bookmark toLaunch) {
		if (toLaunch.launch(this) && BookmarkConfig.bkmkGBool(BookmarkConfig.BookmarkBool.CLOSE_WINDOW)) {
			close();
		}
	}

	private void openFolder() {
		if (getSelected() != null) {
			Bookmark selected = getSelected();
			if (selected.getType() == BookmarkType.FILE) {
				String path = selected.getValue();
				File folder = new File(path).getParentFile();
				try {
					Desktop.getDesktop().open(folder);
				} catch (IllegalArgumentException | FileNotFoundException e) {
					JOptionPane.showMessageDialog(this, String.format("Folder not found: %s", folder.getPath()),
							"Failure", JOptionPane.ERROR_MESSAGE);
					Debug.log(e);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, String.format("Can't open folder: %s", folder.getPath()),
							"Failure", JOptionPane.ERROR_MESSAGE);
					Debug.log(e);
				}
			}
		}
	}

	private void add() {
		Bookmark x = EditBookmark.add(this, p);
		if (x != null) {
			bookmarks.addOrReplaceUnsaved(x);
		}
		manager.save();
	}

	private void edit() {
		if (getSelectedIndex() == -1) return;
		Bookmark x = EditBookmark.edit(this, p, getSelected());
		if (x != null) {
			bookmarks.editBookmark(getSelectedIndex(), x);
		}
		manager.save();
	}

	private void remove() {
		if (getSelectedIndex() == -1) return;
		if (checkDelNoConfirm.isSelected() || JOptionPane.showConfirmDialog(this, "Remove bookmark?",
				"Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			int selectedIndex = getSelectedIndex();
			bookmarks.remove(selectedIndex);
			if (selectedIndex < tblBookmarks.getRowCount()) {
				tblBookmarks.setRowSelectionInterval(selectedIndex, selectedIndex);
			}
		}
		manager.save();
	}

	private void moveUp() {
		int selected = getSelectedIndex();
		bookmarks.moveUp(selected);
		int newSelected = Math.max(0, selected - 1);
		tblBookmarks.setRowSelectionInterval(newSelected, newSelected);
		manager.save();
	}

	private void moveDown() {
		int selected = getSelectedIndex();
		bookmarks.moveDown(selected);
		int newSelected = Math.min(tblBookmarks.getRowCount() - 1, selected + 1);
		tblBookmarks.setRowSelectionInterval(newSelected, newSelected);
		manager.save();
	}

	@Override
	public void dispose() {
		dialogs.remove(this.p);
		super.dispose();
	}

	public void forceShow() {
		setVisible(true);
		setState(NORMAL);
		setAlwaysOnTop(true);
		try {
			// remember the last location of mouse
			final Point oldMouseLocation = MouseInfo.getPointerInfo().getLocation();

			// simulate a mouse click on title bar of window
			Robot robot = new Robot();
			robot.mouseMove(this.getX() + 100, this.getY() + 5);
			robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

			// move mouse to old location
			robot.mouseMove((int) oldMouseLocation.getX(), (int) oldMouseLocation.getY());
		} catch (Exception ex) {
			// just ignore exception, or you can handle it as you want
		} finally {
			setAlwaysOnTop(false);
		}
	}
}
