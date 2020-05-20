package xyz.spiralhalo.sherlock.ocr.persist;

import java.io.Serializable;

public class OCRTargetApp implements Serializable {
    public static final long serialVersionUID = 1L;
    private String exe;
    private String name;
    private int titleBarHeight;
    private int left, right, top, bottom;
    private int threshold;
    private boolean invert;

    public OCRTargetApp(String exe, String name, int titleBarHeight, int left, int right, int top, int bottom, int threshold, boolean invert) {
        this.exe = exe;
        this.name = name;
        this.titleBarHeight = titleBarHeight;
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.threshold = threshold;
        this.invert = invert;
    }

    public String getExe() {
        return exe;
    }

    public String getName() {
        return name;
    }

    public int getTitleBarHeight() {
        return titleBarHeight;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getTop() {
        return top;
    }

    public int getBottom() {
        return bottom;
    }

    public boolean isInvert() {
        return invert;
    }

    public int getThreshold() {
        return threshold;
    }
}
