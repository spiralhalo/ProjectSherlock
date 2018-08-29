package xyz.spiralhalo.sherlock.util;

public class MathUtil {
    public static float normalize(float toRound, float normalizeTo){
        return (float)Math.round(toRound * normalizeTo) / 10;
    }

    public static float round(float toRound, int decimalpoints){
        float x = (float)Math.pow(10, decimalpoints);
        return (float)Math.round(toRound * x) / x;
    }
}
