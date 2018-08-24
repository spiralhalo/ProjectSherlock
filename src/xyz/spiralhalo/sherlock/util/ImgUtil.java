package xyz.spiralhalo.sherlock.util;

import xyz.spiralhalo.sherlock.res.Images;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class ImgUtil {
    public static Image createImage(String path, String description) {
        URL imageURL = Images.class.getResource(path);

        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
}
