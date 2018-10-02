package xyz.spiralhalo.sherlock.report.factory.charts;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.Serializable;

public class StripedPaint implements Paint, Serializable {
    public static final long serialVersionUID = 1L;
    private transient TexturePaint internalPaint;
    private final Color color1, color2;

    public StripedPaint(Color color1, Color color2){
        this.color1 = color1;
        this.color2 = color2;
    }

    private void createTexturePaint(){
        if(internalPaint==null) {
            BufferedImage bi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
            Graphics2D big = bi.createGraphics();
            big.setColor(color1);
            big.fillRect(0, 0, 4, 4);
            big.setColor(color2);
            big.drawLine(1, 0, 3, 2);
            big.drawLine(0, 0, 3, 3);
            big.drawLine(0, 3, 0, 3);
            Rectangle anchor = new Rectangle(0, 0, 4, 4);
            internalPaint = new TexturePaint(bi, anchor);
        }
    }

    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
        createTexturePaint();
        return internalPaint.createContext(cm, deviceBounds, userBounds, xform, hints);
    }

    @Override
    public int getTransparency() {
        createTexturePaint();
        return internalPaint.getTransparency();
    }
}
