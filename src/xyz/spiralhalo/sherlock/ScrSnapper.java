package xyz.spiralhalo.sherlock;

import xyz.spiralhalo.sherlock.EnumerateWindows.WindowInfo;
import xyz.spiralhalo.sherlock.persist.project.Project;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ScrSnapper implements TrackerListener{

    private static final long SNAP_DELAY_MILLIS = 10 * 1000;
    private long lastSnapTimeMillis;

    public ScrSnapper(TrackerAccessor tracker){
        tracker.addListener(this);
        lastSnapTimeMillis = System.currentTimeMillis();
    }

    @Override
    public void onTrackerLog(Project project, WindowInfo windowInfo) {
        if(System.currentTimeMillis() - lastSnapTimeMillis >= SNAP_DELAY_MILLIS
                && project != null && !project.isUtilityTag() && !project.isFinished()
                /*&& !windowInfo.title.contains("force keyword")*/) {
            BufferedImage image;
            try {
                image = new Robot().createScreenCapture(EnumerateWindows.getWindowRect(windowInfo.hwndPointer));
                ImageIO.write(image, "png", getThumbFile(project.getHash()));
            } catch (AWTException | IOException e) {
                Debug.log(e);
            }
            lastSnapTimeMillis = System.currentTimeMillis();
        }
    }

    public static File getThumbFile(long projectHash){
        return new File(Application.getThumbsDir(), Long.toHexString(projectHash));
    }

    public static BufferedImage getThumbImg(long projectHash){
        try {
            File thumbFile = getThumbFile(projectHash);
            if(!thumbFile.exists())
                return null;
            return ImageIO.read(thumbFile);
        } catch (Exception e) {
            Debug.log(e);
            return null;
        }
    }
}
