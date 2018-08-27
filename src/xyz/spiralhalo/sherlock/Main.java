package xyz.spiralhalo.sherlock;

import org.pushingpixels.substance.api.skin.*;
import xyz.spiralhalo.sherlock.persist.settings.AppConfig;
import xyz.spiralhalo.sherlock.util.Debug;

import javax.swing.*;

public class Main {
    public static final String APP_NAME_NOSPACE = "Project Sherlock 2".replace(" ", "");
    public static final String ARG_MINIMIZED = "-minimized";
    public static final String ARG_DELAYED = "-delayed";
    public static final String APP_NAME = "Project Sherlock 2";
    public static AppConfig.Theme currentTheme;

    public static void main(String[] args) {
        for (String arg:args) {
            if(arg.toLowerCase().equals(Main.ARG_DELAYED)){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Debug.log(e);
                }
            }
        }
        SwingUtilities.invokeLater(() -> {
            try {
                switch (AppConfig.getTheme()){
                    default:
                    case BUSINESS:
                        UIManager.setLookAndFeel(new SubstanceBusinessLookAndFeel());
                        break;
                    case BUSINESS_BLUE:
                        UIManager.setLookAndFeel(new SubstanceBusinessBlueSteelLookAndFeel());
                        break;
                    case BUSINESS_BLACK:
                        UIManager.setLookAndFeel(new SubstanceBusinessBlackSteelLookAndFeel());
                        break;
//                    case MIST_SILVER:
//                        UIManager.setLookAndFeel(new SubstanceMistSilverLookAndFeel());
//                        break;
                    case GRAPHITE:
                        UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
                        break;
                    case RAVEN:
                        UIManager.setLookAndFeel(new SubstanceRavenLookAndFeel());
                        break;
                    case TWILIGHT:
                        UIManager.setLookAndFeel(new SubstanceTwilightLookAndFeel());
                        break;
                    case SYSTEM:
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        break;
                }
                currentTheme = AppConfig.getTheme();
            } catch (Exception e) {
                Debug.log(e);
            }

            Runtime.getRuntime().addShutdownHook(new Thread(SysIntegration::createOrDeleteStartupRegistry));
            MainView m = new MainView();
            for (String arg:args) {
                if(arg.toLowerCase().equals(Main.ARG_MINIMIZED)){
                    return;
                }
            }
            m.getFrame().setVisible(true);
        });
    }
}
