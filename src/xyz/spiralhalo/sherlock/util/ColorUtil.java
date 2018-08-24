package xyz.spiralhalo.sherlock.util;

import java.awt.*;

public class ColorUtil {
    public static Color bad = new Color(230,124,115);
    public static Color neu = new Color(255,214,102);
    public static Color gut = new Color(87,187,138);
    public static Color light_gray = new Color(225,225,225);
    public static Color gray = new Color(160,160,160);

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
        float d = b[0] - a[0];
        if (a[0] > b[0])
        {
            // Swap (a[0], b[0])
            float h3 = b[0];
            b[0] = a[0];
            a[0] = h3;

            d = -d;
            t = 1 - t;
        }

        if (d > 0.5) // 180deg
        {
            a[0] = a[0] + 1; // 360deg
            h = ( a[0] + t * (b[0] - a[0]) ) % 1; // 360deg
        }
        if (d <= 0.5) // 180deg
        {
            h = a[0] + t * d;
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
