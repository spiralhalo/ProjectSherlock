package xyz.spiralhalo.sherlock;

import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;
import lc.kra.system.mouse.GlobalMouseHook;
import lc.kra.system.mouse.event.GlobalMouseAdapter;
import lc.kra.system.mouse.event.GlobalMouseEvent;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserInt;
import xyz.spiralhalo.sherlock.persist.settings.UserConfig.UserNode;

/**
 *
 * @author spiralhalo
 */
public class AFKMonitor
{
    private class MouseAdapter extends GlobalMouseAdapter
    {
        @Override public void mousePressed(GlobalMouseEvent event) { logNow(); mousePressed = true; }
        @Override public void mouseReleased(GlobalMouseEvent event) { logNow(); mousePressed = false; }
        @Override public void mouseMoved(GlobalMouseEvent event) { logNow(); }
        @Override public void mouseWheel(GlobalMouseEvent event) { logNow(); }
    }

    private class KeyAdapter extends GlobalKeyAdapter
    {
        @Override public void keyPressed(GlobalKeyEvent event) { logNow(); keyPressed = true; }
        @Override public void keyReleased(GlobalKeyEvent event) { logNow(); keyPressed = false; }
    }

    private void logNow(){ lastInput = System.currentTimeMillis(); }

    private GlobalKeyboardHook keyboardHook;
    private GlobalMouseHook mouseHook;

    private long lastInput;
    private boolean keyPressed;
    private boolean mousePressed;

    public AFKMonitor()
    {
        keyboardHook = new GlobalKeyboardHook(false);
        mouseHook = new GlobalMouseHook(false);
        keyboardHook.addKeyListener(new KeyAdapter());
        mouseHook.addMouseListener(new MouseAdapter());
    }

    public boolean isNotAFK(){
        return keyPressed || mousePressed || (System.currentTimeMillis() - lastInput <
                UserConfig.getInt(UserNode.TRACKING, UserInt.AFK_TIMEOUT_SECOND) * 1000);
    }
}
