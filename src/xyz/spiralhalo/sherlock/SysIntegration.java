package xyz.spiralhalo.sherlock;

import xyz.spiralhalo.sherlock.Main.Arg;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig.AppBool;
import xyz.spiralhalo.sherlock.util.Debug;
import xyz.spiralhalo.sherlock.util.ImgUtil;
import xyz.spiralhalo.sherlock.util.PathUtil;
import xyz.spiralhalo.sherlock.util.WinRegistry;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class SysIntegration {
    public static boolean supportsTrayIcon(){
        return SystemTray.isSupported();
    }
    public static TrayIcon createTrayIcon(ActionListener toggleAction, ActionListener exitAction){
        if(!supportsTrayIcon())return null;
        final PopupMenu popup = new PopupMenu();
        final Image image = ImgUtil.createImage("icon.png", "tray icon");
        if(image==null)return null;
        final TrayIcon trayIcon = new TrayIcon(image);
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a pop-up menu components
        MenuItem showItem = new MenuItem("Restore");
        MenuItem exitItem = new MenuItem("Exit");

        //Add listeners
        showItem.addActionListener(toggleAction);
        exitItem.addActionListener(exitAction);

        //Add components to pop-up menu
        popup.add(showItem);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);
        trayIcon.addActionListener(toggleAction);
        trayIcon.setToolTip(Main.APP_NAME);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return null;
        }
        return trayIcon;
    }

    public static void createOrDeleteStartupRegistry() {
        if (PathUtil.getJavawPath() == null) return;
        if (!PathUtil.isWindows() || !PathUtil.isJar()) return;
        try {
            String key = "Software\\Microsoft\\Windows\\CurrentVersion\\Run";
            String valueName = Main.APP_NAME_NOSPACE;
            String value = String.format("%s -jar \"%s\"%s",
                    PathUtil.getJavawPath(),
                    PathUtil.getJarPath(),
                    AppConfig.getBool(AppBool.RUN_MINIMIZED)?" "+ Arg.Minimized :"");
            if(AppConfig.getBool(AppBool.RUN_ON_STARTUP)) {
                WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, key, valueName, value);
            } else {
                WinRegistry.deleteValue(WinRegistry.HKEY_CURRENT_USER, key, valueName);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            Debug.log(SysIntegration.class, e);
        }
    }

    public static void restartApp(){
        String command = String.format("%s -jar \"%s\" %s",
                PathUtil.getJavawPath(),
                PathUtil.getJarPath(),
                Arg.Delayed);
        try {
            Runtime.getRuntime().exec(command);
            System.exit(0);
        } catch (IOException e) {
            Debug.log(e);
        }
    }
}
