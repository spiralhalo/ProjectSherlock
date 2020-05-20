package xyz.spiralhalo.sherlock.ocr;

import net.sourceforge.tess4j.Tesseract;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class OCREngine {
    private static final Tesseract tesseract;
    static {
        tesseract = new Tesseract();
        tesseract.setDatapath(System.getProperty("user.dir")+"\\tessdata");
    }
    public static String doOCR(BufferedImage img, boolean invert, int threshold) throws Exception {
//        monoImage(img, invert, threshold);
        ImageIO.write(img, "png", new File(System.getProperty("user.home"), "Desktop\\testOCR.png"));
        return tesseract.doOCR(img);
    }

//    private static void monoImage(BufferedImage img, boolean invert, int threshold){ //also invert
//        for (int x = 0; x < img.getWidth(); x++) {
//            for (int y = 0; y < img.getHeight(); y++) {
//                int rgba = img.getRGB(x, y);
//                Color col = new Color(rgba, true);
//                if (col.getRed() + col.getGreen() + col.getBlue() > threshold) //382
//                    img.setRGB(x, y, invert?0xff000000:0xffffffff);
//                else
//                    img.setRGB(x, y, invert?0xffffffff:0xff000000);
//            }
//        }
//    }
}
