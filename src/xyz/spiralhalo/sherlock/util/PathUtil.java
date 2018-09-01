package xyz.spiralhalo.sherlock.util;

import xyz.spiralhalo.sherlock.MainView;

import java.io.File;
import java.net.URISyntaxException;

public class PathUtil {
    public static final String RECDIR = "record";
    private static final String ORGDIR = "spiralhalo";
    private static final String SAVEDIR = "ProjectLogger2";
    private static String cachedSaveDir;
    private static String cachedCacheDir;
    private static String cachedRecordDir;
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

    public static String getRecordDir()
    {
        File recordDir = new File(getSaveDir(), RECDIR);
        if(recordDir.isDirectory() && cachedRecordDir != null){
            return cachedRecordDir;
        }
        if(recordDir.exists() && !recordDir.isDirectory()) {
            recordDir.delete();
        }
        if(!recordDir.exists()){
            recordDir.mkdir();
        }
        return cachedRecordDir = recordDir.getPath();
    }

    public static boolean isWindows(){
        return (System.getProperty("os.name")).toUpperCase().contains("WIN");
    }

    public static boolean isJar(){
        return getJarPath().toUpperCase().endsWith(".JAR");
    }
}
