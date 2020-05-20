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

import org.pushingpixels.substance.api.skin.*;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig.Theme;
import xyz.spiralhalo.sherlock.util.ImgUtil;

import javax.swing.*;
import java.time.ZoneId;

public class Main {

    public static final int VERSION = 6;
    public static final int MINOR_VERSION = 1;
    public static final String APP_TITLE = String.format("Project Sherlock 2 version %d.%d", VERSION, MINOR_VERSION);
    public static final String APP_NAME = "Project Sherlock 2";
    public static final String REGISTRY_KEY = "ProjectSherlock2"; //don't change ever
    public static Theme currentTheme;
    public static ZoneId z = ZoneId.systemDefault();
    private static final Theme FALL_BACK_THEME = Theme.SYSTEM;

    public static void main(String[] args) {

        if(!Application.isWindows()) {
            JOptionPane.showMessageDialog(null, "This operating system is not supported. Exiting...",
                    APP_NAME, JOptionPane.ERROR_MESSAGE);
            return;
        }

        Arg.setArgs(args);

        if(Arg.Delayed.isEnabled()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Debug.log(e);
            }
        }

        if(Application.isAlreadyRunning() && !Arg.Sandbox.isEnabled()) {
            JOptionPane.showMessageDialog(null, "Another instance of Project Sherlock is already running. Please close the other instance and wait for awhile before reopening.",
                    APP_NAME, JOptionPane.ERROR_MESSAGE);
            return;
        }

        if(Application.isJar()){
            Debug.log("Running from a .jar file");
        } else if(Application.isExe()){
            Debug.log("Running from a .exe wrapper");
        } else {
            Debug.log("Running from compiled class files");
        }

        if(Arg.Sandbox.isEnabled()) {
            Debug.log("Sandbox mode is enabled.");
        }

        SwingUtilities.invokeLater(() -> {
            try {
                currentTheme = AppConfig.getTheme();
                setTheme(currentTheme);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | UnsupportedLookAndFeelException e) {
                Debug.log(e);
                currentTheme = FALL_BACK_THEME;
                try {
                    setTheme(currentTheme);
                } catch (Exception e1) {
                    Debug.log(e1);
                }
            }

            Runtime.getRuntime().addShutdownHook(new Thread(Main::onShutdown));
            AppControl.create();
        });
    }

    private static void onShutdown() {
        Application.createOrDeleteStartupRegistry();
    }

    public static void applyButtonTheme(JButton... buttons){
        if(currentTheme.foreground != 0) {
            for (JButton btn : buttons) {
                ImageIcon icon = (ImageIcon)btn.getIcon();
                if(Main.currentTheme.dark) {
                    btn.setRolloverIcon(icon);
                }
                btn.setIcon(ImgUtil.createTintedIcon(icon.getImage(), currentTheme.foreground));
            }
        }
    }

    private static void setTheme(Theme theme) throws UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        switch (theme){
            default:
            case BUSINESS: UIManager.setLookAndFeel(new SubstanceBusinessLookAndFeel()); break;
            case BUSINESS_BLUE:UIManager.setLookAndFeel(new SubstanceBusinessBlueSteelLookAndFeel()); break;
            case BUSINESS_BLACK:UIManager.setLookAndFeel(new SubstanceBusinessBlackSteelLookAndFeel()); break;
            //case MIST_SILVER: UIManager.setLookAndFeel(new SubstanceMistSilverLookAndFeel()); break;
            case GRAPHITE:UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel()); break;
            case RAVEN:UIManager.setLookAndFeel(new SubstanceRavenLookAndFeel()); break;
            case TWILIGHT:UIManager.setLookAndFeel(new SubstanceTwilightLookAndFeel()); break;
            case SYSTEM:UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); break;
        }
    }

    public enum Arg{
        None,
        Minimized("-minimized"),
        Delayed("-delayed"),
        Debug("-debug"),
        Verbose("-verbose"),
        Console("-console"),
        Sandbox("-sandbox");
        private final String v;
        private boolean enabled = false;
        Arg(){ v = "";}
        Arg(String v){this.v = v;}
        public String toString() { return v;}
        private static Arg[] values;
        static Arg lookup(String v){
            if(values==null)values=values();
            for (Arg a:values) { if(a.v.equals(v.toLowerCase())){ return a; } }
            return None;
        }
        static void setArgs(String[] args){
            for (String arg:args) {
                Arg a = Arg.lookup(arg);
                if(a!=null){ a.enabled=true; }
            }
        }
        public boolean isEnabled() { return enabled; }
    }
}
