package xyz.spiralhalo.sherlock.util;

import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.neon.icon.ResizableIcon;
import sun.awt.image.ToolkitImage;
import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.Main;
import xyz.spiralhalo.sherlock.res.Res;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class ImgUtil {
    public static Image createImage(String path, String description) {
        URL imageURL = Res.class.getResource(path);

        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }

    public static ResizableIcon autoColorIcon(String path, int initW, int initH) {
        if(Main.currentTheme.foreground==0) {
            return createResizableIcon(path, initW, initH);
        } else {
            return createResizableTintedIcon(path, Main.currentTheme.foreground, initW, initH);
        }
    }

    public static ResizableIcon createResizableIcon(String path, int initW, int initH) {
        try {
            return ImageWrapperResizableIcon.getIcon(loadImage(path), new Dimension(initW, initH));
        } catch (IOException e) {
            Debug.log(e);
            return null;
        }
    }

    public static ResizableIcon createResizableTintedIcon(String path, int rgb, int initW, int initH) {
        try {
            return ImageWrapperResizableIcon.getIcon(colorImage(loadImage(path), rgb), new Dimension(initW, initH));
        } catch (IOException e) {
            Debug.log(e);
            return null;
        }
    }

    public static ImageIcon createIcon(String path, String description) {
        URL imageURL = Res.class.getResource(path);

        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return new ImageIcon(imageURL, description);
        }
    }

    public static ImageIcon createTintedIcon(String path, int rgb){
        try {
            return createTintedIcon(loadImage(path), rgb);
        } catch (IOException e) {
            Debug.log(e);
            return null;
        }
    }

    public static ImageIcon createTintedIcon(Image image, int rgb){
        return new ImageIcon(colorImage(image, rgb));
    }

    public static BufferedImage colorImage(Image src, int rgb) {
        BufferedImage buff;
        if(src instanceof BufferedImage){
            buff = (BufferedImage) src;
        } else if(src instanceof ToolkitImage){
            buff = ((ToolkitImage) src).getBufferedImage();
        } else throw new IllegalArgumentException("Source must be BufferedImage or ToolkitImage");
        BufferedImage tinted = new BufferedImage(buff.getWidth(), buff.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for(int i = 0; i < tinted.getHeight(); i++) {
            for (int j = 0; j < tinted.getWidth(); j++) {
                tinted.setRGB(j, i, buff.getRGB(j, i) ^ rgb);
            }
        }
        return tinted;
    }

    public static BufferedImage loadImage(String path) throws IOException {
        URL imageURL = Res.class.getResource(path);
        BufferedImage img = ImageIO.read(imageURL);
        return img;
    }
}
