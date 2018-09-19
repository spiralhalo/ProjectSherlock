package xyz.spiralhalo.sherlock;

import xyz.spiralhalo.sherlock.persist.settings.AppConfig;
import xyz.spiralhalo.sherlock.util.ImgUtil;
import xyz.spiralhalo.sherlock.util.WinRegistry;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

public class Application {
    private static final String ORGDIR = "spiralhalo";
    private static final String SAVEDIR = "ProjectLogger2";
    private static String cachedSaveDir;
    private static String cachedCacheDir;
    private static String cachedJarPath;
    private static String cachedJavawPath;

    public static String getJavawPath()
    {
        if(cachedJavawPath == null){
            String javaHome = System.getProperty("java.home");
            File f = new File(javaHome + File.separator + "bin", "javaw.exe");
            if(f.exists()){
                cachedJavawPath = f.getPath();
            }
        }
        return cachedJavawPath;
    }

    public static String getJarPath()
    {
        if(cachedJarPath == null){
            try {
                cachedJarPath = new File(MainView.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return null;
            }
        }
        return cachedJarPath;
    }

    public static String getSaveDir()
    {
        File test = null;
        if(cachedSaveDir != null) {
             test = new File(cachedSaveDir);
        }
        if (cachedSaveDir == null || !test.exists())
        {
            String workingDirectory;
            String OS = (System.getProperty("os.name")).toUpperCase();
            if (OS.contains("WIN")) {
                workingDirectory = System.getenv("AppData");
            } else {
                workingDirectory = System.getProperty("user.home");
                if (OS.contains("MAC")) {
                    workingDirectory += "/Library/Application Support";
                }
            }
            File orgDir = new File(workingDirectory + File.separator + ORGDIR);
            if (!orgDir.exists()) {
                orgDir.mkdir();
            }
            File saveDir = new File(orgDir, SAVEDIR);
            if (!saveDir.exists()) {
                saveDir.mkdir();
            }
            cachedSaveDir = saveDir.getPath() + File.separator;
        }
        return cachedSaveDir;
    }

    public static String getCacheDir()
    {
        File cacheDir = new File(getSaveDir(), "cache");
        if(cacheDir.isDirectory() && cachedCacheDir != null){
            return cachedCacheDir;
        }
        if(cacheDir.exists() && !cacheDir.isDirectory()) {
            cacheDir.delete();
        }
        if(!cacheDir.exists()){
            cacheDir.mkdir();
        }
        return cachedCacheDir = cacheDir.getPath();
    }

    public static boolean isWindows(){
        return (System.getProperty("os.name")).toUpperCase().contains("WIN");
    }

    public static boolean isJar(){
        return getJarPath().toUpperCase().endsWith(".JAR");
    }

    public static boolean isExe(){
        return getJarPath().toUpperCase().endsWith(".EXE");
    }

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
        if (Application.getJavawPath() == null) return;
        if (!Application.isWindows() || (!Application.isJar() && !Application.isExe())) return;
        try {
            String key = "Software\\Microsoft\\Windows\\CurrentVersion\\Run";
            String valueName = Main.APP_NAME_NOSPACE;
            String value;
            if(Application.isJar()) {
                value = String.format("%s -jar \"%s\"%s",
                        Application.getJavawPath(),
                        Application.getJarPath(),
                        AppConfig.getBool(AppConfig.AppBool.RUN_MINIMIZED) ? " " + Main.Arg.Minimized : "");
            } else {
                value = String.format("%s %s",
                        Application.getJarPath(),
                        AppConfig.getBool(AppConfig.AppBool.RUN_MINIMIZED) ? " " + Main.Arg.Minimized : "");
            }
            if(AppConfig.getBool(AppConfig.AppBool.RUN_ON_STARTUP)) {
                WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, key, valueName, value);
            } else {
                WinRegistry.deleteValue(WinRegistry.HKEY_CURRENT_USER, key, valueName);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            Debug.log(e);
        }
    }

    public static void restartApp(){
        String command;
        if(Application.isJar()) {
            command = String.format("%s -jar \"%s\" %s",
                    Application.getJavawPath(),
                    Application.getJarPath(),
                    Main.Arg.Delayed);
        } else if(Application.isExe()) {
            command = String.format("%s %s", Application.getJarPath(), Main.Arg.Delayed);
        } else {
            return;
        }
        try {
            Runtime.getRuntime().exec(command);
            System.exit(0);
        } catch (IOException e) {
            Debug.log(e);
        }
    }
}
