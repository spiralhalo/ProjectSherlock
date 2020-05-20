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

    public static Icon autoColorIcon(Icon source) {
        if(Main.currentTheme.foreground!=0 && source instanceof ImageIcon) {
            return createTintedIcon(((ImageIcon) source).getImage(), Main.currentTheme.foreground);
        } else {
            return source;
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
        rgb = rgb & 0x00ffffff;
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

    public static BufferedImage outlineImage(Image src, int outlineColor, int distance) {
        BufferedImage buff;
        outlineColor = outlineColor & 0x00ffffff;
        if(src instanceof BufferedImage){
            buff = (BufferedImage) src;
        } else if(src instanceof ToolkitImage){
            buff = ((ToolkitImage) src).getBufferedImage();
        } else throw new IllegalArgumentException("Source must be BufferedImage or ToolkitImage");
        BufferedImage outline = new BufferedImage(buff.getWidth(), buff.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for(int i = 0; i < outline.getHeight(); i++) {
            for (int j = 0; j < outline.getWidth(); j++) {
                outline.setRGB(j, i, (buff.getRGB(j, i) & 0xff000000) | outlineColor);
            }
        }
        BufferedImage outlined = new BufferedImage(buff.getWidth(), buff.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = outlined.createGraphics();
        g2.drawImage(outline, -distance,-distance, outline.getWidth(), outline.getHeight(), null);
        g2.drawImage(outline, -distance,distance, outline.getWidth(), outline.getHeight(), null);
        g2.drawImage(outline, distance,-distance, outline.getWidth(), outline.getHeight(), null);
        g2.drawImage(outline, distance,distance, outline.getWidth(), outline.getHeight(), null);
        g2.drawImage(buff, 0,0, buff.getWidth(), buff.getHeight(), null);
        return outlined;
    }

    public static BufferedImage loadImage(String path) throws IOException {
        URL imageURL = Res.class.getResource(path);
        return ImageIO.read(imageURL);
    }
}
