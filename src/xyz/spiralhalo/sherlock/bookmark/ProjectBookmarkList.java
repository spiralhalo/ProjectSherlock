package xyz.spiralhalo.sherlock.bookmark;

import xyz.spiralhalo.sherlock.Main;
import xyz.spiralhalo.sherlock.bookmark.persist.Bookmark;
import xyz.spiralhalo.sherlock.bookmark.persist.ProjectBookmarks;
import xyz.spiralhalo.sherlock.persist.project.Project;
import xyz.spiralhalo.sherlock.util.ImgUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.HashMap;

public class ProjectBookmarkList extends JFrame {
    private static final HashMap<Project, ProjectBookmarkList> dialogs = new HashMap<>();

    public static ProjectBookmarkList getDialog(BookmarkMgr mgr, Project p){
        if(!dialogs.containsKey(p)){
            dialogs.put(p,new ProjectBookmarkList(mgr, p));
        }
        return dialogs.get(p);
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

    private final Project p;
    private final ProjectBookmarks bookmarks;
    private final BookmarkMgr manager;

    private ProjectBookmarkList(BookmarkMgr mgr, Project p) {
        super();
        setIconImages(Arrays.asList(ImgUtil.createImage("icon.png","App Icon Small"),
                ImgUtil.createImage("med_icon.png","App Icon")));
        setTitle("Bookmarks");//String.format("Bookmarks for `%s` - force keyword: %s", p.toString(), p.getTags()[0]));
        lblProjectName.setText("Bookmarks for: "+p.toString());
        this.p = p;
        manager = mgr;
        bookmarks = mgr.getOrAdd(p);
        setContentPane(contentPane);
        setMinimumSize(contentPane.getMinimumSize());
        btnLaunch.setEnabled(false);
        btnRemove.setEnabled(false);
        btnMoveUp.setEnabled(false);
        btnMoveDown.setEnabled(false);
        btnEdit .setEnabled(false);
        tblBookmarks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblBookmarks.setModel(bookmarks.getModel());
        tblBookmarks.getSelectionModel().addListSelectionListener(e->tableSelectionChanged());
        tblBookmarks.getColumnModel().getColumn(0).setMaxWidth(50);
        tblBookmarks.getColumnModel().getColumn(1).setMaxWidth(50);
        Main.applyButtonTheme(btnLaunch, btnAdd, btnRemove, btnMoveUp, btnMoveDown, btnEdit);
        btnLaunch.addActionListener(e->launch());
        btnAdd.addActionListener(e->add());
        btnRemove.addActionListener(e->remove());
        btnMoveUp.addActionListener(e->moveUp());
        btnMoveDown.addActionListener(e->moveDown());
        btnEdit .addActionListener(e->edit ());
        tblBookmarks.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if(mouseEvent.getClickCount() == 2){
                    launch();
                }
            }
        });
        checkDelNoConfirm.setSelected(BookmarkConfig.bkmkGBool(BookmarkConfig.BookmarkBool.DEL_NO_CONFIRM));
        checkDelNoConfirm.addActionListener(actionEvent -> BookmarkConfig.bkmkSBool(BookmarkConfig.BookmarkBool.DEL_NO_CONFIRM, checkDelNoConfirm.isSelected()));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
        for (int i = 0; i < 10; i++) {
            final int z = (i+1)%10;
            final int zz = i;
            contentPane.registerKeyboardAction(e -> onKeyPress(zz), KeyStroke.getKeyStroke(KeyEvent.VK_0+z, 0),
                    JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        }
        contentPane.registerKeyboardAction(e -> close(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        pack();
        setLocationRelativeTo(null);
    }

    private void onKeyPress(int i) {
        if(i>=0 && i<tblBookmarks.getRowCount()){
            launchInternal(bookmarks.get(i));
        }
    }

    private void tableSelectionChanged() {
        if(getSelectedIndex()==-1){
            btnLaunch.setEnabled(false);
            btnRemove.setEnabled(false);
            btnMoveUp.setEnabled(false);
            btnMoveDown.setEnabled(false);
            btnEdit .setEnabled(false);
        } else {
            btnLaunch.setEnabled(true);
            btnRemove.setEnabled(true);
            btnMoveUp.setEnabled(true);
            btnMoveDown.setEnabled(true);
            btnEdit .setEnabled(true);
            if(getSelectedIndex()<=0){
                btnMoveUp.setEnabled(false);
            } else if(getSelectedIndex()>=tblBookmarks.getRowCount()-1){
                btnMoveDown.setEnabled(false);
            }
        }
    }

    public void close() {
        dispose();
    }

    private Bookmark getSelected(){
        int i = tblBookmarks.getSelectedRow();
        if(i==-1)return null;
        return bookmarks.get(i);
    }

    private int getSelectedIndex(){
        return tblBookmarks.getSelectedRow();
    }

    private void launch(){
        if(getSelected()!=null){
            launchInternal(getSelected());
        }
    }

    private void launchInternal(Bookmark toLaunch){
        toLaunch.launch(this);
        if(BookmarkConfig.bkmkGBool(BookmarkConfig.BookmarkBool.CLOSE_WINDOW)){
            close();
        }
    }

    private void add(){
        Bookmark x = EditBookmark.add(this, p);
        if(x!=null){
            bookmarks.addOrReplaceUnsaved(x);
        }
        manager.save();
    }

    private void edit(){
        if(getSelectedIndex()==-1)return;
        Bookmark x = EditBookmark.edit(this, p, getSelected());
        if(x!=null){
            bookmarks.editBookmark(getSelectedIndex(), x);
        }
        manager.save();
    }

    private void remove(){
        if(getSelectedIndex()==-1)return;
        if(checkDelNoConfirm.isSelected() || JOptionPane.showConfirmDialog(this,"Remove bookmark?",
                "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
            int selectedIndex = getSelectedIndex();
            bookmarks.remove(selectedIndex);
            if(selectedIndex < tblBookmarks.getRowCount()){
                tblBookmarks.setRowSelectionInterval(selectedIndex, selectedIndex);
            }
        }
        manager.save();
    }

    private void moveUp(){
        int selected = getSelectedIndex();
        bookmarks.moveUp(selected);
        int newSelected = Math.max(0, selected-1);
        tblBookmarks.setRowSelectionInterval(newSelected, newSelected);
        manager.save();
    }

    private void moveDown(){
        int selected = getSelectedIndex();
        bookmarks.moveDown(selected);
        int newSelected = Math.min(tblBookmarks.getRowCount()-1, selected+1);
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
            //remember the last location of mouse
            final Point oldMouseLocation = MouseInfo.getPointerInfo().getLocation();

            //simulate a mouse click on title bar of window
            Robot robot = new Robot();
            robot.mouseMove(this.getX() + 100, this.getY() + 5);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

            //move mouse to old location
            robot.mouseMove((int) oldMouseLocation.getX(), (int) oldMouseLocation.getY());
        } catch (Exception ex) {
            //just ignore exception, or you can handle it as you want
        } finally {
            setAlwaysOnTop(false);
        }
    }
}
