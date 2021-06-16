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

package xyz.spiralhalo.sherlock.util.img;

import sun.awt.image.ToolkitImage;
import xyz.spiralhalo.sherlock.Debug;
import xyz.spiralhalo.sherlock.res.Res;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class ImgUtil extends BufferedImage {

    public static ImgUtil create(Image source) {
        final BufferedImage bufferedSource;
        if (source instanceof BufferedImage) {
            bufferedSource = (BufferedImage) source;
        } else if (source instanceof ToolkitImage) {
            bufferedSource = ((ToolkitImage) source).getBufferedImage();
        } else {
            return new ImgUtil();
        }
        return new ImgUtil(bufferedSource);
    }

    public static ImgUtil create(String path, String description) {
        try {
            final BufferedImage source = load(path);
            return new ImgUtil(source);
        } catch (IOException e) {
            Debug.log(e);
            return new ImgUtil();
        }
    }

    public static ImgUtil createOrDummy(String path, String description, int w, int h, int color) {
        try {
            final BufferedImage source = load(path);
            return new ImgUtil(source);
        } catch (IOException e) {
            Debug.log(e);
            final ImgUtil dummy = new ImgUtil(w, h);
            Graphics2D painter = dummy.createGraphics();
            painter.setPaint(new Color(color));
            painter.drawOval(0, 0, w, h);
            return dummy;
        }
    }

    static ImgUtil create(String path) {
        return create(path, null);
    }

    private ImgUtil() {
        super(0, 0, TYPE_INT_ARGB);
    }

    private ImgUtil(int w, int h) {
        super(w, h, TYPE_INT_ARGB);
    }

    private ImgUtil(BufferedImage source) {
        super(source.getWidth(), source.getHeight(), TYPE_INT_ARGB);
        createGraphics().drawImage(source, 0,0, source.getWidth(), source.getHeight(), null);
    }

    public ImgUtil tint(int tintColor) {
        final int rgb = tintColor & 0x00ffffff;
        final BufferedImage tinted = new BufferedImage(getWidth(), getHeight(), TYPE_INT_ARGB);

        for(int i = 0; i < tinted.getHeight(); i++) {
            for (int j = 0; j < tinted.getWidth(); j++) {
                final int tintedRGB = getRGB(j, i) ^ rgb;
                setRGB(j, i, tintedRGB);
            }
        }

        return this;
    }

    public ImgUtil outline(int outlineColor, int distance) {
        final int outlineRGB = outlineColor & 0x00ffffff;
        final BufferedImage outline = new BufferedImage(getWidth(), getHeight(), TYPE_INT_ARGB);
        final BufferedImage copy = new BufferedImage(getWidth(), getHeight(), TYPE_INT_ARGB);
        final Graphics2D copier = copy.createGraphics();
        final Graphics2D outliner = createGraphics();

        for(int i = 0; i < outline.getHeight(); i++) {
            for (int j = 0; j < outline.getWidth(); j++) {
                outline.setRGB(j, i, (getRGB(j, i) & 0xff000000) | outlineRGB);
            }
        }

        copier.drawImage(this, 0,0, getWidth(), getHeight(), null);
        outliner.drawImage(outline, -distance,-distance, outline.getWidth(), outline.getHeight(), null);
        outliner.drawImage(outline, -distance,distance, outline.getWidth(), outline.getHeight(), null);
        outliner.drawImage(outline, distance,-distance, outline.getWidth(), outline.getHeight(), null);
        outliner.drawImage(outline, distance,distance, outline.getWidth(), outline.getHeight(), null);
        outliner.drawImage(copy, 0,0, copy.getWidth(), copy.getHeight(), null);

        return this;
    }

    private static BufferedImage load(String path) throws IOException {
        final URL imageURL = Res.class.getResource(path);

        if (imageURL == null) {
            throw new FileNotFoundException("Resource not found: " + path);
        } else {
            return ImageIO.read(imageURL);
        }
    }
}
