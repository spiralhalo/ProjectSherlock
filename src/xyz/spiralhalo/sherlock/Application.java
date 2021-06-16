//
//    Copyright 2020 spiralhalo <re.nanashi95@gmail.com>
//
//    This file is part of Project Sherlock.
//
//    Project Sherlock is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Project Sherlock is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Project Sherlock.  If not, see <https://www.gnu.org/licenses/>.
//

package xyz.spiralhalo.sherlock;

import xyz.spiralhalo.sherlock.persist.settings.AppConfig;
import xyz.spiralhalo.sherlock.util.img.ImgUtil;
import xyz.spiralhalo.sherlock.util.WinRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class Application {
    private static final String ORGDIR = "spiralhalo";
    private static final String SAVEDIR = "ProjectLogger2";
    private static String cachedSaveDir;
//    private static String cachedLogDir;
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
                cachedJarPath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            } catch (URISyntaxException e) {
                Debug.log(e);
                return null;
            }
        }
        return cachedJarPath;
    }

    public static String getSaveDir()
    {
//        return "D:\\Temp\\sherlocktest"; //always add a to do comment when using this
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

    private static String getSubSaveDir(String dirName)
    {
        File subSaveDir = new File(getSaveDir(), dirName);
        if(subSaveDir.isDirectory()){
            return subSaveDir.getPath();
        }
        if(subSaveDir.exists() && !subSaveDir.isDirectory()) {
            subSaveDir.delete();
        }
        if(!subSaveDir.exists()){
            subSaveDir.mkdir();
        }
        return subSaveDir.getPath();
    }

    public static String getCacheDir()
    {
        return getSubSaveDir("cache");
    }

    public static String getThumbsDir()
    {
        return getSubSaveDir("thumbs");
    }

    private static final String LOCKFILE = "running.lock";
    private static FileLock lockReference;

    public static boolean isAlreadyRunning(){
        try {
            File lock = new File(getSaveDir(), LOCKFILE);
            if (lock.exists())
                lock.delete();
            FileChannel lockChannel = new RandomAccessFile(lock, "rw").getChannel();
            FileLock fileLock = lockChannel.tryLock();
            if(fileLock == null) throw new Exception("Unable to obtain lock on file.");
            lockReference = fileLock; //survive gc
            return false;
        } catch (Exception e){
            Debug.log(e);
        }
        return true;
    }

//    public static String getLogDir()
//    {
//        File logDir = new File(getSaveDir(), "logs");
//        if(logDir.isDirectory() && cachedLogDir != null){
//            return cachedLogDir;
//        }
//        if(logDir.exists() && !logDir.isDirectory()) {
//            logDir.delete();
//        }
//        if(!logDir.exists()){
//            logDir.mkdir();
//        }
//        return cachedLogDir = logDir.getPath();
//    }

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
        final Image image = ImgUtil.createOrDummy("icon.png", "Tray icon", 16, 16, 0xffffffff);
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
            String valueName = Main.REGISTRY_KEY;
            String value;
            if(Application.isJar()) {
                value = String.format("%s -jar \"%s\"%s",
                        Application.getJavawPath(),
                        Application.getJarPath(),
                        AppConfig.appGBool(AppConfig.AppBool.RUN_MINIMIZED) ? " " + Main.Arg.Minimized : "");
            } else {
                value = String.format("%s %s",
                        Application.getJarPath(),
                        AppConfig.appGBool(AppConfig.AppBool.RUN_MINIMIZED) ? " " + Main.Arg.Minimized : "");
            }
            if(AppConfig.appGBool(AppConfig.AppBool.RUN_ON_STARTUP)) {
                WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, key, valueName, value);
            } else {
                WinRegistry.deleteValue(WinRegistry.HKEY_CURRENT_USER, key, valueName);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            Debug.log(e);
        }
    }

    public static void restartApp(boolean silent){
        String command;
        if(Application.isJar()) {
            command = String.format("%s -jar \"%s\" %s",
                    Application.getJavawPath(),
                    Application.getJarPath(),
                    Main.Arg.Delayed);
        } else if(Application.isExe()) {
            command = String.format("%s %s", Application.getJarPath(), Main.Arg.Delayed);
        } else {
            if(!silent) {
                JOptionPane.showMessageDialog(null, "Unable to restart. Manual restart might be required.",
                        "Failed to restart", JOptionPane.ERROR_MESSAGE);
            }
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
