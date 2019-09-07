package xyz.spiralhalo.sherlock.util;

import xyz.spiralhalo.sherlock.report.factory.charts.StripedPaint;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ColorUtil {
    public static Color foreground = new Color(0,0,0);
    public static Color trans = new Color(0,0,0, 0);
    public static Color bad = new Color(230,124,115);
    public static Color neu = new Color(255,214,102);
    public static Color gut = new Color(87,187,138);
    public static Color gutw = new Color(57,127,88);
    public static Color lite = new Color(0xff66ff);
    public static Color med = new Color(0x00ff00);
    public static Color excel = new Color(0x00ffff);
    public static Color light_gray = new Color(225,225,225);
    public static Color gray = new Color(160,160,160);
    public static Color white = new Color(250,250,250);

//    public static StripedPaint CONST_IDLE_PAINT = new StripedPaint(multiply(gray,gray), multiply(gray,light_gray));
//    public static StripedPaint CONST_LEFT_PAINT = new StripedPaint(gutw, multiply(gutw,gut));
//    public static StripedPaint CONST_IDLE_PAINT = new StripedPaint(gray,new Color(0,64,118, 200));
//    public static StripedPaint CONST_LEFT_PAINT = new StripedPaint(gray,new Color(0,64,118, 127));
    public static Color CONST_IDLE_PAINT = new Color(160,160,160, 127);
    public static Color CONST_LEFT_PAINT = new Color(87,187,138, 160);
    public static Color CONST_OTHER_GRAY = gray;
    public static Color CONST_DELETED_RED_GRAY = new Color(210,190,190);

    public static Color multiply(Color color1, Color color2){
        return new Color((color1.getRed()*color2.getRed())/255,
                (color1.getGreen()*color2.getGreen())/255,
                (color1.getBlue()*color2.getBlue())/255);
    }

    public static Color interpolateNicely(float t, Color c1, Color c2, Color c3){
        if(t>0.5f) return interpolateNicely(t*2f-1f, c2, c3);
        if(t<0.5f) return interpolateNicely(t*2f, c1, c2);
        return c2;
    }

    public static Color interpolateNicely(float t, Color color1, Color color2){
        if(t>=1.0f)return color2;
        float[] a = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null) ,
                b = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);
        float h=0;

        if(b[1]==0){
            h = a[0];
        } else {
            float d = b[0] - a[0];
            float tx = t;
            if (a[0] > b[0]) {
                // Swap (a[0], b[0])
                float h3 = b[0];
                b[0] = a[0];
                a[0] = h3;

                d = -d;
                tx = 1 - tx;
            }

            if (d > 0.5) // 180deg
            {
                a[0] = a[0] + 1; // 360deg
                h = (a[0] + tx * (b[0] - a[0])) % 1; // 360deg
            }
            if (d <= 0.5) // 180deg
            {
                h = a[0] + tx * d;
            }
        }
        return Color.getHSBColor(h, // H
                a[1] + t * (b[1]-a[1]), // S
                a[2] + t * (b[2]-a[2]) // V
        );
    }

    public static Color interpolate(float val, Color color1, Color color2){
        if(val>1.0f)return color2;
        int r = color1.getRed()+Math.round((color2.getRed() - color1.getRed())*val);
        int g = color1.getGreen()+Math.round((color2.getGreen() - color1.getGreen())*val);
        int b = color1.getBlue()+Math.round((color2.getBlue() - color1.getBlue())*val);
        System.out.println(r+" "+g+" "+b);
        return new Color(r,g,b);
    }
}
