package xyz.spiralhalo.sherlock;

import org.pushingpixels.substance.api.skin.*;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig.Theme;
import xyz.spiralhalo.sherlock.util.ImgUtil;

import javax.swing.*;
import java.time.ZoneId;

public class Main {

    public static final String APP_TITLE = "Project Sherlock 2 version 5 RC4";
    public static final String APP_NAME = "Project Sherlock 2";
    public static final String APP_NAME_NOSPACE = APP_NAME.replace(" ", "");
    public static Theme currentTheme;
    public static ZoneId z = ZoneId.systemDefault();
    private static final Theme FALL_BACK_THEME = Theme.SYSTEM;

    public static void main(String[] args) {
        if(!Application.isWindows()) {
            JOptionPane.showMessageDialog(null, "This operating system is not supported.",
                    "Project Sherlock 2", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Arg.setArgs(args);

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

        if(Arg.Delayed.isEnabled()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Debug.log(e);
            }
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

            Runtime.getRuntime().addShutdownHook(new Thread(Application::createOrDeleteStartupRegistry));
            AppControl.create();
        });
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
