package xyz.spiralhalo.sherlock.ocr;

import xyz.spiralhalo.sherlock.ocr.persist.OCRTargetApp;
import xyz.spiralhalo.sherlock.ocr.persist.OCRTargetList;
import xyz.spiralhalo.sherlock.persist.settings.IniHandler;

import java.util.Collections;

public class OCRConfig {

    private static final String NODE = "OCR";
    private static final String KEY_ENABLED = "ENABLED";

    public static boolean OCRgEnabled() {
        return IniHandler.getInstance().getBoolean(NODE, KEY_ENABLED, OCRdEnabled());
    }

    public static void OCRsEnabled(boolean enabled){
        IniHandler.getInstance().putBoolean(NODE, KEY_ENABLED, enabled);
    }

    public static boolean OCRdEnabled(){return false;}

    private static final OCRTargetList defaultTargets = new OCRTargetList(Collections.singletonList(
            new OCRTargetApp("CLIPStudioPaint.exe", "CLIP STUDIO PAINT", 22, 18, -68, 4, 0, 362, true)));
    public static OCRTargetList OCRgTargetApps(){
        return defaultTargets;
    }
}
