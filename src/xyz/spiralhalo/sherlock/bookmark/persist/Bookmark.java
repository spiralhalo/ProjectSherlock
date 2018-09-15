package xyz.spiralhalo.sherlock.bookmark.persist;

import xyz.spiralhalo.sherlock.Debug;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

public class Bookmark implements Serializable {
    public static final long serialVersionUID = 1L;

    private final BookmarkType type;
    private final String value;

    public Bookmark(BookmarkType type, String value) {
        this.type = type;
        this.value = value;
    }

    public BookmarkType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Bookmark){
            return ((Bookmark) obj).type == type && ((Bookmark) obj).value.equals(value);
        }
        return super.equals(obj);
    }

    public void launch(JFrame origin) {
            if(Desktop.isDesktopSupported()){
                Desktop desktop = Desktop.getDesktop();
                switch (type) {
                    case FILE:
                        try {
                            desktop.open(new File(value));
                        } catch (FileNotFoundException e) {
                            JOptionPane.showMessageDialog(origin, String.format("File not found: %s", value),
                                    "Failed to open bookmark", JOptionPane.ERROR_MESSAGE);
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(origin, String.format("Can't open file: %s", value),
                                    "Failed to open bookmark", JOptionPane.ERROR_MESSAGE);
                            Debug.log(e);
                        }
                        break;
                    case URL:
                        try {
                            desktop.browse(new URI(value));
                        } catch (URISyntaxException e) {
                            JOptionPane.showMessageDialog(origin, String.format("Invalid URL: %s", value),
                                    "Failed to open bookmark", JOptionPane.ERROR_MESSAGE);
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(origin, String.format("Can't open URL: %s", value),
                                    "Failed to open bookmark", JOptionPane.ERROR_MESSAGE);
                            Debug.log(e);
                        }
                        break;
                }
            }else{
                Runtime runtime = Runtime.getRuntime();
                try {
                    runtime.exec("xdg-open " + value);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(origin, String.format("Can't open: %s", value),
                            "Failed to open bookmark", JOptionPane.ERROR_MESSAGE);
                    Debug.log(e);
                }
            }
        }
}
