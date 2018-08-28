package xyz.spiralhalo.sherlock;

import org.pushingpixels.substance.api.skin.*;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig.Theme;
import xyz.spiralhalo.sherlock.util.Debug;

import javax.swing.*;

public class Main {

    public static final String APP_NAME = "Project Sherlock 2";
    public static final String APP_NAME_NOSPACE = APP_NAME.replace(" ", "");
    public static Args programArgs;
    public static Theme currentTheme;
    private static final Theme FALL_BACK_THEME = Theme.SYSTEM;

    public static void main(String[] args0) {
        programArgs = Args.getArgs(args0);

        if(programArgs.delayed) {
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

            Runtime.getRuntime().addShutdownHook(new Thread(SysIntegration::createOrDeleteStartupRegistry));
            MainView m = new MainView();
            if(!programArgs.minimized) {
                m.getFrame().setVisible(true);
            }
        });
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
        Debug("-debug");
        private String v;
        Arg(){ v = "";}
        Arg(String v){this.v = v;}
        public String toString() { return v;}
        private static Arg[] values;
        static Arg lookup(String v){
            if(values==null)values=values();
            for (Arg a:values) {
                if(a.v.equals(v.toLowerCase())){
                    return a;
                }
            }
            return None;
        }
    }

    public static class Args{
        public final boolean minimized;
        public final boolean delayed;
        public final boolean debug;

        Args(boolean minimized, boolean delayed, boolean debug) {
            this.minimized = minimized;
            this.delayed = delayed;
            this.debug = debug;
        }

        static Args getArgs(String[] args){
            boolean minimized = false;
            boolean delayed = false;
            boolean debug = false;
            for (String arg:args) {
                Arg a = Arg.lookup(arg);
                switch (a){
                    case Debug: debug = true; break;
                    case Delayed: delayed = true; break;
                    case Minimized: minimized = true; break;
                }
            }
            return new Args(minimized, delayed, debug);
        }
    }
}
