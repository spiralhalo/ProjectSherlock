package xyz.spiralhalo.sherlock.util;

import xyz.spiralhalo.sherlock.res.Images;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
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

    public static ImageIcon createTintedIcon(String path, int rgb){
        try {
            return new ImageIcon(colorImage(loadImage(path), rgb));
        } catch (IOException e) {
            Debug.log(e);
            return null;
        }
    }

    public static BufferedImage colorImage(BufferedImage img, int rgb) {
        for(int i = 0; i < img.getHeight(); i++) {
            for(int j = 0; j < img.getWidth(); j++) {
                img.setRGB(j,i,img.getRGB(j,i)^rgb);
            }
        }
        return img;
    }

    public static BufferedImage loadImage(String path) throws IOException {
        URL imageURL = Images.class.getResource(path);
        BufferedImage img = ImageIO.read(imageURL);
        return img;
    }
}
