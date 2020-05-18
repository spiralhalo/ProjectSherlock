package xyz.spiralhalo.sherlock.bookmark;

import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.bookmark.persist.Bookmark;
import xyz.spiralhalo.sherlock.bookmark.persist.BookmarkType;
import xyz.spiralhalo.sherlock.persist.project.Project;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class EditBookmark extends JDialog {

    public static Bookmark add(JFrame parent, Project p) {
        EditBookmark x = new EditBookmark(parent, p, false, null);
        x.setVisible(true);
        return x.result;
    }

    public static Bookmark edit(JFrame parent, Project p, Bookmark old) {
        EditBookmark x = new EditBookmark(parent, p, true, old);
        x.setVisible(true);
        return x.result;
    }

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox comboType;
    private JTextField textFile;
    private JButton btnBrowse;
    private JTextField textURL;
    private JLabel lblProject;
    private JPanel panelFile;
    private JLabel lblURL;
    private JLabel lblFile;
    private JPanel panelDrop;

    private Bookmark result;
    private final JFileChooser fileChooser = new JFileChooser();

    private EditBookmark(JFrame parent, Project p, boolean edit, Bookmark old) {
        super(parent, String.format("%s"/* - force keyword: %s"*/, edit?"Edit bookmark":"Add bookmark"/*, p.getTags()[0]*/));
        setContentPane(contentPane);
        setMinimumSize(contentPane.getMinimumSize());
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        lblProject.setText(String.valueOf(p));
        panelDrop.setDropTarget(new DropTarget(panelDrop,
                new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    //intended for Windows only
                    List<File> dropppedFiles = (List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    textFile.setText(dropppedFiles.get(0).getPath());
                } catch (UnsupportedFlavorException | IOException e) {
                    Debug.log(e);
                }
            }
        }));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        comboType.addItemListener(e->typeChange());
        comboType.setModel(new DefaultComboBoxModel(new String[]{"File", "URL"}));
        typeChange();
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        btnBrowse.addActionListener(e -> onBrowse());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        pack();
        setLocationRelativeTo(parent);
        if(edit){
            if(old.getType()==BookmarkType.URL){
                comboType.setSelectedIndex(1);
                textURL.setText(old.getValue());
            } else {
                textFile.setText(old.getValue());
            }
        }
    }

    private void onBrowse() {
        if(fileChooser.showDialog(this, "Select") == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            textFile.setText(selected.getPath());
        }
    }

    private void typeChange(){
        boolean x = comboType.getSelectedIndex()==0;
        lblFile.setVisible(x);
        panelFile.setVisible(x);
        panelDrop.setVisible(x);
        lblURL.setVisible(!x);
        textURL.setVisible(!x);
    }

    private void onOK() {
        BookmarkType type = comboType.getSelectedIndex() == 0?BookmarkType.FILE:BookmarkType.URL;
        if(type==BookmarkType.FILE) {
            result = new Bookmark(BookmarkType.FILE, textFile.getText());
        } else {
            result = new Bookmark(BookmarkType.URL, textURL.getText());
        }
        dispose();
    }

    private void onCancel() {
        dispose();
    }
}
